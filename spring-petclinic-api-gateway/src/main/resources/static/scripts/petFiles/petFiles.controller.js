'use strict';

angular.module('petFiles')
    .controller('PetFilesController', ['$http', '$state', '$stateParams', '$filter', function ($http, $state, $stateParams, $filter) {
        var self = this;
        var petId = $stateParams.petId || 0;
        var url = "api/customer/owners/" + ($stateParams.ownerId || 0) + "/pets/" + petId + "/files";
        self.date = new Date();
        self.desc = "";

        function getFiles() {
            $http.get(url).then(function (resp) {
                self.files = resp.data;
            });
        }

        getFiles();

        self.uploadFile = function () {
            var file = document.getElementById('file').files[0];
            var formData = new FormData();
            formData.append('file', file);
            formData.append('date', $filter('date')(self.date, "yyyy-MM-dd"));
            formData.append('description', self.desc);

            $http.post(url, formData, {
                transformRequest: angular.identity,
                headers: { 'Content-Type': undefined }
            }).then(function () {
                alert("File: " + file.name + " was uploaded successfully!\n" +"file size: "+ file.size/1024/1024 + " MB");
                return $http.get(url);
            }).then(function (response) {
                self.files = response.data;
                $state.go('ownerDetails', { ownerId: ownerId });
            });

        };

    }]);
