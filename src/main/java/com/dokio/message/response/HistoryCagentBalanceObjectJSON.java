package com.dokio.message.response;

import com.dokio.message.response.Reports.HistoryCagentBalanceJSON;

import java.math.BigDecimal;
import java.util.List;

public class HistoryCagentBalanceObjectJSON {

    private List<HistoryCagentBalanceJSON> table;   // таблица отчёта
    private BigDecimal  summ_on_start;              // начальный остаток
    private BigDecimal  summ_on_end;                // конечный остаток
    private BigDecimal  summ_in;                    // приход
    private BigDecimal  summ_out;                   // расход

    public List<HistoryCagentBalanceJSON> getTable() {
        return table;
    }

    public void setTable(List<HistoryCagentBalanceJSON> table) {
        this.table = table;
    }

    public BigDecimal getSumm_on_start() {
        return summ_on_start;
    }

    public void setSumm_on_start(BigDecimal summ_on_start) {
        this.summ_on_start = summ_on_start;
    }

    public BigDecimal getSumm_on_end() {
        return summ_on_end;
    }

    public void setSumm_on_end(BigDecimal summ_on_end) {
        this.summ_on_end = summ_on_end;
    }

    public BigDecimal getSumm_in() {
        return summ_in;
    }

    public void setSumm_in(BigDecimal summ_in) {
        this.summ_in = summ_in;
    }

    public BigDecimal getSumm_out() {
        return summ_out;
    }

    public void setSumm_out(BigDecimal summ_out) {
        this.summ_out = summ_out;
    }
}
