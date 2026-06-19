# Agents

This project uses Claude Code agent skills stored under `.agents/skills/`. Each skill is a markdown file (`SKILL.md`) that the agent loads on demand.

## Available Skills

### `prd-writer`
**Path:** `.agents/skills/prd-writer/SKILL.md`

Generates complete PRDs (Product Requirements Documents) through an iterative clarification interview. Produces a structured 9-section document covering Executive Summary, Problem and Opportunity, Target Audience, Objectives, User Stories, Functionalities, Out of Scope, Dependency Graph, and Acceptance Criteria.

**Invoke when:** starting a new project or feature set and you need structured requirements before implementation.

---

### `spec-writer`
**Path:** `.agents/skills/spec-writer/SKILL.md`

Generates implementation-ready technical specifications (`spec.md`) and implementation plans (`plan.md`) for one or more PRD features. Supports a single-feature interactive mode and a batch mode that processes multiple features from the same wave in parallel.

Output files land in `docs/<feature-id>-<kebab-name>/`.

**Invoke when:** a PRD exists and you need detailed technical specs before coding starts. Reference: `.agents/skills/spec-writer/references/feature-template.md` for the document template.

---

### `implement-feature`
**Path:** `.agents/skills/implement-feature/SKILL.md`

Implements a feature autonomously from its existing `spec.md` + `plan.md`. Executes each plan phase in order, runs lint/typecheck/tests after each phase, commits once per phase, and produces a final report mapping results to the PRD acceptance criteria.

**Invoke when:** `spec.md` and `plan.md` exist for a feature and you want the agent to write and validate the code end-to-end.

---

## Typical Workflow

```
prd-writer  →  spec-writer  →  implement-feature
```

1. **prd-writer** — define the product (what to build and why).
2. **spec-writer** — translate PRD features into technical specs and plans.
3. **implement-feature** — execute the plan, committing one phase at a time.

## Project Docs

- PRD: `docs/PRD.md`
- Feature specs: `docs/<feature-id>-<kebab-name>/spec.md`
- Feature plans: `docs/<feature-id>-<kebab-name>/plan.md`
