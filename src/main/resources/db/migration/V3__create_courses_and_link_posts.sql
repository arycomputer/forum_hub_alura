
-- src/main/resources/db/migration/V3__create_courses_and_link_posts.sql
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

ALTER TABLE posts
ADD COLUMN course_id BIGINT NOT NULL;

ALTER TABLE posts
ADD CONSTRAINT fk_course
FOREIGN KEY (course_id) REFERENCES courses(id);