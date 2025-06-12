FROM grafana/promtail:2.8.0

COPY config.yml /etc/promtail/config.yml

ENTRYPOINT ["/usr/bin/promtail", "-config.file=/etc/promtail/config.yml"]
