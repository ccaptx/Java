package org.dvlyyon.nbi.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.dvlyyon.nbi.util.RunState.State;

public class LogUtils {
	public static String transID = "0";
	public static Logger log     = null;
	public static Level level 	 = Level.ALL;
	public static String format  = "FULL";

	public static void setLevel(String levelStr) {
		if (levelStr.equalsIgnoreCase("trace") ||		
			levelStr.equalsIgnoreCase("finest")) {
			level = Level.FINEST;
		} else if (levelStr.equalsIgnoreCase("Finer")) {
			level = Level.FINER;
		} else if (levelStr.equalsIgnoreCase("Fine") ||
				   levelStr.equalsIgnoreCase("debug")) {
			level = Level.FINE;
		} else if (levelStr.equalsIgnoreCase("config")) {
			level = Level.CONFIG;
		} else if (levelStr.equalsIgnoreCase("info")) {
			level = Level.INFO;
		} else if (levelStr.equalsIgnoreCase("warning")) {
			level = Level.WARNING;
		} else if (levelStr.equalsIgnoreCase("severe") ||
				   levelStr.equalsIgnoreCase("error") ||
				   levelStr.equalsIgnoreCase("fatal")) {
			level = Level.SEVERE;
		}
		log.setLevel(level);
	}
	
	public static void setSourceFormat(String format) {
		Handler [] handlers = log.getHandlers();
		for (Handler h:handlers) {
			if (h instanceof FileHandler) {
				Formatter f = h.getFormatter();
				if (f instanceof MyFormatter) {
					((MyFormatter)f).setSourceFormat(format);
				}
			}
		}
	}
	
	public static void initRollingFileLog(String fileName, RunState state) {
		state.setResult(State.NORMAL);
		LogManager logMgr = LogManager.getLogManager();
		log = logMgr.getLogger("");
		Handler [] handlers = log.getHandlers();
		for (Handler handler:handlers) {
			log.removeHandler(handler);
		}
		try {
			String home = System.getProperty("user.home");
			File logDir = new File(home+File.separator+"logs");
			if (!logDir.exists()) {
				logDir.mkdir();
			}
			FileHandler fhandler = new FileHandler("%h/logs/"+fileName+".%u.%g.log",10*1024*1024,4,true);
			MyFormatter fmtr = new MyFormatter();
			fhandler.setFormatter(fmtr);
			log.addHandler(fhandler);
			log.setLevel(level);
		} catch (IOException e) {
			state.setResult(State.EXCEPTION);
			state.setExp(e);
		}
	}	
}

class MyFormatter extends Formatter {
	Date dat = new Date();
	String format =       "%1$tF %1$tH:%1$tM:%1$tS,%1$tL %4$-7s [%8$s][%5$s] %2$s : %6$s%7$s%n";
	SourceFormat source = SourceFormat.SIMPLE_HEADER_AND_LAST_CLASS;
	StringBuilder sb =    new StringBuilder();
	
	protected String getClassName(String className) {
		if (source == SourceFormat.FULL) return className;
		String [] cs = className.split("\\.");
		String result = className;
		sb.delete(0, sb.length());
		if (cs.length > 1) {
			if (source == SourceFormat.SIMPLE_HEADER_AND_LAST_CLASS) {
				for (int i=0;i<cs.length-1; i++)
					sb.append(cs[i].charAt(0)).append(".");
			}
			sb.append(cs[cs.length-1]);
		}
		return sb.toString();
	}
	
	public synchronized void setSourceFormat(String format) {
		if (format.equalsIgnoreCase("full"))
			source = SourceFormat.FULL;
		else if (format.equalsIgnoreCase("onlyLastClass") ||
				 format.equalsIgnoreCase("simple"))
			source = SourceFormat.ONLY_LAST_CLASS;
		else if (format.equalsIgnoreCase("partial")) {
			source = SourceFormat.SIMPLE_HEADER_AND_LAST_CLASS;
		}
	}

	@Override
	public synchronized String format(LogRecord record) {
        dat.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = getClassName(record.getSourceClassName());
            if (record.getSourceMethodName() != null) {
               source += "." + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return String.format(format,
                             dat,
                             source,
                             record.getLoggerName(),
                             record.getLevel(),
                             Thread.currentThread().getName(),
                             message,
                             throwable,
                             LogUtils.transID);
	}
	
	enum SourceFormat {
		FULL,
		ONLY_LAST_CLASS,
		SIMPLE_HEADER_AND_LAST_CLASS
	}
}

