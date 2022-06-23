package org.lst.trading.main.strategy;

import org.lst.trading.lib.model.Order;
import org.lst.trading.lib.model.TradingContext;
import org.lst.trading.lib.model.TradingStrategy;

import java.util.HashMap;
import java.util.Map;

public class LumpSumTradingStrategy implements TradingStrategy {
    Map<String, Order> mOrders;
    TradingContext mContext;

    @Override public void onStart(TradingContext context) {
        mContext = context;
    }

    @Override public void onTick() {
        if (mOrders == null) {
            String instrument = mContext.getInstruments().get(0);
            double price = mContext.getLastPrice(instrument);
            
            double funds = mContext.getRealFunds();
            
            int amount = (int) Math.floor(funds / price);
            
            //
            Order order = mContext.order(instrument, true, amount);
            
            mOrders = new HashMap<>();
            mOrders.put(instrument, order);
            
            //mContext.getInstruments().stream().forEach(instrument -> mOrders.put(instrument, mContext.order(instrument, true, 1)));
        }
    }
}
