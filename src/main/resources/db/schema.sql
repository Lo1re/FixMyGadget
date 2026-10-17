
CREATE TABLE IF NOT EXISTS manufacturer
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS gadget_type
(
    code VARCHAR(20) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS feature
(
    code VARCHAR(20) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS repair_status
(
    code VARCHAR(20) PRIMARY KEY
);

INSERT INTO gadget_type (code)
VALUES ('SMARTPHONE'),
       ('TABLET'),
       ('WATCH')
ON CONFLICT DO NOTHING;
INSERT INTO feature (code)
VALUES ('CAMERA'),
       ('GPS'),
       ('HEART_RATE')
ON CONFLICT DO NOTHING;
INSERT INTO repair_status (code)
VALUES ('ACCEPTED'),
       ('IN_PROGRESS'),
       ('DONE')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS gadget
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100)   NOT NULL,
    manufacturer_id INT            NOT NULL REFERENCES manufacturer (id),
    price           NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    type_code       VARCHAR(20)    NOT NULL REFERENCES gadget_type (code),
    screen_width    INT            NOT NULL CHECK (screen_width > 0),
    screen_height   INT            NOT NULL CHECK (screen_height > 0),
    UNIQUE (manufacturer_id, name)
);

CREATE TABLE IF NOT EXISTS gadget_feature
(
    gadget_id    INT         NOT NULL REFERENCES gadget (id) ON DELETE CASCADE,
    feature_code VARCHAR(20) NOT NULL REFERENCES feature (code),
    PRIMARY KEY (gadget_id, feature_code)
);


CREATE TABLE IF NOT EXISTS customer
(
    id         SERIAL PRIMARY KEY,
    last_name  VARCHAR(80) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    phone      VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS technician
(
    id         SERIAL PRIMARY KEY,
    last_name  VARCHAR(80) NOT NULL,
    first_name VARCHAR(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS repair_order
(
    id            SERIAL PRIMARY KEY,
    gadget_id     INT         NOT NULL REFERENCES gadget (id),
    customer_id   INT         NOT NULL REFERENCES customer (id),
    technician_id INT REFERENCES technician (id),
    problem       TEXT        NOT NULL,
    status_code   VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED' REFERENCES repair_status (code),
    cost          NUMERIC(10, 2) CHECK (cost >= 0),
    created_at    TIMESTAMP   NOT NULL DEFAULT now(),
    finished_at   TIMESTAMP,
    CHECK ((status_code = 'DONE') = (finished_at IS NOT NULL)),
    CHECK (finished_at IS NULL OR finished_at >= created_at),
    CHECK (status_code <> 'DONE' OR cost IS NOT NULL)
);


CREATE INDEX IF NOT EXISTS idx_gadget_manufacturer ON gadget (manufacturer_id);
CREATE INDEX IF NOT EXISTS idx_gadget_type ON gadget (type_code);
CREATE INDEX IF NOT EXISTS idx_order_gadget ON repair_order (gadget_id);
CREATE INDEX IF NOT EXISTS idx_order_customer ON repair_order (customer_id);
CREATE INDEX IF NOT EXISTS idx_order_technician ON repair_order (technician_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON repair_order (status_code);
CREATE INDEX IF NOT EXISTS idx_order_created ON repair_order (created_at);
