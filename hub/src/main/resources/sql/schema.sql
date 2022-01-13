CREATE TABLE usable_iccs (
    code VARCHAR(20) PRIMARY KEY NOT NULL
);

-- By using the ins_epochtime, we can receive from a client the epochtime of his last query,
-- and only send him SKs with newer/higher ins_epochtime
CREATE TABLE infected_sks (
    epoch_day INT PRIMARY KEY NOT NULL, -- epochtime day (epochtime%86400) associated to SK (SK was used by client during this day)
    sk VARCHAR(256) PRIMARY KEY NOT NULL,
    ins_epoch INT NOT NULL -- epochtime of insertion
);

CREATE TABLE health_services (
    id INT PRIMARY KEY NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(256) NOT NULL
);