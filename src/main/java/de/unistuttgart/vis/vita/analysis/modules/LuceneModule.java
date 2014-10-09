package de.unistuttgart.vis.vita.analysis.modules;

import de.unistuttgart.vis.vita.analysis.Module;
import de.unistuttgart.vis.vita.analysis.ModuleResultProvider;
import de.unistuttgart.vis.vita.analysis.ProgressListener;
import de.unistuttgart.vis.vita.analysis.annotations.AnalysisModule;
import de.unistuttgart.vis.vita.analysis.results.DocumentPersistenceContext;
import de.unistuttgart.vis.vita.analysis.results.ImportResult;
import de.unistuttgart.vis.vita.model.Model;
import de.unistuttgart.vis.vita.model.TextRepository;
import de.unistuttgart.vis.vita.model.document.DocumentPart;

import org.apache.lucene.search.IndexSearcher;

import java.util.ArrayList;
import java.util.List;

/**
 * LuceneModule class with its result IndexSearcher
 */
@AnalysisModule(dependencies = {ImportResult.class, Model.class, DocumentPersistenceContext.class})
public class LuceneModule implements Module<IndexSearcher> {

  private ImportResult importResult;
  private List<DocumentPart> documentParts = new ArrayList<DocumentPart>();
  private TextRepository textRepository;
  private DocumentPersistenceContext documentPersistenceContext;


  @Override
  public void observeProgress(double progress) {
    // TODO Auto-generated method stub

  }

  /**
   * Stores the chapters of DocumentParts from ImportResult in the lucene textRepository of model
   */
  @Override
  public IndexSearcher execute(ModuleResultProvider result, ProgressListener progressListener)
      throws Exception {

    IndexSearcher indexSearcher = null;
    textRepository = result.getResultFor(Model.class).getTextRepository();
    importResult = result.getResultFor(ImportResult.class);
    documentPersistenceContext = result.getResultFor(DocumentPersistenceContext.class);
    documentParts = importResult.getParts();
    if (!documentParts.isEmpty()) {
      for (DocumentPart documentPart : documentParts) {
        textRepository.storeChaptersTexts(documentPart.getChapters(),
                                          documentPersistenceContext.getDocumentId());
      }
      indexSearcher =
          textRepository.getIndexSearcherForDocument(documentPersistenceContext.getDocumentId());
    }
    return indexSearcher;
  }

}
