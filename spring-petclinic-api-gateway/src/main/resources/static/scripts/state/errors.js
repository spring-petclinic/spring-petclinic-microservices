import { reactive } from 'vue';

let errorContext = 0;

export const errorState = reactive({
    message: '',
    details: []
});

export function clearError() {
    errorContext += 1;
    errorState.message = '';
    errorState.details = [];
    return errorContext;
}

export function reportError(error, expectedContext) {
    if (expectedContext !== undefined && expectedContext !== errorContext) {
        return;
    }

    const payload = (error && typeof error.payload === 'object' && error.payload !== null)
        ? error.payload
        : (error && typeof error.payload === 'string' ? { message: error.payload } : {});
    const message = payload.detail
        || payload.message
        || (payload.error && payload.error !== 'Bad Request' ? payload.error : null)
        || error?.message
        || payload.error
        || 'The request could not be completed.';

    errorState.message = message;

    if (Array.isArray(payload.errors) && payload.errors.length) {
        errorState.details = payload.errors.map((entry) =>
            entry.field ? `${entry.field}: ${entry.defaultMessage || entry.message}` : (entry.defaultMessage || entry.message)
        );
    } else if (Array.isArray(payload.invalidParams) && payload.invalidParams.length) {
        errorState.details = payload.invalidParams.map((entry) =>
            entry.name ? `${entry.name}: ${entry.reason}` : entry.reason
        );
    } else if (Array.isArray(payload.violations) && payload.violations.length) {
        errorState.details = payload.violations.map((entry) =>
            entry.propertyPath ? `${entry.propertyPath}: ${entry.message}` : entry.message
        );
    } else {
        errorState.details = [];
    }
}
