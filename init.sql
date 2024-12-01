-- Query to create the file_metadata table
CREATE TABLE file_metadata (
id BIGSERIAL PRIMARY KEY,
file_name VARCHAR(255) NOT NULL,
file_size BIGINT NOT NULL,
total_chunks INTEGER NOT NULL,
uploaded_chunks INTEGER DEFAULT 0,
status VARCHAR(50) DEFAULT 'Pending',
created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Query to create the chunk_metadata table
CREATE TABLE chunk_metadata (
id SERIAL PRIMARY KEY,
file_id BIGINT NOT NULL,
chunk_number INTEGER NOT NULL,
status VARCHAR(20) DEFAULT 'Pending',
created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
progress NUMERIC(5, 2) DEFAULT 0.00,
updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT fk_file FOREIGN KEY (file_id) REFERENCES file_metadata (id)
);
