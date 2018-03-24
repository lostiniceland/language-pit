use futures::future::{Future, FutureResult};
use futures::Stream;
use hyper::{Chunk, StatusCode};
use hyper::header::ContentType;
use hyper::Method::{Get, Put};
use hyper::server::{Request, Response, Service};
use std::cell::RefCell;
use std::io::{stderr, Write};
use std::rc::Rc;
use super::*;


struct RestBikeService {
    repository: Rc<RefCell<BikeRepository>>
}


pub fn start () {
    let mut bike = Bike::new(
        "Nicolai".to_string(),
        "Helius AM Pinion".to_string(),
        16.0,
        vec![]
    );
    bike.add_part(Part::new("BOS Devile 170".to_string(), 2.0));

    let repo = Rc::new(RefCell::new(memory::InMemoryRepository::new()));
    repo.borrow_mut().add_bike(&bike);

    let address = "127.0.0.1:8080".parse().unwrap();
    let server = hyper::server::Http::new()
        .bind(&address, move || Ok(RestBikeService {repository: repo.clone()}))
        .unwrap();
    server.run().unwrap();
}


impl Service for RestBikeService {
    type Request = Request;
    type Response = Response;
    type Error = hyper::Error;
    type Future = Box<Future<Item = Self::Response, Error = Self::Error>>;

    fn call(&self, request: Request) -> Self::Future {
        match (request.method(), request.path()) {
            (&Get, "/bikes") => {
                Box::new(
                    futures::future::ok(
                        Response::new()
                            .with_status(StatusCode::Ok)
                            .with_header(ContentType::json())
                            .with_body(
                                serde_json::to_vec_pretty(
                                    &self.repository
                                        .borrow()
                                        .find_all())
                                    .unwrap())
                    )
                )
            }
            (&Put, "/bikes") => {
                let repo = Rc::clone(&self.repository);
                let future = request
                    .body()
                    .concat2()
                    .and_then(parse_json)
                    .and_then(move |bike: Bike| {
                        // put the RefMut into a local;
                        let mut repo = repo.borrow_mut();
                        // now deref and borrow mut the inner data
                        persist(bike, &mut *repo)
                    })
                    .then(make_put_response);
                Box::new(future)
            }
            _ => {
                Box::new(futures::future::ok(
                Response::new().with_status(StatusCode::NotFound),))
            },
        }
    }
}

fn parse_json(body: Chunk) -> FutureResult<Bike, hyper::Error> {
    let bike: Bike;
    match serde_json::from_slice(&body) {
        Ok(b) => { bike = b; }
        Err(e) => {
            let mut err = e;
            let _ = writeln!(stderr(), "error: {}", err);
            return futures::future::failed::<Bike, hyper::Error>(hyper::Error::Io(err.into()));
        }
    }
    futures::future::ok(bike)
}

fn persist(bike: Bike, repository: &mut BikeRepository ) -> FutureResult<Vec<Bike>, hyper::Error> {
    repository.add_bike(&bike);
    futures::future::ok(repository.find_all())
}

fn make_put_response(result: Result<Vec<Bike>, hyper::Error>) -> FutureResult<hyper::Response, hyper::Error> {
    match result {
        Ok(bike) => {
            futures::future::ok(Response::new()
                .with_status(StatusCode::Ok)
                .with_header(ContentType::json())
                .with_body(serde_json::to_vec_pretty(&bike).unwrap()))
        }
        _ => {
            futures::future::ok(Response::new()
                .with_status(StatusCode::BadRequest))
        }
    }


}

