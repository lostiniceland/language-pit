use super::*;
use std::fmt;

impl Bike {

    pub fn new(manufacturer: String, name: String, weight: f32, parts: Vec<Part>) -> Bike {
        Bike {manufacturer, name, weight, parts}
    }

    #[allow(dead_code)]
    pub fn manufacturer(&self) -> &String {
        &self.manufacturer
    }

    #[allow(dead_code)]
    pub fn name(&self) -> &String {
        &self.name
    }

    #[allow(dead_code)]
    pub fn parts(&self) -> &Vec<Part> {
        &self.parts
    }

    #[allow(dead_code)]
    pub fn add_part(&mut self, part: Part) {
        self.parts.push(part);
    }

}

impl fmt::Display for Bike {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} from {} ({:.2}kg)", self.name, self.manufacturer, &self.weight)
    }
}


#[cfg(test)]
mod tests {
    use super::*;
    use hamcrest::prelude::*;

    fn create_test_bike () -> Bike {
        Bike::new (
            "Nicolai".to_string(),
            "Helius AM Pinion".to_string(),
            16.0,
            vec![] )
    }

    #[test]
    fn test_construction_and_formatting(){
        let helius = create_test_bike();
        assert_that!(&helius.to_string(), is(equal_to("Helius AM Pinion from Nicolai (16.00kg)")));
    }

    #[test]
    fn test_add_part(){
        let mut helius = create_test_bike();
        helius.add_part(Part::new("BOS".to_string(), 2.0));
        assert_that!(&helius.parts[0].to_string(), is(equal_to("BOS (2.0kg)")))
    }
}