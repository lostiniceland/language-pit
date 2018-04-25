// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package wife.infrastructure.protobuf

@SerialVersionUID(0L)
final case class BikeApprovalsMessage(
                                       approvals: _root_.scala.collection.Seq[wife.infrastructure.protobuf.BikeApprovalMessage] = _root_.scala.collection.Seq.empty
                                     ) extends scalapb.GeneratedMessage with scalapb.Message[BikeApprovalsMessage] with scalapb.lenses.Updatable[BikeApprovalsMessage] {
  @transient
  private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0

  private[this] def __computeSerializedValue(): _root_.scala.Int = {
    var __size = 0
    approvals.foreach(approvals => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(approvals.serializedSize) + approvals.serializedSize)
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
    approvals.foreach { __v =>
      _output__.writeTag(1, 2)
      _output__.writeUInt32NoTag(__v.serializedSize)
      __v.writeTo(_output__)
    };
  }

  def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): wife.infrastructure.protobuf.BikeApprovalsMessage = {
    val __approvals = (_root_.scala.collection.immutable.Vector.newBuilder[wife.infrastructure.protobuf.BikeApprovalMessage] ++= this.approvals)
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __approvals += _root_.scalapb.LiteParser.readMessage(_input__, wife.infrastructure.protobuf.BikeApprovalMessage.defaultInstance)
        case tag => _input__.skipField(tag)
      }
    }
    wife.infrastructure.protobuf.BikeApprovalsMessage(
      approvals = __approvals.result()
    )
  }

  def clearApprovals = copy(approvals = _root_.scala.collection.Seq.empty)

  def addApprovals(__vs: wife.infrastructure.protobuf.BikeApprovalMessage*): BikeApprovalsMessage = addAllApprovals(__vs)

  def addAllApprovals(__vs: TraversableOnce[wife.infrastructure.protobuf.BikeApprovalMessage]): BikeApprovalsMessage = copy(approvals = approvals ++ __vs)

  def withApprovals(__v: _root_.scala.collection.Seq[wife.infrastructure.protobuf.BikeApprovalMessage]): BikeApprovalsMessage = copy(approvals = __v)

  def getFieldByNumber(__fieldNumber: _root_.scala.Int): scala.Any = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 1 => approvals
    }
  }

  def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
    require(__field.containingMessage eq companion.scalaDescriptor)
    (__field.number: @_root_.scala.unchecked) match {
      case 1 => _root_.scalapb.descriptors.PRepeated(approvals.map(_.toPMessage)(_root_.scala.collection.breakOut))
    }
  }

  def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)

  def companion = wife.infrastructure.protobuf.BikeApprovalsMessage
}

object BikeApprovalsMessage extends scalapb.GeneratedMessageCompanion[wife.infrastructure.protobuf.BikeApprovalsMessage] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[wife.infrastructure.protobuf.BikeApprovalsMessage] = this

  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): wife.infrastructure.protobuf.BikeApprovalsMessage = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    wife.infrastructure.protobuf.BikeApprovalsMessage(
      __fieldsMap.getOrElse(__fields.get(0), Nil).asInstanceOf[_root_.scala.collection.Seq[wife.infrastructure.protobuf.BikeApprovalMessage]]
    )
  }

  implicit def messageReads: _root_.scalapb.descriptors.Reads[wife.infrastructure.protobuf.BikeApprovalsMessage] = _root_.scalapb.descriptors.Reads {
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      wife.infrastructure.protobuf.BikeApprovalsMessage(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.collection.Seq[wife.infrastructure.protobuf.BikeApprovalMessage]]).getOrElse(_root_.scala.collection.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }

  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WifeProto.javaDescriptor.getMessageTypes.get(0)

  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WifeProto.scalaDescriptor.messages(0)

  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = wife.infrastructure.protobuf.BikeApprovalMessage
    }
    __out
  }

  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty

  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)

  lazy val defaultInstance = wife.infrastructure.protobuf.BikeApprovalsMessage(
  )

  implicit class BikeApprovalsMessageLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, wife.infrastructure.protobuf.BikeApprovalsMessage]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, wife.infrastructure.protobuf.BikeApprovalsMessage](_l) {
    def approvals: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.Seq[wife.infrastructure.protobuf.BikeApprovalMessage]] = field(_.approvals)((c_, f_) => c_.copy(approvals = f_))
  }

  final val APPROVALS_FIELD_NUMBER = 1
}
