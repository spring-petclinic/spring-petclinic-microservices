'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;

        $http.get('api/client/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
        });
    }]);
