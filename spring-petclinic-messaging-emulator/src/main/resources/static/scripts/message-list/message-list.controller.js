'use strict';

angular.module('messageList')
    .controller('MessageListController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;


        $http.get('asb').then(function (resp) {
            self.messages = resp.data;
        });

        self.refresh = function () {
            $http.get('asb').then(function (resp) {
                self.messages = resp.data;
            });
        };
    }]);
