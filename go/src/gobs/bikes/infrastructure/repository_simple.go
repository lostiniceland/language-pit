package infrastructure

import (
	"gobs/bikes/domain"
	"sync"
	"errors"
)

// in-memory storage
type SimpleStorage struct {
	bikesMutex sync.Mutex
	bikes      domain.Bikes
}

func NewSimpleStorage() domain.BikeRepository {
	return &SimpleStorage{bikes: domain.Bikes{}}
}

func (store *SimpleStorage) FindBike(id int) (domain.Bike, error) {
	for _, bike := range store.bikes {
		if bike.Id == id {
			return bike, nil
		}
	}
	return domain.Bike{}, errors.New("bike with id not found")
}

func (store *SimpleStorage) FindAllBikes() domain.Bikes {
	return store.bikes
}

// Add the given bike to the store and assigns an identity to the bike and associated parts
func (store *SimpleStorage) AddBike(bike *domain.Bike) domain.Bike {
	store.bikesMutex.Lock()
	if len(store.bikes) == 0 {
		bike.Id = 1
	} else {
		bike.Id = store.bikes[len(store.bikes)-1].Id + 1
	}
	store.bikes = append(store.bikes, *bike)
	store.bikesMutex.Unlock()
	// this is safe outside the mutex
	for i, part := range bike.Parts {
		part.Id = i
		part.BikeId = bike.Id
	}
	return *bike
}

func (store *SimpleStorage) SaveBike(bike *domain.Bike) error {
	// do nothing
	return nil
}

func (store *SimpleStorage) Close() {
	store.bikes = nil
}


