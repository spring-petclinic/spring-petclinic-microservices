'use strict';

angular.module('vetDetails')
    .controller('VetDetailsController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        self.selectedVetId = null;

        $http.get('api/vet/vets').then(function (resp) {
            self.vetList = resp.data;
        });

        $http.get('api/gateway/vets/' + $stateParams.vetId).then(function (resp) {
            self.vet = resp.data;
        });

        $http.get('api/vet/vets/' + $stateParams.vetId+'/available').then(function (resp){
            self.available = resp.data;
        });

        let aktuelleZeit = new Date();
        let stunden = aktuelleZeit.getHours();
        let minuten = aktuelleZeit.getMinutes();
        let sekunden = aktuelleZeit.getSeconds();
        console.log(`Aktuelle Zeit: ${stunden}:${minuten}:${sekunden}`);


        self.setAvailable = function(){
            let vetId = self.vet.id;
            $http.post("api/vet/vets/" + vetId + "/available", self.available);
        }

        var req;
        self.submitVet =  function () {
            if (self.selectedVetId == null){
                return;
            }

            var vetId = $stateParams.vetId;
            var substitute = self.selectedVetId;

            req = $http.post("api/vet/vets/" + vetId + "/sub", substitute);
            req.then(function () {
                $state.go('vets');
            });
        }
    }]);
