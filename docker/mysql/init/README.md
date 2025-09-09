# MySQL 초기화 스크립트

이 디렉토리에 SQL 덤프 파일을 배치하면 MySQL 컨테이너 시작 시 자동으로 실행됩니다.

## 사용 방법

1. SQL 덤프 파일을 이 디렉토리에 복사합니다:
   ```bash
   cp /path/to/your/dump.sql docker/mysql/init/
   ```

2. Docker Compose를 실행합니다:
   ```bash
   docker-compose up -d
   ```

## 주의사항

- `.sql`, `.sql.gz`, `.sh` 파일만 실행됩니다
- 파일은 알파벳 순서로 실행됩니다
- 컨테이너가 이미 초기화된 경우, 볼륨을 삭제해야 합니다:
  ```bash
  docker-compose down -v
  ```

