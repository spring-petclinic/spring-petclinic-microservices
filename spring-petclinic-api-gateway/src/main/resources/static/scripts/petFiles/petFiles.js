'use strict';

angular.module('petFiles', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('petFiles', {
                parent: 'app',
                url: '/owners/:ownerId/pets/:petId/files',
                template: '<pet-files></pet-files>'
            });
    }]);
