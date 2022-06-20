'use strict';

/**
 * Global HTTP errors handler.
 */
angular.module('infrastructure')
    .factory('HttpErrorHandlingInterceptor', function () {
        return {
            responseError: function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
                return response;
            }
        }
    });
