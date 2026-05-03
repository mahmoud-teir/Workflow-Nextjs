# React Native Workflow Agents

This directory contains AI agent configurations designed to assist with specific tasks in the React Native development lifecycle. Each agent is specialized for a particular phase or skill set.

## Available Agents

### 1. `project-architect.md`
**Role:** Senior Mobile Architect  
**Responsibilities:**
- Analyze requirements and suggest optimal architecture patterns
- Recommend tech stack decisions (Expo vs CLI, State Management, Navigation)
- Review project structure and scalability concerns
- Generate technical specification documents

### 2. `code-generator.md`
**Role:** Expert React Native Developer  
**Responsibilities:**
- Generate boilerplate code for screens, components, and navigation
- Implement TypeScript types and interfaces
- Create reusable hooks and utility functions
- Write platform-specific code (iOS/Android)

### 3. `ui-designer.md`
**Role:** Mobile UI/UX Specialist  
**Responsibilities:**
- Convert design mockups to NativeWind/Tamagui components
- Ensure responsive layouts across devices
- Implement animations using React Native Reanimated
- Optimize component rendering performance

### 4. `api-integrator.md`
**Role:** Backend Integration Specialist  
**Responsibilities:**
- Set up Axios/Fetch clients with interceptors
- Implement authentication flows (OAuth, JWT)
- Handle error responses and retry logic
- Generate TypeScript types from OpenAPI/Swagger specs

### 5. `test-engineer.md`
**Role:** QA Automation Engineer  
**Responsibilities:**
- Write unit tests with Jest and React Native Testing Library
- Create integration tests for critical user flows
- Develop E2E tests using Detox
- Generate test coverage reports and identify gaps

### 6. `performance-optimizer.md`
**Role:** Performance Engineering Specialist  
**Responsibilities:**
- Analyze bundle size and suggest optimizations
- Identify rendering bottlenecks using React Profiler
- Implement code splitting and lazy loading
- Optimize image assets and memory usage

### 7. `security-auditor.md`
**Role:** Mobile Security Expert  
**Responsibilities:**
- Review code for security vulnerabilities
- Implement secure storage solutions
- Configure certificate pinning and SSL validation
- Audit third-party dependencies for known issues

### 8. `devops-engineer.md`
**Role:** CI/CD & Deployment Specialist  
**Responsibilities:**
- Configure EAS Build and Submit pipelines
- Set up automated testing in CI/CD workflows
- Manage app signing certificates and provisioning profiles
- Automate App Store and Play Store deployments

### 9. `debug-assistant.md`
**Role:** Debugging & Troubleshooting Expert  
**Responsibilities:**
- Analyze crash reports and stack traces
- Debug native module integration issues
- Resolve platform-specific bugs (iOS/Android)
- Provide step-by-step troubleshooting guides

### 10. `migration-specialist.md`
**Role:** Legacy Code Migration Expert  
**Responsibilities:**
- Plan migration strategies from older RN versions
- Convert JavaScript codebases to TypeScript
- Migrate from Redux to Zustand or other state managers
- Update deprecated APIs and libraries

## Usage Guidelines

1. **Select the Right Agent**: Choose the agent based on your current task phase
2. **Provide Context**: Always include relevant code snippets, error messages, and requirements
3. **Iterative Refinement**: Use follow-up prompts to refine generated code
4. **Review Generated Code**: Always review and test AI-generated code before production use

## Example Prompt Structure

```markdown
@agent: code-generator
@phase: 05-ui-components-styling
@task: Create a responsive login form with validation

Context:
- Using Expo SDK 52+
- Styling with NativeWind
- Form validation with react-hook-form + zod
- TypeScript enabled

Requirements:
- Email and password fields
- Real-time validation feedback
- Loading state during submission
- Error handling for network failures
```

## Integration with Workflow

These agents are designed to work seamlessly with the 15-phase workflow:
- **Phases 1-5**: Use `project-architect`, `code-generator`, `ui-designer`
- **Phases 6-8**: Use `api-integrator`, `code-generator`, `debug-assistant`
- **Phases 9-10**: Use `test-engineer`, `performance-optimizer`
- **Phases 11-13**: Use `security-auditor`, `devops-engineer`
- **Phases 14-15**: Use `devops-engineer`, `migration-specialist`, `debug-assistant`
