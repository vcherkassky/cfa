package com.cfa.realtime;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
public class JsonParsingBolt extends BaseBasicBolt {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("dd-MMM-yy HH:mm:ss")
            .create();

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Message message = gson.fromJson(input.toString(), Message.class);
        collector.emit(Arrays.<Object>asList(
                message.userId,
                message.currencyFrom,
                message.currencyTo,
                message.amountSell,
                message.amountBuy,
                message.rate,
                message.timePlaced,
                message.originatingCountry
        ));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(
                "userId",
                "currencyFrom",
                "currencyTo",
                "amountSell",
                "amountBuy",
                "rate",
                "timePlaced",
                "originatingCountry"
        ));
    }
}
