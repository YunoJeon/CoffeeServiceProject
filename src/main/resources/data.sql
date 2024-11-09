insert into member (email, member_name, password, phone, role, address, created_at,
                    certification_at)
values ('sellerTest@test.com', '셀러 테스트',
        '$2a$10$MbIXWjflj1coLVrazrc1MOim15SCWsbGapr0d2g2YLFGF6OgjnJMy', '010-1234-5678', 'SELLER',
        'testAddress', '2024-10-22 15:14:46', '2024-10-22 15:14:46'),
       ('buyerTest@test.com', '바이어 테스트',
        '$2a$10$MbIXWjflj1coLVrazrc1MOim15SCWsbGapr0d2g2YLFGF6OgjnJMh', '010-2345-6789', 'BUYER',
        'testAddress', '2024-10-22 15:14:46', '2024-10-22 15:14:46');

insert into roaster (member_id, roaster_name, office_address, contact_info, description, created_at)
values ('1', '로스터명', '주소', '010-5555-6666', '설명', '2024-10-22 15:14:46');

insert into bean (member_id, bean_name, bean_state, bean_region, bean_farm, bean_variety, altitude,
                  process, grade,
                  roasting_level, roasting_date, cup_note, espresso_recipe, filter_recipe,
                  milk_pairing,
                  signature_variation, price, purchase_status)
values (1, '이름', '국가', '지역', '농장', '품종', '고도', '가공법', '등급', '미디움', '어제', '굿', '에쏘 레싶', '필터 레싶',
        '추천우유', '시그니처', 10, 'POSSIBLE');
