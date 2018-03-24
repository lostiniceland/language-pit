package domain

import "errors"


func NewBike( manufacturer string, name string, weight float32, parts Parts) *Bike {
	return &Bike{Manufacturer: manufacturer, Name: name, Weight: weight, Parts: parts, Approval: &Approval{Status: Pending}}
}

func (bike *Bike) AddPart(name string, weight float32){
	bike.Parts = append(bike.Parts, Part {Name: name, Weight: weight})
}

func(bike *Bike) RemovePart(part *Part){
	for i, p := range bike.Parts {
		if IsPartEqual(&p, part) {
			bike.Parts = append(bike.Parts[:i], bike.Parts[i+1:]...)
			break
		}
	}
}

// TODO not the best solution
func IsBikeEqual(left *Bike, right *Bike) bool {
	var equal = left.Manufacturer == right.Manufacturer && left.Name == right.Name && left.Weight == right.Weight
	if equal {
		equal = left.Approval.Id == right.Approval.Id && left.Approval.Status == right.Approval.Status
	}
	if equal {
		// compare parts
		if len(left.Parts) != len(right.Parts) {
			return false
		}

		if (left.Parts == nil) != (right.Parts == nil) {
			return false
		}

		for i, v := range left.Parts {
			if v != right.Parts[i] {
				return false
			}
		}
	}
	return equal
}

// TODO not the best solution
func IsPartEqual (left *Part, right *Part) bool {
	return left.Name == right.Name && left.Weight == right.Weight
}

func (bike *Bike) Update(manufacturer string, name string, weight float32, parts Parts) (error) {
	if bike.Approval.Status != Accepted {
		return errors.New("only approved bikes can be modified")
	}
	bike.Manufacturer = manufacturer
	bike.Name = name
	bike.Weight = weight
	bike.Parts = parts

	return nil
}

func (bike *Bike) UpdateApproval(status ApprovalStatus) (error) {
	if status == Pending && bike.Approval.Status != Pending {
		return errors.New("Cannot change approval back to pending")
	}
	bike.Approval.Status = status
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