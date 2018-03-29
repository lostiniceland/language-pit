package infrastructure

type BikeResources []BikeResource

type PostBike struct {
	Manufacturer string 	`json:"manufacturer"`
	Name	string 			`json:"name"`
	Weight float32			`json:"weight"`
	Parts []PartResource	`json:"parts"`
}


type BikeResource struct {
	Id int					`json:"id"`
	Manufacturer string 	`json:"manufacturer"`
	Name	string 			`json:"name"`
	Weight float32			`json:"weight"`
	Parts []PartResource	`json:"parts"`
	Approval ApprovalStatus	`json:"approval"`
}

type PartResource struct {
	Id		int				`json:"id"`
	Name	string 			`json:"name"`
	Weight float32			`json:"weight"`
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

