DROP TABLE IF EXISTS public.books;
CREATE TABLE IF NOT EXISTS public.books (
  uuid UUID NOT NULL DEFAULT gen_random_uuid()
  ,title TEXT NOT NULL DEFAULT ''
  ,author TEXT NOT NULL DEFAULT ''
);
