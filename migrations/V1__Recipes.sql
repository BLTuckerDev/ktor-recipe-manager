create database recipemanager_dev
    with owner app_user;

create table public.recipes
(
    id                uuid                                not null
        primary key,
    name              varchar(255)                        not null,
    description       text,
    prep_time_minutes integer,
    cook_time_minutes integer,
    servings          integer,
    difficulty        varchar(20),
    created_at        timestamp default CURRENT_TIMESTAMP not null,
    updated_at        timestamp default CURRENT_TIMESTAMP not null
);

alter table public.recipes
    owner to app_user;

