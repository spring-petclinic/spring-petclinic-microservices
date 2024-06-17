'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;

        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
            console.log(resp.data.toString())
        });
        $http.get('api/vet/vets').then(function (resp) {
            self.vetList = resp.data;
        });
    }]);
