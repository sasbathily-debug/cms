# Projet — Backend

Backend Spring Boot modulaire (multi-module Maven).

## Modules

- **user** — domaine utilisateur : entité, repository JPA, DTO, mapping (MapStruct), migrations Flyway
- **security** — authentification JWT (access + refresh token), vérification par OTP à l'inscription, blacklist Redis, rate-limiting côté auth
- **integration** — intégrations externes via le pattern Strategy : envoi d'email (log en dev / SMTP en prod) et stockage de fichiers (disque local en dev / Cloudinary en prod)
- **app** — module bootstrap : assemble les modules ci-dessus en une application exécutable, configuration Spring Boot, Swagger/OpenAPI

## Stack

Java 21 · Spring Boot 4.1.1 · Spring Security · Spring Data JPA · PostgreSQL · Redis · Flyway · MapStruct · Lombok · JWT (jjwt) · Cloudinary · Docker

## Lancer en local

Prérequis : PostgreSQL et Redis en cours d'exécution, variables d'environnement définies dans `app/.env` (voir `app/.env.example`).

```bash
./run-app.sh
```

Ou avec Docker (stack complète isolée) :

```bash
docker compose up -d --build
```

L'API est servie sur `http://localhost:8080`, Swagger UI sur `http://localhost:8080/swagger-ui/index.html`.

## Documentation

Le modèle d'architecture (C4) se trouve dans [`docs/architecture/`](docs/architecture/).
