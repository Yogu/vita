package de.unistuttgart.vis.vita.model.search;



import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

import de.unistuttgart.vis.vita.model.Model;
import de.unistuttgart.vis.vita.model.document.Chapter;
import de.unistuttgart.vis.vita.model.document.Range;
import de.unistuttgart.vis.vita.model.document.TextPosition;

/**
 * This class performs the searching for a word or phrase in a document
 * 
 *
 */
public class Searcher {

  private static final String NO_SPECIAL_CHARACTERS = "[\\d\\s\\p{Alpha}]+";
  private static final String CHAPTER_ID = "chapterId";
  private static final String CHAPTER_TEXT = "chapterText";
  private static final String SPECIAL_CHARACTES = "\\p{Punct}*";

  public List<Range> searchString(de.unistuttgart.vis.vita.model.document.Document document,
      String searchString, List<Chapter> chapters, Model model) throws IOException, ParseException {
    if (chapters.isEmpty()) {
      return new ArrayList<Range>();
    }

    // This empty set allows to search for stop words
    CharArraySet charArraySet = new CharArraySet(0, true);
    StandardAnalyzer analyzer = new StandardAnalyzer(charArraySet);
    List<Range> ranges = new ArrayList<Range>();
    QueryParser queryParser = new QueryParser(CHAPTER_TEXT, analyzer);
    Query query = queryParser.parse(QueryParser.escape(searchString));
    IndexSearcher indexSearcher =
        model.getTextRepository().getIndexSearcherForDocument(document.getId());

    // That are documents in an index, which contains the searchString
    ScoreDoc[] hits =
        indexSearcher.search(query, indexSearcher.getIndexReader().numDocs()).scoreDocs;

    callCorrectTokenizers(searchString, chapters, ranges, indexSearcher, hits, document
        .getMetrics().getCharacterCount());
    indexSearcher.getIndexReader().close();

    Collections.sort(ranges);
    return ranges;
  }

  /**
   * Calls the right Tokenizer regarding the searchString
   *
   * @param searchString
   * @param chapters
   * @param ranges
   * @param indexSearcher
   * @param hits
   * @throws IOException
   */
  private void callCorrectTokenizers(String searchString, List<Chapter> chapters,
      List<Range> ranges, IndexSearcher indexSearcher, ScoreDoc[] hits, int documentLength)
      throws IOException {
    for (int i = 0; i < hits.length; i++) {

      String chapterText = indexSearcher.doc(hits[i].doc).getField(CHAPTER_TEXT).stringValue();
      StringReader reader = new StringReader(chapterText);
      String[] words = searchString.split(" ");
      Tokenizer tokenizer;

      if (searchString.matches(NO_SPECIAL_CHARACTERS)) {
        tokenizer = new StandardTokenizer(reader);
      } else {
        tokenizer = new WhitespaceTokenizer(reader);
      }

      Chapter chapter = getCorrectChapter(indexSearcher.doc(hits[i].doc), chapters);
      if (chapter == null) {
        // We retrieved a hit for a chapter that was not requested
        continue;
      }

      addRangesToList(tokenizer, searchString, words, chapter, ranges, chapterText, documentLength);
    }
  }

  /**
   * Returns the correct chapter regarding the current hit document
   * 
   * @param document
   * @param chapters
   * @return
   */
  private Chapter getCorrectChapter(Document document, List<Chapter> chapters) {

    for (Chapter chapter : chapters) {
      if (document.getField(CHAPTER_ID).stringValue().equals(chapter.getId())) {
        return chapter;
      }
    }
    return null;
  }

  /**
   * Produces the Ranges and adds them to Ranges list
   * 
   * @param tokenizer - The Lucene Tokenizer with the complete text of chapter.
   * @param searchString - The complete input to search for.
   * @param words - The words in the searchString.
   * @param currentChapter
   * @param ranges
   * @param documentLength - the length of the whole document
   * @throws IOException
   */
  private void addRangesToList(Tokenizer tokenizer, String searchString, String[] words,
      Chapter currentChapter, List<Range> ranges, String chapterText, int documentLength)
      throws IOException {

    if (isSingleWord(words)) {
      searchSingleWord(tokenizer, searchString, currentChapter, ranges, documentLength);
      tokenizer.end();
      tokenizer.close();
    } else if (isPhrase(words)) {
      searchPhrase(tokenizer, searchString, words, currentChapter, ranges, chapterText,
          documentLength);
      tokenizer.end();
      tokenizer.close();
    }
  }

  /**
   * Searches for a single word and updates the ranges.
   * 
   * @param tokenizer - The Lucene Tokenizer with the complete text of chapter.
   * @param searchString - The word to search for.
   * @param currentChapter - The chapter to search in.
   * @param ranges - The ranges of the found words will be added here.
   * @param documentLength - The length of the whole document.
   * @throws IOException
   */
  private void searchSingleWord(Tokenizer tokenizer, String searchString, Chapter currentChapter,
      List<Range> ranges, int documentLength) throws IOException {
    // will be incremented
    CharTermAttribute charTermAttrib = tokenizer.getAttribute(CharTermAttribute.class);
    OffsetAttribute offset = tokenizer.getAttribute(OffsetAttribute.class);

    tokenizer.reset();
    while (tokenizer.incrementToken()) {
      if (charTermAttrib.toString().toLowerCase().matches(searchString.toLowerCase())) {
        int startOffset = offset.startOffset() + currentChapter.getRange().getStart().getOffset();
        int endOffset = offset.endOffset() + currentChapter.getRange().getStart().getOffset();

        ranges.add(new Range(TextPosition.fromGlobalOffset(startOffset, documentLength),
            TextPosition.fromGlobalOffset(endOffset, documentLength)));
      }
    }
  }


  /**
   * Searches for a phrase and updates the ranges.
   * 
   * @param tokenizer - The Lucene Tokenizer with the complete text of chapter.
   * @param searchString - The complete input to search for.
   * @param words - The words in the searchString.
   * @param currentChapter - The chapter to search in.
   * @param ranges - The ranges of the found words will be added here.
   * @param chapterText - The text of the chapter.
   * @param documentLength - The length of the whole document.
   * @throws IOException
   */
  private void searchPhrase(Tokenizer tokenizer, String searchString, String[] words,
      Chapter currentChapter, List<Range> ranges, String chapterText, int documentLength)
      throws IOException {

    // will be incremented
    CharTermAttribute charTermAttrib = tokenizer.getAttribute(CharTermAttribute.class);
    OffsetAttribute offset = tokenizer.getAttribute(OffsetAttribute.class);


    List<String> tokens = new ArrayList<String>();
    tokenizer.reset();
    while (tokenizer.incrementToken()) {

      // search for first word of the phrase
      if (charTermAttrib.toString().toLowerCase()
          .matches(SPECIAL_CHARACTES + words[0].toLowerCase() + SPECIAL_CHARACTES)) {

      // global start offset of the first token
      int startOffset = offset.startOffset() + currentChapter.getRange().getStart().getOffset();
      tokens.add(charTermAttrib.toString());

      // local start offset of the second token of the phrase
      int beginIndexOfSubstring = offset.startOffset() + words[0].length() + 1;

      // get the found phrase
      PhraseExtractor phraseExtracter = new PhraseExtractor();
      Phrase tokenInfo =
          phraseExtracter.extractPhrase(words, tokens, chapterText,
              tokenizer.getClass().toString(), beginIndexOfSubstring);
      String phrase = tokenInfo.getPhrase();

      // found phrase is same as searched phrase
      // check with special character at the front and back, because lucene gives the complete
      // tokens with special characters
      if (phrase.toLowerCase().matches(
          SPECIAL_CHARACTES + searchString.toLowerCase() + SPECIAL_CHARACTES)) {
        int endOffset =
            tokenInfo.getEndOffset() + beginIndexOfSubstring
                + currentChapter.getRange().getStart().getOffset();

        ranges.add(new Range(TextPosition.fromGlobalOffset(startOffset, documentLength),
            TextPosition.fromGlobalOffset(endOffset, documentLength)));
      }
      tokens.clear();
      }
    }
  }

  /**
   * Checks if an array of words is a phrase (more than one word).
   * 
   * @param words - The words to search for.
   * @return true: words is a phrase. false: words is not a phrase.
   */
  private boolean isPhrase(String[] words) {
    return words != null && words.length > 1;
  }

  /**
   * Checks if an array of words is a single word.
   * 
   * @param words - The words to search for.
   * @return true: words is a single word. false: words is not a single word.
   */
  private boolean isSingleWord(String[] words) {
    return words != null && words.length == 1;
  }

}
