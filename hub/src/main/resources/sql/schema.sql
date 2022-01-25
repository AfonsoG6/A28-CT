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
    id INT PRIMARY KEY NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(256) NOT NULL
);

INSERT INTO usable_iccs (code) VALUES ('12345678901234567890');
INSERT INTO usable_iccs (code) VALUES ('23456789012345678901');
INSERT INTO usable_iccs (code) VALUES ('34567890123456789012');
INSERT INTO usable_iccs (code) VALUES ('45678901234567890123');
INSERT INTO usable_iccs (code) VALUES ('56789012345678901234');
INSERT INTO usable_iccs (code) VALUES ('67890123456789012345');
INSERT INTO usable_iccs (code) VALUES ('78901234567890123456');
INSERT INTO usable_iccs (code) VALUES ('89012345678901234567');
INSERT INTO usable_iccs (code) VALUES ('90123456789012345678');
INSERT INTO usable_iccs (code) VALUES ('01234567890123456789');