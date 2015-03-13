package com.cfa.realtime;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Accepts data from JsonParsingBolt
 *
 * <br/><br/>Created by victor on 3/13/15.
 */
public class SlidingTransactionCountingBolt extends BaseRichBolt {
    private static final Logger log = LoggerFactory.getLogger(SlidingTransactionCountingBolt.class);

    private final Map<String, LinkedList<Integer>> transactionsPerCountry = Maps.newHashMap();

    // number of counting (or stop) points in a window
    private final int windowCellsCount;
    // total window size in seconds - will be aligned by windowSlideFrequency
    private final int windowSizeSeconds;
    // seconds between window slides
    private final int windowSlideFrequencySeconds;

    private OutputCollector collector;

    public SlidingTransactionCountingBolt(int windowSizeSeconds, int windowSlideFrequencySeconds) {
        this.windowSlideFrequencySeconds = windowSlideFrequencySeconds;
        this.windowCellsCount = (windowSizeSeconds + windowSlideFrequencySeconds - 1) / windowSlideFrequencySeconds;
        checkArgument(windowCellsCount > 1);

        // align window size by slide frequency
        this.windowSizeSeconds = windowCellsCount * windowSlideFrequencySeconds;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        if (isTick(input)) {
            log.info("Tick received, sliding the window");
            Map<String, Integer> countsForEmission = slideWindow();
            for (Map.Entry<String, Integer> entry : countsForEmission.entrySet()) {
                //TODO: this should be a debug level
                log.info("Country {} transactions {} during last {} seconds", entry.getKey(), entry.getValue(), windowSizeSeconds);
                collector.emit(Arrays.<Object>asList(entry.getKey(), entry.getValue(), windowSizeSeconds));
            }
        } else {
            String country = input.getStringByField("originatingCountry");
            DateTime timePlaced = (DateTime) input.getValueByField("timePlaced");
            // if event does not fit current window, ignore it
            if (timePlaced.plus(Period.seconds(windowSizeSeconds)).isBeforeNow()) {
                log.warn("Ignoring an event too far in the past: '{}'", timePlaced);
            } else {
                countTransaction(country);
            }
        }
    }

    private void countTransaction(String country) {
        LinkedList<Integer> window = window(country);
        window.addLast(window.removeLast() + 1);
    }

    private Map<String, Integer> slideWindow() {
        Map<String, Integer> currentCounts = Maps.newHashMap();
        for (Map.Entry<String, LinkedList<Integer>> entry : transactionsPerCountry.entrySet()) {
            String country = entry.getKey();
            LinkedList<Integer> window = entry.getValue();
            int totalCount = 0;
            for (Integer count : window) {
                totalCount += count;
            }
            currentCounts.put(country, totalCount);
            if (window.size() >= windowCellsCount)
                window.removeFirst();
            window.addLast(0);
        }
        return currentCounts;
    }

    private LinkedList<Integer> window(String country) {
        LinkedList<Integer> window = transactionsPerCountry.get(country);
        if (window == null) {
            window = new LinkedList<>();
            window.add(0);
            transactionsPerCountry.put(country, window);
        }
        return window;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("country", "transactions", "windowSizeSeconds"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return ImmutableMap.<String, Object>of(
                Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, windowSlideFrequencySeconds);
    }

    public static boolean isTick(Tuple tuple) {
        return tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID)
                && tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID);
    }
}
