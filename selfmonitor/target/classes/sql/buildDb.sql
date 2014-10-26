CREATE SEQUENCE IF NOT EXISTS metric_seq START WITH 1;

CREATE TABLE Metric (
    id BIGINT DEFAULT NEXTVAL('metric_seq') NOT NULL,
    metric_name VARCHAR(255),
    metric_path VARCHAR(1024),
    PRIMARY KEY  (id)
);