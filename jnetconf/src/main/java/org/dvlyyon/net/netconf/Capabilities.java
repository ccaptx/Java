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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dvlyyon.common.util.XmlUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The Capabilities class represents the set of features supported by a NETCONF device. To learn more about the device capabilities
 * described here, refer to <a href="http://tools.ietf.org/html/rfc6241">RFC 6241</a> and
 * <a href="http://tools.ietf.org/html/rfc5277">RFC 5277</a>.
 * <p>
 * <b>NOTE:</b> Support for the URL Capability is currently missing in this class.
 * 
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class Capabilities
{

   /** The base namespace for all NETCONF XML */
   private static Namespace s_xmlns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");

   /** List of capability strings obtained from device - key/value are both the same (the capability) */
   private HashMap<String, String> m_caps = new HashMap<String, String>();

   /** Set of notification streams supported by the device */
   private List<NotificationStream> m_streams = new ArrayList<NotificationStream>();


   /**
    * Loads up the device's capabilities from the specified XML.
    *
    * @param capsRoot   XML root element representing all the capabilities of the device.
    */
   @SuppressWarnings("unchecked")
   public void fromNetconfXml(Element capsRoot)
   {
      List<Element> caps = (List<Element>) capsRoot.getChildren("capability", s_xmlns);
      for (Element cap : caps)
      {
         processCapability(cap);
      }
   }

   /**
    * Processes the device capability as specified by the XML node.
    * 
    * @param capElem    XML element representing a device capability.
    */
   private void processCapability(final Element capElem)
   {
      String capName = capElem.getText().trim();
      m_caps.put(capName, capName);
   }

   /**
    * Returns the capabilities formatted like it is exchanged over NETCONF as XML.
    *
    * @return  Element representing the capabilities as NETCONF XML.
    */
   public Element toNetconfXml()
   {
      Element ret = new Element("capabilities", s_xmlns);
      for (String cap : m_caps.values())
      {
         addCapability(ret, true, cap);         
      }
      return ret;
   }

   /**
    * Returns the capabilities as a string (formatted like it is exchanged over NETCONF as XML).
    *
    * @return  String representation of the capabilities (as NETCONF XML).
    */
   public String toString()
   {
      return XmlUtils.toXmlString(toNetconfXml(), true);
   }

   /**
    * Adds the specified capability to the specified parent, depending upon whether it exists or not.
    *
    * @param parent        Element representing parent capabilities node.
    * @param isCapable     True if the capability exists, false if not.
    * @param capURI        String representing the capability URI.
    */
   private void addCapability(final Element parent, final boolean isCapable, final String capURI)
   {
      if (isCapable)
      {
         Element cap = new Element("capability", s_xmlns);
         cap.setText(capURI);
         parent.addContent(cap);
      }
   }

   /**
    * Returns the capability that starts with the specified prefix.
    *
    * @param startingUrl   String that specifies what the capability name starts with.
    * @return              Entire value of the capability, or NULL if no such capability exists.
    */
   public String getCapability(String startingUrl)
   {
      String ret = null;
      for (String cap : m_caps.values())
      {
         if (cap.startsWith(startingUrl))
         {
            ret = cap;
         }
      }
      return ret;
   }

   /** */
   public boolean supportsChunkedFraming()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:base:1.1");
   }
   
   /** */
   public boolean supportsWritableRunningConfig()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:writable-running:1.0");
   }
   
   /** */
   public boolean supportsConfirmedCommit()
   {
      return ( m_caps.containsKey("urn:ietf:params:netconf:capability:confirmed-commit:1.0") ||
               m_caps.containsKey("urn:ietf:params:netconf:capability:confirmed-commit:1.1") );
   }

   /** */
   public boolean supportsPersistId()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:confirmed-commit:1.1");
   }

   /** */
   public boolean supportsCandidateConfig()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:candidate:1.0");
   }

   /** */
   public boolean supportsRollbackOnError()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:rollback-on-error:1.0");
   }

   /** */
   public boolean supportsXpath()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:xpath:1.0");
   }

   /** */
   public boolean supportsNotifications()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:notification:1.0");
   }

   /** */
   public boolean supportsInterleave()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:interleave:1.0");
   }

   /** */
   public boolean supportsValidation()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:validate:1.0");
   }

   /** */
   public boolean supportsDistinctStartupConfig()
   {
      return m_caps.containsKey("urn:ietf:params:netconf:capability:startup:1.0");
   }

   /** */
   public List<NotificationStream> getNotificationStreams()
   {
      return m_streams;
   }

   /** */
   public void setNotificationStreams(List<NotificationStream> streams)
   {
      m_streams = streams;
   }

}
