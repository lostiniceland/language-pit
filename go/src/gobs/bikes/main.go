package main

import (
	"gobs/bikes/infrastructure"
	"gobs/bikes/domain"
)

func main() {
	startServer(infrastructure.HttpRouterService{"8080"})
}

func startServer(x domain.ServerAdapter) {
	var store = infrastructure.NewSimpleStorage()
	//var store = infrastructure.NewGormSqlLite3Storage("/tmp/gorm.db")
	defer store.Close()

	if len(store.FindAllBikes()) == 0 {
		var parts = domain.Parts { domain.Part{"BOS", 2.0} }
		var bike = domain.NewBike("Nicolai", "Helius AM Pinion", 16.0, parts)
		store.AddBike(bike)
	}

	x.ListenAndServe(&store)
}


