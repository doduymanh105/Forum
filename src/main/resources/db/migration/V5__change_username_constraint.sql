ALTER TABLE public.users
DROP CONSTRAINT users_user_name_check;

ALTER TABLE public.users
ADD CONSTRAINT users_user_name_check CHECK (char_length(user_name::text) >= 3);