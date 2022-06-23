package org.lst.trading.main;

import org.lst.trading.lib.backtest.Backtest;
import org.lst.trading.lib.model.ClosedOrder;
import org.lst.trading.lib.model.TradingStrategy;
import org.lst.trading.lib.series.MultipleDoubleSeries;
import org.lst.trading.lib.util.AlphaVantageHistoricalPriceService;
import org.lst.trading.lib.util.HistoricalPriceService;
import org.lst.trading.lib.util.Util;
import org.lst.trading.main.strategy.kalman.CointegrationTradingStrategy;

import java.util.Locale;

import static java.lang.String.format;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.apache.http.client.utils.DateUtils;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.main.strategy.DollarCostAveragingTradingStrategy;
import org.lst.trading.main.strategy.LumpSumTradingStrategy;
import org.lst.trading.main.strategy.MixedTradingStrategy;
import rx.Observable;
import rx.observables.BlockingObservable;

public class BacktestMain {
    static String alphaVantantageApiKey = "WVO85NWI7AL2CAIV"; // fill API key in here or pass via system property: -Dalphavantantage.apikey=APIKEY
    
    public static void main(String[] args) throws Exception {
        String ticker = "SPY";

        // initialize the trading strategy
        //TradingStrategy strategy = new LumpSumTradingStrategy();
        //TradingStrategy strategy = new DollarCostAveragingTradingStrategy(36);
        TradingStrategy strategy = new MixedTradingStrategy(36, 0.2, 0.4); // 20% lump-sum if 40% drop
        
        // MixedTradingStrategy actually produced 8% better result.
        // 3.59m vs 3.3m
        // if we lump sum 50% then result is 15% better. 3.85m vs 3.3m
        

        // download historical prices
        HistoricalPriceService finance = new AlphaVantageHistoricalPriceService(alphaVantantageApiKey);
 
        MultipleDoubleSeries priceSeries = new MultipleDoubleSeries(finance.getHistoricalAdjustedPrices(ticker).toBlocking().first());
        
        // Filter by time. For now it's like this.
        String start_date = "28/09/1990"; // "28/09/2007" was good
        String end_date = "28/09/2012";
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");  
        
        Date date_start = formatter.parse(start_date);  
        Date date_end = formatter.parse(end_date); 
        
        //priceSeries.filterByTime(date_start, date_end);
        priceSeries.filterByTimeAndYears(date_start, 10);
        
        // we may need to filter price series for our dates :)
        //System.out.println("a: " +  finance.getHistoricalAdjustedPrices(ticker).toBlocking().first());
        
        
        //
        //System.out.println("finance.getHistoricalAdjustedPrices(x): " + finance.getHistoricalAdjustedPrices(x));
        
        // initialize the backtesting engine
        int deposit = 1000000;
        
        /*
        //
        Backtest backtest = new Backtest(deposit, priceSeries);

        // do the backtest
        Backtest.Result result = backtest.run(strategy);

        // show results
        StringBuilder orders = new StringBuilder();
        orders.append("id,amount,side,instrument,from,to,open,close,pl\n");
        for (ClosedOrder order : result.getOrders()) {
            orders.append(format(Locale.US, "%d,%d,%s,%s,%s,%s,%f,%f,%f\n", order.getId(), Math.abs(order.getAmount()), order.isLong() ? "Buy" : "Sell", order.getInstrument(), order.getOpenInstant(), order.getCloseInstant(), order.getOpenPrice(), order.getClosePrice(), order.getPl()));
        }
        //System.out.print(orders);

        int days = priceSeries.size();

        System.out.println();
        System.out.println("Backtest result of " + strategy.getClass() + ": " + strategy);
        System.out.println("Prices: " + priceSeries);
        System.out.println(format(Locale.US, "Simulated %d days, Initial deposit %d, Leverage %f", days, deposit, backtest.getLeverage()));
        System.out.println(format(Locale.US, "Commissions = %f", result.getCommissions()));
        System.out.println(format(Locale.US, "P/L = %.2f, Final value = %.2f, Result = %.2f%%, Annualized = %.2f%%, Sharpe (rf=0%%) = %.2f", result.getPl(), result.getFinalValue(), result.getReturn() * 100, result.getReturn() / (days / 251.) * 100, result.getSharpe()));

        System.out.println("Orders: " + Util.writeStringToTempFile(orders.toString()));
        System.out.println("Statistics: " + Util.writeCsv(new MultipleDoubleSeries(result.getPlHistory(), result.getMarginHistory())));
        */
        
        // mmultiple test...
        
        int runs = 12*15; // 15 years...
        double sum = 0;
        
        for (int i=0;i<runs;i++) {
            // we will shift just starting time
            date_start = Date.from(date_start.toInstant().plus(30, ChronoUnit.DAYS));
            
            //
            priceSeries.filterByTimeAndYears(date_start, 15);

            //
            Backtest backtest = new Backtest(deposit, priceSeries);

            // do the backtest
            //TradingStrategy strategy2 = new LumpSumTradingStrategy();
            TradingStrategy strategy2 = new DollarCostAveragingTradingStrategy(36);
            //TradingStrategy strategy2 = new MixedTradingStrategy(36, 0.2, 0.4); 
            
            Backtest.Result result = backtest.run(strategy2);
            //System.out.println("result.getFinalValue() " + result.getFinalValue());

            sum += result.getFinalValue();
        }
        
        double avg = sum / runs;
        System.out.println("res: " + avg);
        
        // looks like it gives 2.5% on average if i get all the data from 1990 to +15 years
        // so DCA is quite good anyway.
        // our conditional DCA gave 20% if it saw 40% drop
        
    }

}
