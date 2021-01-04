
CREATE TABLE IF NOT EXISTS config.parameters (
    userId text,
    name text,
    value text,
    PRIMARY KEY (userId, name)
);
