use super::*;

pub struct InMemoryRepository {
    storage: Vec<Bike>
}



impl InMemoryRepository {

    pub fn new () -> InMemoryRepository {
        InMemoryRepository{storage: vec![]}
    }
}

impl BikeRepository for InMemoryRepository {

    fn find_all(&self) -> Vec<Bike> {
        self.storage.clone()
    }

    fn add_bike(&mut self, bike: &Bike) {
        self.storage.push(bike.clone());
    }
}


#[cfg(test)]
mod tests {

    use super::*;
    use hamcrest::prelude::*;


    #[test]
    fn test_database() {
        let mut bike = Bike::new(
            "Nicolai".to_string(),
            "Helius AM Pinion".to_string(),
            16.0,
            vec![]
        );
        bike.add_part(Part::new("BOS Devile 170".to_string(), 2.0));


        let mut repository: Box<BikeRepository> = Box::new(InMemoryRepository::new());
        repository.add_bike(&bike);

        let all_bikes = repository.find_all();

        assert_that!(&bike, is(equal_to(&all_bikes[0])));
    }

}