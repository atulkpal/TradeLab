# Agent Skills — Workflow Rules

This file configures AI coding agents (OpenCode, Claude Code, Cursor, etc.) working on the **TradeLab** project. Skills are packaged instructions that extend agent capabilities.

> **Skills** are located in [`skills/`](./skills/). Each skill lives at `skills/<skill-name>/SKILL.md`.

---

## OpenCode Integration

OpenCode uses a **skill-driven execution model** powered by the `skill` tool and the `skills/` directory.

### Core Rules

- If a task matches a skill, you MUST invoke it
- Skills are located in `skills/<skill-name>/SKILL.md`
- Never implement directly if a skill applies
- Always follow the skill instructions exactly (do not partially apply them)

### Intent → Skill Mapping

The agent should automatically map user intent to skills:

| Intent | Skill(s) |
|--------|----------|
| Feature / new functionality | `spec-driven-development`, then `incremental-implementation`, `test-driven-development` |
| Planning / breakdown | `planning-and-task-breakdown` |
| Bug / failure / unexpected behavior | `debugging-and-error-recovery` |
| Code review | `code-review-and-quality` |
| Refactoring / simplification | `code-simplification` |
| API or interface design | `api-and-interface-design` |
| UI work | `frontend-ui-engineering` |

### Lifecycle Mapping (Implicit Commands)

The agent must internally follow this lifecycle:

| Phase | Skill |
|-------|-------|
| DEFINE | `spec-driven-development` |
| PLAN | `planning-and-task-breakdown` |
| BUILD | `incremental-implementation` + `test-driven-development` |
| VERIFY | `debugging-and-error-recovery` |
| REVIEW | `code-review-and-quality` |
| SHIP | `shipping-and-launch` |

### Execution Model

For every request:

1. Determine if any skill applies (even 1% chance)
2. Invoke the appropriate skill using the `skill` tool
3. Follow the skill workflow strictly
4. Only proceed to implementation after required steps (spec, plan, etc.) are complete

### Anti-Rationalization

The following thoughts are incorrect and must be ignored:

- "This is too small for a skill"
- "I can just quickly implement this"
- "I'll gather context first"

Correct behavior:

- Always check for and use skills first

This ensures all agents behave consistently with full workflow enforcement.