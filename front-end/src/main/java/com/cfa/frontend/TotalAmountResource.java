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
@Path("/total-amount")
public class TotalAmountResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TotalAmount[] list() {
        List<TotalAmount> result = new ArrayList<>();
        ResultSet resultSet = CassandraClient.getInstance().selectTotalAmounts();
        for (Row row : resultSet) {
            result.add(new TotalAmount(
                    row.getString("country"),
                    row.getString("currency"),
                    row.getDate("check_timestamp"),
                    row.getDouble("total_buy"),
                    row.getDouble("total_sell")));
        }
        return result.toArray(new TotalAmount[result.size()]);
    }
}
