import { ref } from 'vue';
import { useRoute } from 'vue-router';

import { request, requireObject } from '../api/client.js';
import { formatDate } from '../utils/dates.js';
import { useRequest } from '../utils/useRequest.js';

export const OwnerDetailsPage = {
    setup() {
        const route = useRoute();
        const owner = ref(null);
        const state = useRequest(async ({ signal }) => {
            owner.value = requireObject(await request(`/api/gateway/owners/${route.params.ownerId}`, { signal }), 'Owner not found.');
        });

        return {
            owner,
            formatDate,
            loading: state.loading,
            failed: state.failed,
            failure: state.failure
        };
    },
    template: `
        <section>
            <loading-spinner v-if="loading"></loading-spinner>
            <load-failure v-else-if="failed" :error="failure" resource="Owner details"></load-failure>
            <div v-else-if="owner">
                <h2>Owner Information</h2>
                <table class="table table-striped">
                    <tbody>
                        <tr>
                            <th class="col-sm-3">Name</th>
                            <td><b>{{ owner.firstName }} {{ owner.lastName }}</b></td>
                        </tr>
                        <tr><th>Address</th><td>{{ owner.address }}</td></tr>
                        <tr><th>City</th><td>{{ owner.city }}</td></tr>
                        <tr><th>Telephone</th><td>{{ owner.telephone }}</td></tr>
                        <tr>
                            <td>
                                <router-link class="btn btn-primary" :to="'/owners/' + owner.id + '/edit'">
                                    Edit Owner
                                </router-link>
                            </td>
                            <td>
                                <router-link class="btn btn-primary" :to="'/owners/' + owner.id + '/new-pet'">
                                    Add New Pet
                                </router-link>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <h2>Pets and Visits</h2>
                <table class="table table-striped">
                    <tbody>
                        <tr v-for="pet in owner.pets" :key="pet.id">
                            <td class="align-top">
                                <dl class="row mb-0">
                                    <dt class="col-md-4">Name</dt>
                                    <dd class="col-md-8">
                                        <router-link :to="'/owners/' + owner.id + '/pets/' + pet.id">
                                            {{ pet.name }}
                                        </router-link>
                                    </dd>
                                    <dt class="col-md-4">Birth Date</dt>
                                    <dd class="col-md-8">{{ formatDate(pet.birthDate) }}</dd>
                                    <dt class="col-md-4">Type</dt>
                                    <dd class="col-md-8">{{ pet.type.name }}</dd>
                                </dl>
                            </td>
                            <td class="align-top">
                                <table class="table table-sm">
                                    <thead>
                                        <tr><th>Visit Date</th><th>Description</th></tr>
                                    </thead>
                                    <tbody>
                                        <tr v-for="visit in pet.visits" :key="visit.id">
                                            <td>{{ formatDate(visit.date) }}</td>
                                            <td>{{ visit.description }}</td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <router-link :to="'/owners/' + owner.id + '/pets/' + pet.id">
                                                    Edit Pet
                                                </router-link>
                                            </td>
                                            <td>
                                                <router-link :to="'/owners/' + owner.id + '/pets/' + pet.id + '/visits'">
                                                    Add Visit
                                                </router-link>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>
    `
};
