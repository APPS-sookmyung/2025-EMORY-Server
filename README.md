
# 2025-EMORY-Server

인터랙티브 감정 다이어리

## 🏄🏻‍♀️ 개발 팀 소개

Emory 개발팀은 숙명여대 소프트웨어학부 학회 APPS 소속 대학생들로 구성된 팀으로,

사용자 친화적인 웹 서비스를 구현하고 있습니다.
감정 기록의 장벽을 낮추고, 누구나 손쉽게 자신의 하루를 돌아볼 수 있도록 만드는 것이 목표입니다.


<br />

<div id="5"></div>

## 📅 개발 기간



<br>

<div id="6"></div>

## 📌 Git Convention

### 🔵 Commit Convention

- `[FEAT]` : 새로운 기능 추가
- `[FIX]` : 버그, 오류 해결
- `[TEST]` : 테스트 코드, 리펙토링 테스트 코드 추가
- `[DOCS]` : README나 WIKI 등의 문서 수정
- `[REMOVE]` : 폴더 또는 파일 삭제, 쓸모없는 코드 삭제
- `[RENAME]` : 파일 이름 변경 또는 파일 이동시
- `[REFACTOR]` : 기능 추가나 버그 수정이 없는 코드 변경 ( 코드 구조 변경 등의 리팩토링 )
- `[COMMENT]` : 필요한 주석 추가 및 변경
- `[CHORE]` : 의존성 추가, yml 추가 및 수정, 패키지 구성, 템플릿, 문서 등 기능이 아닌 작업 업데이트
- `[MERGE]` : merge 하는 경우
- `[!HOTFIX]` : 급하게 치명적인 버그를 고쳐야 하는 경우

### 커밋 예시

- `[<Prefix>] #<Issue_Nomber> <Description>`의 형식으로 커밋 메시지 작성
    - ex) `[feat] #22 최신 일기 피드백 받아오는 API 추가`

<br>

### 🔵 Branch Convention

- `main` : 최종 배포
- `dev` : 주요 개발, main merge 이전에 거치는 branch
- `feat` : 각자 개발, 기능 추가
- `fix` : 에러 수정, 버그 수정
- `docs` : README, 문서
- `refactor` : 코드 리펙토링 (기능 변경 없이 코드만 수정할 때)
- `modify` : 코드 수정 (기능의 변화가 있을 때)

### 브랜치 명 예시

- feat/#이슈번호-기능 이름
    - ex) `feat/#21-header`

<br>

### 🔵 Branch Strategy

- Git-flow 전략을 기반으로 `main`, `dev` 브랜치와 `feat` 보조 브랜치를 운용했습니다.
- `main`, `dev`, `feat` 브랜치로 나누어 개발을 하였습니다.
    - `main` 브랜치는 배포 단계에서만 사용하는 브랜치입니다.
    - `dev` 브랜치는 개발 단계에서 git-flow의 master 역할을 하는 브랜치입니다.
    - `feat` 브랜치는 기능 단위로 독립적인 개발 환경을 위하여 사용하고 merge 후 각 브랜치를 삭제해주었습니다.

<br>

### 🔵 Issue Convention


- [FEAT] : 기능 추가
- [FIX] : 에러 및 버그 수정
- [DOCS] : README 등 문서
- [REFACTOR] : 코드 리펙토링 (기능 변경 없이 코드만 수정할 때)
- [MODIFY] : 코드 수정 (기능의 변화가 있을 때)
- [CHORE] : 그 외 작업 내용

### 이슈 명 예시
> `[<Prefix>] <description>`의 형식을 따르며 이때 Prefix는 `commit tag`를 따릅니다.
>
> 
- [이슈 항목] 개발 내용
    - `ex) [feat] Amazon S3 연동`

<br>