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

    const payload = error.payload || {};
    errorState.message = payload.error || payload.message || error.message || 'The request could not be completed.';
    errorState.details = Array.isArray(payload.errors)
        ? payload.errors.map((entry) => entry.field ? `${entry.field}: ${entry.defaultMessage}` : entry.defaultMessage)
        : [];
}
