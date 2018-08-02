package wife.infrastructure.web;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import wife.application.ApplicationRuntimeException;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<ApplicationRuntimeException> {

  @Override
  public Response toResponse(ApplicationRuntimeException exception) {
    // The internal exception message should not be handed out, instead use a correlation-id soon
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
  }
}
