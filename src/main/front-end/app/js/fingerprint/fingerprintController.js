(function(angular) {
  'use strict';

  var vitaControllers = angular.module('vitaControllers');

  // Controller responsible for the fingerprint page
  vitaControllers.controller('FingerprintCtrl',
      ['$scope', 'Page', '$routeParams', 'DocumentParts', 'Person', 'CssClass', 'FingerprintSynchronizer',
      function($scope, Page, $routeParams, DocumentParts, Person, CssClass, FingerprintSynchronizer) {

        // Provide the service for direct usage in the scope
        $scope.CssClass = CssClass;
        $scope.FingerprintSynchronizer = FingerprintSynchronizer;

        Page.breadcrumbs = 'Fingerprint';
        Page.setUpForCurrentDocument();

        $scope.activeFingerprints = [];
        $scope.activeFingerprintIds = [];
        $scope.toggleFingerprint = function(person) {
          if ($scope.activeFingerprints.indexOf(person) > -1) {
            $scope.activeFingerprints.splice(
              $scope.activeFingerprints.indexOf(person), 1);
          } else {
            $scope.activeFingerprints.push(person);
            $scope.activeFingerprints.sort(
              function(a, b) {
                return a.rankingValue - b.rankingValue;
            });
          }
          $scope.activeFingerprintIds = $scope.activeFingerprints.map(function(e) {
            return e.id;
          });
        };

        DocumentParts.get({
          documentId: $routeParams.documentId
        }, function(response) {
          $scope.parts = response.parts;
        });

        Person.get({
          documentId: $routeParams.documentId
        }, function(response) {
          $scope.persons = response.persons;
        });

        $scope.deselectAll = function() {
          $scope.activeFingerprints = [];
          $scope.activeFingerprintIds = [];
        };
      }]);

})(angular);
