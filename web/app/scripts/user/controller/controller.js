'use strict';

// Dependencies
app.controller('userCtrl', ['$scope', 'userService', function($scope, userService){
  $scope.userList= userService.getUserList();
}])
