import { computed } from 'vue';

export const LoadingSpinner = {
    template: '<div class="text-center" role="status"><span class="spinner-border" aria-hidden="true"></span><span class="visually-hidden">Loading...</span></div>'
};

export const LoadFailure = {
    props: {
        error: {
            type: Object,
            default: null
        },
        resource: {
            type: String,
            default: 'The requested data'
        }
    },
    setup(props) {
        const title = computed(() => {
            if (props.error?.timeout) {
                return `${props.resource} took too long to load.`;
            }
            if (props.error?.status === 503) {
                return `${props.resource} is temporarily unavailable.`;
            }
            if (props.error?.status === 404) {
                return `${props.resource} was not found.`;
            }
            return `${props.resource} could not be loaded.`;
        });
        const details = computed(() => {
            if (props.error?.timeout) {
                return 'The server did not respond within 15 seconds. Please try again.';
            }
            if (props.error?.status === 503) {
                return 'The related service is still starting or cannot be reached. Please try again in a few moments.';
            }
            if (props.error?.status === 404) {
                return 'It may have been removed or the address may be incorrect.';
            }
            if (props.error?.status === null) {
                return 'The server cannot be reached. Please try again in a few moments.';
            }
            return 'Please try again in a few moments.';
        });

        return {
            title,
            details
        };
    },
    template: '<div class="alert alert-danger" role="alert"><strong>{{ title }}</strong><div>{{ details }}</div></div>'
};
