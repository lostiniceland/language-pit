package application

import "gobs/bikes/domain"

type ServerAdapter interface {
	ListenAndServe(app Application)
}



type Application interface {
	// Creates a new bike-entity. The given function will be called before the bike gets addded to the repository
	CreateBike(manufacturer string, name string, weight float32, value float32, parts []domain.Part) (domain.Bike, error)
	UpdateBike(id int64, manufacturer string, name string, weight float32, value float32, parts []domain.Part) (domain.Bike, error)
	UpdateApproval(id int64, status domain.ApprovalStatus) (domain.Bike, error)
	GetRepository() domain.BikeRepository
}