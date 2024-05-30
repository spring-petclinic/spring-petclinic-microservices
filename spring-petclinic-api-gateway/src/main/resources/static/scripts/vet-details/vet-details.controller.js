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

        self.submitVet =  function () {
            if (self.selectedVetId == null){
                return;
            }

            var vetId = $stateParams.vetId;
            var substitute = self.selectedVetId;

            $http.post("api/vet/vets/" + vetId + "/sub", substitute);
        }

    }]);
