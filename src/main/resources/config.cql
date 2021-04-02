
CREATE TABLE IF NOT EXISTS config.parameters (
    userId text,
    appName text,
    name text,
    value text,
    PRIMARY KEY (userId, appName, name)
);
