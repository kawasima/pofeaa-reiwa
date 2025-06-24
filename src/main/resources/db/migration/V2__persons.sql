CREATE TABLE IF NOT EXISTS persons (
    id BIGINT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    number_of_dependents INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_persons PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    reservation_date TIMESTAMP NOT NULL,
    CONSTRAINT pk_reservations PRIMARY KEY (id),
    CONSTRAINT fk_reservations_person FOREIGN KEY (passenger_id) REFERENCES persons(id) ON DELETE CASCADE
);