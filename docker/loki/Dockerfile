FROM grafana/loki:3.0.0

# Create necessary directories
RUN mkdir -p /loki/wal /loki/chunks

# Copy configuration
COPY local-config.yaml /etc/loki/local-config.yaml

# Run as non-root user
USER 10001:10001

ENTRYPOINT ["/usr/bin/loki", "-config.file=/etc/loki/local-config.yaml"]