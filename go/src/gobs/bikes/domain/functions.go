package domain

import "errors"

func NewBike(manufacturer string, name string, weight float32, value float32, parts Parts) *Bike {
	return &Bike{Manufacturer: manufacturer, Name: name, Weight: weight, Value: value, Parts: parts, Approval: Pending}
}

func (bike *Bike) AddPart(name string, weight float32){
	bike.Parts = append(bike.Parts, Part {Name: name, Weight: weight})
}

func(bike *Bike) RemovePart(part *Part){
	for i, p := range bike.Parts {
		if *part == p {
			bike.Parts = append(bike.Parts[:i], bike.Parts[i+1:]...)
			break
		}
	}
}

func (bike *Bike) Update(manufacturer string, name string, weight float32, value float32, parts []Part) (error) {
	if bike.Approval != Accepted {
		return errors.New("only approved bikes can be modified")
	}
	bike.Manufacturer = manufacturer
	bike.Name = name
	bike.Weight = weight
	bike.Value = value
	bike.Parts = parts

	return nil
}

func (bike *Bike) UpdateApproval(status ApprovalStatus) (error) {
	if status == Pending && bike.Approval != Pending {
		return errors.New("Cannot change approval back to pending")
	}
	bike.Approval = status
	return nil
}

func (status ApprovalStatus) String() string {
	names := [...]string {
		"Pending",
		"Accepted",
		"Rejected",
	}
	return names[status]
}