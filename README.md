# codegen_actixweb_demo

[openapi-generator](https://github.com/OpenAPITools/openapi-generator) で [Actix-Web](https://actix.rs/) のソースコードの一部を生成するサンプルです。

<br>

## メモ

**カスタムジェネレータのコンパイル**

```sh
cd fab/out/generators/actix-web

mvn package
```

**ソースコードの生成**

```sh
# cd fab
cd ../../..

npx openapi-generator-cli --custom-generator=out/generators/actix-web/target/actix-web-openapi-generator-1.0.0.jar generate \
-g actix-web \
-i ../backend.openapi.yaml \
-o generated/actix-web
```

**生成されるファイル**

- [fab/generated/actix-web/src/org/openapitools/api/default_api.rs](fab/generated/actix-web/src/org/openapitools/api/default_api.rs)
- [fab/generated/actix-web/src/org/openapitools/model/book.rs](fab/generated/actix-web/src/org/openapitools/model/book.rs)
- [fab/generated/actix-web/src/org/openapitools/model/book_request.rs](fab/generated/actix-web/src/org/openapitools/model/book_request.rs)
- [fab/generated/actix-web/src/org/openapitools/model/user.rs](fab/generated/actix-web/src/org/openapitools/model/user.rs)

**サーバーの起動**

リポジトリのルートで実行します。

```sh
docker compose up -d db
psql -h localhost -U test_user -f sql/prepare.sql testing_db
```

```sh
cd backend
cargo run
```

**リクエストを送信**

```sh
curl http://localhost:8080/books
```

<br>

## 参考

Actix-Web のソースコードはこちらを参考にしています。

https://github.com/actix/examples/tree/master/databases/postgres
