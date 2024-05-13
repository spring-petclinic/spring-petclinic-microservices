'use strict';

angular.module('ownerForm')
    .controller('OwnerFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        var ownerId = $stateParams.ownerId || 0;

        if (!ownerId) {
            self.owner = {};
        } else {
            $http.get("api/customer/owners/" + ownerId).then(function (resp) {
                self.owner = resp.data;
            });
        }

        self.submitOwnerForm = function () {
            var id = self.owner.id;

            if (id) {
                $http.put('api/customer/owners/' + id, self.owner).then(function () {
                    $state.go('ownerDetails', {ownerId: ownerId});
                });
            } else {
                $http.post('api/customer/owners', self.owner).then(function () {
                    $state.go('owners');
                });
            }
        };
    }]);
