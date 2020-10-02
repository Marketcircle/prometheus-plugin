package org.jenkinsci.plugins.prometheus.service;

import hudson.ExtensionList;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
import jenkins.metrics.api.Metrics;
import org.jenkinsci.plugins.prometheus.ExecutorCollector;
import org.jenkinsci.plugins.prometheus.JenkinsStatusCollector;
import org.jenkinsci.plugins.prometheus.util.MetricsFormatter;
import org.jenkinsci.plugins.prometheus.NodeAvailabilityCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultPrometheusMetrics implements PrometheusMetrics {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPrometheusMetrics.class);

    private final CollectorRegistry collectorRegistry;
    private final AtomicReference<String> cachedMetrics;

    public DefaultPrometheusMetrics() {
        CollectorRegistry collectorRegistry = CollectorRegistry.defaultRegistry;
        collectorRegistry.register(new JenkinsStatusCollector());
        collectorRegistry.register(new ExecutorCollector());

        // other collectors from other plugins
        ExtensionList.lookup(Collector.class).forEach( c -> collectorRegistry.register(c));

        collectorRegistry.register(new NodeAvailabilityCollector());
        DefaultExports.initialize();

        this.collectorRegistry = collectorRegistry;
        this.cachedMetrics = new AtomicReference<>("");
    }

    @Override
    public String getMetrics() {
        return cachedMetrics.get();
    }

    @Override
    public void collectMetrics() {
        try (StringWriter buffer = new StringWriter()) {
            TextFormat.write004(buffer, collectorRegistry.metricFamilySamples());
            cachedMetrics.set(MetricsFormatter.formatMetrics(buffer.toString()));
        } catch (IOException e) {
            logger.debug("Unable to collect metrics");
        }
    }
}
