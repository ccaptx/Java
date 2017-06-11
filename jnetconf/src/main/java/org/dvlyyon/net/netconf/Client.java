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

import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.net.ConnectionPoller;
import org.dvlyyon.common.transaction.LocalTransactionContextIf;
import org.dvlyyon.common.transaction.TransactionalResourceIf;
import org.dvlyyon.common.util.CLThread;
import org.dvlyyon.common.util.XmlUtils;
import org.dvlyyon.net.netconf.exception.NetconfException;
import org.dvlyyon.net.netconf.transport.HelloResponseProcessorIf;
import org.dvlyyon.net.netconf.transport.TransportClientIf;
import org.dvlyyon.net.netconf.transport.http.HttpTransportClient;
import org.dvlyyon.net.netconf.transport.ssh.SshTransportClient;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The Client class represents a NETCONF client that provides operations defined by NETCONF. See <a href="http://tools.ietf.org/html/rfc6241">RFC 6241</a>
 * and <a href="http://tools.ietf.org/html/rfc5277">RFC 5277</a> for more details.
 * <p>
 * <b>NOTE:</b> Support for the URL and XPATH capability is currently missing.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class Client implements TransactionalResourceIf, ConfiguratorIf, HelloResponseProcessorIf
{

   /** Logger for tracing */
   protected final static Log s_logger = LogFactory.getLog(Client.class);

   /** The base netconf XML namespace */
   private static Namespace s_xmlns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");

   /** The transport itself */
   private TransportClientIf m_transport;

   /** Health checker that tests device connectivity */
   //private CLThread m_pinger;

   /** Flag that indicates device availability; if true, it means the device is reachable */
   private boolean m_deviceAvailable;

   /** Flag that keeps track of whether a hello response has been received */
   private boolean m_helloResponseReceived;

   /** Capabilities that the device supports */
   private Capabilities m_deviceCaps = new Capabilities();

   /** The current message ID - message ids are monotonically increasing numbers */
   private BigInteger m_messageIdTracker = BigInteger.ONE;

   private String m_transactionTag;
   
   private String m_sessionID;


   public long lastRequestTime = 0;
   public long lastResponseTime = 0;

   
   /**
    * Constructs a NETCONF Client.
    */
   public Client()
   {
   }
   
   public Client(final Properties props)
   {
       setup(props, false);

   }

   /**
    * Called to set up the NETCONF client.
    *
    * @param props               Properties containing setup configuration parameters (such as the protocol, host, port, etc).
    * @throws RuntimeException   if an error occurs during setup.
    */
   public void setup(final Properties props) throws RuntimeException
   {
       setup(props, true);
   }
   
   /**
    * 
    * @param props
    * @param connect - true to connect by loading device caps right away.   if false, you must call connect() afterward, before sending traffic.
    * @throws RuntimeException
    */
   public void setup(final Properties props, boolean connect) throws RuntimeException
   {
      String protocol = props.getProperty("protocol", "ssh");
      if (protocol.equals("ssh"))
      {
         m_transport = new SshTransportClient();
      }
      else if (protocol.equals("http"))
      {
         m_transport = new HttpTransportClient();
      }
      else
      {
         throw new RuntimeException("Invalid netconf protocol specified: " + protocol);
      }
      
      m_transport.setup(props, this);

      if (connect)
         connect();
   }
   
   
   /**
    * call this to establish a connection after calling setup()
    */
   public void connect()
   {
       loadDeviceCaps();

   }

   /**
    * Load the device capabilities. Send the device a <i>getStreams</i> message - this will do two things:<ul>
    * <li>Force a hello exchange, which will give us the device capabilities</li>
    * <li>Extract the list of notification streams the device supports</li>
    * </ul>
    */
   private void loadDeviceCaps()
   {
      Element response = send(NotificationStream.createGetStreamRequest(false), null);
      List<NotificationStream> streams = NotificationStream.processGetStreamsResponse(response);
      getDeviceCapabilities().setNotificationStreams(streams);
   }

   /**
    * Test validity of the connection.  Sends a Get Streams request 
    *
    */
   public void testConection()
   {
       loadDeviceCaps();
   }
   
   /**
    * Called when the device sends <b>hello</b> message. This message comes with a list of device capabilities,
    * so we go process them.
    *
    * @param response            XML node representing the RPC response.
    * @throws RuntimeException   if an error occurred processing the hello message.
    */
   @Override
   public synchronized void processHelloResponse(final Element response) throws RuntimeException
   {
      if (!m_helloResponseReceived)
      {
         Namespace xmlns = response.getNamespace();
         if (!"hello".equals(response.getName()))
         {
            s_logger.error("Unexpected message received, expected a hello response");
            s_logger.error("No capability information was processed!");
            return;
         }
         // This gives us the device capabilities - go get them
         Element capsRoot = response.getChild("capabilities", xmlns);
         if (capsRoot == null)
         {
            s_logger.error("Invalid hello response received; no capabilites sent by device.");
         }
         else
         {
            Element sessionIdElem = response.getChild("session-id", xmlns);
            if (sessionIdElem != null)
            {
            	this.m_sessionID = sessionIdElem.getTextTrim();
               s_logger.debug("Got a session id of: " + sessionIdElem.getText());
            }
            else
            {
               String msg = "Error: No session ID received in hello message";
               s_logger.error(msg);
               throw new RuntimeException(msg);
            }
            m_deviceCaps.fromNetconfXml(capsRoot);
         }
         m_helloResponseReceived = true;
      }
   }
   
   
   public String getSessionID() {
	   return m_sessionID;
   }

   /**
    * Called to shut down the netconf client.
    */
   public void shutdown()
   {
       /*
      if (m_pinger != null)
      {
         m_pinger.shutdown();
         m_pinger = null;
      }*/
      if (m_transport != null)
      {
         m_transport.shutdown();
         m_transport = null;
      }
   }

   /**
    * Returns the capabilities of the device.
    *
    * @return  Device capabilities.
    */
   public Capabilities getDeviceCapabilities()
   {
      return m_deviceCaps;
   }

   /**
    * Returns the underlying transport client.
    *
    * @return  the underlying transport.
    */
   public TransportClientIf getTransport()
   {
      return m_transport;
   }

   ////////////////// NETCONF configuration support ///////////////////////////////////////////////

   @Override
   public Element getConfig(final String configurationName, final Element filter, final String xid) throws RuntimeException
   {
      String store = configurationName == null ? "running" : configurationName;
      if (store.equals("candidate") && !m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Target device does not support the candidate-config capability");
      }
      if (store.equals("startup") && !m_deviceCaps.supportsDistinctStartupConfig())
      {
         throw new RuntimeException("Target device does not support the distinct startup config capability");
      }
      Element getConfig = new Element("get-config", s_xmlns);
      Element src = new Element("source", s_xmlns);
      getConfig.addContent(src);
      Element config = new Element(store, s_xmlns);
      src.addContent(config);
      getConfig.addContent(filter);
      return send(getConfig, xid);
   }

   @Override
   public Element get(final Element filter, final String xid) throws RuntimeException
   {
      Element getConfig = new Element("get", s_xmlns);
      getConfig.addContent(filter);
      return send(getConfig, xid);
   }

   @Override
   public void editConfig(final String configurationName, final Element xml, final EditOperation defaultEditOperation,
                          final TestOption testOption, final ErrorOption errorOption, final String xid) throws RuntimeException
   {
      String store = configurationName == null ? "running" : configurationName;
      if (store.equals("running") && !m_deviceCaps.supportsWritableRunningConfig())
      {
         throw new RuntimeException("Target device does not support the writable-running capability");
      }
      if (store.equals("candidate") && !m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Target device does not support the candidate-config capability");
      }
      Element editConfig = new Element("edit-config", s_xmlns);
      Element target = new Element("target", s_xmlns);
      editConfig.addContent(target);
      if (defaultEditOperation != null)
      {
         Element editOperationE = new Element("default-operation", s_xmlns);
         switch (defaultEditOperation)
         {
            case None: editOperationE.setText("none"); break;
            case Merge: editOperationE.setText("merge"); break;
            case Replace: editOperationE.setText("replace"); break;
            default: break;
         }
         if (editOperationE.getText() != null && !editOperationE.getText().equals(""))
         {
            editConfig.addContent(editOperationE);
         }
      }
      if (testOption != null)
      {
         if (testOption != TestOption.None)
         {
            if (!m_deviceCaps.supportsValidation())
            {
               throw new RuntimeException("Device does not support validation capability");            
            }
            Element testOptionE = new Element("test-option", s_xmlns);
            switch (testOption)
            {
               case TestThenSet: testOptionE.setText("test-then-set"); break;
               case Set: testOptionE.setText("set"); break;
               case TestOnly: testOptionE.setText("test-only"); break;
               default: break;
            }
            editConfig.addContent(testOptionE);
         }  
      }
      if (errorOption != null)
      {
         if (errorOption != ErrorOption.None)
         {
            Element errorOptionE = new Element("error-option", s_xmlns);
            if (errorOption == ErrorOption.RollbackOnError && !m_deviceCaps.supportsRollbackOnError())
            {
               throw new RuntimeException("Target device does not support the rollback-on-error capability");
            }
            switch (errorOption)
            {
               case StopOnError: errorOptionE.setText("stop-on-error"); break;
               case ContinueOnError: errorOptionE.setText("continue-on-error"); break;
               case RollbackOnError: errorOptionE.setText("rollback-on-error"); break;
               default: break;
            }
            editConfig.addContent(errorOptionE);
         }  
      }
      Element config = new Element(store, s_xmlns);
      target.addContent(config);
      Element cfgElem = new Element("config", s_xmlns);
      editConfig.addContent(cfgElem);
      cfgElem.addContent(xml);
      send(editConfig, xid);
   }

   @Override
   public void copyConfig(final String sourceConfigurationName, final String targetConfigurationName, final String xid) throws RuntimeException
   {
      if (targetConfigurationName.equals("running") && !m_deviceCaps.supportsWritableRunningConfig())
      {
         throw new RuntimeException("Target device does not support the writable-running capability");
      }
      if (targetConfigurationName.equals("candidate") && !m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Target device does not support the candidate-config capability");
      }
      if ( (sourceConfigurationName.equals("startup") || targetConfigurationName.equals("startup")) && 
           !m_deviceCaps.supportsDistinctStartupConfig() )
      {
         throw new RuntimeException("Target device does not support the distinct startup config capability");
      }
      Element copyConfig = new Element("copy-config", s_xmlns);
      Element target = new Element("target", s_xmlns);
      target.setText(targetConfigurationName);
      copyConfig.addContent(target);
      Element source = new Element("source", s_xmlns);
      source.setText(sourceConfigurationName);
      copyConfig.addContent(source);
      send(copyConfig, xid);
   }

   @Override
   public void deleteConfig(final String configurationName, final String xid) throws RuntimeException
   {
      if (configurationName.equals("candidate") && !m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Target device does not support the candidate-config capability");
      }
      if (configurationName.equals("startup") && !m_deviceCaps.supportsDistinctStartupConfig())
      {
         throw new RuntimeException("Target device does not support the distinct startup config capability");
      }
      Element deleteConfig = new Element("delete-config", s_xmlns);
      Element target = new Element("target", s_xmlns);
      target.setText(configurationName);
      deleteConfig.addContent(target);
      send(deleteConfig, xid);
   }

   @Override
   public void lock(final String configurationName, final String xid) throws RuntimeException
   {
      if (configurationName.equals("candidate") && !m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Target device does not support the candidate-config capability");
      }
      if (configurationName.equals("startup") && !m_deviceCaps.supportsDistinctStartupConfig())
      {
         throw new RuntimeException("Target device does not support the distinct startup config capability");
      }
      Element lock = new Element("lock", s_xmlns);
      Element target = new Element("target", s_xmlns);
      lock.addContent(target);
      Element configName = new Element(configurationName, s_xmlns);
      target.addContent(configName);
      send(lock, xid);   
   }

   @Override
   public void unlock(final String configurationName, final String xid) throws RuntimeException
   {
      if (configurationName.equals("candidate") && !m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Target device does not support the candidate-config capability");
      }
      if (configurationName.equals("startup") && !m_deviceCaps.supportsDistinctStartupConfig())
      {
         throw new RuntimeException("Target device does not support the distinct startup config capability");
      }
      Element unlock = new Element("unlock", s_xmlns);
      Element target = new Element("target", s_xmlns);
      unlock.addContent(target);
      Element configName = new Element(configurationName, s_xmlns);
      target.addContent(configName);
      send(unlock, xid);
   }

   @Override
   public void validate(final String configurationName, final String xid) throws RuntimeException
   {
      if (!m_deviceCaps.supportsValidation())
      {
         throw new RuntimeException("Target device does not support validation capability");
      }
      if (configurationName.equals("candidate") && !m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Target device does not support the candidate-config capability");
      }
      if (configurationName.equals("startup") && !m_deviceCaps.supportsDistinctStartupConfig())
      {
         throw new RuntimeException("Target device does not support the distinct startup config capability");
      }
      Element validate = new Element("validate", s_xmlns);
      Element source = new Element("source", s_xmlns);
      source.setText(configurationName);
      validate.addContent(source);
      send(validate, xid);
   }

   @Override
   public void commit(final String persistId, final boolean confirm, int timeoutInSeconds, final String xid) throws RuntimeException
   {
      Element commit = new Element("commit", s_xmlns);
      if (!m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Device does not support candidate configuration");
      }
      if (persistId != null && !persistId.equals(""))
      {
         if (!m_deviceCaps.supportsPersistId())
         {
            throw new RuntimeException("Device does not support persist ID for commit");
         }
         else
         {
            Element persist = new Element("persist", s_xmlns);
            persist.setText(persistId);
            commit.addContent(persist);
         }
      }
      if (confirm)
      {
         if (!m_deviceCaps.supportsConfirmedCommit())
         {
            throw new RuntimeException("Device does not support confirmed commit operation");            
         }
         Element confirmed = new Element("confirmed", s_xmlns);
         commit.addContent(confirmed);
         Element timeout = new Element("confirm-timeout", s_xmlns);
         timeout.setText("" + timeoutInSeconds);
         commit.addContent(timeout);
      }
      send(commit, xid);
   }

   @Override
   public void cancelCommit(final String persistId, final String xid) throws RuntimeException
   {
      Element cancelCommit = new Element("cancel-commit", s_xmlns);
      if (!m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Device does not support candidate configuration");
      }
      if (!m_deviceCaps.supportsConfirmedCommit())
      {
         throw new RuntimeException("Device does not support confirmed commit operation");            
      }
      if (persistId != null && !persistId.equals(""))
      {
         if (!m_deviceCaps.supportsPersistId())
         {
            throw new RuntimeException("Device does not support persist ID for commit");
         }
         else
         {
            Element persist = new Element("persist-id", s_xmlns);
            persist.setText(persistId);
            cancelCommit.addContent(persist);
         }
      }
      send(cancelCommit, xid);    
   }

   @Override
   public void discardChanges(final String xid) throws RuntimeException
   {
      if (!m_deviceCaps.supportsCandidateConfig())
      {
         throw new RuntimeException("Device does not support candidate configuration");
      }
      Element discardChanges = new Element("discard-changes", s_xmlns);
      send(discardChanges, xid);
   }

   @Override
   public void closeSession() throws RuntimeException
   {
      Element closeSession = new Element("close-session", s_xmlns);
      // The "close-session" call always closes THIS session
      send(closeSession, null);
   }

   @Override
   public void killSession(String sessionId, final String xid) throws RuntimeException
   {
      Element killSession = new Element("kill-session", s_xmlns);
      Element sessId = new Element("session-id", s_xmlns);
      killSession.addContent(sessId);
      send(killSession, xid);
   }

   ////////////////// Transaction support /////////////////////////////////

   @Override
   public String getIdentifier()
   {
      return m_transactionTag;
   }

   @Override
   public void setIdentifier(String identifier)
   {
      m_transactionTag = identifier;
   }

   @Override
   public LocalTransactionContextIf startTransaction() throws RuntimeException
   {
      String xid = m_transport.startTransaction();
      NetconfLocalTransactionContext context = new NetconfLocalTransactionContext();
      context.setTransactionId(xid);
      discardChanges(xid);
      lock("candidate", xid);
      lock("running", xid);
      return context;
   }

//   public Object performOperation(LocalTransactionContextIf resourceContext, String operationName, List<Object> parameters) throws RuntimeException
//   {
//      NetconfLocalTransactionContext context = (NetconfLocalTransactionContext) resourceContext;
//      String xid = context.getTransactionId();
//      Object ret = null;
//      if (operationName.equals("get"))
//      {
//         Element filter = (Element) parameters.get(0);
//         ret = get(filter, xid);
//      }
//      else if (operationName.equals("getConfig"))
//      {
//         String configurationName = (String) parameters.get(0);
//         Element filter = (Element) parameters.get(1);
//         return getConfig(configurationName, filter, xid);
//      }
//      else if (operationName.equals("editConfig"))
//      {
//         String configurationName = (String) parameters.get(0);
//         Element xml = (Element) parameters.get(1);
//         EditOperation defaultOperation = (EditOperation) parameters.get(2);
//         TestOption testOption = (TestOption) parameters.get(3);
//         ErrorOption errorOption = (ErrorOption) parameters.get(4);
//         editConfig(configurationName, xml, defaultOperation, testOption, errorOption, xid);
//      }
//      else if (operationName.equals("copyConfig"))
//      {
//         String configurationName = (String) parameters.get(0);
//         String targetConfigurationName = (String) parameters.get(1);
//         copyConfig(configurationName, targetConfigurationName, xid);
//      }
//      else if (operationName.equals("deleteConfig"))
//      {
//         String configurationName = (String) parameters.get(0);
//         deleteConfig(configurationName, xid);
//      }
//      else if (operationName.equals("lock"))
//      {
//         String configurationName = (String) parameters.get(0);
//         lock(configurationName, xid);
//      }
//      else if (operationName.equals("unlock"))
//      {
//         String configurationName = (String) parameters.get(0);
//         unlock(configurationName, xid);
//      }
//      else if (operationName.equals("validate"))
//      {
//         String configurationName = (String) parameters.get(0);
//         validate(configurationName, xid);
//      }
//      else if (operationName.equals("discardChanges"))
//      {
//         discardChanges(xid);
//      }
//      else if (operationName.equals("killSession"))
//      {
//         String persistId = (String) parameters.get(0);
//         killSession(persistId, xid);
//      }
//      else // if (operationName.equals("closeSession"))
//           // if (operationName.equals("commit"))
//           // if (operationName.equals("cancelCommit"))
//           // OR any other invalid operation
//      {
//         throw new RuntimeException("Unexpected operation in transaction: " + operationName);
//      }
//      return ret;
//   }

   @Override
   public void prepareTransaction(LocalTransactionContextIf resourceContext, int timeoutInSeconds) throws RuntimeException
   {
      // Perform a confirmed commit
      NetconfLocalTransactionContext context = (NetconfLocalTransactionContext) resourceContext;
      String xid = context.getTransactionId();
      commit(null, true, timeoutInSeconds, xid);
   }

   @Override
   public void commitTransaction(LocalTransactionContextIf resourceContext) throws RuntimeException
   {
      // TODO: try/catch here?
      NetconfLocalTransactionContext context = (NetconfLocalTransactionContext) resourceContext;
      String xid = context.getTransactionId();
      try
      {
         commit(null, false, 0, xid);
         m_transport.commitTransaction(xid);
      }
      catch (final RuntimeException rex)
      {
         // We should stop holding on to the resource if things fail
         m_transport.rollbackTransaction(xid);
         xid = null;
         throw rex;
      }
      finally
      {
         if (xid != null)
         {
            unlock("running", xid);
            unlock("candidate", xid);
         }
      }
   }

   @Override
   public void rollbackTransaction(LocalTransactionContextIf resourceContext) throws RuntimeException
   {
      NetconfLocalTransactionContext context = (NetconfLocalTransactionContext) resourceContext;
      String xid = context.getTransactionId();
      try
      {
         unlock("running", xid);
      }
      catch (final Exception ex)
      {
         s_logger.warn("Error unlocking running configuration: " + ex.getMessage());
      }
      try
      {
         discardChanges(xid);
         unlock("candidate", xid);
      }
      catch (final Exception ex)
      {
         s_logger.warn("Error unlocking candidate configuration: " + ex.getMessage());
      }
      finally
      {
         m_transport.rollbackTransaction(xid);
      }
   }

   @Override
   public void releaseResources(LocalTransactionContextIf resourceContext)
   {
      try
      {
         rollbackTransaction(resourceContext);
      }
      catch (final Exception ex)
      {
         s_logger.warn("Exception rolling back transaction: " + ex.getMessage());
      }
   }

   ////////////////// NETCONF notification support /////////////////////////////////

   /**
    * Called to start notifications from the device on the specified stream.
    *
    * @param stream              Name of notification stream.
    * @param startTime           Start time from which notifications are desired. This will typically be in the past in order to receive older
    *                            notifications that we have not captured.
    * @param listener            Listener that is invoked whenever a notification arrives from the device.
    * @throws RuntimeException   if an error occurs.
    */
   public void startNotifications(String stream, Timestamp startTime, final NotificationListenerIf listener) throws RuntimeException
   {
      if (!m_deviceCaps.supportsNotifications())
      {
         throw new RuntimeException("Netconf notifications are not supported by this device");
      }
      m_transport.startNotifications(stream, startTime, listener);
   }
   
   public void startNotifications(String stream, final Element filter, String startTime, String stopTime,
		   final NotificationListenerIf listener) throws RuntimeException {
	   if (!m_deviceCaps.supportsNotifications())
	   {
		   throw new RuntimeException("Netconf notifications are not supported by this device");
	   }
	   m_transport.startNotifications(stream, filter, startTime, stopTime, listener);	   
   }

   /**
    * Called to stop notifications from the device on the specified stream.
    *
    * @param stream              Name of notification stream.
    * @throws RuntimeException   if an error occurs.
    */
   public void stopNotifications(final String stream) throws RuntimeException
   {
      m_transport.stopNotifications(stream);
   }

   /**
    * Called to stop all notifications from the device.
    *
    * @throws RuntimeException   if an error occurs.
    */
   public void stopAllNotifications() throws RuntimeException
   {
      m_transport.stopAllNotifications();
   }

   /**
    * Returns the availability of the device.
    *
    * @return  true if the device can be reached and is available, false if not.
    */
   public boolean isAvailable()
   {
      return m_deviceAvailable;
   }

   //////////////////////////// Helper methods //////////////////////////////////////////

   // Note: for operations that do not return data (like edit), a NULL is returned

   /**
    * Sends out the specified NETCONF request over to the device, wrapping it in the appropriate envelope. Returns the response, unwrapping
    * it from the response envelope.
    *
    * @param request             XML node representing the request.
    * @param xid                 Transaction Id or NULL if not in transaction context.
    * @return                    Root XML node of response, or NULL if no data is returned in the response.
    * @throws RuntimeException   if an error was encountered in the exchange.
    */
   public Element send(final Element request, final String xid) throws RuntimeException
   {
      Element rpcRequest = wrapRequest(request);
      Element rawResponse = sendRaw(rpcRequest, xid);
      Element data = unwrapResponse(rawResponse);
      return data;
   }

   /**
    * Sends the RPC request synchronously to the device.
    *
    * @param rawRequest          XML representation of RPC request.
    * @param xid                 Transaction Id or NULL if not in transaction context.
    * @return                    RPC response from the device.
    * @throws RuntimeException   if an error occurred during the transmission.
    */
   public Element sendRaw(final Element rawRequest, final String xid) throws RuntimeException
   {
      if (s_logger.isDebugEnabled())
      {
         s_logger.debug(XmlUtils.toXmlString(rawRequest, true));
      }
      
      lastRequestTime = System.currentTimeMillis();
      
      Element rpcReply = m_transport.send(rawRequest, xid);
      
      lastResponseTime = System.currentTimeMillis();
      
      if (s_logger.isDebugEnabled())
      {
         s_logger.debug(XmlUtils.toXmlString(rpcReply, true));
      }
      return rpcReply;
   }  

   /**
    * Given the request data to be sent to the device, wraps it in a NETCONF RPC request envelope. Also sets up the request with a new
    * readable message ID, for easy tracking.
    *
    * @param request          XML node representing the RPC request.
    * @return                 Root element of actual NETCONF RPC.
    */
   public Element wrapRequest(Element request)
   {
      // Generate the next message ID
      Element rpcRequest = new Element("rpc", s_xmlns);
      rpcRequest.setAttribute("message-id", getNextMessageId());
      rpcRequest.addContent(request);
      return rpcRequest;
   }

   /**
    * Given a NETCONF RPC response, extract the data contained in it.
    *
    * @param rpcReply            Root XML node representing the RPC response, or NULL if nothing was returned.
    * @return                    RPC data returned in the response.
    * @throws RuntimeException   if an RPC error was returned.
    */
   public Element unwrapResponse(Element rpcReply) throws RuntimeException
   {
      Element rpcError = rpcReply.getChild("rpc-error", s_xmlns);
      if (rpcError != null)
      {
         s_logger.warn("Received RPC Error from device");
         throw NetconfException.fromXml(rpcError);
      }
      return rpcReply.getChild("data", s_xmlns);
   }

   /**
    * Returns the next message ID; just one more than the previous one.
    *
    * @return  new message ID for NETCONF message.
    */
   private synchronized String getNextMessageId()
   {
      String ret = "" + m_messageIdTracker;
      m_messageIdTracker = m_messageIdTracker.add(BigInteger.ONE);
      return ret;
   }

}
