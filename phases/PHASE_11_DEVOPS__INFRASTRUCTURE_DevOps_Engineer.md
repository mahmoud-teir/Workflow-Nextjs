<a name="phase-11"></a>
# 📌 PHASE 11: DEVOPS & INFRASTRUCTURE (DevOps Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 11.1: Docker Setup

```text
You are a DevOps Engineer. Create a multi-stage Docker build for a Next.js application.

Required:
1. Multi-stage Dockerfile (optimized for size < 200MB)
2. Standalone output mode (critical for minimal footprint)
3. Node.js 22 LTS (Alpine)
4. HEALTHCHECK instruction
5. Graceful shutdown handling (SIGTERM/SIGINT)
6. Non-root user execution
7. ARM64 multi-platform support
8. .dockerignore file
```

```typescript
// next.config.ts — required before building the Docker image
// Without this, .next/standalone is not generated and the Dockerfile COPY steps will fail.
import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  output: 'standalone', // Emits .next/standalone with only needed node_modules
}

export default nextConfig
```

```dockerfile
# Dockerfile
# Stage 1: Base
FROM node:22-alpine AS base
WORKDIR /app
RUN corepack enable && corepack prepare pnpm@latest --activate

# Stage 2: Dependencies
FROM base AS deps
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile --prod=false

# Stage 3: Builder
FROM base AS builder
COPY --from=deps /app/node_modules ./node_modules
COPY . .
ENV NEXT_TELEMETRY_DISABLED=1
RUN pnpm build

# Stage 4: Runner
FROM node:22-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

# Copy standalone output
COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000/api/health || exit 1

# Graceful shutdown: Node.js handles SIGTERM from Docker
CMD ["node", "server.js"]
```

#### Graceful Shutdown Handler:

```typescript
// server.ts or instrumentation.ts — Graceful shutdown
// Next.js standalone server handles SIGTERM by default.
// For custom servers or cleanup tasks:

process.on('SIGTERM', () => {
  console.log('SIGTERM received. Shutting down gracefully...')
  // Close database connections, flush logs, etc.
  process.exit(0)
})

process.on('SIGINT', () => {
  console.log('SIGINT received. Shutting down...')
  process.exit(0)
})
```

#### .dockerignore:

```
node_modules
.next
.git
.github
.env*.local
*.md
e2e
tests
playwright-report
test-results
coverage
```

#### Multi-Platform Build (ARM64 + AMD64):

```bash
# Build for multiple platforms
docker buildx create --use
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t myapp:latest \
  --push .
```

---

### Prompt 11.2: Infrastructure as Code (Terraform)

```text
Set up scalable AWS infrastructure using Terraform.

Resources:
1. VPC (Public/Private Subnets, NAT, IGW)
2. ECR Repository (Container Registry)
3. ECS Fargate Cluster (Serverless containers)
4. ALB (Application Load Balancer)
5. RDS PostgreSQL (or use Neon Serverless)
6. S3 Bucket (Assets/Uploads)
7. CloudFront (CDN)
```

```hcl
# variables.tf
variable "app_name" {
  type    = string
  default = "nextjs-app"
}

variable "environment" {
  type    = string
  default = "production"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "db_password" {
  type      = string
  sensitive = true
}
```

```hcl
# main.tf
terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
  backend "s3" {
    bucket = "my-terraform-state"
    key    = "nextjs-app/terraform.tfstate"
    region = "us-east-1"
  }
}

provider "aws" {
  region = var.aws_region
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"
  name    = "${var.app_name}-vpc"
  cidr    = "10.0.0.0/16"
  azs             = ["${var.aws_region}a", "${var.aws_region}b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]
  enable_nat_gateway = true
  single_nat_gateway = true # Cost optimization for non-prod
}

resource "aws_ecr_repository" "app" {
  name                 = var.app_name
  image_tag_mutability = "IMMUTABLE" # Prevent tag overwriting
  image_scanning_configuration { scan_on_push = true }
}

module "ecs" {
  source       = "terraform-aws-modules/ecs/aws"
  version      = "~> 5.0"
  cluster_name = "${var.app_name}-cluster"
  fargate_capacity_providers = {
    FARGATE      = { default_capacity_provider_strategy = { weight = 50 } }
    FARGATE_SPOT = { default_capacity_provider_strategy = { weight = 50 } } # Cost optimization
  }
}
```

```hcl
# outputs.tf
output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "ecs_cluster_name" {
  value = module.ecs.cluster_name
}

output "vpc_id" {
  value = module.vpc.vpc_id
}
```

```hcl
# terraform.tfvars.example
app_name    = "my-nextjs-app"
environment = "production"
aws_region  = "us-east-1"
db_password = "CHANGE_ME"  # Use secrets manager in production
```

#### Kubernetes Alternative (basic manifest):

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nextjs-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: nextjs-app
  template:
    metadata:
      labels:
        app: nextjs-app
    spec:
      containers:
        - name: nextjs
          image: myregistry/nextjs-app:latest
          ports:
            - containerPort: 3000
          env:
            - name: NODE_ENV
              value: production
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /api/health
              port: 3000
            initialDelaySeconds: 10
          readinessProbe:
            httpGet:
              path: /api/health
              port: 3000
            initialDelaySeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: nextjs-app
spec:
  type: ClusterIP
  selector:
    app: nextjs-app
  ports:
    - port: 80
      targetPort: 3000
```

#### Cost Optimization Notes:

```text
- Use FARGATE_SPOT for non-critical workloads (60-70% cost savings)
- Use single NAT gateway in dev/staging, multi-AZ NAT in production only
- Right-size ECS tasks: start with 0.25 vCPU / 512MB, scale based on metrics
- Use Neon Serverless instead of RDS for dev/staging (free tier, auto-sleep)
- Enable S3 Intelligent-Tiering for uploads
- Use CloudFront for static asset caching (reduces origin requests)
```

---

### Prompt 11.3: Nginx Configuration (Reverse Proxy)

```text
Create modern Nginx configuration for Next.js app (if self-hosted).
```

```nginx
# nginx.conf
upstream nextjs_upstream {
    server localhost:3000;
    keepalive 64;
}

server {
    listen 443 ssl http2;
    server_name myapp.com;

    # SSL Certs (Let's Encrypt / Certbot)
    ssl_certificate /etc/letsencrypt/live/myapp.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/myapp.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;

    # Security Headers (supplement — primary CSP is in Next.js middleware)
    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1000;

    # Static assets (Next.js immutable assets)
    location /_next/static {
        proxy_pass http://nextjs_upstream;
        add_header Cache-Control "public, max-age=31536000, immutable";
    }

    # Public assets
    location /public {
        proxy_pass http://nextjs_upstream;
        add_header Cache-Control "public, max-age=86400";
    }

    # App proxy
    location / {
        proxy_pass http://nextjs_upstream;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    server_name myapp.com;
    return 301 https://$host$request_uri;
}
```

```text
Implement robust containerized infrastructure with focus on security, scalability, and cost efficiency.
```
