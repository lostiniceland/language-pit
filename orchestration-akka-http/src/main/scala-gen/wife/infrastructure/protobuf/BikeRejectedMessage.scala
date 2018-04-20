// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package wife.infrastructure.protobuf

@SerialVersionUID(0L)
final case class BikeRejectedMessage(
                                      bikeId: _root_.scala.Long = 0L
                                    ) extends scalapb.GeneratedMessage with scalapb.Message[BikeRejectedMessage] with scalapb.lenses.Updatable[BikeRejectedMessage] {
  @transient
  private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0

  private[this] def __computeSerializedValue(): _root_.scala.Int = {
    var __size = 0
    if (bikeId != 0L) {
      __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(1, bikeId)
    }
    __size
  }

  final override def serializedSize: _root_.scala.Int = {
    var read = __serializedSizeCachedValue
    if (read == 0) {
      read = __computeSerializedValue()
      __serializedSizeCachedValue = read
    }
    read
  }

  def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): Unit = {
    {
      val __v = bikeId
      if (__v != 0L) {
        _output__.writeInt64(1, __v)
      }
    };
  }

  def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): wife.infrastructure.protobuf.BikeRejectedMessage = {
    var __bikeId = this.bikeId
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __bikeId = _input__.readInt64()
        case tag => _input__.skipField(tag)
      }
    }
    wife.infrastructure.protobuf.BikeRejectedMessage(
      bikeId = __bikeId
    )
  }

  def withBikeId(__v: _root_.scala.Long): BikeRejectedMessage = copy(bikeId = __v)

  def getFieldByNumber(__fieldNumber: _root_.scala.Int): scala.Any = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 1 => {
        val __t = bikeId
        if (__t != 0L) __t else null
      }
    }
  }

  def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
    require(__field.containingMessage eq companion.scalaDescriptor)
    (__field.number: @_root_.scala.unchecked) match {
      case 1 => _root_.scalapb.descriptors.PLong(bikeId)
    }
  }

  def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)

  def companion = wife.infrastructure.protobuf.BikeRejectedMessage
}

object BikeRejectedMessage extends scalapb.GeneratedMessageCompanion[wife.infrastructure.protobuf.BikeRejectedMessage] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[wife.infrastructure.protobuf.BikeRejectedMessage] = this

  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): wife.infrastructure.protobuf.BikeRejectedMessage = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    wife.infrastructure.protobuf.BikeRejectedMessage(
      __fieldsMap.getOrElse(__fields.get(0), 0L).asInstanceOf[_root_.scala.Long]
    )
  }

  implicit def messageReads: _root_.scalapb.descriptors.Reads[wife.infrastructure.protobuf.BikeRejectedMessage] = _root_.scalapb.descriptors.Reads {
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      wife.infrastructure.protobuf.BikeRejectedMessage(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Long]).getOrElse(0L)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }

  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WifeProto.javaDescriptor.getMessageTypes.get(4)

  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WifeProto.scalaDescriptor.messages(4)

  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)

  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty

  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)

  lazy val defaultInstance = wife.infrastructure.protobuf.BikeRejectedMessage(
  )

  implicit class BikeRejectedMessageLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, wife.infrastructure.protobuf.BikeRejectedMessage]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, wife.infrastructure.protobuf.BikeRejectedMessage](_l) {
    def bikeId: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.bikeId)((c_, f_) => c_.copy(bikeId = f_))
  }

  final val BIKE_ID_FIELD_NUMBER = 1
}
