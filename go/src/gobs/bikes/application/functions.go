package application

import "gobs/bikes/domain"

type dependencyHolder struct {
	repository domain.BikeRepository
}


func StartApplication (adapter ServerAdapter, repository domain.BikeRepository) {
	if len(repository.FindAllBikes()) == 0 {
		var parts = domain.Parts { domain.Part{Name: "BOS", Weight: 2.0} }
		var bike = domain.NewBike("Nicolai", "Helius AM Pinion", 16.0, parts)
		repository.AddBike(bike)
	}
	adapter.ListenAndServe(&dependencyHolder{repository: repository})
}


func (app *dependencyHolder) CreateBike(manufacturer string, name string, weight float32, parts []domain.PartDTO) (domain.Bike, error) {
	var domainParts = make(domain.Parts, len(parts), cap(parts))
	for _, part := range parts {
		domainParts = append(domainParts, domain.Part{Name: part.Name, Weight: part.Weight})
	}
	var newBikeP = domain.NewBike(manufacturer, name, weight, domainParts)
	// TODO add error-handling
	app.repository.AddBike(newBikeP)
	return *newBikeP, nil
}


// Handles the retrieval by id, updating the entity and storing it
func (app *dependencyHolder) UpdateBike(id int, manufacturer string, name string, weight float32, parts []domain.PartDTO) (domain.Bike, error) {
	var bike domain.Bike
	bike, err := app.repository.FindBike(id)
	if err != nil {
		return bike, err
	}
	err = bike.Update(manufacturer, name, weight, parts)
	if err != nil {
		return bike, err
	}
	err = app.repository.SaveBike(&bike)
	if err != nil {
		return bike, err
	}
	return bike, nil
}



func (app *dependencyHolder) UpdateApproval(id int, status domain.ApprovalStatus) (domain.Bike, error) {
	var bike domain.Bike
	bike, err := app.repository.FindBike(id)
	if err != nil {
		return bike, err
	}
	err = bike.UpdateApproval(status)
	if err != nil {
		return bike, err
	}
	err = app.repository.SaveBike(&bike)
	if err != nil {
		return bike, err
	}
	return bike, nil
}


func (app *dependencyHolder) GetRepository() domain.BikeRepository {
	return app.repository
}