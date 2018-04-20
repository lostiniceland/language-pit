package domain


type Bike struct {
	Id           int64
	Manufacturer string
	Name         string
	Weight       float32
	Value        float32
	Parts        Parts
	Approval     ApprovalStatus
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

type Bikes []Bike
type Parts []Part


type BikeRepository interface {
	FindBike(id int64) (Bike, error)
	FindAllBikes() Bikes
	AddBike(bike *Bike) Bike
	SaveBike(bike *Bike) error
	Close()
}



