DROP TABLE IF EXISTS public.messages;
DROP TABLE IF EXISTS public.users;

CREATE TABLE public.users (
	id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	username varchar(255) NOT NULL,
	"password" varchar(255) NOT NULL,
	CONSTRAINT users_pk PRIMARY KEY (id),
	CONSTRAINT users_unique UNIQUE (username)
);

CREATE TABLE public.messages (
	id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	"content" varchar(255) NOT NULL,
	"time" timestamp NOT NULL,
	sender int8 NOT NULL,
	recipient int8 NOT NULL,
	CONSTRAINT messages_pk PRIMARY KEY (id),
	CONSTRAINT messages_users_fk FOREIGN KEY (sender) REFERENCES public.users(id),
	CONSTRAINT messages_users_fk_1 FOREIGN KEY (recipient) REFERENCES public.users(id)
);