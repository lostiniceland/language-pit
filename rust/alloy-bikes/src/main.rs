extern crate env_logger;
extern crate futures;
#[cfg(test)] #[macro_use]
extern crate hamcrest;
extern crate hyper;
extern crate mime;
extern crate serde;
#[macro_use]
extern crate serde_derive;
extern crate serde_json;
#[cfg(feature = "repository_diesel")]
#[macro_use]
extern crate diesel;

use domain::{Bike, Part, BikeRepository};

mod domain;
mod infrastructure;

fn main() {
    infrastructure::start();
}







