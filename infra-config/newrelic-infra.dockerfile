FROM newrelic/infrastructure:latest
ENV NRIA_LICENSE_KEY=${NEW_RELIC_LICENSE_KEY}
CMD ["/usr/bin/newrelic-infra", "-config", "/etc/newrelic-infra.yml"]