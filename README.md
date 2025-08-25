# Talki Application

실시간 음성 대화 시스템을 위한 Spring Boot 애플리케이션입니다.

## 기술 스택

- **Backend**: Spring Boot 3.5.0, Java 17
- **Database**: MySQL 8.0
- **Message Queue**: Apache Kafka
- **Communication**: gRPC, WebSocket
- **Containerization**: Docker, Docker Compose
- **Reverse Proxy**: Nginx
- **CI/CD**: Jenkins

## 프로젝트 구조

```
talki-project/
├── src/main/java/com/langhakers/talki/    # 애플리케이션 소스 코드
├── src/main/resources/                    # 설정 파일
├── docker/                               # Docker 관련 설정
│   ├── mysql/                           # MySQL 초기화 스크립트
│   └── nginx/                           # Nginx 설정
├── k8s/                                 # Kubernetes 배포 설정
├── .jenkins/                            # Jenkins 관련 스크립트
├── Dockerfile                           # 애플리케이션 Docker 이미지
├── docker-compose.yml                   # 개발/운영 환경
├── docker-compose.test.yml              # 테스트 환경
└── Jenkinsfile                          # CI/CD 파이프라인
```

## 빠른 시작

### 1. 사전 요구사항

- Docker 및 Docker Compose
- Java 17 (로컬 개발 시)
- Git

### 2. 로컬 개발 환경 실행

```bash
# 프로젝트 클론
git clone <repository-url>
cd talki-project

# Docker Compose로 전체 스택 실행
docker-compose up -d

# 또는 개발용 스크립트 사용
./scripts/dev-setup.sh
```

### 3. 애플리케이션 접근

- **API 서버**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/actuator/health
- **gRPC 서버**: localhost:9090
- **Nginx (프록시)**: http://localhost
- **MySQL**: localhost:3306

## 환경별 설정

### Development
```bash
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

### Docker Environment
```bash
docker-compose up -d
```

### Test Environment
```bash
docker-compose -f docker-compose.test.yml up -d
./gradlew test
```

## API 엔드포인트

### REST API
- `GET /actuator/health` - 헬스 체크
- `POST /api/sessions` - 대화 세션 생성
- `GET /api/sessions/{id}` - 세션 정보 조회
- `POST /api/audio/upload` - 오디오 파일 업로드
- `GET /api/metadata/{id}` - 메타데이터 조회

### WebSocket
- `/ws/chat` - 실시간 채팅
- `/ws/audio` - 실시간 오디오 스트리밍

### gRPC Services
- `ChatService` - 채팅 메시지 처리
- `AudioService` - 오디오 데이터 처리
- `UtteranceService` - 발화 데이터 처리

## 배포

### Jenkins를 통한 자동 배포

1. Jenkins에서 프로젝트 설정
2. Webhook 또는 폴링으로 자동 빌드 트리거
3. 파이프라인이 자동으로 빌드, 테스트, 배포 수행

### 수동 배포

```bash
# 배포 스크립트 사용
./.jenkins/deploy.sh [environment] [image_tag]

# 예시
./.jenkins/deploy.sh development latest
./.jenkins/deploy.sh production v1.0.0
```

## 모니터링

### Health Checks
- Spring Boot Actuator: `/actuator/health`
- Nginx Health: `/health`
- Database: MySQL 헬스체크 포함

### 로그 확인
```bash
# 애플리케이션 로그
docker-compose logs -f talki-app

# 전체 서비스 로그
docker-compose logs -f

# 특정 컨테이너 로그
docker logs talki-application
```

## 개발 가이드

### 로컬 개발 설정

1. Java 17 설치
2. MySQL 8.0 설치 및 데이터베이스 생성
3. Kafka 설치 (선택사항)
4. IDE에서 프로젝트 import

### 빌드 및 테스트

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 통합 테스트
./gradlew integrationTest

# 코드 품질 검사
./gradlew check
```

### Docker 이미지 빌드

```bash
# 애플리케이션 이미지 빌드
docker build -t talki/talki-application:latest .

# 멀티 스테이지 빌드로 최적화된 이미지 생성
docker build --target builder -t talki/talki-application:builder .
```

## 문제 해결

### 일반적인 문제

1. **포트 충돌**: 다른 서비스가 8080, 3306, 9092 포트를 사용하는지 확인
2. **메모리 부족**: Docker Desktop 메모리 할당량 증가
3. **권한 문제**: `chmod +x` 로 스크립트 실행 권한 부여

### 로그 확인 방법

```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health

# 컨테이너 상태 확인
docker-compose ps

# 상세 로그 확인
docker-compose logs --tail=100 talki-app
```

## 기여하기

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

