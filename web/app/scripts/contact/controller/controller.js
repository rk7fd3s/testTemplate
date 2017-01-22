'use strict';

// Dependencies
app.controller('contactCtrl', ['$scope', 'contactService', function($scope, contactService){
  $scope.title= contactService.title;
}])
