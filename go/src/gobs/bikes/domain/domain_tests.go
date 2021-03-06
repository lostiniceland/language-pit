package domain

var _ = Describe("Domain", func() {

	var (
		bike *Bike
		expectedDefault Bike
	)

	BeforeEach(func() {
		bike = &Bike{
			Manufacturer: "Nicolai",
			Name: "Helius AM Pinion",
			Weight: 16.0 ,
			Parts: Parts{Part{Name: "BOS Deville", Weight: 2.0}},
			Approval: Pending,
		}
		expectedDefault  = Bike{
			Manufacturer: "Nicolai",
			Name: "Helius AM Pinion",
			Weight: 16.0,
			Parts: Parts{Part{Name: "BOS Deville", Weight: 2.0}},
			Approval: Pending,
		}
	})

	Describe("Bike", func() {
		It("should have a constructor", func() {
			expectedDefault.Parts = Parts{} // constructor doesnt add any parts
			var b = NewBike("Nicolai", "Helius AM Pinion", 16.0, 0.0, Parts{})
			Expect(b).To(Equal(&expectedDefault))
		})
		It("should be able to add parts", func() {
			expectedDefault.Parts = append(expectedDefault.Parts, Part{Name: "Test", Weight: 1.5})
			bike.AddPart("Test", 1.5)
			Expect(bike).To(Equal(&expectedDefault))
		})
		It("should be able to remove parts", func(){
			expectedDefault.Parts = Parts{}
			bike.RemovePart(&Part{Name: "BOS Deville", Weight: 2.0})
			Expect(bike).To(Equal(&expectedDefault))
		})
	})

	Describe("Logic", func(){
		It("only approved bikes can be modified", func(){
			var expected = Bike{Id: 1, Manufacturer: "Nicolai Updated", Name: "Helius AM Pinion Updated", Weight: 15.0, Approval: Accepted}
			var bike = &Bike{Id: 1, Manufacturer: "Nicolai", Name: "Helius AM Pinion", Weight: 16.0, Value: 8000.0, Approval: Accepted}

			bike.Update(expected.Manufacturer, expected.Name, expected.Weight, 8000.0, []Part{})

			Expect(bike).To(Equal(&expected))
		})
		It("non approved bikes cannot be modified", func(){
			var expected = Bike{Id: 1, Manufacturer: "Nicolai Updated", Name: "Helius AM Pinion Updated", Weight: 15.0 }
			var bike = &Bike{Id: 1, Manufacturer: "Nicolai", Name: "Helius AM Pinion", Weight: 16.0, Approval: Pending}

			err := bike.Update(expected.Manufacturer, expected.Name, expected.Weight, 8000.0, []Part{})

			Ω(err).Should(HaveOccurred())
			// check that reference wasnt updated
			Ω(bike).Should(Equal(&Bike{Id: 1, Manufacturer: "Nicolai", Name: "Helius AM Pinion", Weight: 16.0, Value: 8000.0, Approval: Pending}))
		})
	})
})
