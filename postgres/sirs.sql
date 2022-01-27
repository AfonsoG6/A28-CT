DROP TABLE IF EXISTS usable_iccs CASCADE;
DROP TABLE IF EXISTS infected_sks CASCADE;
DROP TABLE IF EXISTS health_services CASCADE;


CREATE TABLE usable_iccs (
    code VARCHAR(20) PRIMARY KEY NOT NULL
);

-- By using the ins_epochtime, we can receive from a client the epochtime of his last query,
-- and only send him SKs with newer/higher ins_epochtime
CREATE TABLE infected_sks (
    epoch_day INT NOT NULL, -- epochtime day (epochtime/86400) associated to SK (SK was used by client during this day)
    sk BYTEA NOT NULL,
    ins_epoch BIGINT NOT NULL, -- epochtime of insertion
    PRIMARY KEY (epoch_day, sk)
);

CREATE TABLE health_services (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(256) NOT NULL
);

DROP USER IF EXISTS cthub;
CREATE USER cthub WITH ENCRYPTED PASSWORD 'a28cthubsirs';

GRANT CONNECT ON DATABASE sirs TO cthub;
GRANT USAGE ON SCHEMA public TO cthub;
GRANT SELECT, INSERT, DELETE ON ALL TABLES IN SCHEMA public TO cthub;

INSERT INTO usable_iccs (code) VALUES ('12345678901234567890'), ('23456789012345678901'), ('34567890123456789012'),
                                      ('45678901234567890123'), ('56789012345678901234'), ('67890123456789012345'),
                                      ('78901234567890123456'), ('89012345678901234567'), ('90123456789012345678'),
                                      ('01234567890123456789');