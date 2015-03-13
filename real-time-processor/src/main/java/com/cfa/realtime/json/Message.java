package com.cfa.realtime.json;

import backtype.storm.tuple.Fields;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;

/**
 * Message parsed out from the following Json:
 * <pre>
 * {
 *     "userId": "134256",
 *     "currencyFrom": "EUR",
 *     "currencyTo": "GBP",
 *     "amountSell": 1000,
 *     "amountBuy": 747.10,
 *     "rate": 0.7471,
 *     "timePlaced" : "24-JAN-15 10:27:44",
 *     "originatingCountry" : "FR"
 * }
 * </pre>
 * <p/>
 * <br/><br/>Created by victor on 3/13/15.
 */
public class Message {

    final String userId;
    final String currencyFrom;
    final String currencyTo;
    final Double amountSell;
    final Double amountBuy;
    final Double rate;
    final DateTime timePlaced;
    final String originatingCountry;

    public Message(String userId, String currencyFrom, String currencyTo, Double amountSell, Double amountBuy,
                   Double rate, DateTime timePlaced, String originatingCountry) {
        this.userId = userId;
        this.currencyFrom = currencyFrom;
        this.currencyTo = currencyTo;
        this.amountSell = amountSell;
        this.amountBuy = amountBuy;
        this.rate = rate;
        this.timePlaced = timePlaced;
        this.originatingCountry = originatingCountry;
    }

    public List<Object> values() {
        return Arrays.<Object>asList(
                userId,
                currencyFrom,
                currencyTo,
                amountSell,
                amountBuy,
                rate,
                timePlaced,
                originatingCountry
        );
    }

    public static final Fields FIELDS = new Fields(
            "userId",
            "currencyFrom",
            "currencyTo",
            "amountSell",
            "amountBuy",
            "rate",
            "timePlaced",
            "originatingCountry"
    );
}
