import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { jsonRequest, request, requireArray, requireObject } from '../api/client.js';
import { clearError, reportError } from '../state/errors.js';
import { today, toDateInputValue } from '../utils/dates.js';
import { useRequest } from '../utils/useRequest.js';
import { useSubmission } from '../utils/useSubmission.js';

export const PetFormPage = {
    setup() {
        const route = useRoute();
        const router = useRouter();
        const ownerId = route.params.ownerId;
        const petId = route.params.petId;
        const types = ref([]);
        const pet = reactive({
            owner: '',
            name: '',
            birthDate: '',
            typeId: ''
        });
        const birthDateError = ref('');
        const maxDate = today();

        const state = useRequest(async ({ signal }) => {
            types.value = requireArray(await request('/api/customer/petTypes', { signal }), 'Pet types could not be loaded.');

            if (petId) {
                const loadedPet = requireObject(await request(`/api/customer/owners/${ownerId}/pets/${petId}`, { signal }), 'Pet not found.');
                Object.assign(pet, {
                    owner: loadedPet.owner,
                    name: loadedPet.name,
                    birthDate: toDateInputValue(loadedPet.birthDate),
                    typeId: String(loadedPet.type.id)
                });
            } else {
                const owner = requireObject(await request(`/api/customer/owners/${ownerId}`, { signal }), 'Owner not found.');
                pet.owner = `${owner.firstName} ${owner.lastName}`;
                pet.typeId = types.value.length ? String(types.value[0].id) : '';
            }
        });
        const submission = useSubmission();

        function onInput() {
            if (!submission.submitting.value) {
                clearError();
                birthDateError.value = '';
            }
        }

        async function submit(event) {
            birthDateError.value = '';

            if (pet.birthDate && pet.birthDate > maxDate) {
                birthDateError.value = 'Birth date cannot be in the future.';
                reportError(new Error('Birth date cannot be in the future.'));
                return;
            }

            if (!event.target.reportValidity() || submission.submitting.value) {
                return;
            }

            const payload = {
                name: pet.name.trim(),
                birthDate: pet.birthDate,
                typeId: Number(pet.typeId)
            };
            const petPath = petId ? `/${petId}` : '';
            const url = `/api/customer/owners/${ownerId}/pets${petPath}`;

            const errorContext = clearError();
            try {
                await submission.run(async ({ signal }) => {
                    await request(url, { ...jsonRequest(petId ? 'PUT' : 'POST', payload), signal });
                    await router.push(`/owners/details/${ownerId}`);
                });
            } catch (error) {
                if (!error.cancelled) {
                    const payload = (error && typeof error.payload === 'object' && error.payload !== null) ? error.payload : {};
                    let birthDateMsg = '';

                    if (Array.isArray(payload.errors)) {
                        const birthDateEntry = payload.errors.find((entry) => entry.field === 'birthDate');
                        if (birthDateEntry) {
                            birthDateMsg = birthDateEntry.defaultMessage || 'Birth date cannot be in the future.';
                        }
                    } else if (Array.isArray(payload.invalidParams)) {
                        const birthDateEntry = payload.invalidParams.find((entry) => entry.name === 'birthDate');
                        if (birthDateEntry) {
                            birthDateMsg = birthDateEntry.reason || 'Birth date cannot be in the future.';
                        }
                    } else if (Array.isArray(payload.violations)) {
                        const birthDateEntry = payload.violations.find((entry) => entry.propertyPath === 'birthDate');
                        if (birthDateEntry) {
                            birthDateMsg = birthDateEntry.message || 'Birth date cannot be in the future.';
                        }
                    }

                    if (!birthDateMsg && error.status === 400 && (pet.birthDate > maxDate || !payload.message || payload.error === 'Bad Request')) {
                        birthDateMsg = 'Birth date cannot be in the future.';
                    }

                    if (birthDateMsg) {
                        birthDateError.value = birthDateMsg;
                        reportError(new Error(birthDateMsg), errorContext);
                    } else {
                        reportError(error, errorContext);
                    }
                }
            }
        }

        return {
            isEdit: Boolean(petId),
            pet,
            types,
            birthDateError,
            maxDate,
            loading: state.loading,
            failed: state.failed,
            failure: state.failure,
            submitting: submission.submitting,
            clearError,
            onInput,
            submit
        };
    },
    template: `
        <section>
            <loading-spinner v-if="loading"></loading-spinner>
            <load-failure v-else-if="failed" :error="failure" resource="Pet details"></load-failure>
            <div v-else>
                <h2>Pet</h2>
                <form class="row g-3" @input="onInput" @change="onInput" @submit.prevent="submit">
                    <div class="col-12 row">
                        <div class="col-md-2 col-form-label">Owner</div>
                        <div class="col-md-6">
                            <p class="form-control-plaintext mb-0">{{ pet.owner }}</p>
                        </div>
                    </div>
                    <div class="col-12 row">
                        <label class="col-md-2 col-form-label" for="pet-name">Name</label>
                        <div class="col-md-6">
                            <input id="pet-name" v-model.trim="pet.name" class="form-control" required type="text">
                        </div>
                    </div>
                    <div class="col-12 row">
                        <label class="col-md-2 col-form-label" for="pet-birth-date">Birth date</label>
                        <div class="col-md-6">
                            <input
                                id="pet-birth-date"
                                v-model="pet.birthDate"
                                class="form-control"
                                :class="{ 'is-invalid': birthDateError }"
                                :max="maxDate"
                                required
                                type="date"
                            >
                            <div v-if="birthDateError" class="invalid-feedback d-block">
                                {{ birthDateError }}
                            </div>
                        </div>
                    </div>
                    <div class="col-12 row">
                        <label class="col-md-2 col-form-label" for="pet-type">Type</label>
                        <div class="col-md-6">
                            <select id="pet-type" v-model="pet.typeId" class="form-select" required>
                                <option v-for="type in types" :key="type.id" :value="String(type.id)">
                                    {{ type.name }}
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="col-12 offset-md-2">
                        <button class="btn btn-primary" type="submit" :disabled="submitting">
                            {{ submitting ? 'Saving...' : isEdit ? 'Update Pet' : 'Submit' }}
                        </button>
                    </div>
                </form>
            </div>
        </section>
    `
};
