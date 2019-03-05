package infrastructure.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/health")
public class HealthResource {

  @GET
  public Response ping() {
    return Response.ok().build();
  }
}
