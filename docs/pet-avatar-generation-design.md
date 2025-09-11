## PetAvatar 생성 기능 설계서 (이미지-to-아바타, 프롬프트 기반)

### 1) 개요
- **목표**: 사용자가 제공한 참조 이미지(예: "nano banana" 검색으로 얻은 사진)를 기반으로, 프롬프트에 따라 캐릭터화된 반려동물 아바타(PetAvatar) 이미지를 생성.
- **키 포인트**:
  - 이미지-to-이미지(Img2Img) 파이프라인 + 텍스트 프롬프트 컨디셔닝
  - 비동기 작업 처리(큐/워커)와 진행 상태 추적
  - 콘텐츠 안전성 필터링, 프롬프트 가이드라인, 저작권/라이선스 고려

### 1-1) MVP(연결 확인용) 최소 설계
- **목적**: 복잡한 큐/워커 없이 "외부 생성 서비스와 연결이 되는지"만 검증
- **전략**: 동기식 API 1개 + 헬스체크 1개 + 단일 프로바이더(기본 Stability API)
  - 프로바이더는 환경변수로 토글 가능(`stability` | `mock`)

#### 구성 요소(최소)
- 엔드포인트(동기식):
  - `POST /api/v1/pet-avatars/mvp`
    - multipart/form-data: `input_image`(file), `prompt`(string)
    - 서버가 외부 생성 API(예: Stability 이미지 변환) 호출 → 성공 시 결과 URL 반환
- 헬스체크:
  - `GET /api/v1/pet-avatars/health`
    - 프로바이더 API 키 존재 여부, 엔드포인트 도달 여부(간단 ping) 확인

#### 애플리케이션 설정(application.yml 예시)
```
petavatar:
  enabled: true
  provider: stability  # stability | mock
  timeoutMs: 30000
  stability:
    host: https://api.stability.ai
    apiKey: ${STABILITY_API_KEY:}
```

#### 요청/응답 (MVP)
```
POST /api/v1/pet-avatars/mvp
Content-Type: multipart/form-data

form-data
  - input_image: (file)
  - prompt: "Make it a cute pet avatar in cartoon style"

Response 200
{
  "provider":"stability",
  "resultUrl":"https://cdn.example.com/.../result.png"
}
```

#### 헬스체크
```
GET /api/v1/pet-avatars/health
Response 200
{
  "provider":"stability",
  "status":"UP",
  "checks":[{"name":"apiKey","ok":true},{"name":"endpoint","ok":true}]
}
```

#### 테스트 절차(연결 확인)
1) 서버 구동 전 환경변수 설정: `STABILITY_API_KEY`
2) `GET /api/v1/pet-avatars/health` 호출 → `status: UP` 확인
3) 샘플 이미지로 `POST /api/v1/pet-avatars/mvp` 호출 → 200과 `resultUrl` 확인

#### 대안: Mock 모드
- `provider=mock`일 때는 외부 호출 없이 고정 샘플 이미지를 반환하여 프론트/연결만 검증

### 2) 사용자 시나리오
1. 사용자가 웹/앱에서 참조 이미지를 업로드(또는 외부 URL 제공)하고, 아바타 스타일/속성 프롬프트를 입력
2. 서버가 작업(Job)을 생성하고 비동기 처리 시작
3. 생성 완료 후 결과 이미지를 제공(다운로드 URL), 필요 시 다양한 사이즈/포맷으로 변환

### 3) 아키텍처 개요
- **구성**
  - `Spring Boot API` (본 레포): 인증, 요청 수신, 작업 생성/조회, 결과 제공
  - `Worker(생성기)`: 모델 추론 실행(예: SDXL/FLUX/Stable Diffusion img2img, ControlNet 선택적)
  - `Message Queue`(예: Redis Streams, RabbitMQ, SQS): 작업 전달/상태 이벤트
  - `Object Storage`(예: S3, GCS, MinIO): 원본/중간/결과 이미지 저장
  - `RDB`(예: PostgreSQL): 작업 메타데이터/사용량/청구
  - `CDN`: 결과 이미지 가속 배포

```
Client -> Spring API -> Queue -> Worker -> Model Inference
              |                         |
            RDB ----------------------> RDB
              |                         |
            S3 <---------------------  S3
```

### 4) 데이터 모델 (초안)
- `pet_avatar_job`
  - `id` (UUID, PK)
  - `user_id` (nullable: 비로그인 지원 시 익명 토큰)
  - `status` (PENDING, RUNNING, SUCCEEDED, FAILED, CANCELED)
  - `input_image_url` (원본 업로드 경로, 사전 서명 URL 허용)
  - `prompt` (생성 지시문 원문, 민감정보 제거/마스킹)
  - `negative_prompt` (선택)
  - `style_preset` (예: cartoon, watercolor, pixel, manga 등)
  - `guidance_scale`, `strength`, `seed`, `steps`, `sampler` 등 파라미터 JSON(`params_json`)
  - `result_image_url` (최종 결과 경로)
  - `result_variants_json` (리사이즈/썸네일/투명배경 등 파생 결과 목록)
  - `safety_score` / `moderation_flags` (콘텐츠 안전)
  - `cost_credit_used` (비용/크레딧)
  - `created_at`, `updated_at`, `completed_at`

### 5) API 설계 (v1)
- 인증: Bearer(회원), 또는 익명 세션 토큰(레이트 리밋 강화)

1) 작업 생성
```
POST /api/v1/pet-avatars
Content-Type: multipart/form-data | application/json

Form-Data 예시
  - input_image: (file) or input_image_url: (string)
  - prompt: string (필수)
  - negativePrompt: string (선택)
  - stylePreset: string (선택, 기본 cartoon)
  - params: JSON (strength, steps, guidanceScale, seed, sampler, outputFormat 등)

Response 202
{
  "jobId": "uuid",
  "status": "PENDING"
}
```

2) 작업 상태 조회
```
GET /api/v1/pet-avatars/{jobId}
Response 200
{
  "jobId": "uuid",
  "status": "RUNNING|SUCCEEDED|FAILED",
  "resultImageUrl": "https://..." | null,
  "resultVariants": [ {"size":"1024x1024","url":"..."}, ... ],
  "safety": {"score":0.02, "flags":[]}, @RestController
public class NanoBananaController {
    @Value("${nano.banana.api.key}")
    private String apiKey;

    @PostMapping("/edit-image")
    public ResponseEntity<byte[]> editImage(@RequestBody EditRequest editRequest) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("prompt", editRequest.getPrompt());
        body.put("image", editRequest.getImageUrl());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.postForEntity(
            "https://api.higgsfield.ai/nano-banana",
            request,
            byte[].class
        );
        return response;
    }
}
  "error": null | {"code":"MODEL_TIMEOUT","message":"..."}
}
```

3) 결과 다운로드(프록시/리디렉션)
```
GET /api/v1/pet-avatars/{jobId}/image
302 -> CDN/S3 사전서명 URL
```

4) 웹훅(선택)
```
POST {clientWebhookUrl}
{
  "jobId":"uuid",
  "status":"SUCCEEDED",
  "resultImageUrl":"https://..."
}
```

### 6) 생성 파이프라인
1. 업로드/URL 검증 → 이미지 원본 저장(S3) → 해시 계산(중복 방지)
2. 프롬프트 전처리(금칙어 필터, 길이 제한, 스타일 템플릿 병합)
3. 안전성 필터(이미지 모더레이션) → 차단 시 즉시 실패 처리
4. 큐에 Job Publish → 워커가 Dequeue
5. 모델 추론(Img2Img)
   - 기본: Stable Diffusion XL 기반 img2img + LoRA/ControlNet 옵션화
   - 대체: OpenAI, Google, Stability API, ComfyUI 서버 등 플러그 방식
   - 파라미터: `strength(0.2~0.8)`, `guidanceScale(3~9)`, `steps(20~40)`
6. 결과 후처리
   - 배경 제거(선택), 크기 리사이즈, WebP/PNG 변환, 메타데이터 마스킹
7. 결과 저장(S3) 및 메타 업데이트 → 상태 SUCCEEDED

### 7) 프롬프트 템플릿(초안)
- 베이스 템플릿
```
You are generating a stylized pet avatar from the given reference image.
Style: {{stylePreset}}
Subject: cute pet character, high detail, clean edges, well-lit
Output: single character, centered, transparent background if possible
{{userPrompt}}
```
- Negative Prompt 예: "low quality, blurry, extra limbs, deformed, watermark, text"

### 8) 모델 선택 전략
- **기본**: 자체 호스팅 SDXL 파이프라인(성능/비용 균형), NVIDIA GPU 노드
- **대체 라우팅**: 외부 API(혼잡/장애 시)로 페일오버
- **옵션**: 스타일별 LoRA(만화풍, 수채화, 픽셀), ControlNet(포즈/라인), 업스케일러

### 9) 성능/확장성
- 비동기 처리 + 배치 프리페치
- GPU 워커 오토스케일(HPA/Cluster Autoscaler)
- 캐시: 동일 입력 해시 + 동일 프롬프트 조합 재사용(결과 재서빙)
- 썸네일/리사이즈는 CPU 파이프라인으로 분리

### 10) 안정성/재시도
- 워커 타임아웃(예: 120s~300s), 큐 가시성 타임아웃 튜닝
- 일시 오류 재시도(최대 2~3회), 결정적 실패 즉시 중단
- 아이돌 워커 헬스체크, 장애 격리

### 11) 보안/컴플라이언스
- 이미지 URL 사전서명(PUT/GET), 만료 관리
- XSS/SSRF 방지: 외부 URL 페치 시 도메인 화이트리스트/프록시
- 저작권: 구글 검색 이미지 사용 시 라이선스 고지/업로드 약관 동의 필요
- 개인정보/민감정보: 메타데이터 제거(EXIF), PII 포함 프롬프트 거부

### 12) 관측/과금
- 메트릭: 처리 시간, 성공률, 리트라이, GPU 사용률, 평균 비용/작업
- 로깅: 작업 ID 상관관계, 프롬프트/파라미터는 마스킹 후 저장
- 크레딧/쿼터: 사용자별 일일 상한, 초과 시 429

### 13) 에러 코드 가이드 (예)
- `INVALID_IMAGE`, `MODERATION_BLOCKED`, `QUEUE_FULL`, `MODEL_TIMEOUT`, `MODEL_UNAVAILABLE`, `INTERNAL_ERROR`

### 14) 운영 플로우
- 릴리즈 전 테스트: 골든 세트(입력/프롬프트/기대 결과) 회귀 테스트
- A/B: 스타일 프리셋과 파라미터 튜닝
- 롤백: 대체 라우팅 스위치, 모델 버전 태깅

### 15) 테스트 전략
- 유닛: 프롬프트 전처리/밸리데이션, 파라미터 한계값
- 통합: 큐-워커-스토리지 E2E
- 비주얼 회귀: SSIM/LPIPS + 휴리스틱(배경 투명도, 주제 중심성)

### 16) 예시 요청/응답
```
curl -X POST https://api.example.com/api/v1/pet-avatars \
  -H "Authorization: Bearer <token>" \
  -F input_image=@/path/to/nano-banana.jpg \
  -F prompt='Make it a cute corgi astronaut with banana color accents' \
  -F stylePreset='cartoon' \
  -F params='{"strength":0.45,"guidanceScale":6.5,"steps":30,"outputFormat":"png"}'

=> 202 {"jobId":"...","status":"PENDING"}

GET /api/v1/pet-avatars/{jobId}
=> 200 {"status":"SUCCEEDED","resultImageUrl":"https://.../avatar.png"}
```

### 17) 스키마 예시 (DDL 초안: PostgreSQL)
```
CREATE TABLE pet_avatar_job (
  id UUID PRIMARY KEY,
  user_id VARCHAR(64),
  status VARCHAR(16) NOT NULL,
  input_image_url TEXT NOT NULL,
  prompt TEXT NOT NULL,
  negative_prompt TEXT,
  style_preset VARCHAR(32) DEFAULT 'cartoon',
  params_json JSONB,
  result_image_url TEXT,
  result_variants_json JSONB,
  safety_score NUMERIC(5,4),
  moderation_flags JSONB,
  cost_credit_used NUMERIC(10,4),
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  completed_at TIMESTAMPTZ
);
```

### 18) 구현 체크리스트 (백로그)
- [ ] 엔드포인트: 생성/조회/이미지 프록시
- [ ] 스토리지: 업로드(사전서명) + 결과 저장
- [ ] 큐/워커: 추론 파이프라인 연동
- [ ] 모델 어댑터: SDXL 기본 + 외부 API 페일오버
- [ ] 프롬프트/모더레이션/안전성 모듈
- [ ] 관측/메트릭/로그 마스킹
- [ ] 크레딧/쿼터/레이트리밋

### 19) 향후 확장
- 사용자 스타일 파인튜닝(개별 LoRA 학습)
- 포즈 가이드 업로드(ControlNet)
- 비디오 아바타(애니 GIF)


