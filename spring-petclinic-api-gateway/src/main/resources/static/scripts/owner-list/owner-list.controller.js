'use strict';

angular.module('ownerList')
    .controller('OwnerListController', ['$http', function ($http) {
        var self = this;

        $http.get('api/customer/owners').then(function (resp) {
            self.owners = resp.data;
        });
    }]);
