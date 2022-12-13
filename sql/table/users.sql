DROP TABLE IF EXISTS public.users;
CREATE TABLE IF NOT EXISTS public.users (
  uuid UUID NOT NULL DEFAULT gen_random_uuid()
  ,name TEXT NOT NULL DEFAULT ''
);
