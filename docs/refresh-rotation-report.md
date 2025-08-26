# 리프레시 토큰 회전 방식 비교 보고서

## 개요
- 대상: 방식 A(Synchronized-단일 JVM 임계영역), 방식 B(Redis Lua 스크립트-분산 원자성)
- 환경: 로컬 PC, 단일 Redis, Spring Boot 테스트, JUnit 측정
- 측정 방법:
  - 단일 스레드 평균 지연: 워밍업 5회 + 500회 회전 평균
  - 동시성 경합: 스레드 32개가 동일 토큰으로 동시에 회전 시도(성공 1, 실패 N 기대)

## 결과 요약
### 평균 지연(단일 스레드)
- SYNC: ~3.38–3.62 ms
- LUA:  ~3.11–3.46 ms
- 차이: ≈ 0.2–0.5 ms 내외(로컬 환경에서 유사)

### 동시성 경합(32스레드)
- SYNC: [CONC][SYNC] success=1, elapsed ≈ 49 ms
- LUA:  [CONC][LUA]  success=1, elapsed ≈ 142 ms
- 해석: 본 로컬 조건에서는 SYNC가 더 짧게 측정됨(락 경합이 JVM 내부에서 해결). 분산 환경에서는 LUA의 장점이 커질 수 있음.

## 해석
- 로컬 단일 인스턴스에서는 네트워크/분산 경합이 낮아 두 방식 성능이 근접.
- SYNC: 구현 단순, 단일 인스턴스 기준 빠르고 예측 가능하나, 수평 확장 시 JVM 락 한계.
- LUA: 분산 원자성 보장으로 멀티 인스턴스/재시도/충돌 상황에서 정합성 우수.

## 권장 사항
- 단일 인스턴스/저경합: SYNC도 충분.
- 수평 확장·멀티 인스턴스·강한 정합성 요구: LUA 권장.
- 실서비스 근접 검증 필요 시:
  - 동시성 64–128 스레드, 여러 애플리케이션 인스턴스에서 동시 테스트
  - 네트워크 지연/패킷 손실 시뮬레이션 적용
  - Redis 클러스터/레플리카 환경 측정
  - 세션 트림(ZSET) 이벤트 빈도 증가 시나리오 포함

## 부록: 재현 방법
- 프로파일
  - test-sync: `src/test/resources/application-test-sync.yml`
  - test-lua:  `src/test/resources/application-test-lua.yml`
- 실행(Windows PowerShell)
  - SYNC: `$env:SPRING_PROFILES_ACTIVE="test,test-sync"; .\gradlew.bat test --info`
  - LUA:  `$env:SPRING_PROFILES_ACTIVE="test,test-lua";  .\gradlew.bat test --info`
- 테스트 클래스
  - 단일 스레드: `RefreshRotateSyncTests`, `RefreshRotateLuaTests`
  - 동시성: `RefreshRotateSyncConcurrencyTests`, `RefreshRotateLuaConcurrencyTests`

> 주의: 위 수치는 로컬 단일 Redis/단일 머신 기준입니다. 운영 환경에서 수치는 달라질 수 있습니다.
