package de.unistuttgart.vis.vita.analysis.modules;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.IndexSearcher;

import de.unistuttgart.vis.vita.analysis.ModuleResultProvider;
import de.unistuttgart.vis.vita.analysis.annotations.AnalysisModule;
import de.unistuttgart.vis.vita.analysis.results.DocumentPersistenceContext;
import de.unistuttgart.vis.vita.analysis.results.ImportResult;
import de.unistuttgart.vis.vita.model.Model;
import de.unistuttgart.vis.vita.model.document.Chapter;
import de.unistuttgart.vis.vita.model.document.Document;
import de.unistuttgart.vis.vita.model.document.DocumentPart;
import de.unistuttgart.vis.vita.model.progress.AnalysisProgress;
import de.unistuttgart.vis.vita.model.progress.FeatureProgress;

/**
 * The feature module that stores document outline and document metadata
 * 
 * "Feature module" means that it interacts with the data base. It stores the progress as well as
 * the result.
 * 
 * This module depends on {@link IndexSearcher} which guarantees that the document text is persisted
 * in lucene.
 */
@AnalysisModule(dependencies = {ImportResult.class, DocumentPersistenceContext.class, Model.class,
                                IndexSearcher.class}, weight = 0.1)
public class TextFeatureModule extends AbstractFeatureModule<TextFeatureModule> {

  @Override
  public TextFeatureModule storeResults(ModuleResultProvider result, Document document,
      EntityManager em)
      throws Exception {
    
    ImportResult importResult = result.getResultFor(ImportResult.class);
    
    document.getContent().getParts().addAll(importResult.getParts());
    for (DocumentPart part : importResult.getParts()) {
      em.persist(part);
      for (Chapter chapter : part.getChapters()) {
        em.persist(chapter);
      }
    }
    
    String oldTitle = document.getMetadata().getTitle();
    document.setMetadata(importResult.getMetadata());
    
    // Restore the old title which is the file name if no title has been found
    if (StringUtils.isEmpty(document.getMetadata().getTitle()))
        document.getMetadata().setTitle(oldTitle);

    return this;
  }

  @Override
  protected Iterable<FeatureProgress> getProgresses(AnalysisProgress progress) {
    return Arrays.asList(progress.getTextProgress());
  }

}
