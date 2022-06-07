'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        let self = this;

        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
        });
    }]);
