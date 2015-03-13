package com.cfa.realtime;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.cfa.realtime.json.JsonParser;
import com.cfa.realtime.json.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
public class JsonParsingBolt extends BaseBasicBolt {
    private static final Logger log = LoggerFactory.getLogger(JsonParsingBolt.class);

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String json = input.getString(0);
        Message message;
        try {
            message = JsonParser.parseMessage(json);
        } catch (RuntimeException e) {
            collector.reportError(e);
            return;
        }
        collector.emit(message.values());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(Message.FIELDS);
    }

}
