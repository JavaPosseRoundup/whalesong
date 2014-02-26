'use strict';

angular.module('etcdClientApp')
  .directive('rooms', function () {
    return {
      templateUrl: '../views/rooms.html',
      controller: 'RoomsCtrl',
      restrict: 'E'
    };
  });
