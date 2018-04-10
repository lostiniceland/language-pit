package bikes.infrastructure.web;

import bikes.application.ExternalEventPublisher;
import bikes.domain.BikeCreatedEvent;
import bikes.infrastructure.protobuf.Bikes.BikeCreatedMessage;
import com.google.protobuf.Message;
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
  public void notifyWifeAboutNewBike(BikeCreatedEvent event) {
      Optional<Response> response = sendRequest(
          "events/bikes/created",
          () -> BikeCreatedMessage.newBuilder()
              .setBikeId(event.getBikeId())
              .setValue(event.getValue())
              .build());
      if (!response.isPresent() || response.get().getStatus() != Status.ACCEPTED.getStatusCode()) {
        // TODO inconsistency! put event is some store for later processing
        logger.error("Could not deliver BikeCreatedEvent for bikeId '{}'", event.getBikeId());
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
