package com.iblotus.exchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 委托挂单
 */
public class CommissionBook<T extends CommissionBroker> {

    private final List<T> list = new ArrayList<>();

    private final CommissionLocateStrategy<T> strategy;

    private Map<String, T> brokerMap = new HashMap<>();

    private CommissionBook(final CommissionLocateStrategy<T> strategy){
        this.strategy = strategy;
    }

    static <T extends CommissionBroker> CommissionBook<T> HighFirst(){
        return new CommissionBook<>(new HighPriceFirstCommissionStrategy<T>());
    }

    static <T extends CommissionBroker> CommissionBook<T> LowFirst(){
        return new CommissionBook<>(new LowPriceFirstCommissionStategy<T>());
    }

    /**
     * 添加到订单列表（排序）
     * @param c
     */
    public void add(T c){
        int index = strategy.locate(c, this.list);
        if(index >= 0){
            list.add(index, c);
        }else{
            list.add(c);
        }
        brokerMap.put(c.getBrokerId(), c);
    }

    /**
     * 移除委托
     * @param brokerId
     */
    public void remove(String brokerId){
        T broker = brokerMap.get(brokerId);
        if(broker != null){
            brokerMap.remove(brokerId);
            list.remove(broker);
        }
    }

    /**
     * 清空委托
     */
    public void clear(){
        brokerMap.clear();
        list.clear();
    }

    /**
     * 查找委托
     * @param brokerId
     * @return
     */
    public T find(String brokerId){
        return brokerMap.get(brokerId);
    }

    public boolean isEmpty(){
        return list.isEmpty();
    }

    public int size(){
        return list.size();
    }

    public T top(){
        if(this.isEmpty()){
            throw new RuntimeException("OrderBook is empty");
        }
        return list.get(0);
    }

    /**
     * 委托定位策略
     */
    private interface CommissionLocateStrategy<T extends CommissionBroker> {

        /**
         * 查找任务的待插入索引
         * 如果没有合适的，返回-1
         * @param newCommition
         * @return
         */
        int locate(T newCommition, List<T> list);
    }

    /**
     * 低价优先（二分查找法高性能）
     */
    private static class LowPriceFirstCommissionStategy<T extends CommissionBroker> implements CommissionLocateStrategy<T> {

        @Override
        public int locate(T newCommition, List<T> list) {
            int mid;
            int start = 0;
            int end = list.size() - 1;
            while (start <= end){
                mid = (end - start) / 2 + start;
                if(newCommition.getPrice().compareTo(list.get(mid).getPrice()) < 0){
                    if(mid == 0){
                        return 0;
                    }
                    if(newCommition.getPrice().compareTo(list.get(mid-1).getPrice()) >= 0){
                        return mid;
                    }
                    end = mid - 1;
                } else {
                    if(mid > end){
                        return -1;
                    }
                    if(newCommition.getPrice().compareTo(list.get(mid).getPrice()) < 0){
                        return mid;
                    }
                    start = mid + 1;
                }
            }
            return -1;
        }
    }

    /**
     * 高价优先（二分查找法高性能）
     */
    private static class HighPriceFirstCommissionStrategy<T extends CommissionBroker> implements CommissionLocateStrategy<T> {

        @Override
        public int locate(T newCommition, List<T> list) {
            int mid;
            int start = 0;
            int end = list.size() - 1;
            while (start <= end){
                mid = (end - start) / 2 + start;
                if(newCommition.getPrice().compareTo(list.get(mid).getPrice()) > 0){
                    if(mid == 0){
                        return 0;
                    }
                    if(newCommition.getPrice().compareTo(list.get(mid-1).getPrice()) <= 0){
                        return mid;
                    }
                    end = mid - 1;
                } else {
                    if(mid > end){
                        return -1;
                    }
                    if(newCommition.getPrice().compareTo(list.get(mid).getPrice()) > 0){
                        return mid;
                    }
                    start = mid + 1;
                }
            }
            return -1;
        }
    }
}