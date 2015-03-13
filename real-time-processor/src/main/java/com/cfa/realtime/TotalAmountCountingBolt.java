package com.cfa.realtime;

import backtype.storm.Config;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static com.cfa.realtime.SlidingTransactionCountingBolt.isTick;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
public class TotalAmountCountingBolt extends BaseRichBolt {
    private static final Logger log = LoggerFactory.getLogger(TotalAmountCountingBolt.class);

    private final int emitFrequencySeconds;

    private final Map<String, Map<String, CurrencyStats>> currenciesByCountry = Maps.newHashMap();

    private OutputCollector collector;

    public TotalAmountCountingBolt(int emitFrequencySeconds) {
        this.emitFrequencySeconds = emitFrequencySeconds;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        if (isTick(input)) {
            log.info("Tick received, emitting");
            for (Map.Entry<String, Map<String, CurrencyStats>> entry : currenciesByCountry.entrySet()) {
                String country = entry.getKey();
                for (CurrencyStats currencyStats : entry.getValue().values()) {
                    //TODO: this should be a debug level
                    log.info("Country {} currency {} totalSell {} totalBuy {}", country, currencyStats.currency,
                            currencyStats.totalSell, currencyStats.totalSell);
                    collector.emit(Arrays.<Object>asList(country, currencyStats.currency, currencyStats.totalSell,
                            currencyStats.totalSell, new DateTime()));
                }
            }
        } else {
            try {
                String country = input.getStringByField("originatingCountry");
                String currencySell = input.getStringByField("currencyFrom");
                Double amountSell = input.getDoubleByField("amountSell");
                String currencyBuy = input.getStringByField("currencyTo");
                Double amountBuy = input.getDoubleByField("amountBuy");

                trackTransaction(country, currencySell, amountSell, currencyBuy, amountBuy);
            } catch (RuntimeException e) {
                log.error("Could not count stats for tuple " + input, e);
                collector.reportError(e);
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("country", "currency", "totalSell", "totalBuy", "checkDateTime"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return ImmutableMap.<String, Object>of(
                Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequencySeconds);
    }

    private void trackTransaction(String country, String currencySell, Double amountSell, String currencyBuy,
                                  Double amountBuy) {
        getStats(country, currencySell).addSell(amountSell);
        getStats(country, currencyBuy).addSell(amountBuy);
    }

    private CurrencyStats getStats(String country, String currency) {
        Map<String, CurrencyStats> currencies = currenciesByCountry.get(country);
        if (currencies == null) {
            currencies = Maps.newHashMap();
            currenciesByCountry.put(country, currencies);
        }
        CurrencyStats stats = currencies.get(currency);
        if (stats == null) {
            stats = new CurrencyStats(currency);
            currencies.put(currency, stats);
        }
        return stats;
    }

    private class CurrencyStats {
        final String currency;

        BigDecimal totalSell = BigDecimal.ZERO;
        BigDecimal totalBuy = BigDecimal.ZERO;

        private CurrencyStats(String currency) {
            this.currency = currency;
        }

        public void addSell(double amount) {
            totalSell = totalSell.add(new BigDecimal(amount));
        }

        public void addBuy(double amount) {
            totalBuy = totalBuy.add(new BigDecimal(amount));
        }
    }
}
