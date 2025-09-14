## PetAvatar 운영 S3 연동 계획

### 목표
- 입력(원본) 이미지와 결과(아바타) 이미지를 S3에 안전하게 저장/배포하며, 사전서명 URL을 통해 클라이언트 업로드 및 다운로드를 제어한다.

### 버킷 설계
- 버킷: `pet-avatar-prod-<env>` (예: `pet-avatar-prod-apne2`)
- 경로 구조
  - `input/{yyyy}/{MM}/{dd}/{uuid}.ext` (원본)
  - `result/{yyyy}/{MM}/{dd}/{uuid}/avatar.png` (결과)
  - `thumb/{yyyy}/{MM}/{dd}/{uuid}/avatar_256.webp` (썸네일/파생)
- 퍼블릭 접근 차단, S3 Block Public Access ON
- 정적 파일 배포는 CloudFront(CDN)로 처리, S3는 Origin (OAC 사용)

### 보안/권한
- IAM 역할 최소권한(원본/결과 경로에 PutObject/GetObject 제한)
- 서버 측만 List 권한 보유, 클라이언트는 절대 List 불가
- 사전서명 URL 만료(예: 5~10분) 및 Content-MD5/Content-Type 제한
- 업로드 정책: 최대 크기 제한(예: 10MB) 및 MIME 화이트리스트

### 사전서명 업로드 플로우
1) 클라이언트: 업로드 요청 → 서버: `POST /api/v1/storage/presign-upload`
2) 서버: S3 PutObject 사전서명 URL 생성 후 반환(키 경로, 만료, 필요 헤더)
3) 클라이언트: 사전서명 URL로 직접 업로드(PUT)
4) 완료 후 서버에 업로드 경로(URL) 통보 → 생성 API 호출(`/api/v1/pet-avatars/mvp`)

요청/응답 예시
```
POST /api/v1/storage/presign-upload
{"ext":".png","mime":"image/png"}

=> 200 {"uploadUrl":"https://s3...","objectKey":"input/2025/09/11/uuid.png"}
```

### 결과 저장 플로우
1) 생성 완료 후 서버가 결과 바이트를 S3 `result/...` 키로 업로드
2) 썸네일/웹 최적화(WebP, AVIF) 생성 → `thumb/...` 업로드
3) 응답엔 CDN URL(CloudFront 도메인) 반환

### 선택/저장 플로우 (사용자가 아바타 하나를 선택)
1) 클라이언트: 생성된 이미지들 중 선택된 항목의 `resultKey`(또는 CDN URL)와 메타를 서버에 통보
2) 서버: `PetAvatar` 테이블에 영구 저장하고, 소유자/버전/대표 여부를 반영
3) 필요 시 이전 대표 아바타를 비활성화하고 새 대표로 스위칭(트랜잭션)

요청/응답 예시
```
POST /api/v1/pet-avatars/select
{
  "memberId": 123,
  "resultKey": "result/2025/09/11/2b6c.../avatar.png",
  "thumbKey": "thumb/2025/09/11/2b6c.../avatar_256.webp",
  "prompt": "귀여운 시바견 스타일",
  "model": "gemini-2.5-flash-image-preview",
  "setAsPrimary": true
}

=> 200 {
  "isSuccess": true,
  "code": "PET2012",
  "message": "AI 변환이 성공적으로 완료되었습니다.",
  "data": {
    "petAvatarId": 987,
    "cdnUrl": "https://cdn.example.com/result/2025/09/11/2b6c.../avatar.png",
    "thumbCdnUrl": "https://cdn.example.com/thumb/2025/09/11/2b6c.../avatar_256.webp",
    "primary": true
  }
}
```

선택 저장 API(신규)
```
POST /api/pet-avatars/select
Content-Type: application/json

// 모드 1: BYTES (클라이언트가 이미지 바이트를 보냄)
{
  "mode": "BYTES",
  "imageBase64": "<base64>",
  "mime": "image/png",
  "prompt": "귀여운 시바 스타일",
  "model": "gemini-2.5-flash-image-preview",
  "setAsPrimary": true,
  "petType": "DOG",
  "style": "CUTE"
}

// 모드 2: KEY (이미 S3에 있는 키를 서버가 복사/정규화)
{
  "mode": "KEY",
  "sourceKey": "temp/2025/09/11/uuid.png",
  "prompt": "귀여운 시바 스타일",
  "model": "gemini-2.5-flash-image-preview",
  "setAsPrimary": true,
  "petType": "DOG",
  "style": "CUTE"
}

=> 200 {
  "isSuccess": true,
  "code": "PET2012",
  "message": "AI 변환이 성공적으로 완료되었습니다.",
  "data": {
    "petAvatarId": 1234,
    "cdnUrl": "https://cdn.example.com/result/2025/09/11/abcd.../avatar.png",
    "thumbCdnUrl": "https://cdn.example.com/thumb/2025/09/11/abcd.../avatar_256.webp",
    "primary": true
  }
}
```

왜 mode가 필요한가?
- **BYTES**: 클라이언트가 아직 서버/S3에 업로드하지 않은 선택 이미지를 즉시 서버로 전송해 저장해야 할 때 사용. 모바일/웹에서 결과를 바로 선택했지만 파일이 로컬에만 있는 경우에 효율적.
- **KEY**: 이미지를 미리 S3(예: presign 업로드로 `temp/...`)에 올려둔 뒤 서버가 같은 리전에 있는 S3 내에서 **서버사이드 복사**(COPY)로 `result/...`로 이동/정규화. 네트워크 대역폭 절감, 재업로드 방지, 큰 파일 처리에 유리.
  - 또한 서버가 키 정책을 검증해 우리 버킷/경로만 허용함으로써 보안을 강화.

DB 스키마(예시) — `PetAvatar`
- `id` (PK)
- `member_id` (FK: Member)
- `result_key` (S3 키)
- `thumb_key` (S3 키, nullable)
- `cdn_url` (정규화 선택, 보통 키로부터 계산 가능하므로 생략 가능)
- `prompt` (text)
- `model` (varchar)
- `image_mime` (varchar, 기본 `image/png`)
- `width`, `height` (int, optional)
- `primary` (boolean, 기본 false)
- `version` (int, 기본 1)
- `created_at`, `updated_at`

제약/인덱스
- `(member_id, primary)` Unique Partial(Primary=true) 또는 트리거로 1명당 1개만 대표 허용
- `member_id` 인덱스, `created_at` 정렬용 인덱스

### 라이프사이클/비용 관리
- 원본: 30~90일 후 Glacier로 이전 또는 삭제(정책 선택)
- 결과: 장기 보관(핫), 썸네일은 캐시 우선
- 접근 로그 활성화, 비용 태깅(owner=pet-avatar)

### 백엔드 변경점
- 설정(`application.yml`)
```
aws:
  s3:
    bucket: pet-avatar-prod-apne2
    region: ap-northeast-2
    cloudfrontDomain: https://cdn.example.com
    presignExpireSeconds: 600
```
- 컴포넌트
  - `S3Client` / `S3PresignService` (사전서명 생성)
  - `ImageStorageService` (saveInput/saveResult/saveThumb 반환: s3://key 및 CDN URL)
  - 컨트롤러: `/api/v1/storage/presign-upload`, `/api/v1/pet-avatars/...`, `POST /api/v1/pet-avatars/select`
  - 서비스: `PetAvatarService.saveSelection(memberId, keys, meta, setAsPrimary)`
  - 리포지토리: `PetAvatarRepository` (대표 플래그 토글 메서드 포함)

권한/검증
- 요청 주체가 `memberId`의 소유자인지 인증/인가로 검증
- 입력된 `resultKey`/`thumbKey`가 우리 버킷의 경로 정책에 부합하는지 확인
- `setAsPrimary=true`일 때, 동일 `memberId`의 기존 대표를 비활성화하고 새 레코드를 대표로 설정(동일 트랜잭션)

### 오류/안전성
- MIME 검증 실패/크기 초과 → 400
- 만료 사전서명 재요청 유도
- 재시도 정책: PutObject 5xx 백오프 재시도

### 점진 롤아웃
- MVP: 결과만 S3 저장, 입력은 외부 URL/업로드 유지
- 이후: 입력도 S3 업로드 강제(사전서명)
- 마지막: CDN 도입 및 도메인 교체


