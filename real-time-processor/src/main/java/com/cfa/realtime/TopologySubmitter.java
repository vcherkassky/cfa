package com.cfa.realtime;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import com.cfa.commons.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;

/**
 * <br/><br/>Created by victor on 3/12/15.
 */
public class TopologySubmitter {
    private static final Logger log = LoggerFactory.getLogger(TopologySubmitter.class);

    private final String topic = Consts.KAFKA_TOPIC;
    private final String zookeeperAddress;

    public TopologySubmitter(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    private KafkaSpout consumeSpout() {
        SpoutConfig spoutConfig = new SpoutConfig(new ZkHosts(zookeeperAddress),
                topic,
                "/kafka",  // zookeeper root path for offset storing TODO: this may be invalid
                "KafkaSpout");
        //TODO: there is something about timestamps => http://stackoverflow.com/questions/17807292/kafkaspout-is-not-receiving-anything-from-kafka
        return new KafkaSpout(spoutConfig);
    }

    public StormTopology createTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("consume-messages", consumeSpout());

        //TODO: add bolts
        return builder.createTopology();
    }

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        if (args.length != 1) {
            System.err.println("Please run with arguments <ZOOKEPER_ADDRESS>");
            System.exit(1);
        }
        String zookeeperAddress = args[0];

        Config config = new Config();
        //TODO: make this number configurable
        config.setNumWorkers(20);
        //TODO: tune this property
        config.setMaxSpoutPending(5000);

        TopologySubmitter submitter = new TopologySubmitter(zookeeperAddress);
        log.info("Submitting topology to Zookeeper address {}", zookeeperAddress);
        StormSubmitter.submitTopology("process-messages", config, submitter.createTopology());
    }
}
