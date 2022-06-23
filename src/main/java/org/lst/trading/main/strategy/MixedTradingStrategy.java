package org.lst.trading.main.strategy;

import java.time.Instant;
import java.util.Date;
import org.lst.trading.lib.model.Order;
import org.lst.trading.lib.model.TradingContext;
import org.lst.trading.lib.model.TradingStrategy;

import java.util.HashMap;
import java.util.Map;

public class MixedTradingStrategy implements TradingStrategy {
    Map<String, Order> mOrders;
    TradingContext mContext;
    int buying_amount = 0;
    int months = 1;
   
    int purchase_day = -1;
    
    double highest_price = -1;
    
    double lumpsum_percentage = 0;
    double drop_ratio = 99999;
    
    boolean special_purchase_done = false;
    
    // we put the same value every month for MONTHS.
    public MixedTradingStrategy(int months, double lumpsum_percentage, double drop_ratio) {
        this.months = months;
        this.lumpsum_percentage = lumpsum_percentage;
        this.drop_ratio = 1 - drop_ratio;
    }

    @Override public void onStart(TradingContext context) {
        mContext = context;
    }

    @Override public void onTick() {
        //
        Date myDate = Date.from(mContext.getTime());
        int now_purchase_day = myDate.getDate();
        
        // get instrument (for now just one) and price
        String instrument = mContext.getInstruments().get(0);
        double price = mContext.getLastPrice(instrument);
        
        // init DCA
        if (purchase_day < 0) {
            buying_amount = (int) Math.floor(mContext.getInitialFunds() / months);
            purchase_day = Math.min( myDate.getDate(), 28);
        }
        
        // actual tick
        if (((mOrders == null) || (now_purchase_day == purchase_day)) && (months > 0)) {
            double funds = buying_amount;
            
            if (funds > mContext.getRealFunds()) {
                funds = Math.max(0, mContext.getRealFunds());
            }
            
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
        
        //
        highest_price = Math.max(highest_price, price);
        
        //
        if (!special_purchase_done) {
            double current_drop = (double) (price / highest_price);
            //System.out.println("current_drop: " + current_drop);
            
           // System.out.println("price: " + price);
            //System.out.println("highest price: " + highest_price);
            
            if (current_drop < drop_ratio) {
                // special purchase!
                double funds = mContext.getInitialFunds() * lumpsum_percentage;
                if (funds > mContext.getRealFunds()) {
                    funds = mContext.getRealFunds();
                }

                int amount = (int) Math.floor(funds / price);
                if (amount > 0) {
                    //
                    Order order = mContext.order(instrument, true, amount);

                    mOrders = new HashMap<>();
                    mOrders.put(instrument, order);
                }

                special_purchase_done = true;
            }
        }

    }
}
