package com.cfa.frontend;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
@XmlRootElement
public class TotalAmount {

    public String country;
    public String currency;
    public Date checkTime;
    public Double totalBuy;
    public Double totalSell;

    public TotalAmount() {
    }

    public TotalAmount(String country, String currency, Date checkTime, Double totalBuy, Double totalSell) {
        this.country = country;
        this.currency = currency;
        this.checkTime = checkTime;
        this.totalBuy = totalBuy;
        this.totalSell = totalSell;
    }
}
