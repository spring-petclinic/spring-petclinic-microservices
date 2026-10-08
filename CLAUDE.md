# Spring Petclinic - Claude Engineering Guide

## Project Context & Commands
- Build Command: `./mvnw clean test-compile`
- Test Command: `./mvnw test`

## Multi-File Optimization Triggers
Whenever a task requests an "Architecture Alignment", "Performance Audit", or "Security Harden", check and update the following files systematically:
1. **Validation Checks:** Ensure `@NotBlank` and custom domain triggers match both Entity definitions (`src/main/java/org/springframework/samples/petclinic/model/`) and their respective HTTP controllers (`src/main/java/org/springframework/samples/petclinic/owner/`).
2. **Cache Compliance:** Look for the `// CLAUDE-ACTION: TRIGGER-CACHE-ALIGNMENT` marker. Ensure database read methods are cleanly aligned with Spring Cache annotations across repositories and controllers.
3. **Traceability:** Maintain `@UseCase` tracing metrics across the application services.