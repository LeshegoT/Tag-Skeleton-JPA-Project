CREATE TABLE skeleton_name (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    date_value TIMESTAMP WITH TIME ZONE NOT NULL,
    is_updated BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_skeleton_name PRIMARY KEY (id),
    CONSTRAINT uk_skeleton_name_name UNIQUE (name)
);
