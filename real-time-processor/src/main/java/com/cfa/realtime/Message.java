package com.cfa.realtime;

import org.joda.time.DateTime;

import java.math.BigDecimal;

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
 *
 * <br/><br/>Created by victor on 3/13/15.
 */
public class Message {
    public String userId;
    public String currencyFrom;
    public String currencyTo;
    public BigDecimal amountSell;
    public BigDecimal amountBuy;
    public BigDecimal rate;
    public DateTime timePlaced;
    public String originatingCountry;
}
