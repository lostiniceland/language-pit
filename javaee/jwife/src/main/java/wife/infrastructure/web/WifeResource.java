package wife.infrastructure.web;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import wife.application.WifeService;
import wife.domain.BikeApproval;
import wife.domain.BikeApproval.ApprovalStatus;
import wife.domain.WifeRepository;
import wife.infrastructure.protobuf.Wife;
import wife.infrastructure.protobuf.Wife.ApprovalEnumType;
import wife.infrastructure.protobuf.Wife.BikeApprovalMessage;
import wife.infrastructure.protobuf.Wife.BikeApprovalMessage.Builder;
import wife.infrastructure.protobuf.Wife.BikeApprovalsMessage;
import wife.infrastructure.protobuf.Wife.CreateBikeApprovalMessage;

@Path("/")
@Produces({MediaType.APPLICATION_JSON, "application/x-protobuf"})
public class WifeResource {

  @Context
  UriInfo uriInfo;

  @Inject
  WifeService wifeService;
  @Inject
  WifeRepository wifeRepository;


  @GET
  @Path("/bikes")
  public Response getBikeApprovals() {
    List<BikeApprovalMessage> approvalMessages = wifeRepository.findAllBikeApprovals().stream()
        .map(convertBikeApprovalToProtobuf)
        .collect(Collectors.toList());
    return Response.ok().entity(BikeApprovalsMessage.newBuilder().addAllApprovals(approvalMessages).build()).build();
  }

  @GET
  @Path("/bikes/{id}")
  public Response getBikeApprovalById(@PathParam("id") long id) {
    final Response resp;
    Optional<BikeApproval> approval = wifeRepository.findBikeApproval(id);
    if (approval.isPresent()) {
      resp = Response.ok().entity(convertBikeApprovalToProtobuf.apply(approval.get())).build();
    } else {
      resp = Response.status(Status.NOT_FOUND).build();
    }
    return resp;
  }


  @POST
  @Path("/bikes")
  @Consumes({MediaType.APPLICATION_JSON, "application/x-protobuf"})
  public Response addBike(CreateBikeApprovalMessage approvalRequest) {
    final Response resp;
    BikeApproval approval = wifeService.addBikeApproval(
        approvalRequest.getBikeId(),
        approvalRequest.getValue());
    URI uri = uriInfo.getBaseUriBuilder().path(String.valueOf(approval.getId())).build();
    resp = Response.created(uri).entity(convertBikeApprovalToProtobuf.apply(approval)).build();

    return resp;
  }


  private static Function<BikeApproval, Wife.BikeApprovalMessage> convertBikeApprovalToProtobuf = entity -> {
    Builder builder = BikeApprovalMessage.newBuilder()
        .setBikeId(entity.getBikeId())
        .setValue(entity.getValue())
        .setApproval(convertApprovalToProtobuf(entity.getApproval()));
    return builder.build();
  };


  private static Wife.ApprovalEnumType convertApprovalToProtobuf(ApprovalStatus approvalStatus) {
    ApprovalEnumType result;
    switch (approvalStatus) {
      case Pending:
        result = ApprovalEnumType.PENDING;
        break;
      case Accepted:
        result = ApprovalEnumType.ACCEPTED;
        break;
      case Rejected:
        result = ApprovalEnumType.REJECTED;
        break;
      default:
        throw new IllegalArgumentException("enum not handled: " + approvalStatus);
    }
    return result;
  }
}
