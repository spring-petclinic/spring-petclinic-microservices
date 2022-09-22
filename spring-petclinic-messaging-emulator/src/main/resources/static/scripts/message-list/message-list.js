'use strict';

angular.module('messageList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('message-list', {
                parent: 'app',
                url: '/message-list',
                template: '<message-list></message-list>'
            })
    }]);

