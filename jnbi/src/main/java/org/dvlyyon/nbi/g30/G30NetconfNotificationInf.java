package org.dvlyyon.nbi.g30;

import java.sql.Timestamp;
import java.util.List;

import org.jdom2.Element;

public interface G30NetconfNotificationInf {
	public void putNotification(Timestamp time, Element data);
	public List<NetconfEvent> getNotification(int timeout, boolean clear);
}
