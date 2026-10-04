import { onMounted, onUnmounted, ref } from 'vue';

export function useRequest(load) {
    const loading = ref(true);
    const failed = ref(false);
    const failure = ref(null);
    let controller = null;
    let unmounted = false;

    async function run() {
        controller?.abort();
        const currentController = new AbortController();
        controller = currentController;
        loading.value = true;
        failed.value = false;
        failure.value = null;

        try {
            await load({ signal: currentController.signal });
        } catch (error) {
            if (!currentController.signal.aborted) {
                failed.value = true;
                failure.value = error;
            }
        } finally {
            if (controller === currentController) {
                controller = null;
                if (!unmounted) {
                    loading.value = false;
                }
            }
        }
    }

    onMounted(run);
    onUnmounted(() => {
        unmounted = true;
        controller?.abort();
    });
    return {
        loading,
        failed,
        failure
    };
}
