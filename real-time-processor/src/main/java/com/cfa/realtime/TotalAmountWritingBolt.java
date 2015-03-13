package com.cfa.realtime;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.cfa.commons.cassandra.CassandraClient;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
public class TotalAmountWritingBolt extends BaseBasicBolt {

    private final String cassandraAddress;
    private final int ttlSeconds;

    public TotalAmountWritingBolt(String cassandraAddress, int ttlSeconds) {
        this.cassandraAddress = cassandraAddress;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String country = input.getStringByField("country");
        String currency = input.getStringByField("currency");
        BigDecimal totalSell = (BigDecimal) input.getValueByField("totalSell");
        BigDecimal totalBuy = (BigDecimal) input.getValueByField("totalBuy");
        DateTime checkDateTime = (DateTime) input.getValueByField("checkDateTime");

        CassandraClient.getInstance(cassandraAddress)
                .insertTotalAmount(country, currency, totalSell, totalBuy, checkDateTime, ttlSeconds);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
