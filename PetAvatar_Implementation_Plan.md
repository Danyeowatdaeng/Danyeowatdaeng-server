# 🐾 PetAvatar 구현 계획서 (AI 확장 고려)

## 📋 개요
회원가입 플로우에서 약관 동의 후 PetAvatar 선택 단계를 구현하여 사용자가 자신만의 펫 캐릭터를 선택할 수 있도록 합니다. 향후 AI 이미지 변환 기능을 고려한 확장 가능한 설계를 적용합니다.

## 🎯 구현 목표
1. **기본 PetAvatar 제공**: 강아지, 고양이 등 펫 타입별 기본 아바타 제공
2. **PetAvatar 선택 API**: 회원가입 완료 전 필수 단계
3. **확장성 확보**: AI 이미지 변환 기능 대비 확장 가능한 구조
4. **커스텀 PetAvatar 지원**: 사용자 업로드 이미지 기반 AI 변환

## 🏗️ 구현 단계

### **Phase 1: 기본 구조 완성 (AI 확장 고려)**

#### **1.1 PetType Enum 생성**
- **파일**: `src/main/java/com/tourapi/tourapi/petAvatar/enums/PetType.java`
- **내용**: DOG, CAT, BIRD, FISH 등 펫 타입 정의

#### **1.2 PetAvatar 엔티티 완성 (확장 고려)**
- **파일**: `src/main/java/com/tourapi/tourapi/petAvatar/PetAvatar.java`
- **ERD 기반 필드 + 확장 필드**:
  - `id`: bigint (Primary Key)
  - `pet`: PetType (Enum, Not Null)
  - `code`: varchar(40) (Not Null)
  - `displayName`: varchar(40) (Not Null)
  - `imageUrl`: text
  - `isActive`: boolean (Not Null)
  - `isCustom`: boolean (기본값: false) - **AI 확장용**
  - `originalImageUrl`: text - **AI 확장용 (원본 이미지)**
  - `style`: varchar(20) - **AI 확장용 (변환 스타일)**
  - `memberId`: bigint - **AI 확장용 (소유자)**
  - `createdAt`: LocalDateTime (BaseEntity 상속)

#### **1.3 PetAvatarStyle Enum 생성 (AI 확장용)**
- **파일**: `src/main/java/com/tourapi/tourapi/petAvatar/enums/PetAvatarStyle.java`
- **내용**: 
  - `DEFAULT`: 기본 스타일
  - `ANIME`: 애니메이션 스타일
  - `CARTOON`: 카툰 스타일
  - `PIXEL`: 픽셀 아트 스타일
  - `WATERCOLOR`: 수채화 스타일
  - `DIGITAL_ART`: 디지털 아트 스타일

#### **1.4 PetAvatar Repository 생성**
- **파일**: `src/main/java/com/tourapi/tourapi/petAvatar/repository/PetAvatarRepository.java`
- **기능**:
  - 활성화된 PetAvatar 목록 조회
  - 펫 타입별 필터링
  - ID로 PetAvatar 조회
  - **AI 확장용**:
    - 사용자별 커스텀 PetAvatar 조회
    - 스타일별 PetAvatar 조회
    - 기본 PetAvatar만 조회

#### **1.5 PetAvatar Service 생성**
- **파일**: `src/main/java/com/tourapi/tourapi/petAvatar/service/PetAvatarService.java`
- **파일**: `src/main/java/com/tourapi/tourapi/petAvatar/service/PetAvatarServiceImpl.java`
- **기능**:
  - PetAvatar 목록 조회
  - PetAvatar 선택 처리
  - Member에 PetAvatar 연결
  - **AI 확장용**:
    - 커스텀 PetAvatar 생성
    - AI 변환 요청 처리
    - 변환 결과 저장

### **Phase 2: API 구현**

#### **2.1 PetAvatar Controller 생성**
- **파일**: `src/main/java/com/tourapi/tourapi/web/controller/petAvatar/PetAvatarController.java`
- **API 엔드포인트**:
  - `GET /api/pet-avatars`: PetAvatar 목록 조회 (기본 + 커스텀)
  - `GET /api/pet-avatars/default`: 기본 PetAvatar만 조회
  - `GET /api/pet-avatars/{petType}`: 특정 타입 PetAvatar 조회
  - `GET /api/pet-avatars/custom`: 사용자 커스텀 PetAvatar 조회
  - `POST /api/members/pet-avatar`: PetAvatar 선택
  - **AI 확장용**:
    - `POST /api/pet-avatars/upload`: 이미지 업로드
    - `POST /api/pet-avatars/convert`: AI 변환 요청
    - `GET /api/pet-avatars/convert/{requestId}`: 변환 상태 조회

#### **2.2 DTO 클래스 생성**
- **PetAvatarResponse**: PetAvatar 정보 응답용
- **PetAvatarSelectionRequest**: PetAvatar 선택 요청용
- **PetAvatarListResponse**: PetAvatar 목록 응답용
- **AI 확장용**:
  - **PetAvatarUploadRequest**: 이미지 업로드 요청
  - **PetAvatarConvertRequest**: AI 변환 요청
  - **PetAvatarConvertResponse**: AI 변환 응답
  - **PetAvatarConvertStatusResponse**: 변환 상태 응답

### **Phase 3: 회원가입 플로우 연동**

#### **3.1 Member Service 확장**
- **파일**: `src/main/java/com/tourapi/tourapi/member/service/MemberService.java`
- **파일**: `src/main/java/com/tourapi/tourapi/member/service/MemberServiceImpl.java`
- **기능**:
  - PetAvatar 선택 처리
  - 회원가입 완료 상태 확인
  - **AI 확장용**:
    - 커스텀 PetAvatar 소유권 확인
    - PetAvatar 변경 이력 관리

#### **3.2 회원가입 완료 API 생성**
- **파일**: `src/main/java/com/tourapi/tourapi/web/controller/member/MemberController.java`
- **API**: `POST /api/members/complete-signup`
- **기능**: PetAvatar 선택 후 최종 회원가입 완료

### **Phase 4: 에러 처리 및 검증**

#### **4.1 PetAvatar 관련 에러 상태 추가**
- **파일**: `src/main/java/com/tourapi/tourapi/common/exception/petAvatar/status/PetAvatarErrorStatus.java`
- **에러 코드**:
  - `PET_AVATAR_NOT_FOUND`: PetAvatar를 찾을 수 없음
  - `PET_AVATAR_INACTIVE`: 비활성화된 PetAvatar
  - `PET_AVATAR_ALREADY_SELECTED`: 이미 PetAvatar 선택됨
  - **AI 확장용**:
    - `PET_AVATAR_UPLOAD_FAILED`: 이미지 업로드 실패
    - `PET_AVATAR_CONVERT_FAILED`: AI 변환 실패
    - `PET_AVATAR_CONVERT_TIMEOUT`: AI 변환 시간 초과
    - `PET_AVATAR_ACCESS_DENIED`: 커스텀 PetAvatar 접근 권한 없음

#### **4.2 성공 상태 추가**
- **파일**: `src/main/java/com/tourapi/tourapi/common/exception/petAvatar/status/PetAvatarSuccessStatus.java`
- **성공 코드**:
  - `PET_AVATAR_LIST_FOUND`: PetAvatar 목록 조회 성공
  - `PET_AVATAR_SELECTED`: PetAvatar 선택 성공
  - **AI 확장용**:
    - `PET_AVATAR_UPLOADED`: 이미지 업로드 성공
    - `PET_AVATAR_CONVERT_REQUESTED`: AI 변환 요청 성공
    - `PET_AVATAR_CONVERTED`: AI 변환 완료

## 📁 파일 구조 (AI 확장 고려)

```
src/main/java/com/tourapi/tourapi/
├── petAvatar/
│   ├── enums/
│   │   ├── PetType.java
│   │   └── PetAvatarStyle.java
│   ├── repository/
│   │   └── PetAvatarRepository.java
│   ├── service/
│   │   ├── PetAvatarService.java
│   │   ├── PetAvatarServiceImpl.java
│   │   └── PetAvatarAIService.java (AI 확장용)
│   ├── dto/
│   │   ├── PetAvatarResponse.java
│   │   ├── PetAvatarSelectionRequest.java
│   │   ├── PetAvatarListResponse.java
│   │   ├── PetAvatarUploadRequest.java (AI 확장용)
│   │   ├── PetAvatarConvertRequest.java (AI 확장용)
│   │   ├── PetAvatarConvertResponse.java (AI 확장용)
│   │   └── PetAvatarConvertStatusResponse.java (AI 확장용)
│   └── PetAvatar.java
├── web/controller/
│   ├── petAvatar/
│   │   └── PetAvatarController.java
│   └── member/
│       └── MemberController.java
└── common/exception/petAvatar/
    └── status/
        ├── PetAvatarErrorStatus.java
        └── PetAvatarSuccessStatus.java
```

## 🗄️ 데이터베이스 설계 (AI 확장 고려)

### **PetAvatar 테이블 스키마**
```sql
CREATE TABLE pet_avatar (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet VARCHAR(20) NOT NULL, -- PetType enum
    code VARCHAR(40) NOT NULL,
    display_name VARCHAR(40) NOT NULL,
    image_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_custom BOOLEAN NOT NULL DEFAULT FALSE, -- AI 확장용
    original_image_url TEXT, -- AI 확장용
    style VARCHAR(20), -- AI 확장용 (PetAvatarStyle enum)
    member_id BIGINT, -- AI 확장용 (소유자)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_pet_avatar_pet (pet),
    INDEX idx_pet_avatar_active (is_active),
    INDEX idx_pet_avatar_custom (is_custom),
    INDEX idx_pet_avatar_member (member_id),
    INDEX idx_pet_avatar_style (style)
);
```

### **PetAvatarConvertRequest 테이블 (AI 확장용)**
```sql
CREATE TABLE pet_avatar_convert_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    original_image_url TEXT NOT NULL,
    requested_style VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    result_image_url TEXT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_convert_request_member (member_id),
    INDEX idx_convert_request_status (status)
);
```

## 🔄 API 플로우 (AI 확장 고려)

### **기본 회원가입 플로우**
1. **소셜 로그인** → JWT 토큰 발급
2. **약관 동의** → `POST /api/terms/agree-terms`
3. **PetAvatar 선택** → `POST /api/members/pet-avatar`
4. **회원가입 완료** → `POST /api/members/complete-signup`

### **AI 커스텀 PetAvatar 플로우**
1. **이미지 업로드** → `POST /api/pet-avatars/upload`
2. **AI 변환 요청** → `POST /api/pet-avatars/convert`
3. **변환 상태 확인** → `GET /api/pet-avatars/convert/{requestId}`
4. **변환된 PetAvatar 선택** → `POST /api/members/pet-avatar`

## 🧪 테스트 계획

### **단위 테스트**
- PetAvatarService 테스트
- PetAvatarRepository 테스트
- DTO 변환 테스트
- **AI 확장용**:
  - PetAvatarAIService 테스트
  - 이미지 업로드 테스트
  - AI 변환 요청 테스트

### **통합 테스트**
- PetAvatar API 테스트
- 회원가입 플로우 테스트
- 에러 처리 테스트
- **AI 확장용**:
  - 이미지 업로드 플로우 테스트
  - AI 변환 플로우 테스트

## 🚀 향후 확장 계획

### **Phase 5: AI 이미지 변환 기능**
- **PetAvatarAIService 구현**
- **이미지 업로드 API**
- **AI 모델 연동** (Stable Diffusion, DALL-E 등)
- **비동기 처리** (Redis Queue 활용)
- **변환 결과 저장 및 관리**

### **Phase 6: 고급 AI 기능**
- **다양한 스타일 옵션**
- **배치 처리**
- **품질 최적화**
- **사용량 제한 및 과금**

### **Phase 7: PetAvatar 관리**
- **관리자용 PetAvatar CRUD**
- **PetAvatar 활성화/비활성화**
- **사용 통계**
- **AI 변환 통계**

## ⏱️ 예상 개발 일정

- **Phase 1**: 2-3일 (기본 구조 + AI 확장 고려)
- **Phase 2**: 2-3일 (API 구현 + AI 확장용 API)
- **Phase 3**: 1일 (회원가입 연동)
- **Phase 4**: 1일 (에러 처리)
- **Phase 5**: 3-5일 (AI 기능 구현)
- **총 예상**: 9-13일

---

## 🤖 AI 이미지 변환 기능 상세 설계

### **기술 스택**
- **AI 모델**: Stable Diffusion, DALL-E API
- **비동기 처리**: Redis + Spring Boot Async
- **이미지 저장**: AWS S3 또는 로컬 파일 시스템
- **큐 관리**: Redis Queue 또는 RabbitMQ

### **구현 아키텍처**
```
사용자 업로드 → 이미지 전처리 → 큐 등록 → AI 모델 호출 → 스타일 변환 → 결과 저장 → 알림
```

### **API 설계**
```java
// 이미지 업로드
POST /api/pet-avatars/upload
{
    "image": MultipartFile,
    "petType": "DOG"
}

// AI 변환 요청
POST /api/pet-avatars/convert
{
    "imageUrl": "string",
    "style": "ANIME",
    "petType": "DOG"
}

// 변환 상태 조회
GET /api/pet-avatars/convert/{requestId}
{
    "status": "COMPLETED",
    "resultImageUrl": "string",
    "progress": 100
}
```

이 설계로 AI 기능까지 고려한 확장 가능한 PetAvatar 시스템을 구축할 수 있습니다!
