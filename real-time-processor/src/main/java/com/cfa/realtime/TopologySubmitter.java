package com.cfa.realtime;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.cfa.commons.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <br/><br/>Created by victor on 3/12/15.
 */
public class TopologySubmitter implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(TopologySubmitter.class);

    private final String topic = Consts.KAFKA_TOPIC;
    private final String zookeeperAddress;

    public TopologySubmitter(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    private KafkaSpout consumeSpout() {
        SpoutConfig kafkaConfig = new SpoutConfig(new ZkHosts(zookeeperAddress), topic, "", "storm");
        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        //TODO: there is something about timestamps => http://stackoverflow.com/questions/17807292/kafkaspout-is-not-receiving-anything-from-kafka
        return new KafkaSpout(kafkaConfig);
    }

    public StormTopology createTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("consume-messages", consumeSpout(), 10);

        builder.setBolt("log", new BaseBasicBolt() {
            @Override
            public void execute(Tuple input, BasicOutputCollector collector) {
                log.debug("Received tuple {}", input);
            }

            @Override
            public void declareOutputFields(OutputFieldsDeclarer declarer) {
            }
        });
        //TODO: add bolts
        return builder.createTopology();
    }

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        if (args.length == 0) {
            System.err.println("Please run with arguments <ZOOKEPER_ADDRESS> <DOCKER_IP>");
            System.exit(1);
        }
        String zookeeperAddress = args[0];
        log.info("Submitting topology to Zookeeper address {}", zookeeperAddress);

        Config config = new Config();
        //TODO: make these numbers configurable
        config.setNumWorkers(2);
        config.setMaxTaskParallelism(5);
        //TODO: tune this property
        config.setMaxSpoutPending(5000);

        TopologySubmitter submitter = new TopologySubmitter(zookeeperAddress);

        if (args.length == 2) {
            String dockerIp = args[1];
            config.put(Config.NIMBUS_HOST, dockerIp);
            config.put(Config.NIMBUS_THRIFT_PORT, 6627);
            config.put(Config.STORM_ZOOKEEPER_PORT, 2181);
            config.put(Config.STORM_ZOOKEEPER_SERVERS, Arrays.asList(dockerIp));
            StormSubmitter.submitTopology("process-messages", config, submitter.createTopology());
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("process-messages", config, submitter.createTopology());
        }
    }
}
