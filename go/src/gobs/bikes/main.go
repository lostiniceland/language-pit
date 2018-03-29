package main

import (
	"gobs/bikes/infrastructure"
	"gobs/bikes/application"
	"flag"
	"os"
	"gobs/bikes/domain"
)

const (
	defaultPort              = "8080"
	defaultSqlLitePath       = "/tmp/bikes.db"
)

func main() {

	var (
		port   = envString("PORT", defaultPort)
		dbUrl = envString("DB_URL", defaultSqlLitePath)

		httpPort   = flag.String("http.port", ":"+port, "HTTP listen address")
		sqlLiteUrl = flag.String("db.url", dbUrl, "SqlLite3 Url (path)")
		inmemory   = flag.Bool("inmem", false, "use in-memory repositories")
	)

	var repository domain.BikeRepository
	if *inmemory {
		repository = infrastructure.NewSimpleStorage()
	} else{
		repository = infrastructure.NewGormSqlLite3Storage(*sqlLiteUrl)
	}

	var adapter = infrastructure.HttpRouterService{*httpPort}
	application.StartApplication(adapter, repository)
}

func envString(env, fallback string) string {
	e := os.Getenv(env)
	if e == "" {
		return fallback
	}
	return e
}