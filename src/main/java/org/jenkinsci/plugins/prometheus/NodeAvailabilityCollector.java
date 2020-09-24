package org.jenkinsci.plugins.prometheus;

import hudson.model.Computer;
import hudson.model.Node;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NodeAvailabilityCollector extends Collector {

  private static final Logger logger = LoggerFactory.getLogger(NodeAvailabilityCollector.class);
  private Jenkins jenkins;


  @Override
  public List<MetricFamilySamples> collect() {
    jenkins = Jenkins.get();

    String subsystem = ConfigurationUtils.getSubSystem();
    String namespace = ConfigurationUtils.getNamespace();
    List<MetricFamilySamples> samples = new ArrayList<>();
    Gauge nodeUpGauge = Gauge.build()
      .name("jenkins_node_up")
      .help("Is the node up and running")
      .labelNames("node_name")
      .namespace(namespace)
      .subsystem(subsystem)
      .create();

    for (Computer node : jenkins.getComputers()) {
      nodeUpGauge.labels(node.getDisplayName()).set(node.isOnline() ? 1 : 0);
      samples.addAll(nodeUpGauge.collect());
    }
    return samples;
  }
}
