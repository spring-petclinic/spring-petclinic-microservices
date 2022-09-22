'use strict';

angular.module('messageForm')
    .controller('MessageFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        self.submit = function () {
            const msg = {
                petId: self.petId,
                message: self.message
            }

            let req = $http.post('asb', msg);
            req.then(function (response) {
                self.result = response.data;
            })
            req.catch(function (response) {
                alert(response);
            });
            
        }
    }]);
