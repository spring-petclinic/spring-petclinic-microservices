import { onUnmounted, ref } from 'vue';

export function useSubmission() {
    const submitting = ref(false);
    let controller = null;

    async function run(action) {
        if (submitting.value) {
            return;
        }

        const currentController = new AbortController();
        controller = currentController;
        submitting.value = true;

        try {
            return await action({ signal: currentController.signal });
        } finally {
            if (controller === currentController) {
                controller = null;
                submitting.value = false;
            }
        }
    }

    onUnmounted(() => {
        controller?.abort();
    });

    return {
        submitting,
        run
    };
}
