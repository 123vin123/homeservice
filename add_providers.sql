-- 8 New Providers

-- 1. Create Users
INSERT INTO users (id, email, password_hash, first_name, last_name, phone, role, created_at) VALUES 
('c3333333-3333-3333-3333-333333333333', 'luigi@plumbing.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Luigi', 'Bros', '555-0400', 'PROVIDER', CURRENT_TIMESTAMP),
('c4444444-4444-4444-4444-444444444444', 'tesla@electric.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Nikola', 'Tesla', '555-0500', 'PROVIDER', CURRENT_TIMESTAMP),
('c5555555-5555-5555-5555-555555555555', 'bob@builder.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Bob', 'Builder', '555-0600', 'PROVIDER', CURRENT_TIMESTAMP),
('c6666666-6666-6666-6666-666666666666', 'john@smithplumbing.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'John', 'Smith', '555-0700', 'PROVIDER', CURRENT_TIMESTAMP),
('c7777777-7777-7777-7777-777777777777', 'edison@lights.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Thomas', 'Edison', '555-0800', 'PROVIDER', CURRENT_TIMESTAMP),
('c8888888-8888-8888-8888-888888888888', 'mike@pipes.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Mike', 'Piper', '555-0900', 'PROVIDER', CURRENT_TIMESTAMP),
('c9999999-9999-9999-9999-999999999999', 'sarah@wiring.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Sarah', 'Wire', '555-1000', 'PROVIDER', CURRENT_TIMESTAMP),
('caaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'david@fixit.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'David', 'Fixit', '555-1100', 'PROVIDER', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 2. Profiles
INSERT INTO provider_profiles (user_id, bio, status, average_rating, total_reviews, created_at, updated_at) VALUES 
('c3333333-3333-3333-3333-333333333333', 'Reliable plumber.', 'APPROVED', 4.5, 45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c4444444-4444-4444-4444-444444444444', 'Master electrician.', 'APPROVED', 5.0, 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c5555555-5555-5555-5555-555555555555', 'General contractor and plumber.', 'APPROVED', 4.2, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c6666666-6666-6666-6666-666666666666', 'Emergency plumbing services.', 'APPROVED', 4.7, 89, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c7777777-7777-7777-7777-777777777777', 'Commercial electrical.', 'APPROVED', 4.6, 67, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c8888888-8888-8888-8888-888888888888', 'Residential plumbing specialist.', 'APPROVED', 4.8, 150, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c9999999-9999-9999-9999-999999999999', 'Licensed electrical contractor.', 'APPROVED', 4.9, 110, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('caaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Handyman and plumber.', 'APPROVED', 4.3, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 3. Provider Services (mix of Leak Repair '666' and Light Fixture '777')
INSERT INTO provider_services (provider_id, service_id, custom_price) VALUES 
('c3333333-3333-3333-3333-333333333333', '66666666-6666-6666-6666-666666666666', 130.00),
('c4444444-4444-4444-4444-444444444444', '77777777-7777-7777-7777-777777777777', 150.00),
('c5555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666666', 120.00),
('c6666666-6666-6666-6666-666666666666', '66666666-6666-6666-6666-666666666666', 160.00),
('c7777777-7777-7777-7777-777777777777', '77777777-7777-7777-7777-777777777777', 110.00),
('c8888888-8888-8888-8888-888888888888', '66666666-6666-6666-6666-666666666666', 145.00),
('c9999999-9999-9999-9999-999999999999', '77777777-7777-7777-7777-777777777777', 125.00),
('caaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '66666666-6666-6666-6666-666666666666', 135.00)
ON CONFLICT DO NOTHING;

-- 4. Locations (scattered around downtown Metropolis 40.7128, -74.0060)
INSERT INTO provider_service_areas (id, provider_id, latitude, longitude, radius_km, created_at) VALUES 
(gen_random_uuid(), 'c3333333-3333-3333-3333-333333333333', 40.7200, -74.0200, 20.0, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c4444444-4444-4444-4444-444444444444', 40.7100, -73.9900, 30.0, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c5555555-5555-5555-5555-555555555555', 40.7300, -74.0000, 15.0, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c6666666-6666-6666-6666-666666666666', 40.7400, -74.0100, 25.0, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c7777777-7777-7777-7777-777777777777', 40.7000, -74.0300, 10.0, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c8888888-8888-8888-8888-888888888888', 40.7150, -73.9800, 35.0, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c9999999-9999-9999-9999-999999999999', 40.7500, -74.0500, 20.0, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'caaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 40.7050, -74.0050, 15.0, CURRENT_TIMESTAMP);

-- 5. Availability (9 to 5 for everyone)
INSERT INTO provider_availability (id, provider_id, day_of_week, start_time, end_time, is_active, created_at) 
SELECT gen_random_uuid(), id, day, '09:00:00', '17:00:00', true, CURRENT_TIMESTAMP 
FROM users 
CROSS JOIN generate_series(1,5) day
WHERE role = 'PROVIDER' 
AND email NOT IN ('mario@plumbing.com', 'sparky@electric.com'); -- Exclude the first two that already have availability
