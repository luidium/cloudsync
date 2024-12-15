CREATE TABLE connections (
    id SERIAL PRIMARY KEY,
    directory_path VARCHAR(255) NOT NULL,
    bucket_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT false
);