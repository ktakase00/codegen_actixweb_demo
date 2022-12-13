use serde::{Deserialize, Serialize};
use tokio_pg_mapper_derive::PostgresMapper;

#[derive(Deserialize, PostgresMapper, Serialize)]
#[pg_mapper(table = "books")] // singular 'book' is a keyword..
pub struct Book {
    pub uuid: uuid::Uuid,
    pub title: String,
    pub author: String,
}
