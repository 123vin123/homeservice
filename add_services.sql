-- Add more Categories
INSERT INTO categories (id, name, description, created_at) VALUES 
('d1111111-1111-1111-1111-111111111111', 'Cleaning', 'Home and deep cleaning services', CURRENT_TIMESTAMP),
('d2222222-2222-2222-2222-222222222222', 'Handyman', 'General household repairs and assembly', CURRENT_TIMESTAMP),
('d3333333-3333-3333-3333-333333333333', 'HVAC', 'Heating, Ventilation, and Air Conditioning', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Add more Services
INSERT INTO services (id, category_id, name, description, base_price, duration_minutes, is_active, created_at) VALUES 
('e1111111-1111-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 'Deep House Cleaning', 'Thorough deep cleaning of the entire house', 200.00, 180, true, CURRENT_TIMESTAMP),
('e2222222-2222-2222-2222-222222222222', 'd2222222-2222-2222-2222-222222222222', 'Furniture Assembly', 'Assemble flat-pack furniture', 80.00, 90, true, CURRENT_TIMESTAMP),
('e3333333-3333-3333-3333-333333333333', 'd2222222-2222-2222-2222-222222222222', 'TV Mounting', 'Securely mount TV to wall', 100.00, 60, true, CURRENT_TIMESTAMP),
('e4444444-4444-4444-4444-444444444444', 'd3333333-3333-3333-3333-333333333333', 'AC Tune-Up', 'Annual maintenance for AC units', 120.00, 60, true, CURRENT_TIMESTAMP),
('e5555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444', 'Drain Snaking', 'Clear clogged drains', 110.00, 45, true, CURRENT_TIMESTAMP),
('e6666666-6666-6666-6666-666666666666', '55555555-5555-5555-5555-555555555555', 'Panel Upgrade', 'Upgrade electrical panel', 800.00, 240, true, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Map existing providers to these new services
INSERT INTO provider_services (provider_id, service_id, custom_price) VALUES 
('caaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'e2222222-2222-2222-2222-222222222222', 75.00), -- David Fixit does Furniture
('caaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'e3333333-3333-3333-3333-333333333333', 90.00),  -- David Fixit does TV Mount
('c5555555-5555-5555-5555-555555555555', 'e4444444-4444-4444-4444-444444444444', 115.00), -- Bob does AC
('c3333333-3333-3333-3333-333333333333', 'e5555555-5555-5555-5555-555555555555', 105.00), -- Luigi does Drains
('c4444444-4444-4444-4444-444444444444', 'e6666666-6666-6666-6666-666666666666', NULL)    -- Tesla does Panels
ON CONFLICT DO NOTHING;
