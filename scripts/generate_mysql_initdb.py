#!/usr/bin/env python3
"""Regenerate the mysql-initdb ConfigMap in k8s/06-mysql.yaml.

The MySQL schema is shared: visits.pet_id is a foreign key onto pets.id, which
customers-service owns. So the files must be applied in dependency order, which
is what the numeric prefixes below encode (MySQL's entrypoint runs
/docker-entrypoint-initdb.d in alphabetical order).

Run after editing any db/mysql/*.sql, then re-apply the manifest. Note that the
scripts only run when MySQL initialises an empty data directory - an existing
PVC keeps its schema.
"""
import io
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MANIFEST = os.path.join(ROOT, "k8s", "06-mysql.yaml")

ORDER = [
    ("01-customers-schema.sql", "spring-petclinic-customers-service/src/main/resources/db/mysql/schema.sql"),
    ("02-vets-schema.sql",      "spring-petclinic-vets-service/src/main/resources/db/mysql/schema.sql"),
    ("03-visits-schema.sql",    "spring-petclinic-visits-service/src/main/resources/db/mysql/schema.sql"),
    ("04-customers-data.sql",   "spring-petclinic-customers-service/src/main/resources/db/mysql/data.sql"),
    ("05-vets-data.sql",        "spring-petclinic-vets-service/src/main/resources/db/mysql/data.sql"),
    ("06-visits-data.sql",      "spring-petclinic-visits-service/src/main/resources/db/mysql/data.sql"),
]


def render_data():
    out = []
    for key, rel in ORDER:
        # utf-8-sig: some of these files carry a BOM
        body = io.open(os.path.join(ROOT, rel), encoding="utf-8-sig").read().rstrip("\n")
        if "USE petclinic" not in body:
            body = "USE petclinic;\n\n" + body
        indented = "\n".join(("    " + l).rstrip() for l in body.splitlines())
        out.append("  %s: |\n%s\n" % (key, indented))
    return "".join(out)


def main():
    manifest = io.open(MANIFEST, encoding="utf-8").read()
    pattern = re.compile(
        r"(?P<head>name: mysql-initdb\n  namespace: petclinic\ndata:\n)(?P<data>.*?)(?P<tail>^---$)",
        re.DOTALL | re.MULTILINE,
    )
    if not pattern.search(manifest):
        sys.exit("could not locate the mysql-initdb ConfigMap in %s" % MANIFEST)
    updated = pattern.sub(lambda m: m.group("head") + render_data() + m.group("tail"), manifest)
    if updated == manifest:
        print("k8s/06-mysql.yaml already up to date")
        return
    io.open(MANIFEST, "w", encoding="utf-8", newline="").write(updated)
    print("k8s/06-mysql.yaml regenerated from db/mysql/*.sql")


if __name__ == "__main__":
    main()
