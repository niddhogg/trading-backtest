package org.lst.trading.main.strategy;

import java.time.Instant;
import java.util.Date;
import org.lst.trading.lib.model.Order;
import org.lst.trading.lib.model.TradingContext;
import org.lst.trading.lib.model.TradingStrategy;

import java.util.HashMap;
import java.util.Map;

public class DollarCostAveragingTradingStrategy implements TradingStrategy {
    Map<String, Order> mOrders;
    TradingContext mContext;
    int buying_amount = 0;
    int months = 1;
   
    int purchase_day = -1;
    
    // we put the same value every month for MONTHS.
    public DollarCostAveragingTradingStrategy(int months) {
        this.months = months;
    }

    @Override public void onStart(TradingContext context) {
        mContext = context;
    }

    @Override public void onTick() {
        //
        Date myDate = Date.from(mContext.getTime());
        int now_purchase_day = myDate.getDate();
        
        // init DCA
        if (purchase_day < 0) {
            buying_amount = (int) Math.floor(mContext.getInitialFunds() / months) ;// *2 for test
            purchase_day = Math.min( myDate.getDate(), 28);
        }
        
        // actual tick
        if (((mOrders == null) || (now_purchase_day == purchase_day)) && (months > 0)) {
            String instrument = mContext.getInstruments().get(0);
            double price = mContext.getLastPrice(instrument);
            
            double funds = buying_amount;
            if (funds > mContext.getRealFunds()) {
                funds = mContext.getRealFunds();
            }
            
            //System.out.println("funds: " + mContext.getRealFunds());
            
            int amount = (int) Math.floor(funds / price);
            if (amount > 0) {
                //
                Order order = mContext.order(instrument, true, amount);

                mOrders = new HashMap<>();
                mOrders.put(instrument, order);

                // month used
                months--;
            }
        }
    }
}
