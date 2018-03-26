package infrastructure

import (
	"gobs/bikes/domain"
	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/sqlite"
	"log"
	"errors"
	"os/signal"
	"os"
	"syscall"
)

type GormSqlLite3Repository struct {
	db *gorm.DB
}


func NewGormSqlLite3Storage (path string) GormSqlLite3Repository {
	db, err := gorm.Open("sqlite3", path)
	if err != nil {
		log.Fatal(err)
		panic("Cannot connect to database!")
	}
	db.LogMode(true)
	db.AutoMigrate(domain.Bike{}, domain.Part{}, domain.Approval{})
	log.Print("Database created")

	store := GormSqlLite3Repository{db }

	// Make sure DB is closed in case of termination
	c := make(chan os.Signal, 2)
	signal.Notify(c, os.Interrupt, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-c
		store.Close()
		os.Exit(1)
	}()

	return store

}

func (store *GormSqlLite3Repository) FindBike(id int) (domain.Bike, error) {
	var bike domain.Bike
	if store.db.Where("id = ?", id).First(&bike).RecordNotFound() {
		return domain.Bike{}, errors.New("bike with id not found")
	}
	return bike, nil
}

func (store *GormSqlLite3Repository) FindAllBikes() domain.Bikes {
	var bikes domain.Bikes
	store.db.Find(&bikes)
	return bikes
}

func (store *GormSqlLite3Repository) AddBike(bike *domain.Bike) domain.Bike {
	store.db.Save(&bike)
	return *bike
}


func (store *GormSqlLite3Repository) SaveBike(bike *domain.Bike) error {
	err := store.db.Save(bike).GetErrors()
	if err != nil {
		return err[0]
	}
	return nil
}



func (store *GormSqlLite3Repository) Close() {
	err := store.db.Close()
	if err != nil {
		log.Fatal(err)
	}
	log.Print("Database closed")
}