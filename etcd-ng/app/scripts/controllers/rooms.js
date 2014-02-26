'use strict';

angular.module('etcdClientApp')
  .controller('RoomsCtrl', function ($scope) {

    $scope.rooms = ['room1', 'room2'];

    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
