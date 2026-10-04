import { computed, ref } from 'vue';

import { request, requireArray } from '../api/client.js';
import { matchesQuery } from '../utils/matchesQuery.js';
import { useRequest } from '../utils/useRequest.js';

export const OwnersPage = {
    setup() {
        const owners = ref([]);
        const query = ref('');
        const state = useRequest(async ({ signal }) => {
            owners.value = requireArray(await request('/api/customer/owners', { signal }), 'Owners could not be loaded.');
        });
        const filteredOwners = computed(() => {
            const normalizedQuery = query.value.trim().toLowerCase();
            if (!normalizedQuery) {
                return owners.value;
            }
            return owners.value.filter((owner) => matchesQuery(owner, normalizedQuery));
        });

        return {
            query,
            filteredOwners,
            loading: state.loading,
            failed: state.failed,
            failure: state.failure
        };
    },
    template: `
        <section>
            <loading-spinner v-if="loading"></loading-spinner>
            <load-failure v-else-if="failed" :error="failure" resource="Owner data"></load-failure>
            <div v-else>
                <h2>Owners</h2>
                <form class="mb-4" style="max-width: 20em; margin-top: 2em;" @submit.prevent>
                    <div class="mb-3">
                        <label for="owner-query">Last name</label>
                        <input id="owner-query" v-model="query" class="form-control" name="query">
                    </div>
                </form>
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Address</th>
                            <th class="d-none d-md-table-cell">City</th>
                            <th>Telephone</th>
                            <th class="d-none d-md-table-cell">Pets</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="owner in filteredOwners" :key="owner.id">
                            <td>
                                <router-link :to="'/owners/details/' + owner.id">
                                    {{ owner.firstName }} {{ owner.lastName }}
                                </router-link>
                            </td>
                            <td>{{ owner.address }}</td>
                            <td class="d-none d-md-table-cell">{{ owner.city }}</td>
                            <td>{{ owner.telephone }}</td>
                            <td class="d-none d-md-table-cell">
                                {{ owner.pets.map(function (pet) { return pet.name; }).join(', ') }}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>
    `
};
