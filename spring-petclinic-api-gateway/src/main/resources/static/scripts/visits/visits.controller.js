'use strict';

angular.module('visits')
    .controller('VisitsController', ['$http', '$state', '$stateParams', '$filter', function ($http, $state, $stateParams, $filter) {
        var self = this;
        var petId = $stateParams.petId || 0;
        var url = "api/visit/owners/" + ($stateParams.ownerId || 0) + "/pets/" + petId + "/visits";
        self.date = new Date();
        self.desc = "";
        self.selectedVetId = null;

        $http.get(url).then(function (resp) {
            self.visits = resp.data;
        });

        $http.get('api/vet/vets').then(function (resp) {
            self.vetList = resp.data;
        });

        self.submit = function () {
            $http.get('api/vet/vets/' + self.selectedVetId + '/chose').then(function (resp) {
                    var availableVet = resp.data;
                    console.log("DEBUG: availableVet= "+ availableVet)
                    if (availableVet === -1) {
                        alert("Vet and their substitute are not available at this time");
                        return;
                    }
                    var data = {
                        date: $filter('date')(self.date, "yyyy-MM-dd"),
                        description: self.desc,
                        vetId: availableVet
                    };
                    if(self.selectedVetId !== data.vetId){
                        alert("Originally requested vet is not available, substitute was booked instead.")
                    }
                    console.log("DEBUG: data= "+ data)
                    $http.post(url, data).then(function () {
                        $state.go('ownerDetails', {ownerId: $stateParams.ownerId});
                    });
                    alert("Visit added")
                }
            );
        };
    }]);
