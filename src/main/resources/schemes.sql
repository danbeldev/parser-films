CREATE TABLE countries
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE genres
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE persons
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,

    UNIQUE (first_name, last_name)
);

CREATE TABLE films
(
    id                  BIGINT PRIMARY KEY,
    name                VARCHAR(255),
    rating              FLOAT,
    ratings_count       INT,
    year                INT,
    duration            INT,
    poster_url          TEXT,
    is_available_online BOOLEAN,
    country_id          INT REFERENCES countries (id),
    genre_id            INT REFERENCES genres (id),
    director_id         INT REFERENCES persons (id)
);

CREATE TABLE film_actors
(
    film_id  INT REFERENCES films (id) ON DELETE CASCADE,
    actor_id INT REFERENCES persons (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, actor_id)
);
