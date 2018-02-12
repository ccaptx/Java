package org.dvlyyon.nbi.dci;

import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Element;

import org.dvlyyon.net.netconf.Capabilities;
import org.dvlyyon.net.netconf.NotificationListenerIf;

public class NetconfNotificationListener implements NotificationListenerIf {
	DCINetconfNotificationInf queue;
	protected final static Log logger = LogFactory.getLog(NetconfNotificationListener.class);
	
	public NetconfNotificationListener(DCINetconfNotificationInf eventQueue) {
		queue = eventQueue;
	}
	@Override
	public void connectionTerminated() {
		// TODO Auto-generated method stub
		logger.fatal("WARNING: The notification connect has terminated.");
	}

	@Override
	public void notify(Timestamp time, Element data) {
		// TODO Auto-generated method stub
		queue.putNotification(time, data);
	}

	@Override
	public void setDeviceCapabilities(Capabilities capacities) {
		logger.info(capacities);
	}

}
