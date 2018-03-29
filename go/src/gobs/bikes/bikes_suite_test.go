package main

import (
	"testing"

	. "github.com/onsi/ginkgo"
	. "github.com/onsi/gomega"
)

func TestBikesSuite(t *testing.T) {
	RegisterFailHandler(Fail)
	RunSpecs(t, "Bikes Suite")
}

