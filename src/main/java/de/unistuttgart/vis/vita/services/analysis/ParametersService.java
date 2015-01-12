/*
 * ParametersService.java
 *
 */

package de.unistuttgart.vis.vita.services.analysis;

import com.sun.org.glassfish.gmbal.Description;

import de.unistuttgart.vis.vita.model.dao.DocumentDao;
import de.unistuttgart.vis.vita.model.document.AnalysisParameters;
import de.unistuttgart.vis.vita.model.document.Document;
import de.unistuttgart.vis.vita.services.responses.parameters.AbstractParameter;
import de.unistuttgart.vis.vita.services.responses.parameters.BooleanParameter;
import de.unistuttgart.vis.vita.services.responses.parameters.MinMaxParameter;
import de.unistuttgart.vis.vita.services.responses.parameters.ParametersResponse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@ManagedBean
public class ParametersService {

  private String id;

  @Inject
  private DocumentDao documentDao;

  /**
   * Sets the id of the document this resource should represent
   *
   * @param id the id
   */
  public ParametersService setId(String id) {
    this.id = id;
    return this;
  }

  @GET
  @Path("/available")
  @Produces(MediaType.APPLICATION_JSON)
  public ParametersResponse getAvailableParameters() {
    Class<AnalysisParameters> params = AnalysisParameters.class;
    List<AbstractParameter> parameterList = new ArrayList<>();
    AbstractParameter parameter;

    for (Field field : params.getDeclaredFields()) {
      field.setAccessible(true);

      String dsp = field.getAnnotation(Description.class).value();

      if (field.getType() != boolean.class) {
        long min = field.getAnnotation(Min.class).value();
        long max = field.getAnnotation(Max.class).value();
        parameter = new MinMaxParameter(field.getName(), field.getType(), dsp, min, max);
      } else {
        parameter = new BooleanParameter(field.getName(), field.getType(), dsp);
      }

      parameterList.add(parameter);
    }

    return new ParametersResponse(parameterList);
  }

  @GET
  @Path("/current")
  @Produces(MediaType.APPLICATION_JSON)
  public AnalysisParameters getParameters() {
    Document readDoc;
    try {
      readDoc = documentDao.findById(id);
    } catch (NoResultException e) {
      throw new WebApplicationException(e, Response.status(Response.Status.NOT_FOUND).build());
    }

    return readDoc.getParameters();
  }
}
