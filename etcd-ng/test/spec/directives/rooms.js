'use strict';

describe('Directive: rooms', function () {

  // load the directive's module
  beforeEach(module('etcdClientApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<rooms></rooms>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the rooms directive');
  }));
});
