import { createRouter, createWebHistory } from 'vue-router';

import { OwnerDetailsPage } from './pages/OwnerDetailsPage.js';
import { OwnerFormPage } from './pages/OwnerFormPage.js';
import { OwnersPage } from './pages/OwnersPage.js';
import { PetFormPage } from './pages/PetFormPage.js';
import { VetsPage } from './pages/VetsPage.js';
import { VisitsPage } from './pages/VisitsPage.js';
import { WelcomePage } from './pages/WelcomePage.js';
import { clearError } from './state/errors.js';

export const router = createRouter({
    history: createWebHistory(),
    routes: [{
        path: '/',
        component: WelcomePage
    }, {
        path: '/welcome',
        redirect: '/'
    }, {
        path: '/vets',
        component: VetsPage
    }, {
        path: '/owners',
        component: OwnersPage
    }, {
        path: '/owners/new',
        component: OwnerFormPage
    }, {
        path: '/owners/details/:ownerId',
        component: OwnerDetailsPage
    }, {
        path: '/owners/:ownerId/edit',
        component: OwnerFormPage
    }, {
        path: '/owners/:ownerId/new-pet',
        component: PetFormPage
    }, {
        path: '/owners/:ownerId/pets/:petId',
        component: PetFormPage
    }, {
        path: '/owners/:ownerId/pets/:petId/visits',
        component: VisitsPage
    }, {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }]
});

router.afterEach(clearError);
