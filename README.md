# 전자정부프레임워크 CMS

전자정부프레임워크 기반의 CMS 백엔드 시스템입니다.

## 개발 환경 설정

### 1. 필수 요구사항

- Docker
- Docker Compose
- Java 17+
- Gradle

### 2. 데이터베이스 및 캐시 설정

#### Docker Compose를 사용한 설정 (권장)

1. **환경 변수 파일 생성**
   ```bash
   # .env 파일을 생성하고 다음 내용을 추가
   # MySQL 설정
   MYSQL_ROOT_PASSWORD=Yubi2025!
   MYSQL_DATABASE=EGOV_CSERVER
   MYSQL_USER=itid
   MYSQL_PASSWORD=Yubi2025!
   
   # Redis 설정
   REDIS_PASSWORD=Yubi!!@@##5630
   ```

2. **SQL 덤프 파일 배치 (선택사항)**
   ```bash
   # SQL 덤프 파일이 있는 경우
   cp /path/to/your/dump.sql docker/mysql/init/
   ```

3. **컨테이너 실행**
   ```bash
   docker-compose up -d
   ```

4. **컨테이너 상태 확인**
   ```bash
   docker-compose ps
   ```

#### 수동 Docker 명령어를 사용한 설정

1. **MySQL 8 컨테이너 생성**
   ```bash
   docker run -d --name mysql8-cms \
     -e MYSQL_ROOT_PASSWORD=Yubi2025! \
     -e MYSQL_DATABASE=EGOV_CSERVER \
     -e MYSQL_USER=itid \
     -e MYSQL_PASSWORD=Yubi2025! \
     -p 3306:3306 \
     mysql:8.0
   ```

2. **Redis 컨테이너 생성**
   ```bash
   docker run -d --name redis-cms \
     -p 6379:6379 \
     redis:7-alpine redis-server --requirepass 'Yubi!!@@##5630'
   ```

3. **SQL 덤프 Import (필요한 경우)**
   ```bash
   docker exec -i mysql8-cms mysql -u itid -pYubi2025! EGOV_CSERVER < /path/to/dump.sql
   ```

### 3. 연결 정보

- **MySQL**: `localhost:3306`
  - Database: `EGOV_CSERVER`
  - Username: `itid`
  - Password: `Yubi2025!`

- **Redis**: `localhost:6379`
  - Password: `Yubi!!@@##5630`

### 4. 애플리케이션 실행

```bash
# 애플리케이션 빌드 및 실행
./gradlew bootRun
```

### 5. 컨테이너 관리

```bash
# 컨테이너 중지
docker-compose down

# 데이터 볼륨까지 삭제 (주의: 데이터 손실)
docker-compose down -v

# 로그 확인
docker-compose logs -f mysql
docker-compose logs -f redis
```

## 트러블슈팅

### MySQL 연결 테스트
```bash
docker exec -it mysql8-cms mysql -u itid -pYubi2025! -e "SELECT 1;"
```

### Redis 연결 테스트
```bash
docker exec -it redis-cms redis-cli -a 'Yubi!!@@##5630' ping
```