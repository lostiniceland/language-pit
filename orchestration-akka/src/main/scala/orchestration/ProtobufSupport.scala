package orchestration

import akka.http.scaladsl.model.{ContentType, HttpCharsets, MediaType}

object ProtobufSupport {
  val headerContentTypeProto = ContentType(MediaType.customWithFixedCharset("application", "x-protobuf", HttpCharsets.`UTF-8`))
}

/**
  * @see https://github.com/scalapb/ScalaPB/issues/247
  */
trait ProtobufSupport {

  import akka.http.scaladsl.marshalling.{PredefinedToEntityMarshallers, ToEntityMarshaller}
  import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
  import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}


  implicit def protobufMarshaller[T <: GeneratedMessage]: ToEntityMarshaller[T] =
    PredefinedToEntityMarshallers.ByteArrayMarshaller.compose[T](r => r.toByteArray)

  implicit def protobufUnmarshaller[T <: GeneratedMessage with Message[T]](implicit companion: GeneratedMessageCompanion[T]): FromEntityUnmarshaller[T] =
    Unmarshaller.byteArrayUnmarshaller.map[T](bytes => companion.parseFrom(bytes))
}
