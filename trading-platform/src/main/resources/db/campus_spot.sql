CREATE TABLE campus_spot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sort (sort_order)
);

INSERT INTO campus_spot (name, description, latitude, longitude, sort_order) VALUES
('图文信息中心正门', '图书馆门口，校园标志性建筑', 29.132413, 119.641780, 1),
('桃源餐厅门口', '西区食堂，人流量大', 29.132000, 119.640500, 2),
('杏园餐厅门口', '杏园公寓片区食堂', 29.133200, 119.641000, 3),
('桂苑餐厅门口', '东区食堂', 29.132800, 119.642500, 4),
('大学生活动中心', '团委所在地，办活动多', 29.131800, 119.641200, 5),
('美食苑', '大活东面，特色小吃集中', 29.131900, 119.641800, 6),
('体育馆', '校园内体育馆', 29.131500, 119.640800, 7),
('启明公寓门口', '学生宿舍区', 29.132500, 119.642000, 8),
('桃源公寓门口', '学生宿舍区', 29.132200, 119.640800, 9),
('师大印象主题餐厅', '校内接待餐厅，环境好', 29.132300, 119.641500, 10);

ALTER TABLE product ADD COLUMN spot_id BIGINT;
ALTER TABLE product ADD INDEX idx_spot (spot_id);
