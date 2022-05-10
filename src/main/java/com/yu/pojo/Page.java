package com.yu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 页面
 *
 * @author shah
 * @date 2022/05/10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page {
    private int current = 1;
    private int limit = 10;
    private int rows;
    private String path;

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {
        if (rows > 0){
            this.rows = rows;
        }
    }

    /**
     * 得到当前页起始行
     *
     * @return int
     */
    public int getOffset(){
        return (current - 1) * limit;
    }

    /**
     * 得到总页数
     *
     * @return int
     */
    public int getTotal(){
        if (rows % limit == 0) {
            return rows / limit;
        }
        return rows / limit + 1;
    }

    /**
     * 起始页码
     *
     * @return int
     */
    public int getFrom(){
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 截止页码
     *
     * @return int
     */
    public int getTo(){
        int to = current + 2;
        return to > getTotal() ? getTotal() : to;
    }
}
