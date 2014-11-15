(function(angular) {
  'use strict';

  var vitaControllers = angular.module('vitaControllers');

  vitaControllers.controller('GraphNetworkCtrl', ['$scope', '$routeParams', 'DocumentParts',
      function($scope, $routeParams, DocumentParts) {
        $scope.entities = [];

        var sliderMin = 0, sliderMax = 100;

        $('#slider-range').slider({
          range: true,
          min: sliderMin,
          max: sliderMax,
          values: [sliderMin, sliderMax],
          slide: function(event, ui) {
            var start = ui.values[0], end = ui.values[1];

            setSliderLabel(start, end);

            $scope.rangeStart = start / 100;
            $scope.rangeEnd = end / 100;
            $scope.$apply();
          }
        });

        setSliderLabel(sliderMin, sliderMax);

        function setSliderLabel(start, end) {
          $('#amount').val(start + ' - ' + end);
        }

        $scope.loadGraphNetwork = function(person) {
          var position = $scope.entities.indexOf(person);
          if (position > -1) {
            $scope.entities.splice(position, 1);
          } else {
            $scope.entities.push(person);
          }
        };

        $scope.showFingerprint = function(ids) {
          $scope.fingerprintEntityIds = ids;
          $scope.$apply();
        };

        $scope.deselectAll = function() {
          $scope.entities = [];
        };

        $scope.reset = function(persons) {
          $scope.entities = persons.slice(0, 5);
        };

        DocumentParts.get({
          documentId: $routeParams.documentId
        }, function(response) {
          $scope.parts = response.parts;
        });

        $scope.isActive = function(person) {
          return ($scope.entities.indexOf(person) > -1);
        };
      }]);

})(angular);
