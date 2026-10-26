# TaskFlow API – Module 4: Dockerize & Deploy

TaskFlow API (from Module 3), containerized with Docker and deployed to Railway.

- **GitHub repository:** https://github.com/vcsandstorm/taskflow-M4
- **Live application URL:** https://taskflow-m4-production.up.railway.app/api/tasks

## Tech stack

- Java 21
- Spring Boot 4.1.1 (Spring Web MVC)
- Spring AI (OpenAI-compatible client, pointed at OpenRouter)
- Maven
- Docker (multi-stage build)
- Railway (deployment)

## API endpoints

Base path: `/api/tasks`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tasks` | List all tasks |
| GET | `/api/tasks/{id}` | Get one task |
| POST | `/api/tasks` | Create a task |
| PUT | `/api/tasks/{id}` | Update a task |
| DELETE | `/api/tasks/{id}` | Delete a task |
| PUT | `/api/tasks/{id}/complete` | Mark a task as complete |
| GET | `/api/tasks/summary` | Task summary |

## Task 1: Dockerfile

The `Dockerfile` in the project root uses a multi-stage build:

1. **Build stage** (`maven:3.9-eclipse-temurin-21`) – copies the source and runs `mvn clean package -DskipTests` to build the JAR inside Docker. This means Railway can build the image straight from GitHub, even though the `target/` folder is not pushed.
2. **Runtime stage** (`eclipse-temurin:21-jre-alpine`) – copies only the built JAR into a small JRE image, exposes port 8080 and runs `java -jar app.jar`.

## Task 2: Build and test locally

```bash
docker build -t taskflow:latest .
docker run -p 8080:8080 -e OPENAI_API_KEY=<your-key> taskflow:latest
```

In a second terminal:

```bash
curl http://localhost:8080/api/tasks
```

Result: the API returned a JSON response with task data.

## Task 3: Deploy to Railway

1. Pushed the code (including the `Dockerfile`) to GitHub.
2. On Railway: **New Project → Deploy from GitHub repo →** selected this repository. Railway detected the `Dockerfile` and built the image.
3. Added the `OPENAI_API_KEY` environment variable in Railway (the app reads it from `application.properties`; the key is not committed to the repo).
4. **Settings → Networking → Generate Domain**, port set to `8080`.

## Task 4: Verify deployment

```bash
curl https://taskflow-m4-production.up.railway.app/api/tasks
```

Result: `200 OK` with this JSON response (tested with curl and Postman):

```json
[{"id":1,"title":"Sample task","description":"Confirm the Taskflow API is running.","completed":false}]
```

Railway shows the deployment **"Add Dockerfile for Module 4"** as **ACTIVE**, status **Online**, region Southeast Asia, 1 replica.

### Screenshots

**Railway dashboard – deployment active**

![Railway dashboard](screenshots/railway-dashboard.png)

**Live application URL working**

![Live API response](screenshots/live-api.png)

## Errors and failures

All four baseline tasks were completed. No blocking errors.

One issue during testing: running `curl taskflow-m4-production.up.railway.app/api/tasks` **without** `https://` returned nothing in the terminal (no error, no JSON). Adding `https://` to the URL fixed it and returned the JSON response. Without a scheme, curl uses plain HTTP, and the Railway domain did not return the API response over HTTP.

## Stretch goal

Not attempted for this submission (baseline pass only).
