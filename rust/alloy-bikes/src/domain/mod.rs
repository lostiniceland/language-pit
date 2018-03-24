mod bike;
mod part;

#[derive(PartialEq, Clone, Serialize, Deserialize, Debug)]
pub struct Bike {
    manufacturer: String,
    name: String,
    weight: f32,
    parts: Vec<Part>
}

#[derive(PartialEq, Clone, Serialize, Deserialize, Debug)]
pub struct Part {
    name: String,
    weight: f32
}

pub trait BikeRepository {
    fn find_all(&self) -> Vec<Bike>;

    fn add_bike(&mut self, bike: &Bike);
}






