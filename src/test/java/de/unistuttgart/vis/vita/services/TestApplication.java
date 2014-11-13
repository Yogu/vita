package de.unistuttgart.vis.vita.services;

import javax.persistence.EntityManager;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import de.unistuttgart.vis.vita.analysis.AnalysisController;
import de.unistuttgart.vis.vita.model.Model;
import de.unistuttgart.vis.vita.model.UnitTestModel;
import de.unistuttgart.vis.vita.services.document.DocumentsService;

public class TestApplication extends ResourceConfig {
  public TestApplication() {
    super(MultiPartFeature.class, DocumentsService.class);
    packages(true, "de.unistuttgart.vis.vita.services");
    register(new MyApplicationBinder());
  }

  private static class MyApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
      bind(UnitTestModel.class).to(Model.class);
      bindFactory(UnitTestModel.class).to(EntityManager.class);
      bind(AnalysisController.class).to(AnalysisController.class);
    }
  }
}
