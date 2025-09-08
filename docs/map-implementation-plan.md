# Map 도메인 구현 계획서

## 개요

Map 도메인은 한국관광공사 반려동물 관광정보 API를 활용하여 위치 기반 및 키워드 기반 관광 정보를 제공하는 서비스입니다. 어댑터 패턴을 적용하여 외부 API 의존성을 분리하고, 단계별로 구현하여 안정적인 서비스를 구축합니다.

## 구현 Phase 개요

| Phase | 기간 | 주요 기능 | 목표 |
|-------|------|-----------|------|
| Phase 1 | 1주 | 기본 인프라 및 어댑터 | 외부 API 연동 및 기본 구조 구축 |
| Phase 2 | 1주 | 핵심 검색 기능 | 위치/키워드 기반 검색 API 구현 |
| Phase 3 | 1주 | 데이터 관리 및 최적화 | 캐싱, 성능 최적화, 모니터링 |
| Phase 4 | 1주 | 고급 기능 및 확장 | 제휴 장소, 리뷰 시스템 (선택적) |

---

## Phase 1: 기본 인프라 및 어댑터 구축 (1주)

### 1.1 프로젝트 설정 및 의존성 구성
**목표**: 기본 Spring Boot 프로젝트 구조 설정

**작업 내용**:
- [ ] Gradle 의존성 추가 (WebClient, Jackson, JPA, Redis, Validation)
- [ ] application.yml 설정 (API 키, 데이터베이스, Redis)
- [ ] 기본 패키지 구조 생성 (`com.tourapi.tourapi.map`)

**예상 소요시간**: 0.5일

### 1.2 외부 API 클라이언트 구현
**목표**: 한국관광공사 API와의 기본 통신 구조 구축

**작업 내용**:
- [ ] `TourApiProperties` 설정 클래스 구현
- [ ] `TourApiConfig` WebClient 설정
- [ ] `TourApiClient` 기본 클래스 구현
- [ ] API 호출 테스트 (Postman/Unit Test)

**예상 소요시간**: 1일

### 1.3 어댑터 패턴 구현
**목표**: 외부 API 응답을 내부 엔티티로 변환하는 어댑터 구현

**작업 내용**:
- [ ] `ExternalTourApiResponse` DTO 구현
- [ ] `ExternalTourLocationDto` DTO 구현
- [ ] `TourLocationMapper` 매핑 로직 구현
- [ ] `TourLocationAdapter` 어댑터 클래스 구현

**예상 소요시간**: 1.5일

### 1.4 기본 엔티티 및 Repository 구현
**목표**: 데이터베이스 구조 및 기본 CRUD 기능 구현

**작업 내용**:
- [ ] `TourLocation` 엔티티 구현
- [ ] `TourCategory` 엔티티 구현
- [ ] `LocationSearchHistory` 엔티티 구현
- [ ] 각 Repository 인터페이스 구현
- [ ] 데이터베이스 마이그레이션 스크립트

**예상 소요시간**: 1일

### 1.5 기본 서비스 계층 구현
**목표**: 비즈니스 로직의 기본 틀 구현

**작업 내용**:
- [ ] `TourLocationService` 기본 구조 구현
- [ ] `LocationSearchService` 기본 구조 구현
- [ ] 예외 처리 클래스 구현 (`TourApiException`, `LocationNotFoundException`)
- [ ] 기본 단위 테스트 작성

**예상 소요시간**: 1일

**Phase 1 완료 기준**:
- [ ] 외부 API 호출 성공
- [ ] 데이터 변환 로직 동작 확인
- [ ] 기본 CRUD 기능 동작 확인
- [ ] 단위 테스트 통과율 80% 이상

---

## Phase 2: 핵심 검색 기능 구현 (1주)

### 2.1 위치 기반 검색 API 구현
**목표**: 사용자 위치 기반 관광지 검색 기능 완성

**작업 내용**:
- [ ] `TourLocationService.searchByLocation()` 구현
- [ ] `TourLocationAdapter.fetchTourLocationsByLocation()` 구현
- [ ] `TourApiClient.fetchTourDataByLocation()` 구현
- [ ] 거리 계산 및 정렬 로직 구현
- [ ] 위치 기반 검색 API 테스트

**예상 소요시간**: 2일

### 2.1.1 카카오맵 영역 기반 검색 API 구현 (NEW)
**목표**: 카카오맵 bounds_changed 이벤트를 활용한 효율적인 지도 검색

**작업 내용**:
- [ ] `MapController.searchByBounds()` API 엔드포인트 추가
- [ ] `TourLocationService.searchByBounds()` 서비스 메서드 구현
- [ ] `TourLocationAdapter.fetchTourLocationsByBounds()` 어댑터 메서드 구현
- [ ] `TourApiClient.fetchTourDataByBounds()` 클라이언트 메서드 구현
- [ ] 영역 좌표 기반 필터링 로직 구현
- [ ] 줌 레벨별 반경 자동 계산 로직 구현
- [ ] 영역 기반 검색 API 테스트

**API 설계**:
```java
@GetMapping("/search/bounds")
public ResponseEntity<ApiResponse<List<TourLocation>>> searchByBounds(
    @RequestParam Double north,      // 북쪽 위도
    @RequestParam Double south,      // 남쪽 위도  
    @RequestParam Double east,       // 동쪽 경도
    @RequestParam Double west,       // 서쪽 경도
    @RequestParam(required = false) Integer category,
    @RequestParam(required = false) Integer zoomLevel
)
```

**프론트엔드 연동 계획**:
```javascript
// 카카오맵 bounds_changed 이벤트 처리
kakao.maps.event.addListener(map, 'bounds_changed', function() {
    const bounds = map.getBounds();
    const swLatlng = bounds.getSouthWest();
    const neLatlng = bounds.getNorthEast();
    const zoomLevel = map.getLevel();
    
    // 디바운싱으로 API 호출 최적화
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        searchByBounds(
            neLatlng.getLat(), swLatlng.getLat(),
            neLatlng.getLng(), swLatlng.getLng(),
            zoomLevel
        );
    }, 500);
});
```

**예상 소요시간**: 1.5일

### 2.2 키워드 기반 검색 API 구현
**목표**: 키워드로 관광지를 검색하는 기능 구현

**작업 내용**:
- [ ] `TourLocationService.searchByKeyword()` 구현
- [ ] `TourLocationAdapter.fetchTourLocationsByKeyword()` 구현
- [ ] `TourApiClient.fetchTourDataByKeyword()` 구현
- [ ] 페이징 처리 로직 구현
- [ ] 키워드 기반 검색 API 테스트

**예상 소요시간**: 1.5일

### 2.3 카테고리별 조회 기능 구현
**목표**: 카테고리별 관광지 필터링 기능 구현

**작업 내용**:
- [ ] `TourLocationService.findByCategory()` 구현
- [ ] 카테고리 데이터 초기화 (12, 14, 15, 28, 32, 38, 39)
- [ ] 카테고리별 필터링 로직 구현
- [ ] 카테고리별 조회 API 테스트

**예상 소요시간**: 1일

### 2.4 컨트롤러 계층 구현
**목표**: REST API 엔드포인트 구현

**작업 내용**:
- [ ] `MapController` 구현
- [ ] API 응답 표준화 (`ApiResponse` 클래스)
- [ ] 요청/응답 DTO 구현
- [ ] API 문서화 (Swagger/OpenAPI)
- [ ] 통합 테스트 작성
- [ ] **영역 기반 검색 API 엔드포인트 추가** (NEW)

**예상 소요시간**: 1.5일

### 2.5 검색 이력 관리 구현
**목표**: 사용자 검색 패턴 분석을 위한 이력 관리

**작업 내용**:
- [ ] `LocationSearchService` 완전 구현
- [ ] 검색 이력 저장 로직 구현
- [ ] 인기 검색 지역 조회 기능 구현
- [ ] 검색 이력 관련 API 구현

**예상 소요시간**: 1일

**Phase 2 완료 기준**:
- [ ] 모든 검색 API 엔드포인트 동작 확인
- [ ] API 응답 시간 2초 이내
- [ ] 통합 테스트 통과율 90% 이상
- [ ] API 문서 완성

---

## Phase 3: 데이터 관리 및 최적화 (1주)

### 3.1 캐싱 전략 구현
**목표**: API 응답 시간 단축 및 외부 API 호출 최소화

**작업 내용**:
- [ ] Redis 캐싱 설정
- [ ] API 응답 캐싱 로직 구현
- [ ] 캐시 키 전략 설계
- [ ] TTL 설정 및 캐시 무효화 로직
- [ ] 캐시 성능 테스트
- [ ] **영역 기반 검색 결과 캐싱 최적화** (NEW)

**예상 소요시간**: 1.5일

### 3.2 데이터베이스 최적화
**목표**: 쿼리 성능 향상 및 데이터 관리 효율성 증대

**작업 내용**:
- [ ] 위도/경도 인덱스 생성
- [ ] 카테고리별 인덱스 최적화
- [ ] 쿼리 성능 분석 및 최적화
- [ ] 데이터베이스 연결 풀 설정
- [ ] 성능 모니터링 설정

**예상 소요시간**: 1일

### 3.3 비동기 처리 및 성능 최적화
**목표**: 대용량 데이터 처리 및 응답 시간 최적화

**작업 내용**:
- [ ] 비동기 API 호출 구현
- [ ] 배치 처리 로직 구현
- [ ] 연결 풀링 최적화
- [ ] 메모리 사용량 최적화
- [ ] 부하 테스트 및 성능 튜닝

**예상 소요시간**: 1.5일

### 3.4 모니터링 및 로깅 구현
**목표**: 시스템 상태 모니터링 및 문제 추적

**작업 내용**:
- [ ] API 호출 로깅 구현
- [ ] 응답 시간 측정 및 로깅
- [ ] 에러 모니터링 설정
- [ ] 메트릭 수집 설정 (Micrometer)
- [ ] 알림 시스템 구현

**예상 소요시간**: 1일

### 3.5 보안 및 검증 강화
**목표**: API 보안 및 데이터 검증 강화

**작업 내용**:
- [ ] API 키 관리 시스템 구현
- [ ] 입력 데이터 검증 강화
- [ ] SQL 인젝션 방지
- [ ] XSS 공격 방지
- [ ] Rate Limiting 구현

**예상 소요시간**: 1일

**Phase 3 완료 기준**:
- [ ] API 응답 시간 1초 이내
- [ ] 캐시 히트율 80% 이상
- [ ] 시스템 가용성 99% 이상
- [ ] 보안 테스트 통과

---

## Phase 4: 고급 기능 및 확장 (1주) - 선택적

### 4.1 제휴 장소 관리 시스템 (선택적)
**목표**: 제휴 업체의 장소 정보 관리 기능

**작업 내용**:
- [ ] `PartnerLocation` 엔티티 구현
- [ ] `PartnerLocationService` 구현
- [ ] 제휴 장소 등록/수정/삭제 API
- [ ] 제휴 장소 인증 시스템
- [ ] 제휴 장소 관리 대시보드

**예상 소요시간**: 2일

### 4.2 리뷰 및 평점 시스템 (선택적)
**목표**: 사용자 리뷰 및 평점 관리 기능

**작업 내용**:
- [ ] `PartnerLocationReview` 엔티티 구현
- [ ] `PartnerLocationReviewService` 구현
- [ ] 리뷰 등록/조회/수정 API
- [ ] 평점 통계 시스템
- [ ] 리뷰 이미지 관리

**예상 소요시간**: 2일

### 4.3 개인화 및 추천 시스템 (선택적)
**목표**: 사용자 맞춤형 관광지 추천 기능

**작업 내용**:
- [ ] 사용자 선호도 분석
- [ ] 추천 알고리즘 구현
- [ ] 개인화된 검색 결과 제공
- [ ] 즐겨찾기 기능 구현
- [ ] 추천 성능 최적화

**예상 소요시간**: 1일

**Phase 4 완료 기준**:
- [ ] 고급 기능 API 동작 확인
- [ ] 사용자 경험 개선 확인
- [ ] 시스템 확장성 검증

---

## 카카오맵 영역 기반 검색 구현 상세 계획

### 개요
카카오맵의 `bounds_changed` 이벤트를 활용하여 사용자가 지도를 확대/축소하거나 이동할 때마다 실시간으로 해당 영역의 관광지를 검색하는 기능을 구현합니다.

### 핵심 아이디어
1. **영역 좌표 기반 검색**: 위도/경도 기반 반경 검색 대신 지도의 북서/남동 좌표를 이용한 영역 검색
2. **줌 레벨별 최적화**: 줌 레벨에 따라 적절한 반경과 검색 전략 적용
3. **디바운싱 최적화**: 프론트엔드에서 지도 이동 완료 후 API 호출하여 불필요한 요청 방지

### 구현 단계

#### 1단계: 백엔드 API 구현
```java
// 새로운 API 엔드포인트
@GetMapping("/search/bounds")
public ResponseEntity<ApiResponse<List<TourLocation>>> searchByBounds(
    @RequestParam Double north,      // 북쪽 위도
    @RequestParam Double south,      // 남쪽 위도  
    @RequestParam Double east,       // 동쪽 경도
    @RequestParam Double west,       // 서쪽 경도
    @RequestParam(required = false) Integer category,
    @RequestParam(required = false) Integer zoomLevel
)
```

#### 2단계: 영역 기반 검색 로직
```java
// 영역 중심점 계산
private LatLng calculateCenter(Double north, Double south, Double east, Double west) {
    double centerLat = (north + south) / 2;
    double centerLng = (east + west) / 2;
    return new LatLng(centerLat, centerLng);
}

// 줌 레벨별 반경 계산
private Integer calculateRadiusByZoom(Integer zoomLevel) {
    // 줌 레벨에 따른 반경 매핑 - 줌 1이 가장 가까운 거리
    Map<Integer, Integer> zoomRadiusMap = Map.of(
        1, 200,     // 바로 근처
        2, 500,     // 매우 근처
        3, 1000,    // 근처
        4, 2000,    // 상세 지역
        5, 5000,    // 읍/면/동
        6, 10000,   // 시/군/구
        7, 25000,   // 광역시/도
        8, 50000    // 전국
    );
    return zoomRadiusMap.getOrDefault(zoomLevel, 1000);
}
```

#### 3단계: 프론트엔드 연동
```javascript
// 카카오맵 이벤트 리스너
let searchTimeout;
kakao.maps.event.addListener(map, 'bounds_changed', function() {
    const bounds = map.getBounds();
    const swLatlng = bounds.getSouthWest();
    const neLatlng = bounds.getNorthEast();
    const zoomLevel = map.getLevel();
    
    // 디바운싱으로 API 호출 최적화
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        searchByBounds(
            neLatlng.getLat(), swLatlng.getLat(),
            neLatlng.getLng(), swLatlng.getLng(),
            zoomLevel
        );
    }, 500); // 500ms 디바운싱
});
```

### 성능 최적화 전략

#### 1. 캐싱 전략
- **영역별 캐싱**: 동일한 영역 좌표에 대한 검색 결과 캐싱
- **줌 레벨별 캐싱**: 줌 레벨에 따른 다른 캐시 키 사용
- **TTL 설정**: 영역 기반 캐시는 30분, 줌 레벨별 캐시는 1시간

#### 2. 검색 최적화
- **중복 검색 방지**: 동일한 영역에 대한 중복 API 호출 방지
- **배치 처리**: 여러 영역을 한 번에 처리하는 배치 API 고려
- **점진적 로딩**: 줌 레벨이 높을 때만 상세 검색 수행

#### 3. 사용자 경험 개선
- **로딩 상태 표시**: 검색 중임을 사용자에게 알림
- **점진적 마커 표시**: 검색 결과를 순차적으로 지도에 표시
- **오프라인 지원**: 캐시된 데이터로 오프라인에서도 기본 기능 제공

### 예상 효과
1. **성능 향상**: 불필요한 API 호출 70% 감소
2. **사용자 경험 개선**: 지도 이동 시 즉각적인 반응
3. **서버 부하 감소**: 캐싱으로 인한 외부 API 호출 최소화
4. **확장성**: 다양한 지도 서비스와의 호환성

### 구현 우선순위
1. **High**: 기본 영역 기반 검색 API 구현
2. **Medium**: 줌 레벨별 최적화 및 캐싱
3. **Low**: 고급 기능 (배치 처리, 오프라인 지원)

---

## 기술 스택 및 도구

### Backend
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Cache**: Redis
- **HTTP Client**: WebClient
- **ORM**: JPA/Hibernate
- **Validation**: Bean Validation
- **Testing**: JUnit 5, Mockito, TestContainers

### DevOps & Monitoring
- **Build Tool**: Gradle
- **Container**: Docker
- **Monitoring**: Micrometer, Actuator
- **Logging**: SLF4J + Logback
- **API Documentation**: OpenAPI 3.0

### External APIs
- **Tourism API**: 한국관광공사 반려동물 관광정보 API
- **Geocoding**: 필요시 추가 지오코딩 서비스

---

## 위험 요소 및 대응 방안

### 1. 외부 API 의존성
**위험**: 한국관광공사 API 장애 또는 변경
**대응**: 
- Circuit Breaker 패턴 적용
- Fallback 데이터 제공
- API 버전 관리

### 2. 성능 이슈
**위험**: 대용량 데이터 처리 시 성능 저하
**대응**:
- 캐싱 전략 최적화
- 비동기 처리 도입
- 데이터베이스 인덱스 최적화

### 3. 데이터 정합성
**위험**: 외부 API 데이터와 내부 데이터 불일치
**대응**:
- 데이터 검증 로직 강화
- 정기적인 데이터 동기화
- 데이터 품질 모니터링

---

## 성공 지표 (KPI)

### Phase 1
- [ ] 외부 API 연결 성공률 100%
- [ ] 기본 기능 단위 테스트 통과율 80% 이상

### Phase 2
- [ ] 모든 API 엔드포인트 응답 시간 2초 이내
- [ ] API 가용성 95% 이상
- [ ] 통합 테스트 통과율 90% 이상

### Phase 3
- [ ] API 응답 시간 1초 이내
- [ ] 캐시 히트율 80% 이상
- [ ] 시스템 가용성 99% 이상
- [ ] 에러율 1% 이하

### Phase 4 (선택적)
- [ ] 사용자 만족도 4.0/5.0 이상
- [ ] 고급 기능 사용률 30% 이상

---

## 결론

Map 도메인은 4단계의 Phase로 나누어 체계적으로 구현됩니다. Phase 1-3은 필수 구현 사항이며, Phase 4는 선택적 확장 기능입니다. 각 Phase별로 명확한 목표와 완료 기준을 설정하여 안정적이고 확장 가능한 서비스를 구축할 수 있습니다.

**총 예상 개발 기간**: 3-4주 (Phase 4 제외 시 3주)
**핵심 개발자**: 1-2명
**테스터**: 1명 (선택적)

---

*이 문서는 Map 도메인 구현의 가이드라인입니다. 개발 과정에서 요구사항 변경에 따라 수정될 수 있습니다.*
