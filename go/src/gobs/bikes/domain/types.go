package domain


type Bike struct {
	Id int				`gorm:"primary_key"`
	Manufacturer string
	Name	string
	Weight float32
	Parts []Part
	Approval *Approval
}

type Part struct {
	Name string
	Weight float32
}

type ApprovalStatus int

const (
	Pending ApprovalStatus = 0
	Accepted ApprovalStatus = 1
	Rejected ApprovalStatus = 2
)

type Approval struct {
	Id int 			`gorm:"primary_key"`
	Status ApprovalStatus
}

type Bikes []Bike
type Parts []Part


type ServerAdapter interface {
	ListenAndServe(bikeRepo BikeRepository)
}

type BikeRepository interface {
	FindBike(id int) (Bike, error)
	FindAllBikes() Bikes
	AddBike(bike *Bike) Bikes
	SaveBike(bike *Bike) error
}


