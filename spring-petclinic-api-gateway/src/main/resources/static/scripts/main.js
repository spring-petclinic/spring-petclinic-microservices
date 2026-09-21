import { createApp } from 'vue';

import { RootApp } from './RootApp.js';
import { AppFooter } from './components/AppFooter.js';
import { AppNavigation } from './components/AppNavigation.js';
import { ChatBox } from './components/ChatBox.js?v=explicit-esm-dependencies';
import { LoadFailure, LoadingSpinner } from './components/LoadingState.js';
import { router } from './router.js';

const application = createApp(RootApp);
application.component('loading-spinner', LoadingSpinner);
application.component('load-failure', LoadFailure);
application.component('navigation-bar', AppNavigation);
application.component('chat-box', ChatBox);
application.component('app-footer', AppFooter);
application.use(router);
application.mount('#app');
