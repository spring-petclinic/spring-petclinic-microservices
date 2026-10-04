import { reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { jsonRequest, request, requireObject } from '../api/client.js';
import { clearError, reportError } from '../state/errors.js';
import { useRequest } from '../utils/useRequest.js';
import { useSubmission } from '../utils/useSubmission.js';

export const OwnerFormPage = {
    setup() {
        const route = useRoute();
        const router = useRouter();
        const owner = reactive({
            firstName: '',
            lastName: '',
            address: '',
            city: '',
            telephone: ''
        });
        const ownerId = route.params.ownerId;
        const state = useRequest(async ({ signal }) => {
            if (ownerId) {
                const loadedOwner = requireObject(await request(`/api/customer/owners/${ownerId}`, { signal }), 'Owner not found.');
                Object.assign(owner, loadedOwner);
            }
        });
        const submission = useSubmission();

        async function submit(event) {
            if (!event.target.reportValidity() || submission.submitting.value) {
                return;
            }

            const payload = {
                firstName: owner.firstName.trim(),
                lastName: owner.lastName.trim(),
                address: owner.address.trim(),
                city: owner.city.trim(),
                telephone: owner.telephone.trim()
            };

            const errorContext = clearError();
            try {
                await submission.run(async ({ signal }) => {
                    if (ownerId) {
                        await request(`/api/customer/owners/${ownerId}`, { ...jsonRequest('PUT', payload), signal });
                        await router.push(`/owners/details/${ownerId}`);
                    } else {
                        await request('/api/customer/owners', { ...jsonRequest('POST', payload), signal });
                        await router.push('/owners');
                    }
                });
            } catch (error) {
                if (!error.cancelled) {
                    reportError(error, errorContext);
                }
            }
        }

        return {
            isEdit: Boolean(ownerId),
            owner,
            loading: state.loading,
            failed: state.failed,
            failure: state.failure,
            submitting: submission.submitting,
            clearError,
            submit
        };
    },
    template: `
        <section>
            <loading-spinner v-if="loading"></loading-spinner>
            <load-failure v-else-if="failed" :error="failure" resource="Owner details"></load-failure>
            <div v-else>
                <h2>Owner</h2>
                <form style="max-width: 25em;" @input="clearError" @submit.prevent="submit">
                    <div class="mb-3">
                        <label for="owner-first-name">First name</label>
                        <input id="owner-first-name" v-model.trim="owner.firstName" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label for="owner-last-name">Last name</label>
                        <input id="owner-last-name" v-model.trim="owner.lastName" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label for="owner-address">Address</label>
                        <input id="owner-address" v-model.trim="owner.address" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label for="owner-city">City</label>
                        <input id="owner-city" v-model.trim="owner.city" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label for="owner-telephone">Telephone</label>
                        <input id="owner-telephone" v-model.trim="owner.telephone" class="form-control" inputmode="numeric" maxlength="12" pattern="[0-9]{1,12}" placeholder="905554443322" required>
                    </div>
                    <div>
                        <button class="btn btn-primary" type="submit" :disabled="submitting">
                            {{ submitting ? 'Saving...' : isEdit ? 'Update Owner' : 'Submit' }}
                        </button>
                    </div>
                </form>
            </div>
        </section>
    `
};
