import { clearError, errorState } from './state/errors.js';

export const RootApp = {
    setup() {
        return {
            errorState,
            clearError
        };
    },
    template: `
        <div>
            <navigation-bar></navigation-bar>
            <main class="container-fluid">
                <div class="container xd-container">
                    <div v-if="errorState.message" class="alert alert-danger alert-dismissible" role="alert">
                        <button type="button" class="btn-close" aria-label="Close" @click="clearError"></button>
                        <div>{{ errorState.message }}</div>
                        <ul v-if="errorState.details.length"><li v-for="detail in errorState.details" :key="detail">{{ detail }}</li></ul>
                    </div>
                    <router-view></router-view>
                </div>
            </main>
            <chat-box></chat-box>
            <app-footer></app-footer>
        </div>`
};
