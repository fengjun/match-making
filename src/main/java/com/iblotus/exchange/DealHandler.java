package com.iblotus.exchange;


/**
 * 成交回调接口
 */
public interface DealHandler {

    /**
     * 成交回调
     * @param deal
     */
    void onDeal(Deal deal);
}