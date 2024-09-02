INSERT INTO roles (role_id, name) VALUES 
(1, 'ADMIN'),
(2, 'BASIC')
ON CONFLICT (role_id) DO NOTHING;
