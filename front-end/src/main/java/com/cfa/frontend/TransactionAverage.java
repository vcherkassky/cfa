package com.cfa.frontend;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
@XmlRootElement
public class TransactionAverage {

    public String country;
    public Date checkTime;
    public Long averageTransactions;
    public Integer periodSeconds;

    public TransactionAverage() {
    }

    public TransactionAverage(String country, Date checkTime, Long averageTransactions, Integer periodSeconds) {
        this.country = country;
        this.checkTime = checkTime;
        this.averageTransactions = averageTransactions;
        this.periodSeconds = periodSeconds;
    }

}
