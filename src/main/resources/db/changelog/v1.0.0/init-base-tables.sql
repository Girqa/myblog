--liquibase formatted sql
--changeset Reso11er:init-base-tables

create table posts
(
    id        bigint  not null generated always as identity,
    title     text    not null,
    image     bytea   not null,
    post_text text    not null,
    likes     integer not null default 0 check ( likes >= 0 ),

    constraint post_pk primary key (id)
);

create table tags
(
    id       bigint       not null generated always as identity,
    tag_name varchar(255) not null,

    constraint tag_pk primary key (id),
    constraint unique_tag unique (tag_name)
);

create table post_tags
(
    post_id bigint not null,
    tag_id  bigint not null,

    constraint post_tag_pk primary key (post_id, tag_id),
    constraint post_fk foreign key (post_id) references posts (id)
        on delete cascade,
    constraint tag_fk foreign key (tag_id) references tags (id)
        on delete cascade
);

create table commentaries
(
    id              bigint not null generated always as identity,
    post_id         bigint not null,
    commentary_text text   not null,

    constraint commentary_pk primary key (id),
    constraint post_fk foreign key (post_id) references posts (id)
        on delete cascade,
    constraint unique_commentary unique (id, post_id)
);