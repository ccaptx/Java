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
package org.dvlyyon.net.netconf.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.util.XmlUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The NetconfException class represents an exception that is thrown by the NETCONF transport layer. The list of possible
 * exceptions are defined in the NETCONF RFC.
 * 
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class NetconfException extends RuntimeException
{

   /** The possible error codes from NETCONF */
   public enum ErrorTag
   {
      InUse("in-use"),
      InvalidValue("invalid-value"),
      TooBig("too-big"),
      MissingElement("missing-element"),
      BadElement("bad-element"),
      BadAttribute("bad-attribute"),
      UnknownElement("unknown-element"),
      UnknownAttribute("unknown-attribute"),
      UnknownNamespace("unknown-namespace"),
      AccessDenied("access-denied"),
      LockDenied("lock-denied"),
      ResourceDenied("resource-denied"),
      RollbackFailed("rollback-failed"),
      DataExists("data-exists"),
      DataMissing("data-missing"),
      OperationNotSupported("operation-not-supported"),
      OperationFailed("operation-failed"),
      PartialOperation("partial-operation");

      /** The NETCONF error tag */
      private final String m_netconfTag;

      /**
       * Constructs an ErrorTag.
       *
       * @param netconfErrorTag  the Tag as a string.
       */
      ErrorTag(String netconfErrorTag)
      {
         m_netconfTag = netconfErrorTag;
      }

      /** */
      String getNetconfTag()
      {
         return m_netconfTag;
      }
   }

   /** Definition of the type of NETCONF error */
   public enum ErrorType
   {
      /** Transport-layer error */
      transport,
      /** Netconf RPC error */
      rpc,
      /** Protocol error */
      protocol,
      /** Application-layer error (generated by the higher-level app) */
      application;
   }

   /** Definition of error severity */
   public enum ErrorSeverity
   {
      /** Error */
      error,
      /** Warning */
      warning;
   }

   /** Logger for tracing */
   protected final static Log s_logger = LogFactory.getLog(NetconfException.class);

   /** The base NETCONF namespace */
   private static Namespace s_xmlns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");

   /** Mapping between string error and the tag itself */
   private static HashMap<String, ErrorTag> s_tagMap = new HashMap<String, ErrorTag>();

   /** Resource bundles containing descriptive string corresponding to each error */
   private static ResourceBundle s_descriptions;

   /** The error type */
   private ErrorType m_type = ErrorType.rpc;

   /** The error severity */
   private ErrorSeverity m_severity = ErrorSeverity.error;

   /** The error tag */
   private ErrorTag m_tag;

   /** Path information to the source of the error */
   private String m_path;

   /** The error message */
   private String m_message;

   /** Information about the error */
   private String m_info;


   static
   {
      for (ErrorTag et : ErrorTag.values())
      {
         s_tagMap.put(et.getNetconfTag(), et);
      }
      try
      {
         s_descriptions = ResourceBundle.getBundle("com.centeredlogic.net.netconf.exception.exceptionDescriptions", Locale.getDefault());
      }
      catch (Exception ex)
      {
         s_logger.warn("Error loading exception resource:" + ex.getMessage());
      }
   };


   /**
    * Constructs a NetconfException with the specified message.
    *
    * @param msg  Exception message.
    */
   public NetconfException(final String msg)
   {
      super(msg);
   }
   
   /**
    * Constructs a NetconfException with the specified tag.
    *
    * @param tag  The error tag.
    */
   public NetconfException(final ErrorTag tag)
   {
      super(tag.toString());
      m_tag = tag;
   }

   /**
    * Converts the NetconfException to its XML version.
    *
    * @return  XML-ized version of the exception, similar to what you would see on the wire.
    */
   public Element toXml()
   {
      final Element root = new Element("rpc-error", s_xmlns);
      final Element type = new Element("error-type", s_xmlns);
      type.setText(m_type.toString());
      root.addContent(type);
      final Element tag = new Element("error-tag", s_xmlns);
      type.setText(m_tag.getNetconfTag());
      root.addContent(tag);
      final Element severity = new Element("error-severity", s_xmlns);
      type.setText(m_severity.toString());
      root.addContent(severity);
      final Element message = new Element("error-message", s_xmlns);
      type.setText(m_message);
      root.addContent(message);
      if (m_path != null)
      {
         final Element path = new Element("error-path", s_xmlns);
         path.addContent(m_path);
         root.addContent(path);         
      }
      if (m_info != null)
      {
         try
         {
            // Note: the info tag may have a different namespace; we lose it in the transformation
            Element info = XmlUtils.fromXmlString(m_info);
            info.setNamespace(s_xmlns);
            root.addContent(info);
         }
         catch (final Exception ex)
         {
            s_logger.error("Error converting NetconfException to XML");
            if (s_logger.isDebugEnabled())
            {
               s_logger.error(ex, ex);
            }
         }
      }
      return root;
   }

   /**
    * Given the NETCONF RPC error as XML (over the wire), converts it into a NetconfException.
    *
    * @param rpcError   Netconf RPC error (as described in RFC 6241).
    * @return           newly created NetconfException.
    */
   @SuppressWarnings("unchecked")
   public static NetconfException fromXml(Element rpcError)
   {
      ErrorType type = null;
      ErrorTag tag = null;
      ErrorSeverity severity = null;
      String message = null;
      String path = null;
      String info = null;
      if (rpcError.getName().equals("rpc-error"))
      {
         final List<Element> kids = (List<Element>) rpcError.getChildren();
         for (Element kid : kids)
         {
            if (kid.getName().equals("error-type"))
            {
               type = ErrorType.valueOf(kid.getText().trim());
            }
            else if (kid.getName().equals("error-tag"))
            {
               tag = s_tagMap.get(kid.getText());
            }
            else if (kid.getName().equals("error-severity"))
            {
               severity = ErrorSeverity.valueOf(kid.getText());
            }
            else if (kid.getName().equals("error-message"))
            {
               message = kid.getText();
            }
            else if (kid.getName().equals("error-path"))
            {
               path = kid.getText();
            }
            else if (kid.getName().equals("error-info"))
            {
               info = XmlUtils.toXmlString(kid, false);
            }
         }         
      }
      if (tag == null || type == null || severity == null)
      {
         s_logger.error("Error creating NetconfException from XML; missing error-tag, error-type or errr-severity");
         return null;
      }
      final NetconfException ret = new NetconfException(tag);
      ret.setType(type);
      ret.setSeverity(severity);
      ret.setErrorMessage(message);
      ret.setInfo(info);
      ret.setPath(path);
      return ret;
   }

   /** */
   public ErrorType getType()
   {
      return m_type;
   }

   /** */
   public void setType(final ErrorType type)
   {
      m_type = type;
   }

   /** */
   public ErrorSeverity getSeverity()
   {
      return m_severity;
   }

   /** */
   public void setSeverity(final ErrorSeverity severity)
   {
      m_severity = severity;
   }

   /** */
   public ErrorTag getTag()
   {
      return m_tag;
   }

   /** */
   public void setTag(final ErrorTag tag)
   {
      m_tag = tag;
   }

   /** */
   public String getPath()
   {
      return m_path;
   }

   /** */
   public void setPath(final String path)
   {
      m_path = path;
   }

   /** */
   public String getErrorMessage()
   {
      return m_message;
   }

   /** */
   public void setErrorMessage(final String message)
   {
      m_message = message;
   }

   /** */
   public String getInfo()
   {
      return m_info;
   }

   /** */
   public void setInfo(final String errorInfo)
   {
      m_info = errorInfo;
   }

   /**
    * Returns the description of the error. If the descriptions resouce bundle is found, returns
    * the error string translated from the tag.
    *
    * @return  Error tag or error description as translated using the resource bundle.
    */
   public String getDescription()
   {
      String ret = m_tag.name();
      if (s_descriptions != null)
      {
         ret = s_descriptions.getString(ret);
      }
      return ret;
   }

   @Override
   public String getMessage()
   {
      StringBuffer ret = new StringBuffer("");
      ret.append("Error code: " + getTag())
         .append("Error type: " + getType())
         .append("Error Message: " + getErrorMessage())
         .append("Error info: " + getInfo())
         .append("Path: " + getPath());
      return ret.toString();
   }
}
