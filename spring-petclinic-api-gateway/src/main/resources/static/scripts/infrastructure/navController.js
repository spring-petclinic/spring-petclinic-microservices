'use strict';

angular.module('infrastructure')
    .controller('NavController', ['UserService', function (UserService) {
        var self = this;

        self.user = UserService.current();
        self.loginUrl = UserService.loginUrl();
        self.csrfToken = UserService.csrfToken();

        UserService.load().then(function (user) {
            self.user = user;
            self.csrfToken = UserService.csrfToken();
        });
    }]);
