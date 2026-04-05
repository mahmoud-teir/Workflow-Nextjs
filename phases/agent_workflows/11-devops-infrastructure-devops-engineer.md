---
phase: 11
title: DevOps & Infrastructure
role: DevOps Engineer
dependencies: [Phase 1, Phase 2]
estimated_time: 3-5 hours
---

# Phase 11: DevOps & Infrastructure — Agent Workflow

## Prerequisites
- [ ] Phase 2 completed (app runs locally)
- [ ] Docker installed (for containerized deployment)
- [ ] Cloud provider account (AWS, GCP, or Vercel)

## Step-by-Step Execution

### Step 1: Write Dockerfile
**Action:** `write_to_file`
**File:** `Dockerfile`
**Description:** Multi-stage build (deps → build → runner). Use `corepack enable` for pnpm. Add `HEALTHCHECK` instruction. Configure standalone output. Run as non-root user. Add graceful shutdown handler.

### Step 2: Write .dockerignore
**Action:** `write_to_file`
**File:** `.dockerignore`
**Description:** Exclude `node_modules`, `.next`, `.git`, `.env*`, `*.md`, test files.

### Step 3: Write Graceful Shutdown Handler
**Action:** `write_to_file`
**File:** `lib/shutdown.ts`
**Description:** Handle `SIGTERM` and `SIGINT` — close database connections, flush logs, then exit. Import in custom server or `instrumentation.ts`.

### Step 4: Write Docker Compose (Development)
**Action:** `write_to_file`
**File:** `docker-compose.yml`
**Description:** Services: app, postgres, redis (if needed). Volume mounts for dev, health checks.

### Step 5: Write Terraform Configuration (AWS)
**Action:** `write_to_file`
**Files:** `infra/main.tf`, `infra/variables.tf`, `infra/outputs.tf`, `infra/terraform.tfvars.example`
**Description:** ECS Fargate deployment with ALB, VPC, RDS. Use `FARGATE_SPOT` for cost optimization. Include variable definitions and example tfvars.

### Step 6: Write Kubernetes Manifests (Alternative)
**Action:** `write_to_file`
**Files:** `k8s/deployment.yaml`, `k8s/service.yaml`
**Description:** K8s deployment with readiness/liveness probes, resource limits, and HPA. Service with ClusterIP.

### Step 7: Write Nginx Configuration
**Action:** `write_to_file`
**File:** `infra/nginx.conf`
**Description:** Reverse proxy config with upstream keepalive, X-Forwarded headers, gzip compression, and static asset caching.

### Step 8: ARM64 Multi-Platform Build
**Action:** `run_command`
```bash
docker buildx create --use
docker buildx build --platform linux/amd64,linux/arm64 -t myapp:latest .
```

### Step 9: Document Cost Optimization
**Action:** `write_to_file`
**File:** `docs/COST_OPTIMIZATION.md`
**Description:** Notes on spot instances, right-sizing, reserved capacity, and Vercel spend limits.

## Verification
- [ ] `docker build` succeeds
- [ ] `docker run` starts app and health check passes
- [ ] Graceful shutdown completes within 30s
- [ ] `terraform plan` shows expected resources
- [ ] ARM64 build works alongside AMD64

## Troubleshooting
- **Issue:** Docker build fails on pnpm
  **Fix:** Use `corepack enable && corepack prepare` instead of manual pnpm install.
- **Issue:** Container health check fails
  **Fix:** Ensure health endpoint returns 200 and `HEALTHCHECK` interval matches app startup time.
