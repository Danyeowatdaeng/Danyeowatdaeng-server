# Map 도메인 설계 문서

## 1. 개요

Map 도메인은 한국관광공사 반려동물 관광정보 API를 활용하여 위치 기반 관광 정보를 제공하는 서비스입니다. 각 카테고리별로 데이터를 파싱하고 관리하여 사용자에게 맞춤형 관광 정보를 제공합니다.

## 2. API 정보

### 2.1 기본 정보
- **위치 기반 검색 API URL**: `http://apis.data.go.kr/B551011/KorPetTourService/locationBasedList`
- **키워드 기반 검색 API URL**: `http://apis.data.go.kr/B551011/KorPetTourService/searchKeyword`
- **응답 형식**: XML, JSON (`_type=json` 지정 시 JSON 반환)
- **인증 방식**: Service Key

### 2.2 주요 파라미터

#### 2.2.1 위치 기반 검색 API (`locationBasedList`)
- `serviceKey`: API 인증 키
- `pageNo`: 페이지 번호
- `numOfRows`: 한 페이지당 결과 수
- `mapX`: 경도 (X 좌표)
- `mapY`: 위도 (Y 좌표)
- `radius`: 검색 반경 (미터)
- `listYN`: 목록 출력 여부 (Y/N)
- `arrange`: 정렬 방식 (A: 거리순, B: 제목순, C: 수정일순, D: 등록일순)
- `MobileOS`: 모바일 OS (ETC)
- `MobileApp`: 모바일 앱명 (AppTest)
- `_type`: 응답 타입 (생략 시 XML, `json` 지정 시 JSON)

#### 2.2.2 키워드 기반 검색 API (`searchKeyword`)
- `serviceKey`: API 인증 키
- `keyword`: 검색 키워드 (예: "시장", "카페", "공원")
- `pageNo`: 페이지 번호
- `numOfRows`: 한 페이지당 결과 수
- `arrange`: 정렬 방식 (A: 거리순, B: 제목순, C: 수정일순, D: 등록일순)
- `MobileOS`: 모바일 OS (ETC)
- `MobileApp`: 모바일 앱명 (AppTest)
- `_type`: 응답 타입 (생략 시 XML, `json` 지정 시 JSON)

## 3. 도메인 구조

### 3.1 핵심 엔티티

#### 3.1.1 TourLocation (관광지 정보)
```java
@Entity
public class TourLocation {
    @Id
    private Long id;
    
    private String title;           // 제목
    private Integer category;       // 카테고리 (12, 14, 15, 28, 32, 38, 39)
    private String address;         // 주소
    private String description;     // 설명
    private String imageUrl1;       // 이미지 URL 1
    private String imageUrl2;       // 이미지 URL 2
    private String imageUrl3;       // 이미지 URL 3
    private Double latitude;        // 위도
    private Double longitude;       // 경도
    private Integer distance;       // 거리 (미터)
    private String phoneNumber;     // 전화번호
    private String homepageUrl;     // 홈페이지 URL
    private LocalDateTime createdDate;    // 생성일
    private LocalDateTime updatedDate;    // 수정일
}
```

#### 3.1.2 TourCategory (관광 카테고리)
```java
@Entity
public class TourCategory {
    @Id
    private Long id;
    
    private Integer code;           // 카테고리 코드 (12, 14, 15, 28, 32, 38, 39)
    private String name;            // 카테고리명 (한글)
    private String englishName;     // 카테고리명 (영문)
    private String description;     // 카테고리 설명
    private String iconPath;        // 아이콘 경로
    private Integer sortOrder;      // 정렬 순서
    private Boolean isActive;       // 활성화 여부
}
```

#### 3.1.3 LocationSearchHistory (위치 검색 이력)
```java
@Entity
public class LocationSearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double searchLatitude;  // 검색 위도
    private Double searchLongitude; // 검색 경도
    private Integer searchRadius;   // 검색 반경
    private Integer searchCategory; // 검색 카테고리 (12, 14, 15, 28, 32, 38, 39)
    private LocalDateTime searchDate; // 검색 일시
    private String userAgent;       // 사용자 에이전트
    private String ipAddress;       // IP 주소
}
```

#### 3.1.4 PartnerLocation (제휴 장소 정보)
```java
@Entity
public class PartnerLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_location_id")
    private TourLocation tourLocation;  // 기본 관광지 정보
    
    private String partnerName;         // 제휴사명 (예: 스타벅스, 올리브영)
    private String partnerCode;         // 제휴사 코드
    private String businessNumber;      // 사업자등록번호
    private String contactPerson;       // 담당자명
    private String contactPhone;        // 담당자 연락처
    private String contactEmail;        // 담당자 이메일
    private PartnerStatus status;       // 제휴 상태 (ACTIVE, INACTIVE, SUSPENDED)
    private LocalDateTime contractStartDate; // 계약 시작일
    private LocalDateTime contractEndDate;   // 계약 종료일
    private String specialOffers;       // 특별 혜택 (예: "반려동물 동반 시 음료 10% 할인")
    private String petPolicy;           // 반려동물 정책 (예: "소형견만 입장 가능, 케이지 필수")
    private String petAmenities;        // 반려동물 편의시설 (예: "펫 워터볼, 펫 간식 제공")
    private Boolean hasPetMenu;         // 반려동물 전용 메뉴 여부
    private String petMenuDescription;  // 반려동물 메뉴 설명
    private String operatingHours;      // 운영시간
    private String holidayInfo;         // 휴무일 정보
    private LocalDateTime createdDate;  // 생성일
    private LocalDateTime updatedDate;  // 수정일
}

public enum PartnerStatus {
    ACTIVE, INACTIVE, SUSPENDED
}
```

#### 3.1.5 PartnerLocationImage (제휴 장소 이미지)
```java
@Entity
public class PartnerLocationImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_location_id")
    private PartnerLocation partnerLocation;
    
    private String imageUrl;        // 이미지 URL
    private String imageType;       // 이미지 타입 (MAIN, INTERIOR, PET_AREA, MENU)
    private String description;     // 이미지 설명
    private Integer sortOrder;      // 정렬 순서
    private Boolean isActive;       // 활성화 여부
    private LocalDateTime createdDate; // 생성일
}

public enum ImageType {
    MAIN, INTERIOR, PET_AREA, MENU, PROMOTION
}
```

#### 3.1.6 PartnerLocationReview (제휴 장소 리뷰)
```java
@Entity
public class PartnerLocationReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_location_id")
    private PartnerLocation partnerLocation;
    
    private String reviewerName;    // 리뷰어명
    private String petType;         // 반려동물 종류
    private String petSize;         // 반려동물 크기 (소형, 중형, 대형)
    private Integer rating;         // 평점 (1-5)
    private String title;           // 리뷰 제목
    private String content;         // 리뷰 내용
    private String imageUrls;       // 리뷰 이미지 URLs (JSON)
    private LocalDateTime visitDate; // 방문일
    private LocalDateTime createdDate; // 작성일
    private Boolean isVerified;     // 방문 인증 여부
}
```

### 3.2 카테고리 분류

#### 3.2.1 주요 카테고리
1. **12**: 관광지 (Tourist Attractions)
   - 자연 관광지, 역사 문화 관광지 등

2. **14**: 문화시설 (Cultural Facilities)
   - 박물관, 미술관, 전시관, 공연장 등

3. **15**: 행사/공연/축제 (Events/Performances/Festivals)
   - 문화행사, 공연, 축제, 전시회 등

4. **28**: 레포츠 (Leisure Sports)
   - 스포츠 시설, 체험 프로그램, 웰빙/힐링 등

5. **32**: 숙박 (Accommodation)
   - 호텔, 리조트, 펜션, 민박, 캠핑장 등

6. **38**: 쇼핑 (Shopping)
   - 전통시장, 상점가, 특산품점, 쇼핑몰 등

7. **39**: 음식점 (Restaurants)
   - 한식, 중식, 일식, 양식, 카페/디저트 등

## 4. 서비스 계층

### 4.1 TourLocationService
```java
@Service
public class TourLocationService {
    
    private final TourLocationRepository tourLocationRepository;
    private final TourLocationAdapter tourLocationAdapter;
    
    // 위치 기반 관광지 검색
    public List<TourLocation> searchByLocation(Double latitude, Double longitude, 
                                             Integer radius, Integer category);
    
    // 키워드 기반 관광지 검색
    public List<TourLocation> searchByKeyword(String keyword, Pageable pageable);
    
    // 카테고리별 관광지 조회
    public List<TourLocation> findByCategory(Integer category, Pageable pageable);
    
    // 관광지 상세 정보 조회
    public TourLocation findById(Long id);
    
    // 거리순 정렬
    public List<TourLocation> sortByDistance(List<TourLocation> locations, 
                                           Double centerLat, Double centerLng);
}
```

### 4.2 TourLocationAdapter (어댑터 패턴)
```java
@Component
public class TourLocationAdapter {
    
    private final TourApiClient tourApiClient;
    private final TourLocationMapper tourLocationMapper;
    
    // 위치 기반 외부 API를 통해 관광지 데이터 조회
    public List<TourLocation> fetchTourLocationsByLocation(Double latitude, Double longitude, 
                                                         Integer radius, Integer category, boolean useJson);
    
    // 키워드 기반 외부 API를 통해 관광지 데이터 조회
    public List<TourLocation> fetchTourLocationsByKeyword(String keyword, Pageable pageable, boolean useJson);
    
    // 외부 API 응답을 내부 엔티티로 변환
    private List<TourLocation> convertToTourLocations(List<ExternalTourLocationDto> externalData);
}
```

### 4.3 TourApiClient (외부 API 클라이언트)
```java
@Component
public class TourApiClient {
    
    private final WebClient webClient;
    private final TourApiProperties tourApiProperties;
    
    // 위치 기반 한국관광공사 API 호출
    public ExternalTourApiResponse fetchTourDataByLocation(Double latitude, Double longitude, 
                                                         Integer radius, Integer category, boolean useJson);
    
    // 키워드 기반 한국관광공사 API 호출
    public ExternalTourApiResponse fetchTourDataByKeyword(String keyword, Pageable pageable, boolean useJson);
    
    // XML 응답 파싱
    private ExternalTourApiResponse parseXmlResponse(String xmlResponse);

    // JSON 응답 파싱
    private ExternalTourApiResponse parseJsonResponse(String jsonResponse);
}
```

### 4.4 LocationSearchService
```java
@Service
public class LocationSearchService {
    
    // 검색 이력 저장
    public void saveSearchHistory(LocationSearchHistory history);
    
    // 인기 검색 지역 조회
    public List<LocationSearchHistory> getPopularSearchLocations();
    
    // 사용자별 검색 이력 조회
    public List<LocationSearchHistory> getUserSearchHistory(String userId);
}
```

### 4.5 PartnerLocationService (제휴 장소 관리)
```java
@Service
public class PartnerLocationService {
    
    private final PartnerLocationRepository partnerLocationRepository;
    private final TourLocationRepository tourLocationRepository;
    
    // 제휴 장소 등록
    public PartnerLocation registerPartnerLocation(PartnerLocationRegistrationDto registrationDto);
    
    // 제휴 장소 조회
    public PartnerLocation findById(Long partnerLocationId);
    
    // 카테고리별 제휴 장소 조회
    public List<PartnerLocation> findByCategory(Integer category, Pageable pageable);
    
    // 위치 기반 제휴 장소 검색
    public List<PartnerLocation> searchByLocation(Double latitude, Double longitude, Integer radius);
    
    // 제휴 장소 상태 변경
    public PartnerLocation updatePartnerStatus(Long partnerLocationId, PartnerStatus status);
    
    // 제휴 계약 만료 체크
    public List<PartnerLocation> findExpiringContracts(int daysBeforeExpiry);
    
    // 반려동물 친화적 장소 검색
    public List<PartnerLocation> findPetFriendlyLocations(String petSize, String petType);
}
```

### 4.6 PartnerLocationImageService (제휴 장소 이미지 관리)
```java
@Service
public class PartnerLocationImageService {
    
    // 제휴 장소 이미지 등록
    public PartnerLocationImage addImage(Long partnerLocationId, PartnerLocationImageDto imageDto);
    
    // 제휴 장소 이미지 목록 조회
    public List<PartnerLocationImage> getImagesByLocation(Long partnerLocationId);
    
    // 이미지 타입별 조회
    public List<PartnerLocationImage> getImagesByType(Long partnerLocationId, ImageType imageType);
    
    // 이미지 순서 변경
    public void updateImageOrder(Long imageId, Integer newOrder);
    
    // 이미지 비활성화
    public void deactivateImage(Long imageId);
}
```

### 4.7 PartnerLocationReviewService (제휴 장소 리뷰 관리)
```java
@Service
public class PartnerLocationReviewService {
    
    // 리뷰 등록
    public PartnerLocationReview addReview(Long partnerLocationId, PartnerLocationReviewDto reviewDto);
    
    // 제휴 장소 리뷰 목록 조회
    public Page<PartnerLocationReview> getReviewsByLocation(Long partnerLocationId, Pageable pageable);
    
    // 평점별 리뷰 조회
    public List<PartnerLocationReview> getReviewsByRating(Long partnerLocationId, Integer rating);
    
    // 반려동물 크기별 리뷰 조회
    public List<PartnerLocationReview> getReviewsByPetSize(Long partnerLocationId, String petSize);
    
    // 방문 인증된 리뷰만 조회
    public List<PartnerLocationReview> getVerifiedReviews(Long partnerLocationId);
    
    // 리뷰 평점 통계
    public ReviewStatistics getReviewStatistics(Long partnerLocationId);
}
```

### 4.5 어댑터 패턴 적용 이유

#### 4.5.1 장점
- **외부 API 의존성 분리**: 외부 API 변경 시 어댑터만 수정하면 됨
- **테스트 용이성**: 외부 API를 Mock으로 대체하여 단위 테스트 가능
- **확장성**: 다른 관광 정보 제공업체 API 추가 시 쉽게 확장 가능
- **일관성**: 내부 서비스는 항상 동일한 인터페이스 사용

#### 4.5.2 DTO 구조
```java
// 외부 API 응답 DTO
public class ExternalTourApiResponse {
    private String resultCode;
    private String resultMsg;
    private List<ExternalTourLocationDto> items;
}

public class ExternalTourLocationDto {
    private String title;           // 제목
    private String addr1;           // 주소1
    private String addr2;           // 주소2
    private String tel;             // 전화번호
    private String homepage;        // 홈페이지
    private String firstimage;      // 대표 이미지
    private String firstimage2;     // 이미지2
    private String mapx;            // 경도
    private String mapy;            // 위도
    private String contenttypeid;   // 콘텐츠 타입 ID
    private String contentid;       // 콘텐츠 ID
}

// 제휴 장소 관련 DTO
public class PartnerLocationRegistrationDto {
    private Long tourLocationId;    // 기본 관광지 ID
    private String partnerName;     // 제휴사명
    private String partnerCode;     // 제휴사 코드
    private String businessNumber;  // 사업자등록번호
    private String contactPerson;   // 담당자명
    private String contactPhone;    // 담당자 연락처
    private String contactEmail;    // 담당자 이메일
    private LocalDateTime contractStartDate; // 계약 시작일
    private LocalDateTime contractEndDate;   // 계약 종료일
    private String specialOffers;   // 특별 혜택
    private String petPolicy;       // 반려동물 정책
    private String petAmenities;    // 반려동물 편의시설
    private Boolean hasPetMenu;     // 반려동물 전용 메뉴 여부
    private String petMenuDescription; // 반려동물 메뉴 설명
    private String operatingHours;  // 운영시간
    private String holidayInfo;     // 휴무일 정보
}

public class PartnerLocationImageDto {
    private String imageUrl;        // 이미지 URL
    private ImageType imageType;    // 이미지 타입
    private String description;     // 이미지 설명
    private Integer sortOrder;      // 정렬 순서
}

public class PartnerLocationReviewDto {
    private String reviewerName;    // 리뷰어명
    private String petType;         // 반려동물 종류
    private String petSize;         // 반려동물 크기
    private Integer rating;         // 평점 (1-5)
    private String title;           // 리뷰 제목
    private String content;         // 리뷰 내용
    private List<String> imageUrls; // 리뷰 이미지 URLs
    private LocalDate visitDate;    // 방문일
}

public class ReviewStatistics {
    private Long partnerLocationId; // 제휴 장소 ID
    private String partnerName;     // 제휴사명
    private Double averageRating;   // 평균 평점
    private Integer totalReviews;   // 총 리뷰 수
    private Map<Integer, Integer> ratingDistribution; // 평점별 분포
    private Integer verifiedReviews; // 방문 인증된 리뷰 수
    private List<String> petTypes;  // 방문한 반려동물 종류
    private List<String> petSizes;  // 방문한 반려동물 크기
}

// 내부 엔티티와 외부 DTO 간 매핑
@Component
public class TourLocationMapper {
    
    public TourLocation toTourLocation(ExternalTourLocationDto externalDto);
    
    public List<TourLocation> toTourLocationList(List<ExternalTourLocationDto> externalDtoList);
}
```

## 5. 컨트롤러 계층

### 5.1 MapController
```java
@RestController
@RequestMapping("/api/map")
public class MapController {
    
    // 위치 기반 관광지 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TourLocation>>> searchLocations(
        @RequestParam Double latitude,
        @RequestParam Double longitude,
        @RequestParam(defaultValue = "1000") Integer radius,
        @RequestParam(required = false) Integer category,
        @RequestParam(defaultValue = "true") boolean json
    );
    
    // 키워드 기반 관광지 검색
    @GetMapping("/search/keyword")
    public ResponseEntity<ApiResponse<Page<TourLocation>>> searchByKeyword(
        @RequestParam String keyword,
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(defaultValue = "true") boolean json
    );
    
    // 카테고리별 관광지 조회
    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<ApiResponse<Page<TourLocation>>> getLocationsByCategory(
        @PathVariable Integer categoryCode,
        @PageableDefault(size = 20) Pageable pageable
    );
    
    // 관광지 상세 정보
    @GetMapping("/location/{id}")
    public ResponseEntity<ApiResponse<TourLocation>> getLocationDetail(@PathVariable Long id);
    
    // 인기 검색 지역
    @GetMapping("/popular-locations")
    public ResponseEntity<ApiResponse<List<LocationSearchHistory>>> getPopularLocations();
}
```

제휴 관련은 나중에 구현
<!-- ### 5.2 PartnerLocationController (제휴 장소 관리)
```java
@RestController
@RequestMapping("/api/v1/partner-locations")
public class PartnerLocationController {
    
    // 제휴 장소 등록
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PartnerLocation>> registerPartnerLocation(@RequestBody PartnerLocationRegistrationDto registrationDto);
    
    // 제휴 장소 정보 조회
    @GetMapping("/{partnerLocationId}")
    public ResponseEntity<ApiResponse<PartnerLocation>> getPartnerLocation(@PathVariable Long partnerLocationId);
    
    // 카테고리별 제휴 장소 조회
    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<ApiResponse<Page<PartnerLocation>>> getPartnerLocationsByCategory(
        @PathVariable Integer categoryCode,
        @PageableDefault(size = 20) Pageable pageable
    );
    
    // 위치 기반 제휴 장소 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PartnerLocation>>> searchPartnerLocations(
        @RequestParam Double latitude,
        @RequestParam Double longitude,
        @RequestParam(defaultValue = "1000") Integer radius
    );
    
    // 반려동물 친화적 장소 검색
    @GetMapping("/pet-friendly")
    public ResponseEntity<ApiResponse<List<PartnerLocation>>> findPetFriendlyLocations(
        @RequestParam(required = false) String petSize,
        @RequestParam(required = false) String petType
    );
    
    // 제휴 장소 상태 변경
    @PutMapping("/{partnerLocationId}/status")
    public ResponseEntity<ApiResponse<PartnerLocation>> updatePartnerStatus(
        @PathVariable Long partnerLocationId, 
        @RequestParam PartnerStatus status
    );
}
```

### 5.3 PartnerLocationImageController (제휴 장소 이미지 관리)
```java
@RestController
@RequestMapping("/api/v1/partner-locations/{partnerLocationId}/images")
public class PartnerLocationImageController {
    
    // 이미지 등록
    @PostMapping
    public ResponseEntity<ApiResponse<PartnerLocationImage>> addImage(
        @PathVariable Long partnerLocationId,
        @RequestBody PartnerLocationImageDto imageDto
    );
    
    // 이미지 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<PartnerLocationImage>>> getImages(@PathVariable Long partnerLocationId);
    
    // 이미지 타입별 조회
    @GetMapping("/type/{imageType}")
    public ResponseEntity<ApiResponse<List<PartnerLocationImage>>> getImagesByType(
        @PathVariable Long partnerLocationId,
        @PathVariable ImageType imageType
    );
    
    // 이미지 순서 변경
    @PutMapping("/{imageId}/order")
    public ResponseEntity<ApiResponse<Void>> updateImageOrder(
        @PathVariable Long partnerLocationId,
        @PathVariable Long imageId,
        @RequestParam Integer newOrder
    );
}
```

### 5.4 PartnerLocationReviewController (제휴 장소 리뷰 관리)
```java
@RestController
@RequestMapping("/api/v1/partner-locations/{partnerLocationId}/reviews")
public class PartnerLocationReviewController {
    
    // 리뷰 등록
    @PostMapping
    public ResponseEntity<ApiResponse<PartnerLocationReview>> addReview(
        @PathVariable Long partnerLocationId,
        @RequestBody PartnerLocationReviewDto reviewDto
    );
    
    // 리뷰 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartnerLocationReview>>> getReviews(
        @PathVariable Long partnerLocationId,
        @PageableDefault(size = 20) Pageable pageable
    );
    
    // 평점별 리뷰 조회
    @GetMapping("/rating/{rating}")
    public ResponseEntity<ApiResponse<List<PartnerLocationReview>>> getReviewsByRating(
        @PathVariable Long partnerLocationId,
        @PathVariable Integer rating
    );
    
    // 방문 인증된 리뷰만 조회
    @GetMapping("/verified")
    public ResponseEntity<ApiResponse<List<PartnerLocationReview>>> getVerifiedReviews(@PathVariable Long partnerLocationId);
    
    // 리뷰 통계 조회
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ReviewStatistics>> getReviewStatistics(@PathVariable Long partnerLocationId);
} -->
```

## 6. 데이터 파싱 전략

### 6.1 XML/JSON 파싱
- XML: **JAXB (Java Architecture for XML Binding)** 사용
- JSON: **Jackson** 또는 **WebClient bodyToMono(ExternalTourApiResponse.class)** 사용
- 동일 DTO(`ExternalTourApiResponse`, `ExternalTourLocationDto`)로 역직렬화
- 파싱 실패 시 어댑터 계층에서 일관된 예외 변환 및 로깅

### 6.2 데이터 정규화
- API 응답 데이터를 내부 엔티티 구조에 맞게 변환
- 좌표 정보 검증 및 정규화
- 이미지 URL 유효성 검사

### 6.3 캐싱 전략
- Redis를 활용한 API 응답 캐싱
- TTL 설정으로 데이터 신선도 유지
- 카테고리별 캐시 키 분리

## 7. 예외 처리

### 7.1 커스텀 예외
```java
public class TourApiException extends RuntimeException {
    private String errorCode;
    private String apiResponse;
    private String externalApiUrl;
}

public class LocationNotFoundException extends RuntimeException {
    private Double latitude;
    private Double longitude;
    private Integer radius;
}

public class ExternalApiException extends RuntimeException {
    private String apiName;
    private String errorCode;
    private String errorMessage;
}

// 제휴 앱 관련 예외
public class UnauthorizedException extends RuntimeException {
    private String apiKey;
    private String reason;
}

public class RateLimitExceededException extends RuntimeException {
    private Long partnerId;
    private String partnerName;
    private Integer currentUsage;
    private Integer limit;
}

public class PartnerNotFoundException extends RuntimeException {
    private String apiKey;
    private String partnerCode;
}
```

### 7.2 어댑터 계층 예외 처리
```java
@Component
public class TourLocationAdapter {
    
    public List<TourLocation> fetchTourLocationsByLocation(Double latitude, Double longitude, 
                                                         Integer radius, Integer category, boolean useJson) {
        try {
            ExternalTourApiResponse response = tourApiClient.fetchTourDataByLocation(latitude, longitude, radius, category, useJson);
            return convertToTourLocations(response.getItems());
        } catch (ExternalApiException e) {
            log.error("외부 API 호출 실패: {}", e.getMessage());
            throw new TourApiException("관광지 정보 조회에 실패했습니다.", e);
        }
    }
    
    public List<TourLocation> fetchTourLocationsByKeyword(String keyword, Pageable pageable, boolean useJson) {
        try {
            ExternalTourApiResponse response = tourApiClient.fetchTourDataByKeyword(keyword, pageable, useJson);
            return convertToTourLocations(response.getItems());
        } catch (ExternalApiException e) {
            log.error("외부 API 호출 실패: {}", e.getMessage());
            throw new TourApiException("키워드 기반 관광지 정보 조회에 실패했습니다.", e);
        }
    }
}
```

### 7.2 예외 처리 전략
- API 호출 실패 시 재시도 로직
- 네트워크 타임아웃 설정
- 사용자 친화적 에러 메시지 제공

## 8. 성능 최적화

### 8.1 데이터베이스 최적화
- 위도/경도 인덱스 생성
- 카테고리별 파티셔닝 고려
- 쿼리 최적화

### 8.2 API 최적화
- 비동기 처리로 응답 시간 단축
- 배치 처리로 대량 데이터 처리
- 연결 풀링으로 리소스 효율성 증대

## 9. 보안 고려사항

### 9.1 API 키 관리
- 환경 변수를 통한 API 키 관리
- API 키 로테이션 정책
- 요청 제한 및 모니터링

### 9.2 제휴 장소 관리 및 권한 관리
```java
@Component
public class PartnerLocationAuthInterceptor implements HandlerInterceptor {
    
    private final PartnerLocationService partnerLocationService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 제휴 장소 관리 API에 대한 권한 체크
        if (isPartnerLocationManagementEndpoint(request)) {
            String businessNumber = request.getHeader("X-Business-Number");
            String partnerCode = request.getHeader("X-Partner-Code");
            
            if (businessNumber == null || partnerCode == null) {
                throw new UnauthorizedException("사업자등록번호와 제휴사 코드가 필요합니다.");
            }
            
            // 제휴 장소 인증 및 권한 확인
            PartnerLocation partnerLocation = partnerLocationService.findByBusinessNumberAndCode(businessNumber, partnerCode);
            if (partnerLocation == null || partnerLocation.getStatus() != PartnerStatus.ACTIVE) {
                throw new UnauthorizedException("유효하지 않은 제휴 정보입니다.");
            }
            
            // 요청 정보를 ThreadLocal에 저장
            PartnerLocationContext.setCurrentPartnerLocation(partnerLocation);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // ThreadLocal 정리
        PartnerLocationContext.clear();
    }
    
    private boolean isPartnerLocationManagementEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/partner-locations") && 
               (request.getMethod().equals("POST") || request.getMethod().equals("PUT") || request.getMethod().equals("DELETE"));
    }
}

// 제휴 장소 컨텍스트 관리
public class PartnerLocationContext {
    private static final ThreadLocal<PartnerLocation> currentPartnerLocation = new ThreadLocal<>();
    
    public static void setCurrentPartnerLocation(PartnerLocation partnerLocation) {
        currentPartnerLocation.set(partnerLocation);
    }
    
    public static PartnerLocation getCurrentPartnerLocation() {
        return currentPartnerLocation.get();
    }
    
    public static void clear() {
        currentPartnerLocation.remove();
    }
}
```

### 9.2 어댑터 설정 클래스
```java
@Configuration
@ConfigurationProperties(prefix = "tour.api")
@Data
public class TourApiProperties {
    private String baseUrl = "http://apis.data.go.kr/B551011/KorPetTourService";
    private String serviceKey;
    private String locationBasedListPath = "/locationBasedList";
    private String searchKeywordPath = "/searchKeyword";
    private int connectionTimeout = 5000;
    private int readTimeout = 10000;
    private int maxRetries = 3;
}

@Configuration
public class TourApiConfig {
    
    @Bean
    public WebClient tourApiWebClient(TourApiProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getBaseUrl())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE + "," + MediaType.APPLICATION_XML_VALUE)
            .filter(ExchangeFilterFunction.ofRequestProcessor(
                clientRequest -> {
                    log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
                    return Mono.just(clientRequest);
                }
            ))
            .build();
    }
}
```

### 9.2 입력 검증
- 좌표값 범위 검증
- SQL 인젝션 방지
- XSS 공격 방지

## 10. 테스트 전략

### 10.1 단위 테스트
- 서비스 계층 비즈니스 로직 테스트
- 어댑터 계층 변환 로직 테스트
- XML 파싱 로직 테스트
- 예외 처리 테스트

### 10.2 어댑터 패턴 테스트
```java
@ExtendWith(MockitoExtension.class)
class TourLocationAdapterTest {
    
    @Mock
    private TourApiClient tourApiClient;
    
    @Mock
    private TourLocationMapper tourLocationMapper;
    
    @InjectMocks
    private TourLocationAdapter tourLocationAdapter;
    
    @Test
    void 외부_API_응답을_내부_엔티티로_변환_성공() {
        // given
        ExternalTourApiResponse mockResponse = createMockResponse();
        List<TourLocation> expectedLocations = createExpectedLocations();
        
        when(tourApiClient.fetchTourDataByLocation(any(), any(), any(), any(), any()))
            .thenReturn(mockResponse);
        when(tourLocationMapper.toTourLocationList(any()))
            .thenReturn(expectedLocations);
        
        // when
        List<TourLocation> result = tourLocationAdapter.fetchTourLocationsByLocation(37.5, 127.0, 1000, 12, true);
        
        // then
        assertThat(result).isEqualTo(expectedLocations);
        verify(tourApiClient).fetchTourDataByLocation(37.5, 127.0, 1000, 12, true);
    }
}
```

### 10.2 통합 테스트
- API 엔드포인트 테스트
- 데이터베이스 연동 테스트
- 외부 API 연동 테스트

### 10.3 성능 테스트
- 대용량 데이터 처리 테스트
- 동시 사용자 처리 테스트
- 응답 시간 측정

## 11. 모니터링 및 로깅

### 11.1 로깅
- API 호출 성공/실패 로깅
- 응답 시간 측정 및 로깅
- 에러 상세 정보 로깅

### 11.2 모니터링
- API 응답 시간 모니터링
- 에러율 모니터링
- 데이터베이스 성능 모니터링

## 12. 향후 확장 계획

### 12.1 기능 확장
- 사용자 리뷰 및 평점 시스템
- 관광지 즐겨찾기 기능
- 개인화된 추천 시스템

### 12.2 기술 확장
- GraphQL API 지원
- 실시간 위치 업데이트
- AI 기반 관광지 추천

---

*이 문서는 Map 도메인의 초기 설계를 위한 가이드라인입니다. 개발 과정에서 요구사항 변경에 따라 수정될 수 있습니다.*
