use super::*;
use std::fmt;

impl Part {

    pub fn new(name: String, weight: f32) -> Part {
        Part {name, weight}
    }

}

impl fmt::Display for Part {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} ({:.1}kg)", self.name, &self.weight)
    }
}