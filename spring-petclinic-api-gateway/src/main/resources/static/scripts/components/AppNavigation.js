import { ref, onMounted } from 'vue';
import { request, getCsrfToken } from '../api/client.js';

export const AppNavigation = {
    setup() {
        const user = ref({ authenticated: false, username: null, roles: [] });
        const loginUrl = '/oauth2/authorization/petclinic';
        const csrfToken = ref('');

        onMounted(async () => {
            try {
                const data = await request('/api/user/me');
                if (data && typeof data === 'object') {
                    user.value = data;
                }
            } catch (err) {
                // Ignore error, user remains anonymous
            }
            csrfToken.value = getCsrfToken();
        });

        return {
            user,
            loginUrl,
            csrfToken
        };
    },
    template: `
        <nav class="navbar navbar-expand-lg navbar-dark" role="navigation">
            <div class="container-fluid">
                <router-link class="navbar-brand" to="/"><span></span></router-link>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#main-navbar" aria-controls="main-navbar" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="main-navbar">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                        <li class="nav-item"><router-link class="nav-link" to="/" title="home page"><span class="fa fa-home me-2" aria-hidden="true"></span><span>Home</span></router-link></li>
                        <li class="nav-item"><router-link exact-active-class="active" class="nav-link" to="/owners" title="find owners"><span class="fa fa-search me-2" aria-hidden="true"></span><span>Find owners</span></router-link></li>
                        <li class="nav-item"><router-link exact-active-class="active" class="nav-link" to="/owners/new" title="register owner"><span class="fa fa-plus me-2" aria-hidden="true"></span><span>Register owner</span></router-link></li>
                        <li class="nav-item"><router-link exact-active-class="active" class="nav-link" to="/vets" title="veterinarians"><span class="fa fa-th-list me-2" aria-hidden="true"></span><span>Veterinarians</span></router-link></li>
                    </ul>
                    <ul class="navbar-nav ms-auto mb-2 mb-lg-0">
                        <li v-if="!user.authenticated" class="nav-item">
                            <a class="nav-link" :href="loginUrl" title="log in">
                                <span class="fa fa-sign-in me-2" aria-hidden="true"></span>
                                <span>Login</span>
                            </a>
                        </li>
                        <li v-if="user.authenticated" class="nav-item">
                            <span class="nav-link" :title="user.roles ? user.roles.join(', ') : ''">
                                <span class="fa fa-user me-2" aria-hidden="true"></span>
                                <span>{{ user.username }}</span>
                            </span>
                        </li>
                        <li v-if="user.authenticated" class="nav-item">
                            <form method="post" action="/logout" class="d-inline">
                                <input type="hidden" name="_csrf" :value="csrfToken" />
                                <button type="submit" class="nav-link btn btn-link" title="log out">
                                    <span class="fa fa-sign-out me-2" aria-hidden="true"></span>
                                    <span>Logout</span>
                                </button>
                            </form>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>`
};
