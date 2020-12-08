
CREATE TABLE IF NOT EXISTS config.parameters (
    userId text,
    configKey text,
    configValue text,
    PRIMARY KEY (userId, configKey)
);
