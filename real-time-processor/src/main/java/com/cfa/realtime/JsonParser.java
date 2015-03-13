package com.cfa.realtime;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
public class JsonParser {
    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    private static final DateTimeFormatterBuilder formatterBuilder = new DateTimeFormatterBuilder()
            .appendDayOfMonth(2)
            .appendLiteral('-')
            .appendMonthOfYearShortText()
            .appendLiteral('-')
            .appendYear(2, 2)
            .appendLiteral(' ')
            .appendHourOfDay(2)
            .appendLiteral(':')
            .appendMinuteOfHour(2)
            .appendLiteral(':')
            .appendSecondOfMinute(2);
    public static final DateTimeFormatter dateFormatter = new DateTimeFormatter(formatterBuilder.toPrinter(), formatterBuilder.toParser());

    private static final JSONParser parser = new JSONParser();

    public static Message parseMessage(String json) {
        try {
            JSONObject jsonObj = (JSONObject) parser.parse(json);
            return new Message(
                    getString(jsonObj, "userId"),
                    getString(jsonObj, "currencyFrom"),
                    getString(jsonObj, "currencyTo"),
                    getNumber(jsonObj, "amountSell"),
                    getNumber(jsonObj, "amountBuy"),
                    getNumber(jsonObj, "rate"),
                    getDateTime(jsonObj, "timePlaced"),
                    getString(jsonObj, "originatingCountry")
            );
        } catch (Exception e) {
            String msg = "Error occurred while parsing json: " + json;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }

    private static String getString(JSONObject json, String fieldName) {
        return json.get(fieldName) + "";
    }

    private static Double getNumber(JSONObject json, String fieldName) {
        Object value = json.get(fieldName);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new NumberFormatException(format("Field '%s' = '%s' is not a number", fieldName, value));
    }

    private static DateTime getDateTime(JSONObject json, String fieldName) {
        return dateFormatter.parseDateTime((String) json.get(fieldName));
    }
}
