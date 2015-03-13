package com.cfa.realtime;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.cfa.commons.Consts;
import com.cfa.realtime.cassandra.CassandraClient;
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

    private final int TOTAL_AMOUNT_SYNC_FREQUENCY_SEC = 120;
    private final int TRANSACTION_COUNT_WINDOW_SEC = 300;
    private final int TRANSACTION_COUNT_SLIDE_SEC = 20;
    // entries older than 1 day will be deleted
    private final int CASSANDRA_STATS_TTL_SEC = 86400;

    private final String topic = Consts.KAFKA_TOPIC;
    private final String zookeeperAddress;

    public TopologySubmitter(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    private KafkaSpout consumeSpout() {
        SpoutConfig kafkaConfig = new SpoutConfig(new ZkHosts(zookeeperAddress), topic, "", "storm");
        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        return new KafkaSpout(kafkaConfig);
    }

    public StormTopology createTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("consume-messages", consumeSpout(), 5);

        builder.setBolt("json-parser", new JsonParsingBolt(), 3).shuffleGrouping("consume-messages");

        // group by country, so that each country count would have its own separate bolt
        builder.setBolt("sliding-tx-counter", new SlidingTransactionCountingBolt(TRANSACTION_COUNT_WINDOW_SEC, TRANSACTION_COUNT_SLIDE_SEC), 3)
                .fieldsGrouping("json-parser", new Fields("originatingCountry"));

        builder.setBolt("total-amount-counter", new TotalAmountCountingBolt(TOTAL_AMOUNT_SYNC_FREQUENCY_SEC), 3)
                .fieldsGrouping("json-parser", new Fields("originatingCountry"));

        builder.setBolt("tx-counter-writer", new TransactionCounterWritingBolt(CASSANDRA_STATS_TTL_SEC), 2)
                .shuffleGrouping("sliding-tx-counter");

        builder.setBolt("total-amount-writer", new TotalAmountWritingBolt(CASSANDRA_STATS_TTL_SEC), 2)
                .shuffleGrouping("total-amount-counter");

        return builder.createTopology();
    }

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        if (args.length == 0) {
            System.err.println("Please run with arguments <ZOOKEPER_ADDRESS> <CASSANDRA_ADDRESS> <DOCKER_IP>");
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

        String cassandraAddress = args[1];
        CassandraClient.init(cassandraAddress);
        CassandraClient.getInstance().connect();
        CassandraClient.getInstance().createSchema();

        if (args.length == 3) {
            String dockerIp = args[2];
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
