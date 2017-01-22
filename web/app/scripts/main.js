'use strict';

var app = angular.module('webapp', [
    'ui.router'
  ])

  .config(['$locationProvider', '$urlRouterProvider',
  function ($locationProvider, $urlRouterProvider) {

    $urlRouterProvider
      .otherwise('/');
      // $locationProvider.html5Mode(true);
    }
  ])

  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('top', {
        url: '/',
        templateUrl: 'scripts/top/view/index.html',
        controller: 'topCtrl as top'
      })
      .state('user', {
        url: '/user',
        templateUrl: 'scripts/user/view/index.html',
        controller: 'userCtrl as user'
      })
      .state('contact', {
        url: '/contact',
        templateUrl: 'scripts/contact/view/index.html',
        controller: 'contactCtrl as contact'
      })
  }])

  // .config(function($urlRouterProvider){
  //   $urlRouterProvider.when('', '/top')
  // })

  .controller('containerCtrl', ['$scope', function ($scope) {
    var MenuProperties = {'top':false, 'user':false, 'contact':false};
    $scope.menu = [];

    for (var prop in MenuProperties) {
      $scope.menu[prop] = MenuProperties[prop];
    }
    $scope.menu['top'] = true;
    $scope.onMenuClick = function (pages) {
      for (var prop in MenuProperties) {
        $scope.menu[prop] = MenuProperties[prop];
      }
      $scope.menu[pages] = true;
    };
  }])
;
