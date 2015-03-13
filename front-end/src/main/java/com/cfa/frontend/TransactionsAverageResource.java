package com.cfa.frontend;

import com.cfa.commons.cassandra.CassandraClient;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
@Path("/transactions-average")
public class TransactionsAverageResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionAverage[] list() {
        List<TransactionAverage> result = new ArrayList<>();
        ResultSet resultSet = CassandraClient.getInstance().selectTotalAmounts();
        for (Row row : resultSet) {
            result.add(new TransactionAverage(
                    row.getString("country"),
                    row.getDate("check_timestamp"),
                    row.getLong("transactions"),
                    row.getInt("window_size_seconds")));
        }
        return result.toArray(new TransactionAverage[result.size()]);
    }
}
