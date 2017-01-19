'use strict';

angular.module('webapp', [
    'ui.router'
  ])
  .config(['$locationProvider', '$urlRouterProvider',
  function ($locationProvider, $urlRouterProvider) {

    $urlRouterProvider
      .otherwise('/');

    $locationProvider.html5Mode(true);

  }
]);
