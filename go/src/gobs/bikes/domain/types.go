package domain

//import (
//	"database/sql/driver"
//	"errors"
//)

// I hate DTOs but for now I need one
type PartDTO struct {
	Id		int
	Name	string
	Weight float32
}

type Bike struct {
	Id int					`gorm:"primary_key"`
	Manufacturer string
	Name	string
	Weight float32
	Parts Parts				`gorm:"foreignkey:BikeId"`
	Approval ApprovalStatus
}

type Part struct {
	Id int					`gorm:"primary_key"`
	// WTF: I need to pollute the domain model with a foreign key in order to use GORM
	BikeId int
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
	FindBike(id int) (Bike, error)
	FindAllBikes() Bikes
	AddBike(bike *Bike) Bike
	SaveBike(bike *Bike) error
}


// Scan - Implement the database/sql scanner interface
// STUPID: I want to have this knowledge in the infrastructure package, but I am not allowed to define a method there
//func (status *ApprovalStatus) Scan(value interface{}) error {
//	// if value is nil, false
//	if value == nil {
//		// set the value of the pointer status to YesNoEnum(false)
//		*status = ApprovalStatus(Pending)
//		return nil
//	}
//	if intValue, err := driver.Int32.ConvertValue(value); err == nil {
//		// if this is a string type
//		if v, ok := intValue.(int); ok {
//			// set the value of the pointer status to YesNoEnum(v)
//			*status = ApprovalStatus(v)
//			return nil
//		}
//	}
//	// otherwise, return an error
//	return errors.New("failed to scan ApprovalStatus")
//}


