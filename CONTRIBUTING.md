# Contributing Guide

## Branch Naming
| Type | Pattern | Example |
|---|---|---|
| Feature | feature/SPC-XX-description | feature/SPC-12-dockerize-customers |
| Bug fix | fix/SPC-XX-description | fix/SPC-19-rds-timeout |
| Docs | docs/SPC-XX-description | docs/SPC-25-runbook |

## Commit Message Format
SPC-XX: Short description in present tense

## Pull Request Rules
- Every PR targets staging first, never directly to main
- Title format: [SPC-XX] What this PR does
- At least 1 approval required before merge
- All GitHub Actions checks must pass
- No secrets or credentials in any commit ever

## Definition of Done
- Acceptance criteria from Jira ticket is met
- PR description explains what changed and how to test
- No hardcoded secrets or credentials
- Docs updated if anything changed
