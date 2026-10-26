# TaskFlow API – Module 4: Dockerize & Deploy

TaskFlow API (from Module 3), containerized with Docker, deployed to Railway, with a GitHub Actions CI pipeline that publishes the image to Docker Hub.

- **GitHub repository:** https://github.com/vcsandstorm/taskflow-M4
- **Live application URL:** https://taskflow-m4-production.up.railway.app/api/tasks
- **Frontend on Netlify (stretch goal 2):** https://taskflow-m4.netlify.app
- **Docker Hub image (stretch goal 1):** https://hub.docker.com/r/vcsandstorm/taskflow

> Note: the `/api/tasks/summary` (AI summary) endpoint needs an `OPENAI_API_KEY`. It is not set on Railway because this assignment only uses `/api/tasks`.

## Tech stack

- Java 21
- Spring Boot 4.1.1 (Spring Web MVC)
- Spring AI (OpenAI-compatible client, pointed at OpenRouter)
- Maven
- Docker (multi-stage build)
- Railway (deployment)
- GitHub Actions (CI) + Docker Hub (image registry)
- JUnit 5 (unit tests)
- React + Vite (frontend), Netlify (frontend hosting)

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
docker run -p 8080:8080 taskflow:latest
```

In a second terminal:

```bash
curl http://localhost:8080/api/tasks
```

Result: the API returned a JSON response with task data.

## Task 3: Deploy to Railway

1. Pushed the code (including the `Dockerfile`) to GitHub.
2. On Railway: **New Project → Deploy from GitHub repo →** selected this repository. Railway detected the `Dockerfile` and built the image.
3. **Settings → Networking → Generate Domain**, port set to `8080`.

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

All four baseline tasks and stretch goals Option 1 and Option 2 were completed. Below are the errors I hit along the way and how I fixed them.

### 1. `curl` returned nothing without `https://`

- **What happened:** `curl taskflow-m4-production.up.railway.app/api/tasks` printed nothing (no error, no JSON).
- **Fix:** added `https://` to the URL, which returned the JSON response.
- **Lesson:** without a scheme, curl uses plain HTTP, and the Railway domain did not return the API response over HTTP. Always use the full `https://` URL.

### 2. `git push` failed: branch name typo

- **Error:**
  ```
  error: src refspec stretch/ci-pipeline does not match any
  error: failed to push some refs to 'https://github.com/vcsandstorm/taskflow-M4'
  ```
- **Cause:** the branch had been created as `stretch/ci-pipline` (missing an "e"), so Git could not find a branch called `stretch/ci-pipeline`.
- **Fix:** checked the name with `git branch`, renamed it with `git branch -m stretch/ci-pipeline`, then pushed again.
- **Lesson:** run `git branch` to confirm the exact branch name before pushing.

### 3. Workflow run failed: "No jobs were run"

- **What happened:** after the first push of the branch, GitHub Actions reported a failed run for `.github/workflows/ci.yml` with the message **"No jobs were run"**.
- **Cause:** `ci.yml` had been committed as an **empty file (0 bytes)** – the content was not saved before `git add`.
- **Fix:** pasted the workflow content, saved the file, confirmed it was no longer empty with `wc -c .github/workflows/ci.yml`, then committed and pushed again.
- **Lesson:** save files before committing, and check `git status` / file size when something looks wrong.

### 4. Docker build failed: invalid tag `/taskflow:latest`

- **Error** (in the "Build and push Docker image" step):
  ```
  ERROR: failed to build: invalid tag "/taskflow:latest": invalid reference format
  ```
- **Cause:** the `DOCKERHUB_USERNAME` GitHub secret had not been added yet. GitHub replaced the missing secret with an empty string, so the image tag became `/taskflow:latest` instead of `vcsandstorm/taskflow:latest`. The workflow uses the username in the image tag even on Pull Requests (where it does not push), so the secret is needed for every run.
- **Fix:** created a Docker Hub personal access token (Read & Write), added `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` under **Settings → Secrets and variables → Actions**, then used **Re-run jobs → Re-run failed jobs**. The check passed.
- **Lesson:** a missing secret does not raise its own error – it silently becomes empty, and the failure shows up later in a confusing place.

### 5. Frontend blocked by CORS (local and on Netlify)

- **Error** (Safari console; the page showed "Could not load tasks: Load failed"):
  ```
  Origin http://localhost:5173 is not allowed by Access-Control-Allow-Origin. Status code: 200
  Fetch API cannot load https://taskflow-m4-production.up.railway.app/api/tasks due to access control checks.
  ```
  The same error appeared for `https://taskflow-m4.netlify.app` after deploying to Netlify.
- **Cause:** the frontend and the API are on different domains. The browser only lets the page read the response if the API sends an `Access-Control-Allow-Origin` header for that domain. `Status code: 200` shows the API itself answered fine – the browser blocked it.
- **Fix:** added `CorsConfig.java` (a `WebMvcConfigurer`) that allows the origins listed in `app.cors.allowed-origins`, set from the `CORS_ALLOWED_ORIGINS` environment variable (default `http://localhost:5173`). On Railway: `CORS_ALLOWED_ORIGINS=http://localhost:5173,https://taskflow-m4.netlify.app`.
- **Lesson:** fix CORS on the server. Turning off cross-origin restrictions in my own browser would only hide the problem on my Mac.

![CORS error on localhost](screenshots/cors-error.png)
![CORS error on Netlify](screenshots/netlify-cors-error.png)

### 6. The CORS fix did not work at first: `CorsConfig.java` committed empty

- **What happened:** after merging, the Netlify page was still blocked. Testing the API directly showed **no** `Access-Control-Allow-Origin` header, even for localhost:
  ```bash
  curl -s -o /dev/null -D - -H "Origin: https://taskflow-m4.netlify.app" https://taskflow-m4-production.up.railway.app/api/tasks
  ```
- **Cause:** `CorsConfig.java` was committed as an **empty file (0 bytes)** – the code was visible in VS Code but never saved. An empty `.java` file compiles without errors, so `./mvnw test` and CI still passed.
- **Fix:** saved the file, confirmed it with `wc -c`, committed and merged again. Turned on **File → Auto Save** in VS Code to stop this happening again (same root cause as error 3).
- **Lesson:** "tests pass" does not prove the change is there. Checking the real behaviour (here, the response headers with `curl`) found the problem.

### 7. Railway variable added but not applied

- **What happened:** after adding `CORS_ALLOWED_ORIGINS` on Railway, the running deployment still showed **0 Variables**.
- **Cause:** Railway stages variable changes ("Apply 1 change") until they are deployed.
- **Fix:** clicked **Deploy** in the "Apply 1 change" bar and waited for the new deployment to become Active.

### Note: the project had no tests at first

The `pom.xml` already included the test libraries, but `src/test` was empty, so a "test" step would have passed without testing anything. I added `TaskServiceTest` (8 unit tests) so the CI test step does real work.

## Stretch goal – Option 1: CI pipeline (GitHub Actions → Docker Hub)

I added a GitHub Actions workflow at [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

**What it does**

| Trigger | Steps |
|---|---|
| Pull Request into `main` | Check out code → set up Java 21 → `./mvnw -B clean verify` (build + unit tests) → build Docker image (**not pushed**) |
| Push to `main` (including merges) | Same as above, then log in to Docker Hub and **push** the image |

Images are pushed with two tags:
- `vcsandstorm/taskflow:latest`
- `vcsandstorm/taskflow:<commit SHA>` – so every build can be traced back to its exact commit

**Two pipelines side by side**

```
push to main ─┬─► GitHub Actions ─► Docker Hub   (CI: build, test, publish image)
              └─► Railway        ─► live app    (deployment: Railway builds from the Dockerfile)
```

**Secrets:** Docker Hub credentials are stored as GitHub Actions secrets (`DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`) and are never in the code.

**Unit tests:** [`src/test/java/sg/edu/ntu/taskflow_api/service/TaskServiceTest.java`](src/test/java/sg/edu/ntu/taskflow_api/service/TaskServiceTest.java) – 8 tests covering create, find all, find by id (incl. missing id), update, delete, mark complete (incl. missing id), and the empty-list AI summary. They use the in-memory repository and make no AI calls, so CI does not need an `OPENAI_API_KEY`.

**Workflow:** the work was done on branch `stretch/ci-pipeline` and merged into `main` through Pull Request #1 after the CI check passed.

**Links**

- Successful pipeline run: https://github.com/vcsandstorm/taskflow-M4/actions/runs/40264854693
- Docker Hub image: https://hub.docker.com/r/vcsandstorm/taskflow
- All workflow runs: https://github.com/vcsandstorm/taskflow-M4/actions/workflows/ci.yml

<!-- Optional screenshots:
![CI run](screenshots/ci-run.png)
![Docker Hub tags](screenshots/dockerhub-tags.png)
-->

## Stretch goal – Option 2: React frontend on Netlify

- **Frontend URL:** https://taskflow-m4.netlify.app
- **Code:** [`frontend/`](frontend/) – a React app created with Vite. [`frontend/src/App.jsx`](frontend/src/App.jsx) fetches `GET /api/tasks` from the Railway API when the page loads and shows each task with a Done / Pending label, plus loading and error messages.
- **Example data:** the API seeds 5 example tasks on startup (3 Done, 2 Pending) in `TaskflowApiApplication.java`. Data is in memory only, so it resets to these 5 tasks on every redeploy.
- **API URL:** defaults to `https://taskflow-m4-production.up.railway.app`; can be overridden at build time with the `VITE_API_URL` environment variable.

**Netlify build settings** (deployed from this repo's `main` branch)

| Setting | Value |
|---|---|
| Base directory | `frontend` |
| Build command | `npm run build` |
| Publish directory | `frontend/dist` |

**CORS in Spring Boot** – [`CorsConfig.java`](src/main/java/sg/edu/ntu/taskflow_api/config/CorsConfig.java) allows `GET, POST, PUT, DELETE` on `/api/**` from the origins in `app.cors.allowed-origins`:

```properties
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

The allowed origins are a setting, not hard-coded, so adding the Netlify URL only needed a Railway variable change (no code change):

```
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://taskflow-m4.netlify.app
```

**Workflow:** branch `stretch/netlify-frontend` → Pull Request → CI green → merge → Railway redeploys the API; Netlify rebuilds the frontend from `main`.

**Frontend showing data from the API**

![Netlify frontend](screenshots/netlify-frontend.png)

