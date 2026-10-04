export const AppNavigation = {
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
                </div>
            </div>
        </nav>`
};
