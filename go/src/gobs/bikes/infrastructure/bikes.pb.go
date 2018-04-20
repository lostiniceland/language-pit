// Code generated by protoc-gen-go. DO NOT EDIT.
// source: bikes.proto

/*
Package infrastructure is a generated protocol buffer package.

It is generated from these files:
	bikes.proto

It has these top-level messages:
	BikesMessage
	BikeMessage
	CreateBikeMessage
	PartType
	ApprovalMessage
	BikeCreatedMessage
	BikeDeletedMessage
*/
package infrastructure

import proto "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion2 // please upgrade the proto package

type ApprovalEnumType int32

const (
	ApprovalEnumType_PENDING  ApprovalEnumType = 0
	ApprovalEnumType_ACCEPTED ApprovalEnumType = 1
	ApprovalEnumType_REJECTED ApprovalEnumType = 2
)

var ApprovalEnumType_name = map[int32]string{
	0: "PENDING",
	1: "ACCEPTED",
	2: "REJECTED",
}
var ApprovalEnumType_value = map[string]int32{
	"PENDING":  0,
	"ACCEPTED": 1,
	"REJECTED": 2,
}

func (x ApprovalEnumType) String() string {
	return proto.EnumName(ApprovalEnumType_name, int32(x))
}
func (ApprovalEnumType) EnumDescriptor() ([]byte, []int) { return fileDescriptor0, []int{0} }

type BikesMessage struct {
	Bikes []*BikeMessage `protobuf:"bytes,1,rep,name=bikes" json:"bikes,omitempty"`
}

func (m *BikesMessage) Reset()                    { *m = BikesMessage{} }
func (m *BikesMessage) String() string            { return proto.CompactTextString(m) }
func (*BikesMessage) ProtoMessage()               {}
func (*BikesMessage) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{0} }

func (m *BikesMessage) GetBikes() []*BikeMessage {
	if m != nil {
		return m.Bikes
	}
	return nil
}

type BikeMessage struct {
	Manufacturer string           `protobuf:"bytes,1,opt,name=manufacturer" json:"manufacturer,omitempty"`
	Name         string           `protobuf:"bytes,2,opt,name=name" json:"name,omitempty"`
	Weight       float32          `protobuf:"fixed32,3,opt,name=weight" json:"weight,omitempty"`
	Value        float32          `protobuf:"fixed32,4,opt,name=value" json:"value,omitempty"`
	Parts        []*PartType      `protobuf:"bytes,5,rep,name=parts" json:"parts,omitempty"`
	Approval     ApprovalEnumType `protobuf:"varint,6,opt,name=approval,enum=bikes.ApprovalEnumType" json:"approval,omitempty"`
}

func (m *BikeMessage) Reset()                    { *m = BikeMessage{} }
func (m *BikeMessage) String() string            { return proto.CompactTextString(m) }
func (*BikeMessage) ProtoMessage()               {}
func (*BikeMessage) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{1} }

func (m *BikeMessage) GetManufacturer() string {
	if m != nil {
		return m.Manufacturer
	}
	return ""
}

func (m *BikeMessage) GetName() string {
	if m != nil {
		return m.Name
	}
	return ""
}

func (m *BikeMessage) GetWeight() float32 {
	if m != nil {
		return m.Weight
	}
	return 0
}

func (m *BikeMessage) GetValue() float32 {
	if m != nil {
		return m.Value
	}
	return 0
}

func (m *BikeMessage) GetParts() []*PartType {
	if m != nil {
		return m.Parts
	}
	return nil
}

func (m *BikeMessage) GetApproval() ApprovalEnumType {
	if m != nil {
		return m.Approval
	}
	return ApprovalEnumType_PENDING
}

type CreateBikeMessage struct {
	Manufacturer string      `protobuf:"bytes,1,opt,name=manufacturer" json:"manufacturer,omitempty"`
	Name         string      `protobuf:"bytes,2,opt,name=name" json:"name,omitempty"`
	Weight       float32     `protobuf:"fixed32,3,opt,name=weight" json:"weight,omitempty"`
	Value        float32     `protobuf:"fixed32,4,opt,name=value" json:"value,omitempty"`
	Parts        []*PartType `protobuf:"bytes,5,rep,name=parts" json:"parts,omitempty"`
}

func (m *CreateBikeMessage) Reset()                    { *m = CreateBikeMessage{} }
func (m *CreateBikeMessage) String() string            { return proto.CompactTextString(m) }
func (*CreateBikeMessage) ProtoMessage()               {}
func (*CreateBikeMessage) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{2} }

func (m *CreateBikeMessage) GetManufacturer() string {
	if m != nil {
		return m.Manufacturer
	}
	return ""
}

func (m *CreateBikeMessage) GetName() string {
	if m != nil {
		return m.Name
	}
	return ""
}

func (m *CreateBikeMessage) GetWeight() float32 {
	if m != nil {
		return m.Weight
	}
	return 0
}

func (m *CreateBikeMessage) GetValue() float32 {
	if m != nil {
		return m.Value
	}
	return 0
}

func (m *CreateBikeMessage) GetParts() []*PartType {
	if m != nil {
		return m.Parts
	}
	return nil
}

type PartType struct {
	Name   string  `protobuf:"bytes,1,opt,name=name" json:"name,omitempty"`
	Weight float32 `protobuf:"fixed32,2,opt,name=weight" json:"weight,omitempty"`
}

func (m *PartType) Reset()                    { *m = PartType{} }
func (m *PartType) String() string            { return proto.CompactTextString(m) }
func (*PartType) ProtoMessage()               {}
func (*PartType) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{3} }

func (m *PartType) GetName() string {
	if m != nil {
		return m.Name
	}
	return ""
}

func (m *PartType) GetWeight() float32 {
	if m != nil {
		return m.Weight
	}
	return 0
}

type ApprovalMessage struct {
	BikeId   int64            `protobuf:"varint,1,opt,name=bike_id,json=bikeId" json:"bike_id,omitempty"`
	Approval ApprovalEnumType `protobuf:"varint,2,opt,name=approval,enum=bikes.ApprovalEnumType" json:"approval,omitempty"`
}

func (m *ApprovalMessage) Reset()                    { *m = ApprovalMessage{} }
func (m *ApprovalMessage) String() string            { return proto.CompactTextString(m) }
func (*ApprovalMessage) ProtoMessage()               {}
func (*ApprovalMessage) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{4} }

func (m *ApprovalMessage) GetBikeId() int64 {
	if m != nil {
		return m.BikeId
	}
	return 0
}

func (m *ApprovalMessage) GetApproval() ApprovalEnumType {
	if m != nil {
		return m.Approval
	}
	return ApprovalEnumType_PENDING
}

type BikeCreatedMessage struct {
	BikeId int64   `protobuf:"varint,1,opt,name=bike_id,json=bikeId" json:"bike_id,omitempty"`
	Value  float32 `protobuf:"fixed32,2,opt,name=value" json:"value,omitempty"`
}

func (m *BikeCreatedMessage) Reset()                    { *m = BikeCreatedMessage{} }
func (m *BikeCreatedMessage) String() string            { return proto.CompactTextString(m) }
func (*BikeCreatedMessage) ProtoMessage()               {}
func (*BikeCreatedMessage) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{5} }

func (m *BikeCreatedMessage) GetBikeId() int64 {
	if m != nil {
		return m.BikeId
	}
	return 0
}

func (m *BikeCreatedMessage) GetValue() float32 {
	if m != nil {
		return m.Value
	}
	return 0
}

type BikeDeletedMessage struct {
	BikeId int64 `protobuf:"varint,1,opt,name=bike_id,json=bikeId" json:"bike_id,omitempty"`
}

func (m *BikeDeletedMessage) Reset()                    { *m = BikeDeletedMessage{} }
func (m *BikeDeletedMessage) String() string            { return proto.CompactTextString(m) }
func (*BikeDeletedMessage) ProtoMessage()               {}
func (*BikeDeletedMessage) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{6} }

func (m *BikeDeletedMessage) GetBikeId() int64 {
	if m != nil {
		return m.BikeId
	}
	return 0
}

func init() {
	proto.RegisterType((*BikesMessage)(nil), "bikes.BikesMessage")
	proto.RegisterType((*BikeMessage)(nil), "bikes.BikeMessage")
	proto.RegisterType((*CreateBikeMessage)(nil), "bikes.CreateBikeMessage")
	proto.RegisterType((*PartType)(nil), "bikes.PartType")
	proto.RegisterType((*ApprovalMessage)(nil), "bikes.ApprovalMessage")
	proto.RegisterType((*BikeCreatedMessage)(nil), "bikes.BikeCreatedMessage")
	proto.RegisterType((*BikeDeletedMessage)(nil), "bikes.BikeDeletedMessage")
	proto.RegisterEnum("bikes.ApprovalEnumType", ApprovalEnumType_name, ApprovalEnumType_value)
}

func init() { proto.RegisterFile("bikes.proto", fileDescriptor0) }

var fileDescriptor0 = []byte{
	// 366 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0xcc, 0x53, 0x41, 0x6b, 0xf2, 0x40,
	0x10, 0xfd, 0x36, 0x9a, 0xe8, 0x37, 0x11, 0xb5, 0x4b, 0xa9, 0x7b, 0x29, 0x84, 0x40, 0x21, 0x14,
	0x6a, 0x41, 0xa1, 0x14, 0x7a, 0xd2, 0x18, 0x8a, 0x85, 0x8a, 0x04, 0x4f, 0xbd, 0xc8, 0x5a, 0x57,
	0x1b, 0xaa, 0x31, 0x6c, 0x36, 0x96, 0xfe, 0x9b, 0xfe, 0xa3, 0xfe, 0xa5, 0xb2, 0xbb, 0x51, 0x4c,
	0x29, 0xd4, 0x63, 0x6f, 0xfb, 0xde, 0x9b, 0x99, 0x37, 0xf3, 0x42, 0xc0, 0x9e, 0x45, 0xaf, 0x2c,
	0x6d, 0x27, 0x7c, 0x23, 0x36, 0xd8, 0x54, 0xc0, 0xbd, 0x85, 0x5a, 0x5f, 0x3e, 0x1e, 0x59, 0x9a,
	0xd2, 0x25, 0xc3, 0x1e, 0x68, 0x81, 0x20, 0xa7, 0xe4, 0xd9, 0x1d, 0xdc, 0xd6, 0x3d, 0xb2, 0x26,
	0x2f, 0x09, 0xf3, 0xce, 0x4f, 0x04, 0xf6, 0x01, 0x8d, 0x5d, 0xa8, 0xad, 0x69, 0x9c, 0x2d, 0xe8,
	0xb3, 0xc8, 0x38, 0xe3, 0x04, 0x39, 0xc8, 0xfb, 0x1f, 0x16, 0x38, 0x8c, 0xa1, 0x1c, 0xd3, 0x35,
	0x23, 0x86, 0xd2, 0xd4, 0x1b, 0x9f, 0x81, 0xf5, 0xc6, 0xa2, 0xe5, 0x8b, 0x20, 0x25, 0x07, 0x79,
	0x46, 0x98, 0x23, 0x7c, 0x0a, 0xe6, 0x96, 0xae, 0x32, 0x46, 0xca, 0x8a, 0xd6, 0x00, 0x5f, 0x80,
	0x99, 0x50, 0x2e, 0x52, 0x62, 0xaa, 0xfd, 0x1a, 0xf9, 0x7e, 0x63, 0xca, 0xc5, 0xe4, 0x3d, 0x61,
	0xa1, 0x56, 0x71, 0x17, 0xaa, 0x34, 0x49, 0xf8, 0x66, 0x4b, 0x57, 0xc4, 0x72, 0x90, 0x57, 0xef,
	0xb4, 0xf2, 0xca, 0x5e, 0x4e, 0x07, 0x71, 0xb6, 0x56, 0x1d, 0xfb, 0x42, 0xf7, 0x03, 0xc1, 0x89,
	0xcf, 0x19, 0x15, 0xec, 0xaf, 0xde, 0xe5, 0xde, 0x40, 0x75, 0x47, 0xed, 0x4d, 0xd1, 0x8f, 0xa6,
	0xc6, 0xa1, 0xa9, 0x3b, 0x85, 0xc6, 0xee, 0xf0, 0xdd, 0x5d, 0x2d, 0xa8, 0x48, 0x8f, 0x69, 0x34,
	0x57, 0x13, 0x4a, 0xa1, 0x25, 0xe1, 0x70, 0x5e, 0xc8, 0xce, 0x38, 0x36, 0x3b, 0x1f, 0xb0, 0x0c,
	0x4d, 0xc7, 0x37, 0xff, 0xd5, 0x63, 0x1f, 0x82, 0x71, 0x10, 0x82, 0x7b, 0xa5, 0x87, 0x0c, 0xd8,
	0x8a, 0x1d, 0x31, 0xe4, 0xf2, 0x0e, 0x9a, 0xdf, 0x37, 0xc2, 0x36, 0x54, 0xc6, 0xc1, 0x68, 0x30,
	0x1c, 0xdd, 0x37, 0xff, 0xe1, 0x1a, 0x54, 0x7b, 0xbe, 0x1f, 0x8c, 0x27, 0xc1, 0xa0, 0x89, 0x24,
	0x0a, 0x83, 0x87, 0xc0, 0x97, 0xc8, 0xe8, 0x5f, 0xc3, 0xb9, 0x3e, 0x2a, 0x8a, 0x17, 0x9c, 0xa6,
	0x82, 0x67, 0xea, 0x5b, 0xea, 0xbf, 0x63, 0x96, 0x2d, 0x9e, 0xea, 0x45, 0x61, 0x66, 0x29, 0xa5,
	0xfb, 0x15, 0x00, 0x00, 0xff, 0xff, 0xa8, 0x27, 0xd5, 0x55, 0x46, 0x03, 0x00, 0x00,
}
