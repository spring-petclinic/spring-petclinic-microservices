'use strict';

/**
 * Knows the identity of the current user, as exposed by the API gateway, and drives the
 * OAuth2 login and logout of the application.
 */
angular.module('infrastructure')
    .factory('UserService', ['$http', function ($http) {

        var LOGIN_URL = '/oauth2/authorization/petclinic';

        var currentUser = {authenticated: false, username: null, roles: []};

        function readCookie(name) {
            var match = document.cookie.match(new RegExp('(^|;\\s*)' + name + '=([^;]*)'));
            return match ? decodeURIComponent(match[2]) : null;
        }

        return {
            load: function () {
                return $http.get('/api/user/me').then(function (response) {
                    currentUser = response.data;
                    return currentUser;
                });
            },
            current: function () {
                return currentUser;
            },
            login: function () {
                window.location.href = LOGIN_URL;
            },
            loginUrl: function () {
                return LOGIN_URL;
            },
            /**
             * The logout is a form POST so that the browser follows the redirections of
             * the RP initiated logout, up to the authorization server and back.
             */
            csrfToken: function () {
                return readCookie('XSRF-TOKEN');
            }
        };
    }]);
