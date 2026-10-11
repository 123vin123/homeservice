CREATE TABLE provider_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    bio TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    average_rating DECIMAL(3, 2) DEFAULT 0.0,
    total_reviews INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE provider_services (
    provider_id UUID NOT NULL REFERENCES provider_profiles(user_id),
    service_id UUID NOT NULL REFERENCES services(id),
    custom_price DECIMAL(10, 2),
    PRIMARY KEY (provider_id, service_id)
);
