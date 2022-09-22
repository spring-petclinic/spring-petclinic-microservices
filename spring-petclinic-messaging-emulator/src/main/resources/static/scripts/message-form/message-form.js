'use strict';

angular.module('messageForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('messages', {
                parent: 'app',
                url: '/messages',
                template: '<message-form></message-form>'
            })
    }]);

