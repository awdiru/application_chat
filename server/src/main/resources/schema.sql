DROP TABLE IF EXISTS public.invitation_chat;
DROP TABLE IF EXISTS public.chat_participants;
DROP TABLE IF EXISTS public.messages;
DROP TABLE IF EXISTS public.chats;
DROP TABLE IF EXISTS public.users;

CREATE TABLE public.users
(
    id             int8 GENERATED ALWAYS AS IDENTITY ( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    username       varchar(32)                                                                                                          NOT NULL,
    "password"     varchar(255)                                                                                                         NOT NULL,
    icon varchar(255)                                                                                                                   NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (id),
    CONSTRAINT users_unique UNIQUE (username)
);

CREATE TABLE public.chats (
	id           varchar(255) NOT NULL,
	chat_name    varchar(255) NOT NULL,
	private_chat bool         NOT NULL,
	admin_id     int8,
	CONSTRAINT chats_pk PRIMARY KEY (id),
	CONSTRAINT chats_users_fk FOREIGN KEY (admin_id) REFERENCES public.users(id) ON DELETE SET NULL
);

CREATE TABLE public.messages (
	id          int8 GENERATED ALWAYS AS IDENTITY NOT NULL,
	"time"      timestamp                         NOT NULL,
	"content"   varchar                           NOT NULL,
    sender_id   int8                              NOT NULL,
	chat_id     varchar(255)                      NOT NULL,
	"file_name" varchar,
	CONSTRAINT messages_pk PRIMARY KEY (id),
	CONSTRAINT messages_chats_fk FOREIGN KEY (chat_id) REFERENCES public.chats(id) ON DELETE CASCADE,
	CONSTRAINT messages_users_fk FOREIGN KEY (sender_id) REFERENCES public.users(id)
);

CREATE TABLE public.chat_participants (
	chat_id varchar NOT NULL,
	user_id int8    NOT NULL,
	CONSTRAINT chat_participants_unique UNIQUE (chat_id, user_id),
	CONSTRAINT chat_participants_chats_fk FOREIGN KEY (chat_id) REFERENCES public.chats(id) ON DELETE CASCADE,
	CONSTRAINT chat_participants_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.invitation_chat (
	id int8 GENERATED ALWAYS AS IDENTITY NOT NULL,
	chat_id varchar                      NOT NULL,
	user_id int8                         NOT NULL,
	CONSTRAINT invitation_chat_pk PRIMARY KEY (id),
	CONSTRAINT invitation_chat_chats_fk FOREIGN KEY (chat_id) REFERENCES public.chats(id),
	CONSTRAINT invitation_chat_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id)
);


