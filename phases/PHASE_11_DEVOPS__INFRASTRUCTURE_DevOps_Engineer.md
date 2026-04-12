<a name="phase-11"></a>
# 📌 PHASE 11: DEVOPS & INFRASTRUCTURE (DevOps Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 11.1: Docker Setup

```text
You are a DevOps Engineer. Create a multi-stage Docker build tailored specifically for a Next.js application.

Constraints:
- You MUST utilize the Next.js `standalone` output mode to shrink the final image size (< 200MB target).
- Explicitly disable Next.js Telemetry inside the builder and runner stages.
- The Node.js production runner must execute as a non-root user (`nextjs`).
- Provide an overarching architecture diagram or explanation of how the container will be orchestrated.

Required Output Format: Provide complete code for:
1. `next.config.ts`: Modifying the config to output `standalone`.
2. `Dockerfile`: Utilizing Alpine Node.js or Distroless.
3. A `.dockerignore` file perfectly tailored to Next.js (excluding `.next`, `node_modules`).
4. A Docker Compose (`docker-compose.yml`) file that pairs the Next.js app with a local PostgreSQL and Redis instance for local development.

⚠️ Common Pitfalls:
- **Pitfall:** `standalone` mode does not automatically copy the `public` or `.next/static` folders into the runner stage, resulting in broken images and CSS.
- **Solution:** Manually `COPY` the `public` and `.next/static` directories in the final runner stage of the Dockerfile.
```

✅ **Verification Checklist:**
- [ ] Run `docker build -t app .`. Ensure the resulting image is under 300MB.
- [ ] Run `docker run -p 3000:3000 app`. Verify the app serves correctly on localhost without crashing due to missing `.env` variables (it should fail gracefully).

---

### Prompt 11.2: Infrastructure as Code (Terraform)

```text
You are a Cloud Infrastructure Architect. Set up scalable AWS infrastructure using Terraform for a Next.js container.

Constraints:
- All state must be managed via a remote backend (e.g., S3).
- Do not hardcode sensitive secrets in Terraform files; use AWS Secrets Manager or variables.
- Optimize for cost: use FARGATE_SPOT for non-prod environments.

Required Output Format: Provide complete `.tf` logic for:
1. VPC configuration (Public/Private Subnets, NAT Gateway).
2. ECS Fargate Cluster setup.
3. ALB (Application Load Balancer) mapping port 80/443 to the container's 3000.
4. RDS PostgreSQL provisioning (if using AWS).

⚠️ Common Pitfalls:
- **Pitfall:** Using a multi-AZ NAT gateway for development environments, incurring hundreds of dollars in idle AWS fees.
- **Solution:** Add conditional logic (`single_nat_gateway = true`) for non-production environments.
```

✅ **Verification Checklist:**
- [ ] Run `terraform plan`. Ensure it succeeds without outputting plaintext passwords to the console.

---

### Prompt 11.3: Nginx Configuration (Reverse Proxy)

```text
You are a Linux Sysadmin. Create a modern Nginx configuration for a self-hosted Next.js app.

Constraints:
- HTTP traffic MUST redirect to HTTPS.
- Websockets (used by Next.js HMR and some features) must be explicitly proxied.
- Next.js static assets (`/_next/static/`) should be cached by Nginx to reduce node process load.

Required Output Format:
1. Provide the main `nginx.conf` virtual host file.
2. Outline the SSL certification strategy (certbot/Let's Encrypt).
```

✅ **Verification Checklist:**
- [ ] Run `nginx -t` to verify syntax.
- [ ] Ensure requesting a static JS chunk returns an Nginx-level `Cache-Control: public, max-age=31536000, immutable` header.

---
📎 **Related Phases:**
- Prerequisites: [Phase 10: Performance Optimization](./PHASE_10_PERFORMANCE_OPTIMIZATION_Frontend_Backend_DevOps.md)
- Proceeds to: [Phase 12: Observability & Monitoring](./PHASE_12_OBSERVABILITY__MONITORING_DevOps_SRE.md)
