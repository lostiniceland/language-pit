package infrastructure.jaxrs;

import application.BikeService;
import application.EntityNotFoundException;
import bikes.infrastructure.protobuf.Bikes;
import bikes.infrastructure.protobuf.Bikes.ApprovalEnumType;
import bikes.infrastructure.protobuf.Bikes.ApprovalMessage;
import bikes.infrastructure.protobuf.Bikes.BikeMessage;
import bikes.infrastructure.protobuf.Bikes.BikeMessage.Builder;
import bikes.infrastructure.protobuf.Bikes.BikesMessage;
import bikes.infrastructure.protobuf.Bikes.CreateBikeMessage;
import domain.bikes.ApprovalStatus;
import domain.bikes.Bike;
import domain.bikes.BikeRepository;
import domain.bikes.Part;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

@Path("/bikes")
@Produces({MediaType.APPLICATION_JSON, "application/x-protobuf"})
public class BikeResource {

  @Context
  UriInfo uriInfo;

  @Inject
  BikeService bikeService;
  @Inject
  BikeRepository bikeRepository;


  @GET
  public Response getBikes() {
    List<Bikes.BikeMessage> bikeResponses = bikeRepository.findAllBikes().stream()
        .map(convertBikeToProtobuf)
        .collect(Collectors.toList());
    return Response.ok().entity(BikesMessage.newBuilder().addAllBikes(bikeResponses).build()).build();
  }

  @GET
  @Path("{id}")
  public Response getBikeById(@PathParam("id") long id) {
    final Response resp;
    Optional<Bike> bike = bikeRepository.findBike(id);
    if (bike.isPresent()) {
      resp = Response.ok().entity(convertBikeToProtobuf.apply(bike.get())).build();
    } else {
      resp = Response.status(Status.NOT_FOUND).build();
    }
    return resp;
  }


  @POST
  @Consumes({MediaType.APPLICATION_JSON, "application/x-protobuf"})
  public Response addBike(CreateBikeMessage bikeRequest) {
    final Response resp;
    Bike bike = bikeService.addBike(bikeRequest.getManufacturer(),
        bikeRequest.getName(),
        bikeRequest.getWeight(),
        bikeRequest.getValue(),
        bikeRequest.getPartsList().stream().map(part -> new Part(part.getName(), part.getWeight()))
            .collect(Collectors.toList()));
    URI uri = uriInfo.getBaseUriBuilder().path(BikeResource.class).path(String.valueOf(bike.getId())).build();
    resp = Response.created(uri).entity(convertBikeToProtobuf.apply(bike)).build();

    return resp;
  }

  @PUT
  @Path("{id}")
  @Consumes({MediaType.APPLICATION_JSON, "application/x-protobuf"})
  public Response updateBike(@PathParam("id") long id, Bikes.BikeMessage bikeRequest) {
    Response resp;
    try {
      Bike entity = bikeService.updateBike(id, bikeRequest.getManufacturer(),
          bikeRequest.getName(),
          bikeRequest.getWeight(),
          bikeRequest.getValue(),
          bikeRequest.getPartsList().stream().map(part -> new Part(part.getName(), part.getWeight()))
              .collect(Collectors.toList()));
      resp = Response.ok().entity(convertBikeToProtobuf.apply(entity)).build();
    } catch (EntityNotFoundException e) {
      resp = Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
    }
    return resp;
  }

  @GET
  @Path("{id}/approval")
  public Response getBikeApproval(@PathParam("id") long id) {
    final Response resp;
    Optional<Bike> bike = bikeRepository.findBike(id);
    if (bike.isPresent()) {
      resp = Response.ok().entity(convertApprovalStatusToProtobuf.apply(bike.get())).build();
    } else {
      resp = Response.status(Status.NOT_FOUND).build();
    }
    return resp;
  }

  @PUT
  @Path("{id}/approval")
  public Response setBikeApproval(@PathParam("id") long id, ApprovalMessage approvalRequest) {
    Response resp;
    if (id != approvalRequest.getBikeId()) {
      resp = Response.status(Status.BAD_REQUEST).entity("payload-id does not match path-id").build();
    } else {
      try {
        Bike entity = bikeService.updateApproval(id, convertApprovalToDomain(approvalRequest.getApproval()));
        resp = Response.ok().entity(convertBikeToProtobuf.apply(entity)).build();
      } catch (EntityNotFoundException e) {
        resp = Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
      } catch (IllegalStateException e) {
        resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
    }
    return resp;
  }


  private static Function<Bike, Bikes.BikeMessage> convertBikeToProtobuf = entity -> {
    Builder builder = BikeMessage.newBuilder()
        .setManufacturer(entity.getManufacturer())
        .setName(entity.getName())
        .setWeight(entity.getWeight())
        .setValue(entity.getValue())
        .setApproval(convertApprovalToProtobuf(entity.getApproval()));
    entity.getParts().forEach(part -> builder
        .addParts(Bikes.PartType.newBuilder().setName(part.getName()).setWeight(part.getWeight()).build()));
    return builder.build();
  };

  private static Function<Bike, Bikes.ApprovalMessage> convertApprovalStatusToProtobuf = entity -> ApprovalMessage
      .newBuilder()
      .setBikeId(entity.getId())
      .setApproval(convertApprovalToProtobuf(entity.getApproval()))
      .build();

  private static Bikes.ApprovalEnumType convertApprovalToProtobuf(ApprovalStatus approvalStatus) {
    ApprovalEnumType result = switch (approvalStatus) {
      case Pending -> ApprovalEnumType.PENDING;
      case Accepted -> ApprovalEnumType.ACCEPTED;
      case Rejected -> ApprovalEnumType.REJECTED;
    };
    return result;
  }

  private static ApprovalStatus convertApprovalToDomain(Bikes.ApprovalEnumType approval) {
    return switch (approval) {
      case PENDING -> ApprovalStatus.Pending;
      case ACCEPTED -> ApprovalStatus.Accepted;
      case REJECTED -> ApprovalStatus.Rejected;
      case UNRECOGNIZED, UNKNOWN -> throw new IllegalArgumentException("enum not handled: " + approval);
    };
  }
}
