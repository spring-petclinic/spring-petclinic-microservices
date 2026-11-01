# UI regression checklist (W15)

Run by hand in a browser against the running stack, before each demo and after a change to the
UI or to an endpoint the UI uses. The backend counterpart is `scripts/regression-backend.sh`.

Automated browser tests were left out for now: the AngularJS client has no test setup, and adding
one did not fit the sprint.

## Pages that existed before Phase 2

| # | Step | Expected |
|---|---|---|
| 1 | Open http://localhost:8080 | Welcome page and the menu |
| 2 | Find Owners | List of owners with their pets |
| 3 | Veterinarians | List of vets with their specialties |
| 4 | Click an owner | Owner details, pets and their visits with correct dates |
| 5 | Register Owner, fill in the form (telephone has 12 digits), submit | Back on the list, new owner shown |
| 6 | Edit Owner, change the address, submit | The change is saved |
| 7 | Add New Pet, fill in the form, submit | Back on the owner page, new pet shown |
| 8 | Add Visit, choose a date, enter a description, submit | Back on the owner page, new visit shown with that date |

## Phase 2 additions

| # | Step | Expected |
|---|---|---|
| 9 | Add Visit form | Visit type dropdown, ROUTINE selected |
| 10 | Book with type EMERGENCY | Owner page shows the type and status SCHEDULED |
| 11 | Check `/api/alert/audit?petId=<pet>` | One new record, severity EMERGENCY |
| 12 | Click Cancel on that visit | Status CANCELLED, the button disappears, the row stays |
| 13 | Check the audit search again | One more record, VISIT_CANCELLED |

## Results

| Date | Where | Steps | Result | Stack |
|---|---|---|---|---|
| 2026-11-01 | Jawad's laptop | 1 to 13 | All as expected | Own images, Spring Boot 4.0.1, built from the W9 branch |

This run is on the Phase 2 code. No run on the code before Phase 2 was recorded, so there is no
separate "before" baseline.
