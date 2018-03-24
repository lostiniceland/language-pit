package infrastructure

import "gobs/bikes/domain"

type BikeResources []BikeResource

type PostBike struct {
	Manufacturer string 	`json:"manufacturer"`
	Name	string 			`json:"name"`
	Weight float32			`json:"weight"`
	Parts domain.Parts		`json:"parts"`
}


type BikeResource struct {
	Id int					`json:"id"`
	Manufacturer string 	`json:"manufacturer"`
	Name	string 			`json:"name"`
	Weight float32			`json:"weight"`
	Parts domain.Parts		`json:"parts"`
	Approval ApprovalStatus	`json:"approval"`
}

type ApprovalResource struct {
	BikeId int							`json:"bike-id"`
	Approval ApprovalStatus				`json:"approval"`
}

type ApprovalStatus string

const (
	Pending ApprovalStatus = "pending"
	Accepted ApprovalStatus = "accepted"
	Rejected ApprovalStatus = "rejected"
)

