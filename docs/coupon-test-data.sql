-- 쿠폰 시스템 테스트 데이터 생성 스크립트

-- 1. 쿠폰 테이블에 테스트 데이터 삽입
INSERT INTO coupon (name, description, type, discount_type, discount_value, exchange_cost, exchange_type, validity_days, max_exchange_count, current_exchange_count, is_active, start_date, end_date, created_at, updated_at) VALUES
-- 음료 쿠폰
('음료 무료 쿠폰', '카페에서 음료 1잔 무료로 받을 수 있는 쿠폰입니다.', 'DRINK', 'FREE', 0, 100, 'POINT', 30, 100, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),
('음료 20% 할인 쿠폰', '카페에서 음료 20% 할인받을 수 있는 쿠폰입니다.', 'DRINK', 'PERCENTAGE', 20, 50, 'POINT', 30, 200, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),

-- 미용 쿠폰
('반려견 미용 할인쿠폰', '펫샵에서 반려견 미용 10% 할인받을 수 있는 쿠폰입니다.', 'GROOMING', 'PERCENTAGE', 10, 200, 'POINT', 60, 50, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),
('반려견 미용 5000원 할인쿠폰', '펫샵에서 반려견 미용 5000원 할인받을 수 있는 쿠폰입니다.', 'GROOMING', 'FIXED_AMOUNT', 5000, 300, 'POINT', 60, 30, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),

-- 펫호텔 쿠폰
('펫호텔 할인권', '펫호텔 이용 시 15% 할인받을 수 있는 쿠폰입니다.', 'HOTEL', 'PERCENTAGE', 15, 500, 'POINT', 90, 20, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),
('펫호텔 1박 무료 쿠폰', '펫호텔 1박 무료로 이용할 수 있는 쿠폰입니다.', 'HOTEL', 'FREE', 0, 1000, 'POINT', 90, 10, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),

-- 사료/간식 쿠폰
('사료 10% 할인쿠폰', '펫샵에서 사료 구매 시 10% 할인받을 수 있는 쿠폰입니다.', 'FOOD', 'PERCENTAGE', 10, 150, 'POINT', 45, 100, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),
('간식 무료 쿠폰', '펫샵에서 간식 1개 무료로 받을 수 있는 쿠폰입니다.', 'FOOD', 'FREE', 0, 80, 'POINT', 30, 150, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),

-- 장난감 쿠폰
('장난감 15% 할인쿠폰', '펫샵에서 장난감 구매 시 15% 할인받을 수 있는 쿠폰입니다.', 'TOY', 'PERCENTAGE', 15, 120, 'POINT', 30, 80, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),

-- 의료 쿠폰
('건강검진 할인쿠폰', '동물병원에서 건강검진 20% 할인받을 수 있는 쿠폰입니다.', 'MEDICAL', 'PERCENTAGE', 20, 800, 'POINT', 120, 15, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),

-- 기타 쿠폰
('펫샵 이용권', '펫샵에서 다양한 서비스를 이용할 수 있는 쿠폰입니다.', 'ETC', 'FREE', 0, 250, 'POINT', 60, 40, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW());

-- 2. 만료된 쿠폰 테스트 데이터 (스케줄러 테스트용)
INSERT INTO coupon (name, description, type, discount_type, discount_value, exchange_cost, exchange_type, validity_days, max_exchange_count, current_exchange_count, is_active, start_date, end_date, created_at, updated_at) VALUES
('만료된 쿠폰', '이미 만료된 테스트 쿠폰입니다.', 'DRINK', 'FREE', 0, 100, 'POINT', 30, 10, 0, true, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW(), NOW());

-- 3. 교환 한도에 도달한 쿠폰 테스트 데이터
INSERT INTO coupon (name, description, type, discount_type, discount_value, exchange_cost, exchange_type, validity_days, max_exchange_count, current_exchange_count, is_active, start_date, end_date, created_at, updated_at) VALUES
('한도 초과 쿠폰', '교환 한도에 도달한 테스트 쿠폰입니다.', 'DRINK', 'FREE', 0, 100, 'POINT', 30, 5, 5, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW());

-- 4. 비활성화된 쿠폰 테스트 데이터
INSERT INTO coupon (name, description, type, discount_type, discount_value, exchange_cost, exchange_type, validity_days, max_exchange_count, current_exchange_count, is_active, start_date, end_date, created_at, updated_at) VALUES
('비활성화 쿠폰', '비활성화된 테스트 쿠폰입니다.', 'DRINK', 'FREE', 0, 100, 'POINT', 30, 10, 0, false, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW());

-- 5. 스탬프 교환 쿠폰 테스트 데이터 (향후 구현용)
INSERT INTO coupon (name, description, type, discount_type, discount_value, exchange_cost, exchange_type, validity_days, max_exchange_count, current_exchange_count, is_active, start_date, end_date, created_at, updated_at) VALUES
('스탬프 교환 쿠폰', '스탬프로 교환할 수 있는 테스트 쿠폰입니다.', 'DRINK', 'FREE', 0, 10, 'STAMP', 30, 50, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW());

-- 테스트 데이터 확인 쿼리
-- SELECT * FROM coupon WHERE is_active = true ORDER BY created_at DESC;
-- SELECT type, COUNT(*) as count FROM coupon WHERE is_active = true GROUP BY type;
-- SELECT exchange_type, COUNT(*) as count FROM coupon WHERE is_active = true GROUP BY exchange_type;
