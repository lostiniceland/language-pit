package infrastructure

import (
	"gobs/bikes/domain"
)

//func (resource *BikeResource) toEntity () *domain.Bike {
//	return &domain.Bike {
//		Manufacturer: resource.Manufacturer,
//		Name: resource.Name,
//		Weight: resource.Weight,
//		Parts: resource.Parts,
//	}
//}

func convertBikeEntities(bikes domain.Bikes) BikeResources {
	result := make(BikeResources, 0, cap(bikes))
	for _, entity := range bikes {
		result = append(result, convertBikeEntity(&entity))
	}
	return result
}

func convertBikeEntity (entity *domain.Bike) BikeResource {
	return BikeResource{
		Id: entity.Id,
		Manufacturer: entity.Manufacturer,
		Name: entity.Name,
		Weight: entity.Weight,
		Parts: convertPartEntities(&entity.Parts),
		Approval: convertStatus(entity.Approval),
	}
}

func convertPartEntities(entities *domain.Parts) []PartResource {
	//result := make([]PartResource, len(*entities), cap(*entities)) // WTF go creates a first element which is empty
	result := make([]PartResource, 0)
	for _, part := range *entities {
		result = append(result, convertPartEntity(&part))
	}
	return result
}

func convertPartEntity(entity *domain.Part) PartResource {
	return PartResource{
		Id: entity.Id,
		Name: entity.Name,
		Weight: entity.Weight,
	}
}


func convertPartResourcesToDTOs (parts []PartResource) []domain.PartDTO {
	var result = make([]domain.PartDTO, len(parts), cap(parts))
	for _, part := range parts {
		result = append(result, domain.PartDTO{part.Id, part.Name, part.Weight})
	}
	return result
}



//func convertApprovalEntity(bikeId int, x *domain.Approval) ApprovalStatus {
//	return ApprovalResource{
//		BikeId: bikeId,
//		Approval:
//	}
//}

func convertStatus(status domain.ApprovalStatus) ApprovalStatus {
	names := [...]ApprovalStatus {
		Pending,
		Accepted,
		Rejected,
	}
	return names[status]
}

func convertStringStatus(status ApprovalStatus) domain.ApprovalStatus {
	names := map[ApprovalStatus]domain.ApprovalStatus {
		Pending: domain.Pending,
		Accepted: domain.Accepted,
		Rejected: domain.Rejected,
	}
	return names[status]
}