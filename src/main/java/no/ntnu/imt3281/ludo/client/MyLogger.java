package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Singleton class for doing logging
 */
public class MyLogger {
    private static MyLogger myLoggerInstance = null;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private MyLogger(){
    }

    public static MyLogger getInstance()
    {
        if (myLoggerInstance == null)
            myLoggerInstance = new MyLogger();

        return myLoggerInstance;
    }

    public static void log(String message){
        Logger logger = Logger.getLogger("MyLog");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler("Logging.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
