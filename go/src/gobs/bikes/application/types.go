package application

import "gobs/bikes/domain"

type ServerAdapter interface {
	ListenAndServe(app Application)
}

type Application interface {
	CreateBike(manufacturer string, name string, weight float32, parts domain.Parts) (domain.Bike, error)
	UpdateBike(id int, manufacturer string, name string, weight float32, parts domain.Parts) (domain.Bike, error)
	UpdateApproval(id int, status domain.ApprovalStatus) (domain.Bike, error)
	GetRepository() domain.BikeRepository
}