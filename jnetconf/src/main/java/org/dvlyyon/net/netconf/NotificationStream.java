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
package org.dvlyyon.net.netconf;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.dvlyyon.common.net.RFC3399Timestamp;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The NotificationStream class represents a notification stream supported on a NETCONF device. To learn more about notification streams
 * refer to <a href="http://tools.ietf.org/html/rfc5277">RFC 5277</a>.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class NotificationStream
{

   /** The base NETCONF XML namespace - see RFC 6241 */
   private static Namespace s_xmlns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");

   /** The NETCONF namespace used for notifications - see RFC 5277 */
   private static Namespace s_notificationNs = Namespace.getNamespace("urn:ietf:params:xml:ns:netmod:notification");

   /** Name of the stream */
   private String m_name;

   /** Description of the stream */
   private String m_description;

   /** True if the stream supports event replay, false if not */
   private boolean m_supportsReplay;

   /** Time at which the replay log for this stream was created (or null if replay is not supported) */
   private Timestamp m_replayLogCreationTime;

   /** Time of the oldest entry in the replay log (any notifications that took place before this are effectively lost) */
   private Timestamp m_replayLogAgedTime;


   /**
    * Constructs a default NotificationStream.
    */
   public NotificationStream()
   {
      // Default public, no-args CTOR
   }

   /**
    * Loads up a notification stream from a NETCONF XML tree.
    *
    * @param stream  XML root element representing the stream as it is sent via netconf.
    */
   public void fromNetconfXml(Element stream)
   {
      Element child = stream.getChild("name", s_notificationNs);
      if (child != null)
      {
         m_name = child.getText();
      }
      child = stream.getChild("description", s_notificationNs);
      if (child != null)
      {
         m_description = child.getText();
      }
      child = stream.getChild("replaySupport", s_notificationNs);
      if (child != null)
      {
         m_supportsReplay = "true".equals(child.getText());
      }
      child = stream.getChild("replayLogCreationTime", s_notificationNs);
      if (child != null)
      {
         RFC3399Timestamp ts = new RFC3399Timestamp(child.getText());
         m_replayLogCreationTime = ts.getSqlTimestamp();
      }
      child = stream.getChild("replayLogAgedTime", s_notificationNs);
      if (child != null)
      {
         RFC3399Timestamp ts = new RFC3399Timestamp(child.getText());
         m_replayLogAgedTime = ts.getSqlTimestamp();
      }
   }

   /**
    * Returns the notification stream formatted like it is exchanged over NETCONF as XML.
    *
    * @return  Element representing the stream as NETCONF XML.
    */
   public Element toNetconfXml()
   {
      Element ret = new Element("stream", s_notificationNs);
      Element child = null;
      if (m_name != null)
      {
         child = new Element("name", s_notificationNs);
         child.setText(m_name);
         ret.addContent(child);
      }
      if (m_description != null)
      {
         child = new Element("description", s_notificationNs);
         child.setText(m_description);
         ret.addContent(child);
      }
      child = new Element("supportsReplay", s_notificationNs);
      child.setText("" + m_supportsReplay);
      ret.addContent(child);
      if (m_replayLogCreationTime != null)
      {
         child = new Element("replayLogCreationTime", s_notificationNs);
         RFC3399Timestamp ts = new RFC3399Timestamp(m_replayLogCreationTime);
         child.setText(ts.toString());
         ret.addContent(child);
      }
      if (m_replayLogAgedTime != null)
      {
         child = new Element("replayLogAgedTime", s_notificationNs);
         RFC3399Timestamp ts = new RFC3399Timestamp(m_replayLogAgedTime);
         child.setText(ts.toString());
         ret.addContent(child);
      }
      return ret;
   }

   /** */
   public String getName()
   {
      return m_name;
   }

   /** */
   public String getDescription()
   {
      return m_description;
   }

   /** */
   public boolean supportsReplay()
   {
      return m_supportsReplay;
   }

   /** */
   public Timestamp getReplayLogCreationTime()
   {
      return m_replayLogCreationTime;
   }

   /** */
   public Timestamp getReplayLogAgedTime()
   {
      return m_replayLogAgedTime;
   }

   /**
    * Creates the <b>getStreams</b> request to send to the NETCONF client.
    *
    * @param wrap    false if just the request XML is desired, true if the complete (valid) XML RPC is required. 
    * @return        The XML node representing the getStreams request (if unwrapped) or the completely formed RPC request (if wrapped)
    */
   public static Element createGetStreamRequest(boolean wrap)
   {
      final Element get = new Element("get", s_xmlns);
      final Element filter = new Element("filter", s_xmlns);
      filter.setAttribute("type", "subtree");
      get.addContent(filter);
      final Element netconf = new Element("netconf", s_notificationNs);
      filter.addContent(netconf);
      final Element streams = new Element("streams", s_notificationNs);
      netconf.addContent(streams);
      if (wrap)
      {
         Namespace xmlns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");
         Element rpcRequest = new Element("rpc", xmlns);
         rpcRequest.setAttribute("message-id", "000");
         rpcRequest.addContent(get);
         return rpcRequest;
      }
      else
      {
         return get;
      }
   }

   /**
    * Processes the response from the getStreams message, returning information about the supported notification streams.
    *
    * @param response   Data from getStreams response from the device.
    * @return           List of notification streams supported by the device, or an empty list if none are supported.
    */
   @SuppressWarnings("unchecked")
   public static List<NotificationStream> processGetStreamsResponse(Element response)
   {
      List<NotificationStream> streamsOnDevice = new ArrayList<NotificationStream>();
      final Element netconf = response.getChild("netconf", s_notificationNs);
      if (netconf != null)
      {
         final Element streamsElem = netconf.getChild("streams", s_notificationNs);
         if (streamsElem != null)
         {
            final List<Element> streams = (List<Element>) streamsElem.getChildren("stream", s_notificationNs);
            for (Element stream : streams)
            {
               NotificationStream s = new NotificationStream();
               s.fromNetconfXml(stream);
               streamsOnDevice.add(s);
            }
         }
      }
      return streamsOnDevice;
   }

}
