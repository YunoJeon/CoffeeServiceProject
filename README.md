# 원두 추천 레시피 서비스

## 프로젝트 개요

원두 추천 레시피 서비스는 커피 원두에 대한 다양한 레시피와 리뷰를 공유하고, 커피 원두를 구매하며 즐길 수 있는 플랫폼입니다. 공급처는 추천 레시피와 설명을 제공하고, 사용자들은 이를 기반으로 다양한 커피 레시피를 추가하거나 서로의 레시피를 평가할 수 있습니다.

---

## 기술 스택

- Backend : `Java`, `Spring Boot`, `Spring Data JPA`, `Swagger`
- Database : `MySQL`
- Cache : `Redis`
- API : `REST API`
- Search Engine : `ElasticSearch`
- Infra : `Docker`, `AWS(EC2)`, `Smtp`

---

## 주요 기능
[회원]

회원가입
- 유저 : 일반 유저, 로스터(판매자) 두가지 유형
- 입력 정보 :
    - 일반 유저 : 아이디, 비밀번호, 이메일, 연락터
    - 로스터(판매자) : 아이디, 비밀번호, 이메일, 연락처, 로스터 정보 등
- 인증 과정 : 이메일 인증을 통해 회원가입이 완료됨. 이메일 인증이 완료되지 않으면 계정이 활성화 되지 않음

회원정보 조회
- 권한 : 유저 유형과 관계없이 조회 가능, 로스터 라면 로스터 정보도 같이 조회 가능

회원정보 수정
- 권한 : 유저 유형과 관계없이 수정 가능, 본인 확인(토큰) 이 된 유저만 수정 가능
- 수정 가능 정보 : 연락처, 비밀번호, 이메일, 주소
- 로스터 유형이면, 로스터 정보도 수정 가능

회원 탈퇴
- 권한 : 본인만 탈퇴 가능

로그인
- 방식 : JWT 토큰을 이용한 로그인

[원두 등록]

원두 등록
- 권한 : 로그인한 유저라면 유저 유형과 관계없이 누구나 등록가능
- 입력 정보 : 각 원두 정보와 레시피 등록(원두별로 Null 값 허용)

[원두 조회]

필터 기능
- 필터 조건 : 원두 이름, 산지, 로스터리 이름을 기준으로 필터링
    - 검색어가 포함된 원두 이름을 찾기 위해 %{검색어}% 형태로 비교를 진행
- 검색 기능 : 검색어로 조회 가능
- 권한 : 로그인 여부와 관계없이 사용 가능
- 별점 : 해당 원두에 별점이 없을 시 기본값 0.0 으로 설정
- 사용 기술 : ES

[원두 구매]

원두 구매
- 권한 : 로그인한 유저라면 유저 유형과 관계없이 구매 가능
- 구매 가능 원두 : 로스터가 등록한 원두만 구매 가능

[리뷰 기능]

리뷰 등록
- 권한 : 로그인한 유저라면 유저 유형과 관계없이 리뷰 작성 가능
- 입력 정보 : 별점, 코멘트

리뷰 수정 / 삭제
- 권한 : 리뷰 작성자만 수정 및 삭제 가능

[즐겨찾기]

즐겨찾기 추가 / 조회 / 삭제
- 권한 : 로그인한 유저라면 유저 유형과 관계없이 즐겨찾기 추가 가능
- 조회 및 삭제 기능 : 본인이 추가한 즐겨찾기 목록만 조회, 삭제 가능

[장바구니]

장바구니 기능
- 장바구니 담기 : 로그인한 유저이고, 유저 유형과 관계없이, 로스터가 등록한 레시피와 연결된 원두를 장바구니에 담기 가능
- 장바구니 조회 / 수정 / 삭제 : 본인이 담은 장바구니 목록만 조회, 수정, 삭제 가능
- 결제 금액 : 원두 수량이 변경시, 추가적으로 원두를 장바구니에 담을 시, 총 금액이 업데이트

[구매목록]

구매목록 조회
- 권한 : 본인의 구매 기록만 조회 가능
- 조회 가능 정보 : 구매일자, 구매한 원두 정보, 로스터 정보, 갯수 등
- 구매목록 조회 : 구매한 원두의 현재 상태(결제 완료, 배송 준비중, 배송완료 등) 확인 가능

---

## 데이터베이스 설계 (ERD)

![coffee_service-4](https://github.com/user-attachments/assets/c2025be2-4933-4721-a923-28d78575e68c)


---

## API 엔드포인트
[회원]

- 회원가입 : POST /members
- 메일발송 : GET /verification
- 로그인 : POST /members/login (토큰발행)
- 회원조회 : GET /members/info (토큰필요)
- 회원수정 : PATCH /members/me (토큰필요)
- 회원탈퇴 : DELETE /members/cancel-membership (토큰필요)
- 로스터등록 : POST /members/roaster/add-roaster (토큰필요)
- 로스터수정 : PATCH /members/roaster/me (토큰필요, 등록한 본인인지 확인)


[원두]

- 원두등록 : POST /beans (토큰필요)
- 원두목록 : GET /beans?page={페이지번호}&size={페이지당 결과 갯수&role={회원유형}&purchaseStatus={구매가능여부}
- 원두조회 : GET /beans/{id}/info
- 원두검색 : GET /search?query={검색어}&page={페이지번호&size={페이지당 결과 갯수}&role={회원유형}&purchaseStatus={구가능여부}
- 원두수정 : PATCH /beans/{id} (토큰필요, 등록한 본인인지 확인)
- 원두삭제 : DELETE /beans/{id} (토큰필요, 등록한 본인인지 확인)
- 원두공유 : POST /beans/{id}/share

[리뷰]

- 리뷰등록 : POST /reviews/{beanId} (토큰필요)
- 나의 리뷰조회 : GET /reviews/members/me (토큰필요)
- 원두 리뷰조회 : GET /reviews/beans/{beanId}
- 리뷰수정 : PATCH /reviews/{id} (토큰필요, 등록한 본인인지 확인)
- 리뷰삭제 : DELETE /reviews/{id} (토큰필요, 등록한 본인인지 확인)

[원두구매-장바구니]

- 장바구니담기 : POST /carts/{beanId} (토큰필요)
- 장바구니조회 : GET /carts (토큰필요, 등록한 본인인지 확인)
- 장바구니수정 : PATCH /carts/{id} (토큰필요, 등록한 본인인지 확인)
- 장바구니삭제 : DELETE /carts/{id} (토큰필요, 등록한 본인인지 확인)

[결제]

- 주문서등록 : POST /order (토큰필요)
- 주문서검증 및 결제진행 : POST /order/verification/{imp_uid}
- 결제취소 : POST /order/cancel/{imp_uid}
- 구매목록조회 : GET /order/buyer/purchases?&page={페이지번호&size={페이지당 결과 갯수} (토큰필요, 구매한 본인인지 확인)
- 구매상태변경 : PATCH /order/buyer/purchases/{id}/paymentStatus={변경할 상태}
- 판매목록조회 : GET /order/seller/purchases?&page={페이지번호&size={페이지당 결과 갯수} (토큰필요, 판매한 본인인지 확인)
- 판매상태변경 : PATCH /order/seller/purchases/{id}/?paymentStatus={변경할 상태}
	
[즐겨찾기]

- 즐겨찾기등록 : POST /favorites/beans/{beanId} (토큰필요)
- 즐겨찾기조회 : GET /favorites/members/{memberId} (토큰필요, 등록한 본인인지 확인)
- 즐겨찾기삭제 : DELETE /members/favorites/{favoriteId} (토큰필요, 등록한 본인인지 확인)
