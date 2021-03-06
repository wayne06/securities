package com.wnzhong.counter.bean.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class StockInfo {

    private int code;
    private String name;
    private String abbrName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StockInfo stockInfo = (StockInfo) o;

        if (code != stockInfo.code) {
            return false;
        }
        if (name != null ? !name.equals(stockInfo.name) : stockInfo.name != null) {
            return false;
        }
        return abbrName != null ? abbrName.equals(stockInfo.abbrName) : stockInfo.abbrName == null;
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (abbrName != null ? abbrName.hashCode() : 0);
        return result;
    }
}
