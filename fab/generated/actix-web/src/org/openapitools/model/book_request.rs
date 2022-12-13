use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize)]
pub struct BookRequest {
    pub title: String,
    pub author: String,
}
