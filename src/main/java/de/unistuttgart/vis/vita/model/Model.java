package de.unistuttgart.vis.vita.model;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.server.CloseableService;

/**
 * Represents the Model of the application.
 */
@ManagedBean
@ApplicationScoped
public class Model implements Factory<EntityManager> {
  private TextRepository textRepository = new TextRepository();
  protected EntityManagerFactory entityManagerFactory;

  @Inject
  CloseableService closeableService;

  private static final String PERSISTENCE_UNIT_NAME = "de.unistuttgart.vis.vita";

  static {
    /*
     * Glassfish does not use the driver provided in the war if not explicitly loaded:
     * https://java.net/jira/browse/GLASSFISH-19451
     */
    loadDriver();
  }

  /**
   * Create a default Model instance
   */
  public Model() {
    entityManagerFactory =
        Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
  }

  protected Model(EntityManagerFactory emf) {
    entityManagerFactory = emf;
  }

  /**
   * @return The entity manager.
   */
  public EntityManager getEntityManager() {
    return entityManagerFactory.createEntityManager();
  }

  /**
   * @return the TextRepository
   */
  public TextRepository getTextRepository() {
    return textRepository;
  }

  private static void loadDriver() {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException e) {
      Logger.getLogger(Model.class.getName()).log(Level.WARNING, "Unable to load H2 driver", e);
    }

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      Logger.getLogger(Model.class.getName()).log(Level.WARNING, "Unable to load mysql driver", e);
    }
  }

  @Override
  @RequestScoped
  public EntityManager provide() {
    // hk2
    final EntityManager instance = getEntityManager();

    if (closeableService != null) {
      closeableService.add(new Closeable() {
        @Override
        public void close() throws IOException {
          dispose(instance);
        }
      });
    }
    return instance;
  }

  @RequestScoped
  @Produces
  public EntityManager produce() {
    // weld
    return getEntityManager();
  }

  @Override
  public void dispose(@Disposes EntityManager instance) {
    if (!instance.isOpen())
      return;

    instance.close();
  }

  public void closeAllEntityManagers() {
    entityManagerFactory.close();
  }
}
