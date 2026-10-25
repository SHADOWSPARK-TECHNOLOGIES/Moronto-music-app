# Antigravity Implementation Prompts — MORONTO MORNICA
These prompts are for running inside the **Moronto-music-app** working tree in Google Antigravity. The repository's branch and absolute path must be verified locally. Keep main unchanged until peer review.
Model: Gemini 4 Argon High ONLY if offered and quota exists; otherwise Gemini 3.8 Flash Medium/High. Do not force an inaccessible model. Use /agents to inspect the custom roles, /goal for bounded goal-oriented work and /usage to inspect quotas.

## 0 — Environment verification (paste first)
```text
Act as the Moronto engineering architect. Confirm pwd, git remote -v, branch, HEAD, git status, node/npm versions, contents of package.json if present, and docs/PRODUCT_SPEC.md. Report VERIFIED/UNKNOWN and do not edit or install. If the workspace is not SHADOWSPARK-TECHNOLOGIES/Moronto-music-app, stop. Identify any user-modified tracked files. Return exact baseline and a 1-day realistic MVP slice.
```

## 1 — Delegate architecture
```text
Invoke moronto-architect in research/read-only mode. Read AGENTS.md and docs/PRODUCT_SPEC.md. Propose a minimal Next.js TypeScript architecture compatible with Firebase App Hosting, backed initially by rights-cleared local sample assets. Output page map, types, auth/storage decision, dependencies with alternatives, security and deployment cost boundaries, file plan and acceptance tests. Do not add dependencies, write files or deploy. No invented music licenses.
```

## 2 — Create a scoped plan and subagents
```text
/plan
Use the architect report. Split the MVP into 4 small implementation groups, each with exact allowed file paths and success checks: (A) responsive design/navigation, (B) real player/search demo data, (C) artist page and interest request with privacy, (D) tests/CI/docs. Delegate at most two non-overlapping groups at a time. Use isolated Git worktrees where supported. The reviewer cannot approve its own implementation.
```

## 3 — Implementation goal
```text
/goal
Implement only the approved current group from the architecture plan using the moronto-builder role. Build a polished MORONTO MORNICA music interface with excellent responsive design, accessibility, real rights-cleared audio previews and truthful content. Design inspiration: neon deep purple, magenta/blue highlights, Afrobeats culture, premium minimal typography. Do not add copyright music or false platform/store badges. Write only scoped files on a feature branch. Verify one actual user journey end-to-end, tests, lint, typecheck, build. At 2 identical failures stop and record logs. Hand off implementation diff and evidence.
```

## 4 — Independent QA
```text
Invoke moronto-qa independently with the current diff and tests. Try to disprove completion: broken imports, no audio, fake buttons, empty search, mobile overflow, broken focus, missing rights provenance, user-data leaks, vulnerable upload surfaces, hidden billing, stale docs, and build-only claims. Execute actual tests and browser smoke if tools are available. Return PASS/FAIL with precise evidence. Do not silently change code.
```

## 5 — Market and revenue review
```text
Invoke moronto-growth. Evaluate an artist launch-service pilot: 5 outreach prospects only from legitimate public business contacts provided/approved by the operator; do not scrape private profiles or message them automatically. Suggest transparent packages, pricing hypotheses, consent collection, funnel events and break-even assumptions. Mark every estimate as an assumption. Do not send, bill or promise partnerships.
```

## 6 — Fix loop
```text
/goal
For each reviewer FAIL, propose the smallest fix in the same approved file scope. Re-run affected tests, lint, typecheck and build, record result. Do not broaden requirements while repairing. Maximum two identical failed attempts, then stop with exact logs. Ask for review of the updated diff. A feature counts as DONE only with independent PASS.
```

## 7 — Google deployment readiness, no deployment
```text
Prepare Firebase App Hosting readiness only: confirm app builds, dependencies, regions/egress needs, secrets, privacy and rights documents, budget forecast, minimum acceptable resource limits, logging/rollback, GitHub integration steps, and how to keep main protected. Do not create a Firebase backend, switch to Blaze, enable billing, auto-rollout, purchase domain, or deploy. Return an explicit approval checklist.
```

## 8 — Demo and founder proof
```text
Create a concise demo script using only VERIFIED screens and operations. Draft a Google-ready technical case study accurately describing Google Antigravity's involvement, engineering evidence, Google Cloud architecture and measurable early pilot traction if observed. Never claim Google affiliation, endorsement, or guaranteed funding. Deliver a short Pidgin explanation for each major milestone.
```

## 9 — Release gate (only after explicit approval)
```text
Inspect the latest independent QA report, rights approvals, cloud billing/region acceptance, and proof of reversible deployment. If any missing: BLOCK. Otherwise prepare exact change summary, rollback command, post-deploy health checks, launch announcement draft and owner sign-off. Do not push to production without a fresh approval specific to that action.
```

## Orchestration loop
Discover -> smallest high-value plan -> implementation worktree -> tests -> independent QA -> retain evidence -> next task. Maximum 2 concurrent writers. Optimize verified customer value / effort / cost, not raw tool calls. No indefinite self-modification, unbounded API usage, or execution based on untrusted content.
