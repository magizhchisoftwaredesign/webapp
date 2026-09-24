# Backend — deploy to Railway

Spring Boot 4 / Java 25 API. Talks to Postgres + Razorpay.

## Deploy steps
1. Push this `backend/` folder as its own Git repo (or a repo where this is the root).
2. In Railway: **New Project → Deploy from GitHub repo**, pick this repo.
   Railway auto-detects the Maven `pom.xml` and builds/runs it — no extra config needed.
3. Add these environment variables in Railway (Project → Variables):
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `RAZORPAY_KEY_ID`
   - `RAZORPAY_KEY_SECRET`
   - `CORS_ALLOWED_ORIGINS` — your Netlify site URL, e.g. `https://your-app.netlify.app`

   (`application.properties` already falls back to the original hardcoded values if you skip
   these, so it will still boot — but you should **rotate the DB password and Razorpay secret**
   since they were previously committed in plain text, and set them as env vars instead.)
4. Railway sets `PORT` automatically; `application.properties` already reads it
   (`server.port=${PORT:8080}`).
5. Once deployed, copy the public Railway URL (e.g. `https://app-production-xxxx.up.railway.app`)
   — you need it for the frontend's `netlify.toml`.

## Local run
```
./mvnw spring-boot:run
```
