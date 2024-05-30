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

        self.saveSubscription = function() {
            console.log('Subscribe:', self.subscribe);
            console.log('Selected Vet ID:', self.selectedVetId);

            var data = {
                subscribe: self.subscribe,
                selectedVetId: self.selectedVetId
            };
            var req;
            req = $http.post('api/subscription', data).then(function(response) {
                console.log('Data saved successfully');
            }, function(error) {
                console.log('An error occurred:', error);
            });

            req.then(function () {
                $state.go('ownerDetails', {ownerId: ownerId});
            });
        };

    }]);
