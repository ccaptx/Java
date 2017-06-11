package org.dvlyyon.nbi.g30;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.util.RFC3399Timestamp;
import org.dvlyyon.nbi.util.ThreadUtils;
import org.dvlyyon.nbi.util.XMLUtils;
import org.jdom2.Element;

public class G30NetconfNotification implements G30NetconfNotificationInf {
	private static final Log log = LogFactory.getLog(G30NetconfNotification.class);
	private final static String NOTIF_NAMESPACE = "urn:ietf:params:xml:ns:netconf:notification:1.0";
	
	class Event {
		Timestamp time;
		Element   data;
		String    type=null;
		
		public Event(Timestamp t, Element e) {
			time = t;
			data = e;
		}
		
		public String toSpecial() {
			StringBuilder sb = new StringBuilder();
			RFC3399Timestamp rfcT = new RFC3399Timestamp(time);
			sb.append(rfcT.toString(TimeZone.getDefault())).append("\n");
			for (int i=0; i<25; i++) sb.append("---");
			sb.append("\n");
			sb.append(XMLUtils.toXmlString(data));
			return sb.toString();
		}
		
		public String toOriginal() {
			Element notification = new Element("notification",NOTIF_NAMESPACE);
			Element eventTime = new Element("eventTime",NOTIF_NAMESPACE);
			RFC3399Timestamp rfcT = new RFC3399Timestamp(time);
			eventTime.addContent(rfcT.toString());
			notification.addContent(eventTime);
			if (data != null) {
	            List<Element> kids = (List<Element>) data.getChildren();
	            for (int i=0; i<kids.size(); i++) {
	            	Element kid = kids.get(i);
	            	if (type == null) type = kid.getName();
	            	kid.detach();
	            	notification.addContent(kid);
	            }			
			}
			return XMLUtils.toXmlString(notification,true);
		}
		
		public NetconfEvent toNetconfEvent() {
			NetconfEvent event = new NetconfEvent();
			event.setEvent(this.toOriginal());
			event.setType(type);
			return event;
		}
		
		public String toString() {
			return toOriginal();
		}
	}
	
	Queue <Event> notifications;
	static final int DEFAUL_CACHE_SIZE=5000;
	int maxSize = DEFAUL_CACHE_SIZE;
	
	public G30NetconfNotification() {
		notifications = new ArrayBlockingQueue<Event>(DEFAUL_CACHE_SIZE);
	}
	
	public G30NetconfNotification(int capacity) {
		maxSize = capacity;
		notifications = new ArrayBlockingQueue<Event>(capacity);
		log.info("EVENT THRESHOLD:"+capacity);
	}
		
	@Override
	public void putNotification(Timestamp time, Element data) {
		Event e = new Event(time,data);
		synchronized (notifications) {
			boolean success = notifications.offer(e);
			while (!success) {
				Event oldest = notifications.poll();
				if (oldest == null) log.info("UNEXPECTED:a null value is polled when trying to offer a event");
				log.info("WARNING: a event is discarded due to full queue when trype to offer a event\n" + oldest);
				success = notifications.offer(e);
			}
		}
	}

	public List<NetconfEvent> getNotifications(int timeout) {
		// TODO Auto-generated method stub
		Object [] events;
		log.info("begin:"+System.currentTimeMillis());
		ThreadUtils.sleep(timeout);
		log.info("end:"+System.currentTimeMillis()+":"+timeout);
		synchronized(notifications) {
			if (notifications.size()==0) return null;
			events = notifications.toArray();
			log.info("TOTAL EVENTS:"+notifications.size());
		}
		return toList(events);
	}

	public List<NetconfEvent> popNotifications(int timeout) {
		Object [] events;
		log.info("begin:"+System.currentTimeMillis());
		ThreadUtils.sleep(timeout);
		log.info("end:"+System.currentTimeMillis()+":"+timeout);
		synchronized(notifications) {
			log.info("TOTAL EVENTS:"+notifications.size());
			if (notifications.size()==0) return null;
			events = notifications.toArray();
			notifications.clear();
			log.info("TOTAL EVENTS:"+notifications.size());
		}
		return toList(events);
	}
	
	public String toString(Object [] events) {
		StringBuilder sb = new StringBuilder();
		for (Object event:events) {
			for (int i=0;i<50;i++) sb.append("=");
			sb.append("\n");
			sb.append(event.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public List<NetconfEvent> toList(Object [] events) {
		ArrayList<NetconfEvent> eventList = new ArrayList<NetconfEvent>(events.length);
		for (Object event:events) {
			eventList.add(((Event)event).toNetconfEvent());
		}
		return eventList;
	}

	@Override
	public List<NetconfEvent> getNotification(int timeout, boolean clear) {
		if (clear) return popNotifications(timeout);
		return getNotifications(timeout);
	}

}
