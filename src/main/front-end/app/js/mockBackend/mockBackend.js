(function(angular) {
  'use strict';

  var app = angular.module('vita');

  app.run(['$httpBackend', 'TestData', function($httpBackend, TestData) {

    $httpBackend.whenGET(new RegExp('\.html$')).passThrough();
    $httpBackend.whenGET(new RegExp('/documents$')).respond(TestData.documents);
    $httpBackend.whenGET(new RegExp('/documents/[^/]+$')).respond(TestData.singleDocument);

  }]);
})(angular);
