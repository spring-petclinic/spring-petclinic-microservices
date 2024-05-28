'use strict';

angular.module('vetDetails')
    .controller('VetDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;
        self.selectedVetId = null;

        $http.get('api/vet/vets').then(function (resp) {
            self.vetList = resp.data;
        });

        $http.get('api/gateway/vets/' + $stateParams.vetId).then(function (resp) {
            self.vet = resp.data;
        });


    }]);
