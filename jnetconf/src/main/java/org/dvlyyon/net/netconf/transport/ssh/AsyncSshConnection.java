/*
 * This work is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a link to the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
 * 
 * Copyright Model Based Management Technologies, LLC. (c) 2009 - 2011. All rights reserved.
 *
 * This source code is provided "as is" and without warranties as to performance or merchantability.
 * The author and/or distributors of this source code may have made statements about this source code.
 * Any such statements do not constitute warranties and shall not be relied on by the user in deciding
 * whether to use this source code.
 *
 * This source code is provided without any express or implied warranties whatsoever. Because of the
 * diversity of conditions and hardware under which this source code may be used, no warranty of fitness
 * for a particular purpose is offered. The user is advised to test the source code thoroughly before
 * relying on it. The user must assume the entire risk of using the source code.
 */
package org.dvlyyon.net.netconf.transport.ssh;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.net.RFC3399Timestamp;
import org.dvlyyon.common.util.CLRunnable;
import org.dvlyyon.common.util.CLThread;
import org.dvlyyon.common.util.XmlUtils;
import org.dvlyyon.net.netconf.NotificationListenerIf;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.eaio.uuid.UUID;


/**
 * The AsyncSshConnection class handles asynchronous NETCONF notifications from a device. Usage of this class involves registering an
 * application listener for notifications and then creating subscriptions for notifications on the device.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class AsyncSshConnection extends SshConnection
{

   /** Logger for tracing */
   private final static Log s_logger = LogFactory.getLog(AsyncSshConnection.class);

   /** The base NETCONF namespace */
   private final static String BASE_NAMESPACE = "urn:ietf:params:xml:ns:netconf:base:1.0";

   /** The namespace for notifications */
   private final static String NOTIF_NAMESPACE = "urn:ietf:params:xml:ns:netconf:notification:1.0";

   /** Host to which we are connecting */
   private String m_host;

   /** Listener that is to be invoked on receipt of notifications */
   private NotificationListenerIf m_listener;

   /** True if the device supports notification (as indicated by its capabilities */
   private boolean m_notificationsSupported;

   /** True if we are ready for notifications */
   private boolean m_readyToReceiveNotifications;

   /** The message ID used for the <b>create-subscription</b> NETCONF message */
   private String m_createSubscriptionMessageId;

   /** Time of the latest notification received */
   private Timestamp m_lastReceivedNotificationTime;

   /** Thread used to handle notification messages from the device */
   private CLThread m_listenerThread;


   /**
    * Creates an AsyncSshConnection with the specified parameters.
    *
    * @param connectionProperties   Properties containing connection information.
    * @param listener               Application class to notify when events come from device.
    * @throws RuntimeException          if an error occurs during creation.
    */
   public AsyncSshConnection(Properties connectionProperties, NotificationListenerIf listener) throws RuntimeException
   {
       this(connectionProperties, listener, true);    
   }
   
   public AsyncSshConnection(Properties connectionProperties, NotificationListenerIf listener, boolean connectNow) throws RuntimeException
   {
      super(connectionProperties, null, connectNow);
      m_host = connectionProperties.getProperty("host", "localhost");
      m_listener = listener;
   }

   /**
    * Called when a hello message is received. We process the capabilities to check if notifications are supported by the device.
    */
   @Override
   @SuppressWarnings("unchecked")
   protected void handleHelloResponse(Element response)
   {
      // Process the Hello response to make sure we have notification capabilities
      Namespace xmlns = Namespace.getNamespace(BASE_NAMESPACE);
      Element capsRoot = response.getChild("capabilities", xmlns);
      if (capsRoot == null)
      {
         s_logger.error("Invalid hello response received; no capabilites sent by device.");
      }
      else
      {
         List<Element> caps = capsRoot.getChildren("capability", xmlns);
         for (Element capElem : caps)
         {
            String capName = capElem.getText().trim();
            if (capName.equals("urn:ietf:params:netconf:capability:notification:1.0"))
            {
               m_notificationsSupported = true;
               break;
            }
         }
      }
   }

   /**
    * Called when a regular (non-hello) message is received. The message can be one of three types:<ol>
    * <li>rpc-reply - This would be a response to the <b>create-subscription</b> message</li>
    * <li>replayComplete - This is an indicator that says old notifications have been replayed - ignored</li>
    * <li>notification - This is an actual notification - call the registered listener</li>
    * </ol>
    */
   @Override
   @SuppressWarnings("unchecked")
   protected void handleResponse(Element response)
   {
      try
      {
         // We are interested in three types of responses
         // RPC-OK (in response to create-subscription); verify using the message ID and OK response
         String responseType = response.getName();
         if (responseType.equals("rpc-reply"))
         {
            // This must be a response to the create-subscription message; verify using the message ID
            Namespace ns = Namespace.getNamespace(BASE_NAMESPACE);
            String messageId = response.getAttributeValue("message-id");
            if (m_createSubscriptionMessageId.equals(messageId))
            {
               Element ok = response.getChild("ok", ns);
               if (ok != null)
               {
                  s_logger.debug("Got an OK response to create-subscription.");
                  m_readyToReceiveNotifications = true;
               }
               else
               {
                  s_logger.warn("Expected an OK response to create-subscription; did not get one");
                  s_logger.warn("Actual response: " + XmlUtils.toXmlString(response));
               }
            }
            else
            {
               //s_logger.info(XmlUtils.toXmlString(response));
               s_logger.info("Reply received to keep-alive ping on async. channel");
            }
         }
         else if (responseType.equals("replayComplete"))
         {
            // We don't really care about this one
            s_logger.info("Received ReplayComplete indicator from device");
         }
         else if (responseType.equals("notification"))
         {
            Element root = null;
            if (s_logger.isDebugEnabled())
            {
               s_logger.debug("Top-level notification XML: " + XmlUtils.toXmlString(response));
            }
            // Notifications (call the listener)
            s_logger.debug("Got a notification from device: " + responseType);
            Timestamp eventTime = null;
            ArrayList<Element> dataNodes = new ArrayList<Element>();
            List<Element> kids = (List<Element>) response.getChildren();
            for (Element kid : kids)
            {
               if (kid.getName().equals("eventTime") && kid.getNamespaceURI().equals(NOTIF_NAMESPACE))
               {
                  try
                  {
                     eventTime = new RFC3399Timestamp(kid.getText()).getSqlTimestamp();
                  }
                  catch (final Exception ex)
                  {
                     s_logger.warn("Error parsing event date: " + kid.getText() + " using current time");;
                     eventTime = new Timestamp(System.currentTimeMillis());
                  }
               }
               else
               {
                  dataNodes.add(kid);
               }
            }
            if (dataNodes.size() > 0)
            {
               root = new Element("data");
               for (Element dataNode : dataNodes)
               {
                  dataNode.detach();
                  root.addContent(dataNode);
               }
               if (s_logger.isDebugEnabled())
                  s_logger.debug("Notification data: " + XmlUtils.toXmlString(root));
            }
            m_lastReceivedNotificationTime = eventTime;
            m_listener.notify(eventTime, root);
         }
         else
         {
            s_logger.warn("Unexpected XML message received from device: " + responseType);
         }
      }
      catch (final Exception fex)
      {
         // Any exception in event processing should just ignore the event, instead of throwing it to the
         // caller (since the thread it is being called in will bag out otherwise)
         s_logger.error("Error processing notification: " + XmlUtils.toXmlString(response, true));
         if (s_logger.isDebugEnabled())
         {
            s_logger.error(fex, fex);
         }
      }
   }

   /** */
   public Timestamp getLastReceivedNotificationTime()
   {
      return m_lastReceivedNotificationTime;
   }

   /**
    * Called to start listening to notifications from the specified stream. This causes a <b>create-subscription</b> message to be
    * sent to the device.
    *
    * @param stream              Stream to start notifications on (NULL if default stream is to be used).
    * @param startTime           Start time from when messages need to be replayed (NULL to specify no-replay).
    * @throws RuntimeException   if an error occurred during subscription creation.
    */
   public void startNotifications(final String stream, final Timestamp startTime) throws RuntimeException
   {
      if (!m_notificationsSupported)
      {
         throw new RuntimeException("Netconf notifications are not supported by this device");
      }
      if (m_createSubscriptionMessageId != null)
      {
         throw new RuntimeException("A listener is already registered for notifications");
      }
      // Send the <create-subscription> message
      s_logger.info("Subscribing for notifications: - stream: " + stream + "; startTime: " + startTime);
      m_createSubscriptionMessageId = new UUID().toString();
      Element msg = createSubscriptionMessageXml(m_createSubscriptionMessageId, stream, startTime);
      super.syncSend(msg, true);
      if (!m_readyToReceiveNotifications)
      {
         throw new RuntimeException("Failed to register listener to receive notifications");
      }
      // Things are good; start receiving all the notifications
      NotificationListener listener = new NotificationListener(this);
      // Put device address as part of thread name below
      String threadName = "NETCONF Notification (" + m_host + ":" + stream + ")";
      m_listenerThread = new CLThread(threadName, listener);
      m_listenerThread.startup();
   }
   
   public void startNotifications(final String stream, final Element filter, final String startTime, final String stopTime) throws RuntimeException
   {
	      if (!m_notificationsSupported)
	      {
	         throw new RuntimeException("Netconf notifications are not supported by this device");
	      }
	      if (m_createSubscriptionMessageId != null)
	      {
	         throw new RuntimeException("A listener is already registered for notifications");
	      }
	      // Send the <create-subscription> message
	      s_logger.info("Subscribing for notifications: - stream: " + stream + "; startTime: " + startTime);
	      m_createSubscriptionMessageId = new UUID().toString();
	      Element msg = createSubscriptionMessageXml(m_createSubscriptionMessageId, stream, filter, startTime, stopTime);
	      super.syncSend(msg, true);
	      if (!m_readyToReceiveNotifications)
	      {
	         throw new RuntimeException("Failed to register listener to receive notifications");
	      }
	      // Things are good; start receiving all the notifications
	      NotificationListener listener = new NotificationListener(this);
	      // Put device address as part of thread name below
	      String threadName = "NETCONF Notification (" + m_host + ":" + stream + ")";
	      m_listenerThread = new CLThread(threadName, listener);
	      m_listenerThread.startup();	   
   }

   /**
    * Create the <b>create-subscription</b> message to send to the device.
    *
    * @param messageId     Message ID to use.
    * @param stream        Stream to create subscription for (NULL to use the default stream). 
    * @param startTime     Start time from when messages need to be replayed (NULL if no replay is required).
    * @return              XML representing the create-subscription message.
    */
   private static Element createSubscriptionMessageXml(final String messageId, final String stream, final Timestamp startTime)
   {
      Namespace netconfNs = Namespace.getNamespace(BASE_NAMESPACE);
      Element rpc = new Element("rpc", netconfNs);
      rpc.setAttribute("message-id", messageId);
      Namespace notificationNs = Namespace.getNamespace(NOTIF_NAMESPACE);
      Element cs = new Element("create-subscription", notificationNs);
      rpc.addContent(cs);
      // Add the stream name (if specified)
      if (stream != null)
      {
         Element streamElem = new Element("stream", notificationNs);
         streamElem.setText(stream);
         cs.addContent(streamElem);
      }
      // Add the start time (if specified)
      if (startTime != null)
      {
         Element fromWhen = new Element("startTime", notificationNs);
         RFC3399Timestamp ts = new RFC3399Timestamp(startTime);
         fromWhen.setText(ts.toString());
         cs.addContent(fromWhen);
      }
      return rpc;
   }

   private static Element createSubscriptionMessageXml(final String messageId, final String stream, final Element filter, 
		   final String startTime, final String stopTime)
   {
      Namespace netconfNs = Namespace.getNamespace(BASE_NAMESPACE);
      Element rpc = new Element("rpc", netconfNs);
      rpc.setAttribute("message-id", messageId);
      Namespace notificationNs = Namespace.getNamespace(NOTIF_NAMESPACE);
      Element cs = new Element("create-subscription", notificationNs);
      rpc.addContent(cs);
      // Add the stream name (if specified)
      if (stream != null)
      {
         Element streamElem = new Element("stream", notificationNs);
         streamElem.setText(stream);
         cs.addContent(streamElem);
      }
      
      if (filter != null)
      {
    	  cs.addContent(filter);
      }
      
      // Add the start time (if specified)
      if (startTime != null)
      {
         Element fromWhen = new Element("startTime", notificationNs);
         fromWhen.setText(startTime);
         cs.addContent(fromWhen);
      }
      
      if (stopTime != null)
      {
          Element stopAt = new Element("stopTime", notificationNs);
          stopAt.setText(stopTime);
          cs.addContent(stopAt);    	  
      }
      return rpc;
   }

   @Override
   public void disconnect()
   {
      if (m_listenerThread != null)
      {
         m_listenerThread.shutdown();
      }
      super.disconnect();
      
   }


   /**
    * The NotificationListener class is a runnable that listens to notification messages from a device.
    *
    * @author  Subramaniam Aiylam
    * @since   1.6
    */
   private static class NotificationListener implements CLRunnable
   {

      /** The parent connection */
      AsyncSshConnection m_parent;

      /**
       * Creates a NotificationListener.
       *
       * @param connection    parent connection.
       */
      NotificationListener(AsyncSshConnection connection)
      {
         m_parent = connection;
      }

      @Override
      public void run() throws InterruptedException
      {
         boolean noErrors = true;
         while (noErrors)
         {
            try
            {
               // Call out to the base stream reader
               m_parent.streamDataInFromWire(true);
            }
            catch (final Exception ex)
            {
               // Give up on an exception? Or continue?
               s_logger.warn("Error encountered while reading stream: " + ex.getMessage());
               if (s_logger.isDebugEnabled())
               {
                  s_logger.error(ex, ex);
               }
               // Indicate to the upper layer (via NotificationListener)  that the connection needs to be reset
               m_parent.m_listener.connectionTerminated();
               noErrors = false;
            }
         }
         s_logger.info("Exiting notification loop..");
      }
   }

}
