package main

import (
	"gobs/bikes/infrastructure"
	"gobs/bikes/application"
)

func main() {
	//var repository = infrastructure.NewSimpleStorage()
	var repository = infrastructure.NewGormSqlLite3Storage("/tmp/gorm.db")
	defer repository.Close()
	var adapter = infrastructure.HttpRouterService{"8080"}
	application.StartApplication(adapter, &repository)
}