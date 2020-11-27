
CREATE TABLE IF NOT EXISTS config.parameters (
    userId text,
    theme text,
    useName boolean,
    centerLabel boolean,
    diagonalLabel boolean,
    lineFullPath boolean,
    lineParallelPath boolean,
    lineFlowMode text,
    lineFlowColorMode text,
    lineFlowAlertThreshold int,
    viewOverloadsTable boolean,
    substationLayout text,

    PRIMARY KEY (userId)
);
