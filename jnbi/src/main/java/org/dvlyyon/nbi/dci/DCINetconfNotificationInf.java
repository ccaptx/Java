package org.dvlyyon.nbi.dci;

import java.sql.Timestamp;
import java.util.List;

import org.jdom2.Element;

public interface DCINetconfNotificationInf {
	public void putNotification(Timestamp time, Element data);
	public List<NetconfEvent> getNotification(int timeout, boolean clear);
}
