package com.cfa.realtime;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.cfa.realtime.cassandra.CassandraClient;
import org.joda.time.DateTime;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
public class TransactionCounterWritingBolt extends BaseBasicBolt {

    private final String cassandraAddress;
    private final int ttlSeconds;

    public TransactionCounterWritingBolt(String cassandraAddress, int ttlSeconds) {
        this.cassandraAddress = cassandraAddress;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String country = input.getStringByField("country");
        Long transactions = Long.valueOf(input.getIntegerByField("transactions"));
        Integer windowSizeSeconds = input.getIntegerByField("windowSizeSeconds");
        DateTime checkDateTime = (DateTime) input.getValueByField("checkDateTime");

        CassandraClient.getInstance(cassandraAddress)
                .insertTransactionsCount(country, transactions, windowSizeSeconds, checkDateTime, ttlSeconds);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}