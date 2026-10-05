# Setup guide

How to run the whole PetClinic stack plus Kafka on your laptop and check that it works.
This covers Phase 1; later phases extend it.

## Prerequisites

| Tool | Notes |
|---|---|
| Docker Desktop (or Docker Engine with Compose v2) | Engine 25 or newer |
| Git | On Windows, install Git for Windows; it includes Git Bash |
| Bash and curl | Needed for the smoke test. Included in Git Bash, WSL, macOS and Linux |
| Python 3.9 or newer | Only for the contract test |
| GitHub CLI (`gh`) | Only for the issue script |

Give Docker at least **8 GB of memory**. The container limits add up to about 6 GB
(eight Spring services at 512 MB, Kafka at 1 GB, Kafka UI at 512 MB, Grafana and Prometheus
at 256 MB each). On Docker Desktop this is under Settings > Resources; with the WSL 2 backend
it is set in `%UserProfile%\.wslconfig` instead.

Java and Maven are not needed to run the stack; the services start from published images.

## Start the stack

From the repository root:

```bash
docker compose up -d
```

The first run downloads the images and takes several minutes. After that, services need
one to two minutes to become healthy.

`kafka-init` is a one-off container: it creates the topics and exits. Seeing it as
`Exited (0)` in `docker compose ps -a` is expected.

To stop everything:

```bash
docker compose down
```

Kafka has no volume, so topics and messages are discarded on `down` and recreated by
`kafka-init` on the next `up`.

## What runs where

| Service | URL or port on your laptop |
|---|---|
| PetClinic UI and API gateway | http://localhost:8080 |
| Config server | http://localhost:8888 |
| Discovery server (Eureka) | http://localhost:8761 |
| customers-service | 8081 (see [Port 8081](#port-8081-is-already-in-use)) |
| visits-service | 8082 |
| vets-service | 8083 |
| genai-service | 8084 |
| Admin server | http://localhost:9090 |
| Zipkin | http://localhost:9411 |
| Grafana | http://localhost:3030 |
| Prometheus | http://localhost:9091 |
| Kafka UI | http://localhost:8090 |
| Kafka broker | `localhost:29092` from your laptop, `kafka:9092` from other containers |

Kafka topics created by `kafka-init`:

| Topic | Partitions | Purpose |
|---|---|---|
| `visit-events` | 3 | Visit events, keyed by pet id |
| `visit-events.DLT` | 1 | Events that still fail after retries |
| `smoke-test` | 1 | Scratch topic for the smoke test; messages expire after one minute |

Topic auto-creation is switched off on the broker. A new topic must be added to `kafka-init`
in `docker-compose.yml`.

## Smoke test

```bash
./scripts/phase1-smoke-test.sh
```

It runs 18 checks and exits with status 0 only when all 18 pass:

- health of the eight Spring services
- three gateway routes (customers, vets, visits)
- Zipkin, Grafana and Prometheus
- the Kafka port, Kafka UI, both `visit-events` topics with their partition counts,
  and a produce/consume round trip

The script waits up to two minutes in total for services that are still starting, so it can
be run straight after `docker compose up -d`. Set `WAIT=0` for a single attempt, or a larger
value on a slow machine, for example `WAIT=300 ./scripts/phase1-smoke-test.sh`.

CI validates the compose file and lints the scripts, but it does not start the stack.
Run the smoke test on each laptop before a demo and record the result in the sprint report.

## Contract test

Checks the example events in `contracts/examples/` against the VisitEvent schema. It does not
need the stack to be running.

```bash
pip install -r contracts/requirements.txt
python contracts/validate-events.py
```

It prints one PASS or FAIL line per example and exits with status 0 only when every example passes.
On Windows, if `python` opens the Microsoft Store, use `py` instead. CI runs the same test on
every pull request. Details are in [contracts/README.md](contracts/README.md).

## GitHub issues for the next phase

`scripts/create-github-issues.sh` creates the Phase 2 milestone, labels and issues.
Preview first; the dry run needs no login and changes nothing:

```bash
./scripts/create-github-issues.sh --dry-run
```

For the real run, install the GitHub CLI, sign in with `gh auth login`, and enable issues on
the fork (Settings > General > Features > Issues). Running it twice is safe: anything that
already exists is skipped. Each issue is assigned to its lead; the header of the script shows
how to change the GitHub user names.

## Further reading

- [What the baseline lacks](docs/spikes/baseline-limitations.md) (Phase 1 spike)
- [Architecture diagrams](docs/architecture.md)

## Windows notes

- Run the smoke test from **Git Bash** or **WSL 2**, not from PowerShell or cmd.exe.
  From PowerShell you can call it as
  `& "C:\Program Files\Git\bin\bash.exe" scripts/phase1-smoke-test.sh`.
- Docker Desktop must be running. From WSL, enable WSL integration for your distribution
  in Docker Desktop under Settings > Resources.
- Shell scripts must keep LF line endings. `.gitattributes` enforces this for new checkouts.
  If you see `$'\r': command not found`, your copy predates that rule; run
  `git add --renormalize .` and then `git checkout -- scripts`.
- `docker compose` commands work the same in PowerShell, cmd.exe and Git Bash.

## Troubleshooting

### Port 8081 is already in use

Windows often reserves 8081, and `docker compose up` then fails on customers-service.
Create a file named `.env` next to `docker-compose.yml` containing:

```
CUSTOMERS_PORT=18081
```

Then run `docker compose up -d` again. The file is ignored by Git, so it only affects your
machine, and the smoke test picks up the new port by itself. Nothing else is affected:
the gateway reaches customers-service inside Docker, not through this port.

### The chat assistant does not answer

Without an OpenAI key, genai-service starts with the placeholder key `demo`. The service is
healthy but chat requests fail. To use the chat, add a real key to `.env`:

```
OPENAI_API_KEY=your-key
```

### A check fails in the smoke test

```bash
docker compose ps -a
docker compose logs <service>
```

- Several services failing right after startup: wait a minute and rerun, or raise `WAIT`.
- Topic or round-trip checks failing: rerun topic creation with `docker compose up -d kafka-init`.
- Containers restarting or exiting with code 137: Docker is out of memory; raise the limit.
