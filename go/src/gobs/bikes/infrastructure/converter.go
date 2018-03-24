package infrastructure

import "gobs/bikes/domain"

func (resource *BikeResource) toEntity () *domain.Bike {
	return &domain.Bike {
		Manufacturer: resource.Manufacturer,
		Name: resource.Name,
		Weight: resource.Weight,
		Parts: resource.Parts,
	}
}

func convertBikeEntity (entity *domain.Bike) BikeResource {
	return BikeResource{
		Id: entity.Id,
		Manufacturer: entity.Manufacturer,
		Name: entity.Name,
		Weight: entity.Weight,
		Parts: entity.Parts,
		Approval: convertStatus(entity.Approval.Status),
	}
}

func convertBikeEntities(bikes domain.Bikes) BikeResources {
	result := make(BikeResources, 0, cap(bikes))
	for _, entity := range bikes {
		result = append(result, convertBikeEntity(&entity))
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