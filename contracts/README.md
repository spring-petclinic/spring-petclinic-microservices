# VisitEvent contract

The agreed format of the event that visits-service publishes and notification-alert-service
consumes. Producer and consumer are built against this folder, not against each other.

| File | Purpose |
|---|---|
| `visit-event-schema.json` | The contract, as JSON Schema (draft 2020-12) |
| `examples/valid-*.json` | Events the schema must accept |
| `examples/invalid-*.json` | Events the schema must reject, one broken rule each |
| `validate-events.py` | Contract test: checks every example against the schema |

## Transport

| Setting | Value |
|---|---|
| Topic | `visit-events` |
| Message key | `petId`, so all events for one pet stay in order |
| Value | One VisitEvent as JSON |
| Dead-letter topic | `visit-events.DLT` |

## Fields

All nine fields are required, and no other fields are allowed.

| Field | Type | Rule |
|---|---|---|
| `eventId` | string | UUID, unique per event. Consumers use it to skip duplicates |
| `eventType` | string | `VISIT_SCHEDULED` or `VISIT_CANCELLED` |
| `visitId` | integer | 1 or greater |
| `petId` | integer | 1 or greater |
| `ownerId` | integer | 1 or greater |
| `visitDate` | string | Appointment date, `YYYY-MM-DD` |
| `visitType` | string | Upper case with digits and underscores, up to 40 characters, for example `ROUTINE`, `EMERGENCY`, `URGENT_CARE` |
| `description` | string | Free text, up to 8192 characters, may be empty |
| `occurredAt` | string | ISO-8601 timestamp in UTC ending in `Z`, for example `2026-10-01T14:32:05Z` |

`visitType` is deliberately not a fixed list. Which types count as urgent is configuration
(`petclinic.alerts.urgent-types`), and a type the consumer does not know is classified as ROUTINE.

Example:

```json
{
  "eventId": "3f2b8c1e-9a4d-4e6f-8b7a-1c2d3e4f5a6b",
  "eventType": "VISIT_SCHEDULED",
  "visitId": 12,
  "petId": 7,
  "ownerId": 6,
  "visitDate": "2026-10-15",
  "visitType": "ROUTINE",
  "description": "Annual check-up and rabies booster",
  "occurredAt": "2026-10-01T14:32:05Z"
}
```

## Running the contract test

Needs Python 3.9 or newer.

```bash
pip install -r contracts/requirements.txt
python contracts/validate-events.py
```

It prints one PASS or FAIL line per example and exits with status 0 only when all pass.
CI runs it with `--expect 11`, which also fails if an example is added or removed without
updating the workflow.

## Adding an example

Put a JSON file in `examples/`. The file name decides what is expected:

- `valid-NN-what-it-shows.json` must be accepted.
- `invalid-NN-what-is-wrong.json` must be rejected, and only one field may be wrong.
  An example with two faults fails the test, because it would not prove which rule caught it.

Then raise the number after `--expect` in `.github/workflows/phase1-ci.yml`.

## Changing the contract

A change to the schema affects both services. Change the schema, the examples and this README
in one pull request, and have the owners of the producer and the consumer review it.
Because unknown fields are rejected, adding a field is a contract change too.
