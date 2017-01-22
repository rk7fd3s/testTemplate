'use strict';

// Dependencies
app.controller('topCtrl', ['$scope', 'topService', function($scope, topService){
  $scope.title= topService.title;
}])
