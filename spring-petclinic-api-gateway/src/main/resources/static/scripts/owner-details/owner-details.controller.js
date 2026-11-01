'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;

        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
        });

        self.cancelVisit = function (pet, visit) {
            var url = 'api/visit/owners/' + self.owner.id + '/pets/' + pet.id + '/visits/' + visit.id + '/cancel';
            $http.put(url).then(function (resp) {
                visit.status = resp.data.status;
            });
        };
    }]);
