/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.message.response.Reports;

import java.math.BigDecimal;
import java.util.List;

public class ProfitLossJSON {


    private BigDecimal revenue;                     // выручка
    private BigDecimal cost_price;                  // себестоимость
    private BigDecimal gross_profit;                // валовая прибыль
    private BigDecimal operating_expenses;          // операционые расходы
    private BigDecimal operating_profit;            // операционная прибыль
    private BigDecimal taxes_and_fees;              // налоги и сборы
    private BigDecimal net_profit;                  // чистая прибыль
    private List<ProfitLossSerie> operational;      // список операционных расходов типа Имя - Значение

    public BigDecimal getCost_price() {
        return cost_price;
    }

    public void setCost_price(BigDecimal cost_price) {
        this.cost_price = cost_price;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }


    public BigDecimal getGross_profit() {
        return gross_profit;
    }

    public void setGross_profit(BigDecimal gross_profit) {
        this.gross_profit = gross_profit;
    }

    public BigDecimal getOperating_expenses() {
        return operating_expenses;
    }

    public void setOperating_expenses(BigDecimal operating_expenses) {
        this.operating_expenses = operating_expenses;
    }

    public BigDecimal getOperating_profit() {
        return operating_profit;
    }

    public void setOperating_profit(BigDecimal operating_profit) {
        this.operating_profit = operating_profit;
    }

    public BigDecimal getTaxes_and_fees() {
        return taxes_and_fees;
    }

    public void setTaxes_and_fees(BigDecimal taxes_and_fees) {
        this.taxes_and_fees = taxes_and_fees;
    }

    public BigDecimal getNet_profit() {
        return net_profit;
    }

    public void setNet_profit(BigDecimal net_profit) {
        this.net_profit = net_profit;
    }

    public List<ProfitLossSerie> getOperational() {
        return operational;
    }

    public void setOperational(List<ProfitLossSerie> operational) {
        this.operational = operational;
    }
}
