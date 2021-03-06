package com.cfa.commons.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
public class CassandraClient {
    private static final Logger log = LoggerFactory.getLogger(CassandraClient.class);
    private static CassandraClient _instance;
    private final String address;
    private Cluster cluster;
    private Session session;

    private CassandraClient(String address) {
        this.address = address;
    }

    //TODO: this is ugly
    public static CassandraClient getInstance(String address) {
        if (_instance == null) {
            synchronized(CassandraClient.class) {
                if (_instance == null) {
                    _instance = new CassandraClient(address);
                    _instance.connect();
                    _instance.createSchema();
                }
            }
        }
        return _instance;
    }

    public static CassandraClient getInstance() {
        if (_instance == null)
            throw new IllegalStateException("Not initialized. Call getInstance(address) first.");
        return _instance;
    }

    public void connect() {
        cluster = Cluster.builder()
                .addContactPoint(address)
                .build();
        Metadata metadata = cluster.getMetadata();
        log.debug("Connected to cluster: {}", metadata.getClusterName());
        session = cluster.connect();
    }

    public Session getSession() {
        return session;
    }

    public void createSchema() {
        session.execute("CREATE KEYSPACE IF NOT EXISTS currency_exchange " +
                "WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1};");

        session.execute("CREATE TABLE IF NOT EXISTS currency_exchange.total_amount (\n" +
                "  country text,\n" +
                "  currency text,\n" +
                "  total_sell double,\n" +
                "  total_buy double, \n" +
                "  check_timestamp timestamp,\n" +
                "  PRIMARY KEY (country, currency, check_timestamp) \n" +
                ") WITH CLUSTERING ORDER BY (currency ASC, check_timestamp DESC);\n");

        session.execute("CREATE TABLE IF NOT EXISTS currency_exchange.transactions_sliding_average (\n" +
                "  country text,\n" +
                "  transactions bigint,\n" +
                "  window_size_seconds int,\n" +
                "  check_timestamp timestamp,\n" +
                "  PRIMARY KEY (country, check_timestamp)\n" +
                ") WITH CLUSTERING ORDER BY (check_timestamp DESC);\n");
    }

    public void insertTotalAmount(String country, String currency, BigDecimal totalSell, BigDecimal totalBuy,
                                  DateTime checkTimestamp, Integer ttlSeconds) {
        PreparedStatement statement = getSession().prepare(
                "INSERT INTO currency_exchange.total_amount " +
                        "(country, currency, total_sell, total_buy, check_timestamp ) " +
                        "VALUES (?, ?, ?, ?, ?) USING TTL " + ttlSeconds + ";");
        getSession().execute(statement.bind(country, currency, totalSell.doubleValue(), totalBuy.doubleValue(),
                new Timestamp(checkTimestamp.getMillis())));

    }

    public void insertTransactionsCount(String country, Long transactions, Integer windowSizeSeconds,
                                        DateTime checkTimestamp, Integer ttlSeconds) {
        PreparedStatement statement = getSession().prepare(
                "INSERT INTO currency_exchange.transactions_sliding_average " +
                        "(country, transactions, window_size_seconds, check_timestamp ) " +
                        "VALUES (?, ?, ?, ?) USING TTL " + ttlSeconds + ";");
        getSession().execute(statement.bind(country, transactions, windowSizeSeconds,
                new Timestamp(checkTimestamp.getMillis())));
    }

    public ResultSet selectTotalAmounts() {
        Statement statement = QueryBuilder.select().all().from("currency_exchange", "total_amount");
        return getSession().execute(statement);
    }

    public ResultSet selectTransactionsCount() {
        Statement statement = QueryBuilder.select().all().from("currency_exchange", "transactions_sliding_average");
        return getSession().execute(statement);
    }

    public void close() {
        session.close();
        cluster.close();
    }
}
