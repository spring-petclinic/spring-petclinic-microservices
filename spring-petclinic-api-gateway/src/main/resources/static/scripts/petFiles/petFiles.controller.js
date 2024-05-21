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

            // Setzen Sie die Variable auf true, um den Spinner anzuzeigen
            //self.uploading = true;


            var upload = function() {
                let  uploadCompleted = false;
                let repeatUpload = false;
                $http.post(url, formData, {
                    transformRequest: angular.identity,
                    headers: { 'Content-Type': undefined }
                }).then(function () {
                    if(!repeatUpload ){
                        uploadCompleted = true;
                        console.log("File: " + file.name + " was uploaded successfully!\n" +"file size: "+ file.size/1024/1024 + " MB");
                        alert("File: " + file.name + " was uploaded successfully!\n" +"file size: "+ file.size/1024/1024 + " MB");
                        return $http.get(url);
                    }
                    //console.log("Upload hat zu lange gedauert 11:13");

                }).then(function (response) {
                    self.files = response.data;
                    $state.go('ownerDetails', { ownerId: ownerId });
                }).catch(function (error) {
                    //alert("Es gibt einen Fehler beim Hochladen der Datei: " + error.message);
                });

                setTimeout(function() {
                    if (uploadCompleted) {
                        repeatUpload = false;
                    } else {
                        repeatUpload = true;
                        //alert("Upload dauert zu lange, versuche erneut... jbt2");
                        console.log("Upload dauert zu lange, versuche erneut...11:13");
                        upload();
                    }
                }, 10000);
            };

            upload();

            // setTimeout(function() {
            //     if (!uploadCompleted) {
            //         //alert("Upload dauert zu lange, versuche erneut...");
            //         upload();
            //     }
            // }, 5000);


            // $http.post(url, formData, {
            //     transformRequest: angular.identity,
            //     headers: { 'Content-Type': undefined }
            // }).then(function () {
            //     alert("File: " + file.name + " was uploaded successfully!\n" +"file size: "+ file.size/1024/1024 + " MB");
            //     return $http.get(url);
            // }).then(function (response) {
            //     self.files = response.data;
            //     $state.go('ownerDetails', { ownerId: ownerId });
            // }).catch(function (error) {
            //     alert("Fehler beim Hochladen der Datei: " + error.message);
            // });

        };

    }]);
