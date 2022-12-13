use deadpool_postgres::Client;
use tokio_pg_mapper::FromTokioPostgresRow;

use crate::{
    errors::MyError,
    models::{Book, BookRequest, User},
};

pub async fn book_list(client: &Client) -> Result<Vec<Book>, MyError> {
    let sql = "SELECT * FROM public.books";
    let stmt = client.prepare(sql).await?;
    let list = client
        .query(&stmt, &[])
        .await?
        .iter()
        .map(|row| Book::from_row_ref(row).unwrap())
        .collect::<Vec<Book>>();
    Ok(list)
}

pub async fn book_create(client: &Client, book: &BookRequest) -> Result<Book, MyError> {
    let sql = "INSERT INTO public.books (
        title
        ,author
    ) VALUES (
        $1
        ,$2
    ) RETURNING
        uuid
        ,title
        ,author
    ";
    let stmt = client.prepare(sql).await?;
    client
        .query(&stmt, &[&book.title, &book.author])
        .await?
        .iter()
        .map(|row| Book::from_row_ref(row).unwrap())
        .collect::<Vec<Book>>()
        .pop()
        .ok_or(MyError::NotFound)
}

pub async fn book_update(
    client: &Client,
    uuid: &uuid::Uuid,
    book: &BookRequest,
) -> Result<Book, MyError> {
    let sql = "UPDATE public.books SET
        title = $2
        ,author = $3
    WHERE
        uuid = $1
    RETURNING
        uuid
        ,title
        ,author
    ";
    let stmt = client.prepare(sql).await?;
    client
        .query(&stmt, &[uuid, &book.title, &book.author])
        .await?
        .iter()
        .map(|row| Book::from_row_ref(row).unwrap())
        .collect::<Vec<Book>>()
        .pop()
        .ok_or(MyError::NotFound)
}

pub async fn user_show(client: &Client, uuid: &uuid::Uuid) -> Result<User, MyError> {
    let sql = "SELECT * FROM public.users WHERE uuid = $1";
    let stmt = client.prepare(sql).await?;
    client
        .query(&stmt, &[uuid])
        .await?
        .iter()
        .map(|row| User::from_row_ref(row).unwrap())
        .collect::<Vec<User>>()
        .pop()
        .ok_or(MyError::NotFound)
}
