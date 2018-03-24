use super::*;
use diesel::prelude::*;



const DB_URL: &'static str = "/tmp/bikes.rust_diesel.db";


pub struct DieselBikeRepository {
    db: SqliteConnection
}


fn establish_connection() -> SqliteConnection {
    SqliteConnection::establish(&DB_URL)
        .expect(&format!("Error connecting to {}", DB_URL))
}


impl DieselBikeRepository {

    fn new () -> DieselBikeRepository {
        DieselBikeRepository{db: establish_connection()}
    }
}


impl BikeRepository for DieselBikeRepository {

    fn find_all(&self) -> Vec<Bike> {
//        use self::schema::bikes::dsl::*;
//
//        bikes.load::<Bike>(self.db).expect("TODO")
        unimplemented!();
    }

    fn add_bike(&mut self, bike: &Bike) {
//        diesel::insert_into(bike)
//            .values(bike)
//            .execute(self.db)
//            .expect("Error updating db!");
        unimplemented!();
    }
}


//mod schema {
//    table! {
//        bikes (id) {
//            manufacturer: String,
//            name: String,
//            weight: f32,
//            parts: Vec<Part>,
//        }
//    }
//}


//mod tests {
//
//    use super::*;
//    use hamcrest::prelude::*;
//
//    impl Drop for DieselBikeRepository {
//        fn drop(&mut self) {
//            use std::fs;
//
//            match fs::remove_file(DB_URL) {
//                Ok(_) => {println!("Test-database at '{}' deleted", DB_URL)}
//                Err(_) => {panic!("Error deleting test-database at '{}'!", DB_URL)}
//            }
//        }
//    }
//
//    #[test]
//    fn test_database() {
//        let mut bike = Bike::new(
//            "Nicolai".to_string(),
//            "Helius AM Pinion".to_string(),
//            16.0,
//            vec![]
//        );
//        bike.add_part(Part::new("BOS Devile 170".to_string(), 2.0));
//
//
//        let repository: Box<BikeRepository> = Box::new(DieselBikeRepository::new());
//        repository.add_bike(&bike);
//
//        let all_bikes = repository.find_all();
//
//        assert_that!(&bike, is(equal_to(&all_bikes[0])));
//    }
//
//}