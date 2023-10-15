CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table if not exists users (
	id serial primary key,
	username text not null unique,
	email text not null unique,
	uuid uuid not null default uuid_generate_v4(),
	first_name varchar(100) not null,
	last_name varchar(100),
    phone text,
	gender varchar(1),
    age smallint,
	birthdate date,
	city varchar(255),
    information text
);

