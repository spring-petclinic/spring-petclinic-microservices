#!/usr/bin/env python3
"""Contract test for the VisitEvent schema.

Validates every example in contracts/examples/ against contracts/visit-event-schema.json.
The file name says what is expected:

    valid-*.json    must be accepted by the schema
    invalid-*.json  must be rejected, and for exactly one field, so that each example
                    proves one rule instead of failing for an unrelated reason

Exits 0 only when every example behaves as expected.

Usage (from anywhere):
    pip install -r contracts/requirements.txt
    python contracts/validate-events.py            # run all examples
    python contracts/validate-events.py --expect 11  # also fail if the number of examples differs
"""

import argparse
import datetime
import json
import os
import sys
from pathlib import Path

try:
    from jsonschema import Draft202012Validator, FormatChecker
except ImportError:
    sys.exit("jsonschema is not installed. Run: pip install -r contracts/requirements.txt")

CONTRACTS_DIR = Path(__file__).resolve().parent
SCHEMA_FILE = CONTRACTS_DIR / "visit-event-schema.json"
EXAMPLES_DIR = CONTRACTS_DIR / "examples"

USE_COLOR = sys.stdout.isatty() and not os.environ.get("NO_COLOR")
GREEN, RED, BOLD, RESET = ("\033[0;32m", "\033[0;31m", "\033[1m", "\033[0m") if USE_COLOR else ("", "", "", "")


def build_validator():
    schema = json.loads(SCHEMA_FILE.read_text(encoding="utf-8"))
    Draft202012Validator.check_schema(schema)

    # "uuid" and "date" are checked by jsonschema out of the box. "date-time" normally needs an
    # extra package, so it is checked here to keep the test independent of what is installed.
    formats = FormatChecker()

    @formats.checks("date-time", raises=ValueError)
    def is_date_time(value):
        if isinstance(value, str):
            datetime.datetime.fromisoformat(value.replace("Z", "+00:00"))
        return True

    return Draft202012Validator(schema, format_checker=formats)


def field_of(error):
    """Name of the field a validation error is about."""
    if error.path:
        return str(error.path[0])
    if error.validator == "required":
        # message looks like: 'petId' is a required property
        return error.message.split("'")[1]
    return "(event)"


def reason(error):
    """Short reason for the report; pattern errors would otherwise print the whole regex."""
    if error.validator in ("pattern", "format"):
        return f"{error.instance!r} is not in the required format"
    return error.message


def run_example(validator, path):
    """Returns (passed, detail) for one example file."""
    name = path.name
    if not name.startswith(("valid-", "invalid-")):
        return False, "file name must start with valid- or invalid-"
    expect_valid = name.startswith("valid-")

    try:
        event = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return False, f"not parseable as JSON: {exc}"

    errors = sorted(validator.iter_errors(event), key=lambda e: (field_of(e), e.message))
    fields = sorted({field_of(e) for e in errors})

    if expect_valid:
        if not errors:
            return True, "accepted"
        return False, "expected to be accepted, but: " + "; ".join(f"{field_of(e)}: {e.message}" for e in errors)

    if not errors:
        return False, "expected to be rejected, but the schema accepted it"
    if len(fields) > 1:
        return False, "expected one faulty field, found " + ", ".join(fields)
    return True, f"rejected ({fields[0]}: {reason(errors[0])})"


def main():
    parser = argparse.ArgumentParser(description="Validate VisitEvent examples against the schema.")
    parser.add_argument("--expect", type=int, metavar="N", help="fail unless exactly N examples were checked")
    args = parser.parse_args()

    validator = build_validator()
    examples = sorted(EXAMPLES_DIR.glob("*.json"), key=lambda p: (not p.name.startswith("valid-"), p.name))
    if not examples:
        print(f"{RED}ERROR{RESET}: no examples found in {EXAMPLES_DIR}")
        return 2

    print(f"{BOLD}VisitEvent contract test{RESET} ({SCHEMA_FILE.name}, {len(examples)} examples)\n")
    passed = 0
    failed_names = []
    width = max(len(p.name) for p in examples)
    for number, path in enumerate(examples, start=1):
        ok, detail = run_example(validator, path)
        label = f"{GREEN}[PASS]{RESET}" if ok else f"{RED}[FAIL]{RESET}"
        print(f"{label} {number:2d}. {path.name:<{width}}  {detail}")
        if ok:
            passed += 1
        else:
            failed_names.append(path.name)

    total = len(examples)
    print()
    if args.expect is not None and total != args.expect:
        print(f"{RED}ERROR{RESET}: checked {total} examples, expected {args.expect}")
        return 2
    if failed_names:
        print(f"{RED}{BOLD}RESULT: {passed}/{total} passed, {len(failed_names)} FAILED{RESET}")
        for name in failed_names:
            print(f"  - {name}")
        return 1
    print(f"{GREEN}{BOLD}RESULT: {passed}/{total} PASS{RESET}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
