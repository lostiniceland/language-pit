package infrastructure

import (
	"gobs/bikes/domain"
)

var (
	endpointApprovalToDomainApproval = map[ApprovalEnumType]domain.ApprovalStatus{
		ApprovalEnumType_PENDING:  domain.Pending,
		ApprovalEnumType_ACCEPTED: domain.Accepted,
		ApprovalEnumType_REJECTED: domain.Rejected,
	}

	domainApprovalToEndpointApproval = map[domain.ApprovalStatus]ApprovalEnumType{
		domain.Pending:  ApprovalEnumType_PENDING,
		domain.Accepted: ApprovalEnumType_ACCEPTED,
		domain.Rejected: ApprovalEnumType_REJECTED,
	}
)

func convertBikeEntities(bikes domain.Bikes) *BikesMessage {
	result := make([]*BikeMessage, 0, cap(bikes))
	for _, entity := range bikes {
		result = append(result, convertBikeEntity(&entity))
	}
	return &BikesMessage{Bikes: result}
}

func convertBikeEntity(entity *domain.Bike) *BikeMessage {
	return &BikeMessage{
		Manufacturer: entity.Manufacturer,
		Name:         entity.Name,
		Weight:       entity.Weight,
		Value:        entity.Value,
		Parts:        convertPartEntities(&entity.Parts),
		Approval:     convertStatus(entity.Approval),
	}
}

func convertPartEntities(entities *domain.Parts) []*PartType {
	//result := make([]PartResource, len(*entities), cap(*entities)) // WTF go creates a first element which is empty
	result := make([]*PartType, 0)
	for _, part := range *entities {
		result = append(result, convertPartEntity(&part))
	}
	return result
}

func convertPartEntity(entity *domain.Part) *PartType {
	return &PartType{
		Name: entity.Name,
		Weight: entity.Weight,
	}
}

func convertPartToDomainPart(parts []*PartType) domain.Parts {
	var result = make([]domain.Part, len(parts), cap(parts))
	for _, part := range parts {
		result = append(result, domain.Part{Name: part.Name, Weight: part.Weight})
	}
	return result
}

func convertStatus(status domain.ApprovalStatus) ApprovalEnumType {
	return domainApprovalToEndpointApproval[status]
}

func convertProtoStatus(status ApprovalEnumType) domain.ApprovalStatus {
	return endpointApprovalToDomainApproval[status]
}