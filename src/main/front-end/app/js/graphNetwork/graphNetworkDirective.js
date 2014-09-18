(function(angular) {
  'use strict';

  var vitaDirectives = angular.module('vitaDirectives');

  vitaDirectives.directive('graphNetwork', [function() {

    var directive = {
      replace: false,
      restrict: 'EA',
      scope: {
        entities: '=',
        width: '@',
        height: '@'
      },
      link: function(scope, element) {
        buildGraph(element, scope.entities, scope.width, scope.height);

        scope.$watch('entities', function(newValue, oldValue) {
          if (!angular.equals(newValue, oldValue)) {
            updateGraph(scope.entities);
          }
        }, true);
      }
    };

    var graph, force, nodes, links;

    function buildGraph(element, entities, width, height) {
      var container = d3.select(element[0]);
      var width = width || 800;
      var height = height || 400;

      graph = container.append("svg")
          .classed("graph-network", true)
          .attr("width", width)
          .attr("height", height)
          .append('g'); // an extra group for zooming

      var graphData = parseEntitiesToGraphData(entities);

      force = d3.layout.force()
          .nodes(graphData.nodes)
          .links(graphData.links)
          .size([width, height])
          .linkDistance(100)
          .on('tick', setNewPositions);

      redrawElements(graphData);

      force.start();
    }

    function parseEntitiesToGraphData(entities) {
      var entityIdNodeMap = mapEntitiesToNodes(entities);
      var links = [];

      // Create all possible links of each entity
      for (var i = 0, l = entities.length; i < l; i++) {
        var newLinks = createLinksForEntity(entities[i], entityIdNodeMap);
        links = links.concat(newLinks);
      }

      return {
        nodes: entityIdNodeMap.values(),
        links: links
      };
    }

    function mapEntitiesToNodes(entities) {
      var nodeMap = d3.map();

      for (var i = 0, l = entities.length; i < l; i++) {
        var entity = entities[i];

        // Create a shallow copy. We need this, because otherwise d3 would
        // modify the original data
        nodeMap.set(entity.id, {
          id: entity.id,
          displayName: entity.displayName,
          type: entity.type,
          rankingValue: entity.rankingValue
        });
      }

      return nodeMap;
    }

    function createLinksForEntity(entity, entityIdNodeMap) {
      var links = [];

      var possibleRelations = collectPossibleRelations(entity.entityRelations, entityIdNodeMap
              .keys());

      for (var i = 0, l = possibleRelations.length; i < l; i++) {
        var relation = possibleRelations[i];

        var link = {
          // d3 graph attributes
          source: entityIdNodeMap.get(entity.id),
          target: entityIdNodeMap.get(relation.relatedEntity),
          // copy other useful attributes
          relatedEntity: entityIdNodeMap.get(relation.relatedEntity),
          weight: relation.weight
        }

        links.push(link);
      }

      return links;
    }

    function collectPossibleRelations(relations, displayedEntityIds) {
      var possibleRelations = [];

      for (var i = 0, l = relations.length; i < l; i++) {
        var relation = relations[i];

        if (displayedEntityIds.indexOf(relation.relatedEntity) > -1) {
          possibleRelations.push(relation);
        }
      }

      return possibleRelations;
    }

    function setNewPositions() {
      nodes.attr('cx', function(d) {
        return d.x;
      }).attr('cy', function(d) {
        return d.y;
      });

      links.attr('x1', function(d) {
        return d.source.x;
      }).attr('y1', function(d) {
        return d.source.y;
      }).attr('x2', function(d) {
        return d.target.x;
      }).attr('y2', function(d) {
        return d.target.y;
      });
    }

    function redrawElements(graphData) {
      /*
       * Remove all elements because they are redrawn. This is the only solution
       * currently because it isn't guaranteed, that the controller is passing
       * the same objects for the same displayed entities. For example one
       * entity might disappear, but this directive receives completely new
       * objects - even for unchanged entities.
       */
      graph.selectAll('*').remove();

      links = graph.selectAll('.link')
          .data(graphData.links)
          .enter().append('line')
          .classed('link', true);

      nodes = graph.selectAll('.node')
          .data(graphData.nodes)
          .enter().append('circle')
          .classed('node', true)
          .attr('r', 20)
          .call(force.drag);
    }

    function updateGraph(entities) {
      var graphData = parseEntitiesToGraphData(entities);

      force.nodes(graphData.nodes)
          .links(graphData.links)
          .start();

      redrawElements(graphData);
    }

    return directive;
  }]);

})(angular);
