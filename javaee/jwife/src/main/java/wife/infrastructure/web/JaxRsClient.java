package wife.infrastructure.web;

import com.google.protobuf.Message;
import common.infrastructure.protobuf.Events.BikeApprovedMessage;
import common.infrastructure.protobuf.Events.BikeRejectedMessage;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wife.application.ExternalEventPublisher;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;

@ApplicationScoped
public class JaxRsClient implements ExternalEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(JaxRsClient.class);

  @Resource(lookup = "orchestrationUrl")
  private String orchestrationUrl;

  private Client client;


  @PostConstruct
  protected void init(){
    ClientBuilder clientBuilder = ClientBuilder.newBuilder();
    clientBuilder.register(ProtobufBinaryMessageBodyReaderWriter.class);
    clientBuilder.register(ProtobufJsonMessageBodyReaderWriter.class);
    this.client = clientBuilder.build();
  }


  @Override
  public void notifyBikeAboutApproval(BikeApprovedEvent event) {
    Optional<Response> response = sendRequest(
        "events/approvals/accepted",
        () -> BikeApprovedMessage.newBuilder().setBikeId(event.getBikeId()).build());
    if (!response.isPresent() || response.get().getStatus() != Status.ACCEPTED.getStatusCode()) {
      // TODO inconsistency! put event is some store for later processing
      logger.error("Could not deliver BikeApprovedEvent for bikeId '{}'", event.getBikeId());
    }
  }

  @Override
  public void notifyBikeAboutReject(BikeRejectedEvent event) {
    Optional<Response> response = sendRequest(
        "events/approvals/rejected",
        () -> BikeRejectedMessage.newBuilder().setBikeId(event.getBikeId()).build());
    if (!response.isPresent() || response.get().getStatus() != Status.ACCEPTED.getStatusCode()) {
      // TODO inconsistency! put event is some store for later processing
      logger.error("Could not deliver BikeRejectedEvent for bikeId '{}'", event.getBikeId());
    }
  }

  private Optional<Response> sendRequest(String topic, Supplier<? extends Message> supplier) {
    try {
      return Optional.of(client.target(orchestrationUrl)
          .path(topic)
          .request()
          .post(
              Entity.entity(
                  supplier.get(),
                  "application/x-protobuf")
          ));
    }catch (Exception e) {
      logger.error("Could not establish connection", e);
      return Optional.empty();
    }
  }
}
