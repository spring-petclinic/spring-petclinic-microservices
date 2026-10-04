import { ref } from 'vue';

import { request, requireArray } from '../api/client.js';
import { useRequest } from '../utils/useRequest.js';

export const VetsPage = {
    setup() {
        const vets = ref([]);
        const state = useRequest(async ({ signal }) => {
            vets.value = requireArray(await request('/api/vet/vets', { signal }), 'Veterinarians could not be loaded.');
        });

        return {
            vets,
            loading: state.loading,
            failed: state.failed,
            failure: state.failure
        };
    },
    template: `
        <section>
            <loading-spinner v-if="loading"></loading-spinner>
            <load-failure v-else-if="failed" :error="failure" resource="Veterinarian data"></load-failure>
            <div v-else>
                <h2>Veterinarians</h2>
                <table class="table table-striped">
                    <thead>
                        <tr><th>Name</th><th>Specialties</th></tr>
                    </thead>
                    <tbody>
                        <tr v-for="vet in vets" :key="vet.id">
                            <td>{{ vet.firstName }} {{ vet.lastName }}</td>
                            <td>
                                <span v-if="vet.specialties.length">
                                    {{ vet.specialties.map(function (specialty) { return specialty.name; }).join(', ') }}
                                </span>
                                <span v-else>none</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>
    `
};
