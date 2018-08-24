package orchestration.camunda.delegates;

import com.google.protobuf.GeneratedMessageV3;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import orchestration.camunda.ProtobufBinaryMessageBodyReaderWriter;
import orchestration.camunda.ProtobufJsonMessageBodyReaderWriter;

@ApplicationScoped
class JaxRsPublisher {

  private static MediaType MEDIATYPE_PROTOBUF = new MediaType("application", "x-protobuf");

  @Resource(lookup = "urlBikesService")
  private String urlBikes;
  @Resource(lookup = "urlWifeService")
  private String urlWife;

  private Client client;

  @PostConstruct
  void init(){
    client = ClientBuilder.newBuilder()
        .register(ProtobufBinaryMessageBodyReaderWriter.class)
        .register(ProtobufJsonMessageBodyReaderWriter.class)
        .build();
  }

  Optional<Response> sendToWifeService(Supplier<GeneratedMessageV3> bodySupplier){
    return send(urlWife, Optional.of("bikes"), bodySupplier);
  }

  Optional<Response> sendToBikeService(Supplier<GeneratedMessageV3> bodySupplier){
    return send(urlBikes, Optional.empty(), bodySupplier);
  }

  private Optional<Response> send(String url, Optional<String> path, Supplier<GeneratedMessageV3> bodySupplier){
    return Optional.of(client
        .target(url)
        .path(path.orElse(""))
        .request()
        .post(Entity.entity(bodySupplier.get().toByteArray(), MEDIATYPE_PROTOBUF)));
  }

}
