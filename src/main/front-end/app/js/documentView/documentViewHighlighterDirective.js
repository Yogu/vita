(function(angular) {
  'use strict';

  var vitaDirectives = angular.module('vitaDirectives');

  vitaDirectives.directive('documentViewHighlighter', [
      'CssClass',
      function(CssClass) {

        var highlighterElement;
        var chapterMap;

        function link(scope, element) {
          highlighterElement = element;
          createChapterMap(scope.parts);

          scope.$watch('[occurrences, entities]', function(newValues, oldValues) {
            if (!angular.equals(newValues, oldValues)) {
              clearChapters();
              highlight(scope.occurrences, scope.documentId, scope.entities,
                  scope.parts, scope.selectedOccurrenceIndex);
            }
          }, true);

          scope.$watch('parts', function(newValue, oldValue) {
            if (!angular.equals(newValue, oldValue)) {
              createChapterMap(scope.parts);
            }
          }, true);

          scope.$watch('selectedOccurrenceIndex', function(newValue, oldValue) {
            if (!angular.equals(newValue, oldValue) && !angular.isUndefined(newValue)) {
              highlightSelectedOccurrence(scope.selectedOccurrenceIndex);
            }
          }, true);
        }

        function highlight(occurrences, documentId, entities, parts, selectedOccurrenceIndex) {
          occurrences = angular.isUndefined(occurrences) ? [] : occurrences;

          occurrences = occurrences.sort(function(a, b) {
            return a.start.offset - b.start.offset;
          });

          occurrences.forEach(function(occurrence, i) {
            occurrence.index = i;
          });

          var chapterOccurrences = getOccurrencesByChapterId(occurrences, parts);

          // Highlight each chapter that contains occurrence(s)
          Object.keys(chapterOccurrences).forEach(function(chapterId, i) {
            var chapter = chapterMap[chapterId];
            var chapterOffset = chapter.range.start.offset;
            highlightChapter(chapterOccurrences[chapterId], chapterOffset, chapterId, entities);
          });
          highlightSelectedOccurrence(selectedOccurrenceIndex);
        }

        function highlightChapter(chapterOccurrences, chapterOffset, chapterId, entities) {
          var chapterHTMLId = 'chapter-' + chapterId;
          var $chapter = $(highlighterElement[0]).find('[id="' + chapterHTMLId + '"] p');
          if ($chapter.length === 0) {
            return;
          }
          var chapterText = $chapter.text();

          var splitParts = splitChapter(chapterText, chapterOccurrences, chapterOffset);

          var highlightedOccurrenceParts = addHighlights(splitParts.occurrenceParts,
                  chapterOccurrences, entities);

          var highlightedChapterText = mergeChapter(highlightedOccurrenceParts,
                  splitParts.nonOccurrenceParts);

          $chapter.html(highlightedChapterText);
        }

        function splitChapter(chapterText, chapterOccurrences, chapterOffset) {
          var occurrenceParts = [];
          var nonOccurrenceParts = [];

          nonOccurrenceParts.push(chapterText.slice(0, chapterOccurrences[0].start.offset
                  - chapterOffset));

          chapterOccurrences.forEach(function(singleOccurrence, i) {
            var occurrenceStart = singleOccurrence.start.offset;
            var occurrenceEnd = singleOccurrence.end.offset;
            var occurrenceText = chapterText.slice(occurrenceStart - chapterOffset, occurrenceEnd
                    - chapterOffset);
            occurrenceParts.push(occurrenceText);
            if (i !== chapterOccurrences.length - 1) {
              // Parts between two occurrences, does not exist for last
              // occurrence in a chapter
              nonOccurrenceParts.push(chapterText.slice(occurrenceEnd - chapterOffset,
                      chapterOccurrences[i + 1].start.offset - chapterOffset));
            } else {
              nonOccurrenceParts.push(chapterText.slice(occurrenceEnd - chapterOffset));
            }
          });

          return {
            occurrenceParts: occurrenceParts,
            nonOccurrenceParts: nonOccurrenceParts
          };
        }

        function addHighlights(occurrenceParts, occurrences, entities) {
          var highlightedOccurrenceParts = [];
          occurrenceParts.forEach(function(occurrencePart, i) {
            var highlightedOccurrencePart = wrap(highlightEntities(occurrencePart, entities),
                    'occurrence', 'occurrence-' + occurrences[i].index);
            highlightedOccurrenceParts.push(highlightedOccurrencePart);
          });
          return highlightedOccurrenceParts;
        }

        function highlightEntities(occurrenceText, entities) {
          if (angular.isUndefined(entities)) {
            return occurrenceText;
          }
          var highlightedText = occurrenceText;
          entities.forEach(function(entity) {
            var names = getAllNames(entity);

            // We need to highlight the longest names first
            names = names.sort(function(a, b) {
              return b.length - a.length;
            });

            names.forEach(function(name) {
              // dots in names must be escaped as they are a special character in a regex
              name = name.replace('.', '\\.');
              highlightedText = highlightedText.replace(new RegExp('(' + name + ')\\b', 'g'), wrap(
                      '$1', CssClass.forRankingValue(entity.rankingValue)));
            });
          });
          return highlightedText;
        }

        function mergeChapter(occurrenceParts, nonOccurrenceParts) {
          var mergedText = '';
          for (var i = 0; i < occurrenceParts.length; i++) {
            mergedText += nonOccurrenceParts[i] + occurrenceParts[i];
          }
          mergedText += nonOccurrenceParts[nonOccurrenceParts.length - 1];
          return mergedText;
        }

        function wrap(text, cssClass, id) {
          return '<span class="' + cssClass + '" id="' + id + '">' + text + '</span>';
        }

        function getAllNames(entity) {
          var names = [];
          names.push(entity.displayName);

          entity.attributes.forEach(function(attribute) {
            if (attribute.attributetype === 'name') {
              names.push(attribute.content);
            }
          });
          return names;
        }

        function highlightSelectedOccurrence(selectedOccurrenceIndex) {
          var prevSelectedOccurence = $(highlighterElement[0]).find('.selected');
          if (prevSelectedOccurence.length !== 0) {
            prevSelectedOccurence.removeClass('selected');
          }
          var newSelectedOccurrence = $(highlighterElement).find(
                  '#occurrence-' + selectedOccurrenceIndex);
          if (newSelectedOccurrence.length === 0) {
            return;
          }
          newSelectedOccurrence.addClass('selected');
          newSelectedOccurrence[0].scrollIntoView();
        }

        function getOccurrencesByChapterId(occurrences, parts) {
          var chapterOccurrences = {};

          occurrences.forEach(function(occurrence) {
            var chapterId = getChapterAtOffset(occurrence.start.offset, parts).id;

            if (angular.isUndefined(chapterOccurrences[chapterId])) {
              chapterOccurrences[chapterId] = [];
            }
            chapterOccurrences[chapterId].push(occurrence);
          });

          return chapterOccurrences;
        }

        function clearChapters() {
          $(highlighterElement[0]).find('[id^="chapter-"] p').each(function() {
            $(this).html($(this).text());
          });
        }

        function createChapterMap(parts) {
          chapterMap = {};

          if (!parts) {
            return;
          }

          for (var pIndex = 0, pLength = parts.length; pIndex < pLength; pIndex++) {
            var chapters = parts[pIndex].chapters;
            for (var cIndex = 0, cLength = chapters.length; cIndex < cLength; cIndex++) {
              var chapter = chapters[cIndex];
              chapterMap[chapter.id] = chapter;
            }
          }
        }

        function getChapterAtOffset(offset, parts) {
          for (var i = 0; i < parts.length; i++) {
            var part = parts[i];
            for (var j = 0; j < part.chapters.length; j++) {
              var chapter = part.chapters[j];
              if (chapter.range.end.offset > offset) {
                return chapter;
              }
            }
          }
          throw new Error('No chapter at offset ' + offset);
        }

        return {
          restrict: 'A',
          scope: {
            occurrences: '=',
            documentId: '=',
            entities: '=',
            selectedOccurrenceIndex: '=',
            parts: '='
          },
          link: link
        };
      }]);

})(angular);
