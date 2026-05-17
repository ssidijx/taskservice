# TaskService

Сервис распределённого выполнения задач на Java 21 / Spring Boot 3.5. Принимает задачи из Kafka, сохраняет в PostgreSQL и выполняет в пуле воркеров с горизонтальным масштабированием.

## Возможности

- Приём задач из Kafka с идемпотентностью по `externalId`
- Распределённый захват задач через PostgreSQL `SELECT ... FOR UPDATE SKIP LOCKED`
- Пул воркеров с настраиваемым размером
- Горизонтальное масштабирование (несколько инстансов делят работу через общую БД)
- Watchdog для восстановления зависших задач после краха инстанса
- REST API для проверки статуса задач
- MDC-логирование с `taskId` для трейсинга
- Метрики Prometheus, health-check через Spring Boot Actuator

## Стек

- Java 21, Spring Boot 3.5
- PostgreSQL 16, Flyway, Spring Data JPA, HikariCP
- Apache Kafka 3.9 (KRaft режим, без ZooKeeper)
- Lombok, Maven
- Docker Compose для инфраструктуры

## Архитектура

    ┌──────────┐    ┌─────────────┐    ┌──────────────┐    ┌──────────────┐
    │  Kafka   │───▶│  Consumer   │───▶│  PostgreSQL  │◀───│ Worker Pool  │
    │ (tasks)  │    │ + Ingest    │    │   (tasks)    │    │  + Scheduler │
    └──────────┘    │ (идемпот.)  │    └──────┬───────┘    └──────▲───────┘
                    └─────────────┘           │                   │
                                              ▼                   │
                                       ┌──────────────┐           │
                                       │   REST API   │           │
                                       │ /api/tasks   │           │
                                       └──────────────┘           │
                                                                  │
                                       ┌──────────────────────────┘
                                       │ SKIP LOCKED захват
                                       ▼
                                  ┌──────────┐
                                  │ Watchdog │ ── возврат зависших задач
                                  └──────────┘

### Ключевые технические решения

**SKIP LOCKED для распределённой очереди.** При горизонтальном масштабировании несколько инстансов одновременно опрашивают БД. Запрос `SELECT ... FOR UPDATE SKIP LOCKED LIMIT 1` атомарно блокирует и захватывает одну задачу, остальные пропускает. Решает проблему конкуренции без retry и дополнительных компонентов (Redis/ZooKeeper).

**Короткие транзакции.** Захват задачи (NEW → IN_PROGRESS) и обновление финального статуса выполняются в отдельных транзакциях. `Thread.sleep` выполняется вне транзакции — соединения пула не блокируются на время выполнения задачи.

**Идемпотентность Kafka.** Поле `external_id` с UNIQUE-индексом + проверка `existsByExternalId` перед вставкой. Если consumer переобработает то же сообщение (например, после переподключения), дубликат не создастся.

**Watchdog для восстановления.** Шедулер каждые 10 секунд возвращает в `NEW` задачи, висящие в `IN_PROGRESS` дольше threshold. Срабатывает при крахе инстанса, который держал задачу.

**MDC-логирование.** `taskId` автоматически прокидывается во все логи внутри треда воркера через `MDC.put/remove`. Облегчает дебаг при параллельной обработке.

## Запуск

### Требования

- JDK 21
- Docker + Docker Compose
- Maven 3.9+

### Поднять инфраструктуру

```bash
docker compose up -d
```

Поднимет: PostgreSQL (`localhost:5432`), Kafka (`localhost:9092`), Kafka UI (`localhost:8090`).

### Запустить приложение

```bash
./mvnw spring-boot:run
```

Приложение поднимется на `localhost:8080`, применит миграции Flyway и подпишется на топик `tasks`.

### Отправить задачу

Через Kafka:

```bash
docker exec -i task-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic tasks <<< '{"externalId":"task-1","name":"my task","durationMs":2000}'
```

Проверить статус:

```bash
curl http://localhost:8080/api/tasks/1
```

### Горизонтальное масштабирование

Запустить второй инстанс на другом порту:

```bash
SERVER_PORT=8081 ./mvnw spring-boot:run
```

Оба инстанса будут конкурентно тянуть задачи из общей БД без дубликатов.

## Конфигурация

Основные настройки в `application.yaml`:

```yaml
app:
  worker:
    pool-size: 4              # размер пула воркеров
    poll-delay-ms: 1000       # интервал опроса БД
  watchdog:
    stuck-threshold-ms: 30000 # задача считается зависшей через 30 сек
    poll-delay-ms: 10000      # частота проверки watchdog
```

## Endpoints

- `GET /api/tasks/{id}` — статус задачи
- `GET /actuator/health` — health-check
- `GET /actuator/prometheus` — метрики (HikariCP, JVM, HTTP)

## Схема БД

```sql
tasks (
  id          BIGSERIAL PRIMARY KEY,
  external_id VARCHAR UNIQUE NOT NULL,
  name        VARCHAR NOT NULL,
  duration_ms BIGINT NOT NULL,
  status      VARCHAR NOT NULL,  -- NEW | IN_PROGRESS | COMPLETED | FAILED
  result      VARCHAR,
  created_at  TIMESTAMP NOT NULL,
  started_at  TIMESTAMP,
  version     BIGINT NOT NULL    -- optimistic locking
)
```

Индексы: `(status, started_at)` для watchdog, `external_id` UNIQUE для идемпотентности.
