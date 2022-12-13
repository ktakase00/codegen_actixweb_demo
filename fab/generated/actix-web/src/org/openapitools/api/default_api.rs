use actix_web::{web, Error, HttpResponse};
use deadpool_postgres::{Client, Pool};

use crate::db;
use crate::errors::MyError;
use crate::models::BookRequest;
use serde::Deserialize;

#[derive(Deserialize, Debug)]
struct SimplePath {
    uuid: String,
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::resource("/books")
            .route(web::post().to(book_create))
            .route(web::get().to(book_list))
    );
    cfg.service(
        web::resource("/books/{uuid}")
            .route(web::put().to(book_update))
    );
    cfg.service(
        web::resource("/users/{uuid}")
            .route(web::get().to(user_show))
    );
}

async fn book_create(
    json: web::Json<BookRequest>,
    db_pool: web::Data<Pool>,
) -> Result<HttpResponse, Error> {
    let client: Client = db_pool.get().await.map_err(MyError::PoolError)?;
    let item = db::book_create(&client, &json).await?;
    let res_body = serde_json::to_string(&item)?;
    Ok(HttpResponse::Ok().body(res_body))
}

async fn book_list(
    db_pool: web::Data<Pool>,
) -> Result<HttpResponse, Error> {
    let client: Client = db_pool.get().await.map_err(MyError::PoolError)?;
    let list = db::book_list(&client).await?;
    let res_body = serde_json::to_string(&list)?;
    Ok(HttpResponse::Ok().body(res_body))
}

async fn book_update(
    path: web::Path<SimplePath>,
    json: web::Json<BookRequest>,
    db_pool: web::Data<Pool>,
) -> Result<HttpResponse, Error> {
    let client: Client = db_pool.get().await.map_err(MyError::PoolError)?;
    let uuid =
        uuid::Uuid::parse_str(&path.uuid).or(Err(MyError::InvalidUuid(path.uuid.clone())))?;
    let item = db::book_update(&client, &uuid, &json).await?;
    let res_body = serde_json::to_string(&item)?;
    Ok(HttpResponse::Ok().body(res_body))
}

async fn user_show(
    path: web::Path<SimplePath>,
    db_pool: web::Data<Pool>,
) -> Result<HttpResponse, Error> {
    let client: Client = db_pool.get().await.map_err(MyError::PoolError)?;
    let uuid =
        uuid::Uuid::parse_str(&path.uuid).or(Err(MyError::InvalidUuid(path.uuid.clone())))?;
    let item = db::user_show(&client, &uuid).await?;
    let res_body = serde_json::to_string(&item)?;
    Ok(HttpResponse::Ok().body(res_body))
}

