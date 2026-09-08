# 인사 CSV dry-run·적용 가이드

기준일: 2026-09-04

## 원칙

CSV는 항상 dry-run을 먼저 실행한다. dry-run은 사용자 데이터를 변경하지 않지만 `hr_sync_runs`에 실행자, 파일 SHA-256, 건수, 결과를 감사 기록한다. 검증 오류가 하나라도 있으면 실제 적용 요청은 422로 거부된다. CSV 적용은 신규·갱신·외부ID 연결만 수행하며 파일 누락만으로 사용자를 퇴사·삭제하지 않는다.

## 제한

- UTF-8, 선택적으로 BOM 허용
- 최대 10 MiB, 최대 100,000 데이터 행
- RFC 4180 방식 큰따옴표와 `""` escape 지원
- 파일 내 `externalObjectId` 중복 금지
- 오류 응답은 최대 100건 반환, 전체 오류 건수는 실행 결과로 관리

## 필수 컬럼

- `externalObjectId`
- `immutablePersonKey`
- `employmentStatus`: PRE_HIRE, ACTIVE, LEAVE, SUSPENDED, TERMINATED, UNKNOWN

선택 컬럼은 `employeeNumber`, `loginId`, `windowsSid`, `upn`, `email`, `displayName`, `organizationExternalId`, `managerImmutableKey`, `groups`, `effectiveFrom`, `effectiveTo`, `sourceVersion`이다. groups는 세미콜론으로 구분하고 시각은 UTC ISO-8601 형식이다.

## 1. 연동 원천 생성

`POST /api/v1/hr/sources`

```json
{
  "name": "HR manual CSV",
  "connectorType": "MANUAL_UPLOAD",
  "syncMode": "FULL",
  "authorityRank": 100,
  "configJson": "{\"missingUserAction\":\"NONE\"}",
  "secretRef": null
}
```

비밀번호·token·clientSecret·privateKey는 configJson에 넣을 수 없다.

## 2. dry-run

`POST /api/v1/hr/sources/{sourceId}/imports/csv?dryRun=true`, multipart field 이름은 `file`, 실행자는 `X-Admin-Id`에 기록한다.

응답은 `syncRunId`, read/create/update/link/conflict/rejected/unchanged 건수와 오류 목록을 반환한다. `conflictCount` 또는 `rejectedCount`가 있으면 충돌을 먼저 확인한다.

## 3. 실제 적용

동일 파일 SHA-256을 확인하고 `dryRun=false`로 다시 요청한다. 파일이 바뀌었으면 이전 dry-run 승인을 재사용하지 않는다. 실제 적용 중 모든 DB 변경과 실행 이력은 한 트랜잭션에서 처리된다.

## 안전성

- 동일 원천+externalObjectId+동일 payloadHash는 unchanged
- externalObjectId의 immutablePersonKey 변경은 `EXTERNAL_ID_REBOUND` 충돌
- immutablePersonKey가 유일하면 기존 내부 personId에 새 외부 ID를 연결
- 사번, 이메일, 이름만으로 병합하지 않음
- 적용 결과와 파일 원문은 분리하며 파일 원문은 기본 보관하지 않음

## 현재 남은 작업

관리자 화면의 dry-run 비교표와 이중 승인, source별 field mapping UI, 그룹 실제 upsert, 대량변경 임계치, 삭제/퇴사 reconciliation은 다음 단계다.