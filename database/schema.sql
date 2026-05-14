CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE user_role AS ENUM ('CLERK', 'MANAGER');
CREATE TYPE contact_type AS ENUM ('MOBILE', 'PHONE', 'EMAIL');
CREATE TYPE address_type AS ENUM ('HOME', 'WORK', 'MAILING');
CREATE TYPE service_type AS ENUM ('DRIVERS_LICENSE', 'STUDENT_PERMIT', 'CAR_INSURANCE', 'VEHICLE_REGISTRATION');
CREATE TYPE transaction_status AS ENUM ('DRAFT', 'PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED');

CREATE TABLE app_users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role user_role NOT NULL DEFAULT 'CLERK',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE customers (
    customer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(80) NOT NULL,
    middle_name VARCHAR(80),
    last_name VARCHAR(80) NOT NULL,
    suffix VARCHAR(20),
    birth_date DATE,
    gender VARCHAR(30),
    civil_status VARCHAR(30),
    nationality VARCHAR(60) NOT NULL DEFAULT 'Filipino',
    created_by UUID REFERENCES app_users(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE customer_contacts (
    contact_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    contact_type contact_type NOT NULL,
    contact_value VARCHAR(150) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (customer_id, contact_type, contact_value)
);

CREATE TABLE customer_addresses (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    address_type address_type NOT NULL DEFAULT 'HOME',
    line1 VARCHAR(180) NOT NULL,
    line2 VARCHAR(180),
    barangay VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    postal_code VARCHAR(15),
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE customer_identifications (
    identification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    id_type VARCHAR(60) NOT NULL,
    id_number VARCHAR(80) NOT NULL,
    issued_date DATE,
    expiry_date DATE,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (id_type, id_number)
);

CREATE TABLE vehicles (
    vehicle_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID REFERENCES customers(customer_id) ON DELETE SET NULL,
    plate_no VARCHAR(30) UNIQUE,
    engine_no VARCHAR(80) NOT NULL UNIQUE,
    chassis_no VARCHAR(80) NOT NULL UNIQUE,
    make VARCHAR(80) NOT NULL,
    model VARCHAR(80) NOT NULL,
    model_year INTEGER CHECK (model_year BETWEEN 1900 AND EXTRACT(YEAR FROM now())::INTEGER + 1),
    color VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_no VARCHAR(40) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(customer_id) ON DELETE RESTRICT,
    vehicle_id UUID REFERENCES vehicles(vehicle_id) ON DELETE RESTRICT,
    service_type service_type NOT NULL,
    status transaction_status NOT NULL DEFAULT 'PENDING',
    processed_by UUID REFERENCES app_users(user_id),
    amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    remarks TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_customers_name ON customers (last_name, first_name);
CREATE INDEX idx_customer_contacts_value ON customer_contacts (contact_value);
CREATE INDEX idx_vehicles_plate_no ON vehicles (plate_no);
CREATE INDEX idx_transactions_customer ON transactions (customer_id);
CREATE INDEX idx_transactions_service_status ON transactions (service_type, status);
CREATE INDEX idx_transactions_submitted_at ON transactions (submitted_at DESC);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_app_users_updated_at
BEFORE UPDATE ON app_users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_customers_updated_at
BEFORE UPDATE ON customers
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_vehicles_updated_at
BEFORE UPDATE ON vehicles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_transactions_updated_at
BEFORE UPDATE ON transactions
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Passwords are checked by LoginController using pgcrypto crypt().
-- Example seed:
-- INSERT INTO app_users (username, password_hash, full_name, role)
-- VALUES ('manager', crypt('ChangeMe123!', gen_salt('bf')), 'System Manager', 'MANAGER');
