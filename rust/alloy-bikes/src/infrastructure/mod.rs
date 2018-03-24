use super::*;

mod rest;
#[cfg(feature = "repository_diesel")]
mod db;
mod memory;

pub fn start () {
    rest::start();
}