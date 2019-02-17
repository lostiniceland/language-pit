package bootstrap.infrastructure.web;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProtobufJsonMessageBodyReaderWriter implements MessageBodyReader<Message>,
    MessageBodyWriter<Message> {


  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type);
  }

  @Override
  public Message readFrom(Class<Message> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws WebApplicationException {
    try {
      Method newBuilder = type.getMethod("newBuilder");
      GeneratedMessageV3.Builder builder = (GeneratedMessageV3.Builder) newBuilder.invoke(type);
      new JsonFormat().merge(entityStream, builder);
      return builder.build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(Message m, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return new JsonFormat().printToString(m).getBytes().length;
  }

  @Override
  public void writeTo(Message m, Class type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    entityStream.write(new JsonFormat().printToString(m).getBytes());
  }
}
