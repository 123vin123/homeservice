-- Dummy Data for Testing Phase 1

-- 1. Create Users (Password is 'password')
-- CUSTOMER
INSERT INTO users (id, email, password_hash, first_name, last_name, phone, role, created_at) VALUES 
('11111111-1111-1111-1111-111111111111', 'customer@test.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Alice', 'Customer', '555-0100', 'CUSTOMER', CURRENT_TIMESTAMP);

-- PROVIDERS
INSERT INTO users (id, email, password_hash, first_name, last_name, phone, role, created_at) VALUES 
('22222222-2222-2222-2222-222222222222', 'mario@plumbing.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Mario', 'Bros', '555-0200', 'PROVIDER', CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'sparky@electric.com', '$2a$10$hKDVYxLefV75igV0PbskLuQ2X5X0.kQ6qZ.Z7L0l2B9W3G6n5x5Hq', 'Sparky', 'Watts', '555-0300', 'PROVIDER', CURRENT_TIMESTAMP);

-- 2. Create Categories & Services
INSERT INTO categories (id, name, description, created_at) VALUES 
('44444444-4444-4444-4444-444444444444', 'Plumbing', 'All plumbing related services', CURRENT_TIMESTAMP),
('55555555-5555-5555-5555-555555555555', 'Electrical', 'Electrical repairs and installations', CURRENT_TIMESTAMP);

INSERT INTO services (id, category_id, name, description, base_price, duration_minutes, is_active, created_at) VALUES 
('66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444', 'Leak Repair', 'Fixing pipe leaks', 150.00, 60, true, CURRENT_TIMESTAMP),
('77777777-7777-7777-7777-777777777777', '55555555-5555-5555-5555-555555555555', 'Light Fixture Install', 'Install a new ceiling light', 120.00, 45, true, CURRENT_TIMESTAMP);

-- 3. Provider Profiles
INSERT INTO provider_profiles (user_id, bio, status, average_rating, total_reviews, created_at, updated_at) VALUES 
('22222222-2222-2222-2222-222222222222', 'Expert plumber with 10 years experience.', 'APPROVED', 4.8, 120, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'Certified electrician.', 'APPROVED', 4.9, 85, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. Provider Services (Linking providers to catalog)
INSERT INTO provider_services (provider_id, service_id, custom_price) VALUES 
('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666666', 140.00), -- Mario offers Leak Repair for $140
('33333333-3333-3333-3333-333333333333', '77777777-7777-7777-7777-777777777777', NULL);   -- Sparky uses base price for Lights

-- 5. Location (Customer Address in Downtown)
INSERT INTO addresses (id, user_id, label, address_line, city, latitude, longitude, created_at) VALUES 
('88888888-8888-8888-8888-888888888888', '11111111-1111-1111-1111-111111111111', 'Home', '123 Main St', 'Metropolis', 40.7128, -74.0060, CURRENT_TIMESTAMP);

-- 6. Location (Provider Service Areas covering Downtown)
INSERT INTO provider_service_areas (id, provider_id, latitude, longitude, radius_km, created_at) VALUES 
('99999999-9999-9999-9999-999999999991', '22222222-2222-2222-2222-222222222222', 40.7150, -74.0100, 25.0, CURRENT_TIMESTAMP), -- Mario works within 25km of downtown
('99999999-9999-9999-9999-999999999992', '33333333-3333-3333-3333-333333333333', 40.7500, -73.9800, 10.0, CURRENT_TIMESTAMP); -- Sparky works within 10km of downtown

-- 7. Availability (Working 9 to 5 every day)
INSERT INTO provider_availability (id, provider_id, day_of_week, start_time, end_time, is_active, created_at) 
SELECT gen_random_uuid(), '22222222-2222-2222-2222-222222222222', day, '09:00:00', '17:00:00', true, CURRENT_TIMESTAMP FROM generate_series(1,5) day;

INSERT INTO provider_availability (id, provider_id, day_of_week, start_time, end_time, is_active, created_at) 
SELECT gen_random_uuid(), '33333333-3333-3333-3333-333333333333', day, '08:00:00', '16:00:00', true, CURRENT_TIMESTAMP FROM generate_series(1,5) day;
