'use strict';

/**
 * Global HTTP errors handler.
 */
angular.module('infrastructure')
    .factory('HttpErrorHandlingInterceptor', ['$q', function ($q) {
        return {
            responseError: function (response) {
                // The session expired, or the user is not logged in yet: restart an
                // OAuth2 authorization code flow.
                if (response.status === 401) {
                    window.location.href = '/oauth2/authorization/petclinic';
                    return $q.reject(response);
                }
                if (response.status === 403) {
                    alert("You are not allowed to perform this operation.");
                    return $q.reject(response);
                }
                var error = response.data;
                if (error && error.errors) {
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                }
                else if (error && error.error) {
                    alert(error.error);
                }
                return response;
            }
        }
    }]);
