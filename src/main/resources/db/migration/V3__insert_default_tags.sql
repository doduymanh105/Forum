ALTER TABLE tags ADD CONSTRAINT uk_tags_name UNIQUE (tag_name);

INSERT INTO tags (tag_name) VALUES
    ('Java'), ('Spring Boot'), ('Docker'), ('Kubernetes'),
    ('PostgreSQL'), ('Redis'), ('JavaScript'), ('TypeScript'),
    ('React'), ('Angular'), ('Vue.js'), ('Node.js'),
    ('Python'), ('DevOps'), ('CI/CD'), ('Microservices'),
    ('AWS'), ('Cloud Computing'), ('Machine Learning'), ('System Design')
ON CONFLICT (tag_name) DO NOTHING;