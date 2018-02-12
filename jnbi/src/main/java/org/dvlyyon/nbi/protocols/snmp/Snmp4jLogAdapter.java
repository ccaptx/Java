package org.dvlyyon.nbi.protocols.snmp;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogLevel;

public class Snmp4jLogAdapter implements LogAdapter {
	private final Log log;
	private LogLevel level;
	String name;
	
	public Snmp4jLogAdapter(String cName) {
		log = LogFactory.getLog(cName);
		name = cName;
	}
	
	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	@Override
	public void debug(Serializable message) {
		log.debug(message);
	}

	@Override
	public void info(CharSequence message) {
		log.info(message);
	}

	@Override
	public void warn(Serializable message) {
		log.warn(message);
	}

	@Override
	public void error(Serializable message) {
		log.error(message);
	}

	@Override
	public void error(CharSequence message, Throwable throwable) {
		log.error(message,throwable);
	}

	@Override
	public void fatal(Object message) {
		log.fatal(message);
	}

	@Override
	public void fatal(CharSequence message, Throwable throwable) {
		log.fatal(message,throwable);
	}

	@Override
	public void setLogLevel(LogLevel level) {
		this.level = level;
	}

	@Override
	public LogLevel getLogLevel() {
		return level;
	}

	@Override
	public LogLevel getEffectiveLogLevel() {
		// TODO Auto-generated method stub
		return LogLevel.TRACE;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Iterator getLogHandler() {
		// TODO Auto-generated method stub
		return null;
	}

}
