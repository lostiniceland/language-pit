package infrastructure

import (
	. "github.com/onsi/ginkgo"
	. "github.com/onsi/gomega"
	"gobs/bikes/domain"
	"sort"
	"os"
	"fmt"
)


var _ = Describe("SimpleRepository", func() {

	var (
		sut SimpleStorage
	)

	BeforeEach(func() {
		sut = NewSimpleStorage()
	})


	It("should compute id under concurrent load correctly (tests the mutex)", func() {
		var channelCount = 100

		var generatedBikeIds = make([]int, channelCount)

		ch := make(chan int)
		for i := 0; i < channelCount; i++ {

			go func (repo *SimpleStorage, ch chan<-int){
				var bike = domain.NewBike("Nicolai", "Helius AM Pinion", 16.0, domain.Parts{})
				repo.AddBike(bike)
				ch <- bike.Id
			}(&sut, ch)
		}

		for i := 0; i < channelCount; i++ {
			generatedBikeIds = append(generatedBikeIds, <-ch)
		}

		var expected = make([]int, channelCount)
		for i := 0; i < channelCount; i++ {
			expected = append(expected, i + 1)
		}
		sort.Ints(generatedBikeIds) // sort, because channels might come back in different order
		Expect(expected).To(Equal(generatedBikeIds))
	})
})


var _ = Describe("GormRepository", func() {

	const testDbFilename = "test.db"

	var (
		sut GormSqlLite3Repository
		bike *domain.Bike
	)

	BeforeSuite(func() {
		sut = NewGormSqlLite3Storage(testDbFilename)
	})

	AfterSuite(func(){
		sut.Close()
		err := os.Remove(testDbFilename)
		if err != nil {
			fmt.Println(err)
		}
	})


	BeforeEach(func(){
		bike = &domain.Bike{
			Manufacturer: "Nicolai",
			Name: "Helius AM Pinion",
			Weight: 16.0 ,
			Parts: domain.Parts{domain.Part{"BOS Deville", 2.0}},
			Approval: &domain.Approval{Status: domain.Pending},
		}
	})


	It("should be able to add a bike to the database", func() {
		Skip("Gorm doesn persist relation at the moment...need to figure out")
		sut.AddBike(bike)
		read, _ := sut.FindBike(1)
		Ω(read).Should(Equal(bike))
	})
})




