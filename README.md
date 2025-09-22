# TEAM RouteMap

### 🧭 여행 플래너 앱 "RouteMap"

> 카카오맵 기반 여행 동선 설계 · 일정 관리 · 비용 정리까지  
> **여행 계획의 전 과정을 한 번에**

<br>

## 📑 목차
1. [프로젝트 소개](#-프로젝트-소개)
2. [주요 기능](#-주요-기능)
3. [주요 화면 및 기능 소개](#-주요-화면-및-기능-소개)
4. [주요 기술](#-주요-기술)
5. [기술 스택](#-기술-스택)
6. [시스템 아키텍처](#%EF%B8%8F-시스템-아키텍처)
7. [팀원 정보](#-팀원-정보)

<br>

## 📋 프로젝트 소개

<img src="assets/routemap_icon.png" height="160">

**RouteMap** 은 여행 일정을 **지도로 시각화**하고, **경로·날씨·추천·예산**을 한 화면에서 관리하는 **여행 플래너 앱**입니다.  
초기에는 모든 데이터를 첫 화면에서 로딩해 **구동 지연** 문제가 있었으나, 화면 단위 분할 로딩과 캐시 전략으로 **초기 로딩 속도를 개선**에정했습니다.

### 프로젝트 정보
| 항목 | 내용 |
| --- | --- |
| 서비스명 | RouteMap |
| 수행 기간 | 2024.03 ~ 2024.12 |
| 인원 | 6명 (Android 중심) |
| 역할 | **경로 표시 및 전체 UI 개발**, 데이터 지연 로딩 최적화 구상|

<br>

## ✨ 주요 기능

1. **여행 경로 설계**: 장소 추가/순서 변경/이동 수단 선택, 경로선 오버레이  
2. **일정·타임라인 관리**: 일자/시간대별 카드형 편집, 체크리스트  
3. **관광지 추천**: 한국관광공사 API 기반 주변 스팟 추천  
4. **날씨 연동**: 기상청 API로 여행일자 기준 예보 제공  
5. **여행 경비 관리**: 카테고리별 지출 입력/통계  
6. **공유/협업**: 플랜 링크 공유(읽기), PDF/이미지 내보내기(계획)  

<br>

## 🚀 주요 화면 및 기능 소개

- **메인·탐색**: 지도를 중심으로 장소 탐색, 즐겨찾기  
- **경로 편집**: 드래그 앤 드롭으로 순서 변경, 경로선 재계산  
- **타임라인**: 날짜/시간대 카드형 UI, 이동시간 자동 반영  
- **예산**: 항목별 지출 입력, 일자/카테고리 통계  
- **날씨/추천**: 여행지 기준 날씨/관광지 위젯

<p align="left">
  <img src="asset/routemap_main.gif" height="480" style="margin-right: 14px;">
  <img src="asset/routemap_timeline.gif" height="480" style="margin-right: 14px;">
  <img src="asset/routemap_budget.gif" height="480" style="margin-right: 14px;">
</p>

<br>

## 🔬 주요 기술

<details>
<summary><strong>지도 & 경로</strong></summary>

- Kakao Map API로 **마커 표시**와 **경로선 그리기**
- 장소 선택/삭제 시 **리스트와 지도 동기화**
- 기본 **현재 위치 표시** 및 지도 이동/확대 축소
</details>

<details>
<summary><strong>성능 최적화 구상</strong></summary>

- 초기 전체 로딩 → **화면 단위 분할 로딩**으로 전환
- 장소/경로/타임라인 **로컬 캐시** 및 비동기 프리페치  
- 스크롤 구간 **지연 이미지 로딩**(썸네일)
</details>

<details>
<summary><strong>외부 연동</strong></summary>

- **한국관광공사 API**: 테마/지역 기반 스팟 추천  
- **기상청 API**: 여행일정 기준 예보 위젯  
- 공유용 **플랜 내보내기**(PDF/이미지) 준비
</details>

<br>

## 📚 기술 스택

### 📱 Android
- **Kotlin**, Android Studio  
- Jetpack: RecyclerView, ViewModel, LiveData, DataStore, Navigation  
- XML 레이아웃, Material Components  
- 네트워킹: Retrofit/OkHttp  
- 지도: **Kakao Map API**

### 🗄 Data
- Firebase/Firestore (초기 버전)  
- (예정) **Spring Boot + MySQL** 전환 준비

<br>

## 🏗️ 시스템 아키텍처
<img src="asset/routemap_arch.png" width="820">

- App ↔ 공공 API(관광/날씨)  
- App ↔ Firebase/Firestore  
- (전환 준비) App ↔ Spring Boot ↔ MySQL

<br>

## 👨‍👩‍👧‍👦 팀원 정보
| 이름 | 역할 | 메일 |
| --- | --- | --- |
| 송진우 | **Android·UI/UX·경로/성능최적화** | rkddkwl059@naver.com |
| 임채주 | Android UI 레이아웃 담당 ||
| 박희은 | 지도·위치 기능 담당 ||
| 이승호 | 데이터·상태 관리 담당 ||
| 임현정 | API 연동 담당 ||
| 허정주 | QA·릴리즈/리소스 담당 ||



### [🎨 Figma](https://www.figma.com/design/A4gg4CDLoVQ4LW3Pen8lMn/%EB%A3%A8%ED%8A%B8%EB%A7%B5-%ED%94%84%EB%A1%9C%ED%86%A0%ED%83%80%EC%9E%85?node-id=0-1&t=HiijS55V4jQltyA8-1)
