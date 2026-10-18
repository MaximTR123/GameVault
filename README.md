# GameVault

![CI](https://github.com/MaximTR123/GameVault/actions/workflows/ci.yml/badge.svg)

REST API for a game store: users, game catalog, purchases and a personal library.
Built with Java 21, Spring Boot, Spring Security (JWT), PostgreSQL and Docker.

## Features
- Registration and login with JWT
- Roles: `USER` and `ADMIN`
- Game catalog (admin manages games)
- Purchases with price snapshot, personal library
- Business rules: no duplicate purchases, last admin protection

## Quick start (Docker)
```bash
cp .env.example .env     # then fill in the values
docker compose up --build
```
API: http://localhost:8080

## Run tests
```bash
mvn clean verify
```
Coverage report: `target/site/jacoco/index.html`

## API overview
| Method | Path | Access |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | public |
| GET | /api/games, /api/games/{id} | public |
| POST, PUT, DELETE | /api/games | admin |
| POST | /api/purchases | user |
| GET | /api/library | user |
| GET, POST, DELETE, PUT | /api/users... | admin |

## Tech stack
Java 21 · Spring Boot · Spring Security · JPA/Hibernate · PostgreSQL · JUnit 5 · Mockito · JaCoCo · Checkstyle · SpotBugs · Docker · GitHub Actions