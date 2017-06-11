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
import java.util.HashMap;
import java.util.Properties;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.net.netconf.NotificationListenerIf;
import org.dvlyyon.net.netconf.transport.HelloResponseProcessorIf;
import org.dvlyyon.net.netconf.transport.TransportClientIf;
import org.jdom2.Element;


/**
 * The SshTransportClient class provides the implementation of NETCONF over SSH as defined in
 * <a href="http://tools.ietf.org/html/rfc6242">Using the NETCONF Protocol over Secure Shell (SSH)</a>.
 * <p>
 * This class uses a single SSH connection for synchronous NETCONF RPC calls. In previous versions, multiple connections were used as
 * part of a pool; however, this often lead to over-use of socket resources on the system. Also, using a single connection greatly
 * simplifies the transaction handling from the client API perspective.
 * <p>
 * In addition, it also uses an asynchronous SSH connection for each notification stream from the device. To ensure that the
 * notification rate is not bandwidth-limited, application code that uses this client should make sure a notification is handled quickly
 * (by possibly putting it into a queue) and control returns as soon as possible.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class SshTransportClient implements TransportClientIf
{

   /** Logger for tracing */
   protected final static Log s_logger = LogFactory.getLog(SshTransportClient.class);

   /** Connection information as properties */
   private Properties m_properties;

   /** Client hello-response processor */
   private HelloResponseProcessorIf  m_hrp;

   /** The (sync) SSH connection */
   private SyncSshConnection m_nexus;

   /** Flag that indicates the current SSH session is busy */
   private boolean m_busy;

   /** Flag that indicates the current SSH session is running within a "transaction" */
   private boolean m_transactionStarted;

   /** The async. SSH connection (used for notifications) */
   private HashMap<String, AsyncSshConnection> m_streamConnections = new HashMap<String, AsyncSshConnection>();


   /**
    * Creates an SshTransportClient. We need a public, no-argument constructor.
    */
   public SshTransportClient()
   {
   }

   @Override
   /**
    * Only call this once.
    * The properties that define all the connection parameters:<ol>
    * <li>host - Name or IP address of device</li>
    * <li>port - Port over which to do SSH</li>
    * <li>username - SSH login user name</li>
    * <li>password - SSH login password (or certificate file descryption password)</li>
    * <li>certificate - Fully qualified path to (OpenSSH-style PEM) certificate file</li>
    * <li>socketTimeout - socket timeout in seconds</li>
    * <li>responseTimeout - Time to wait for RPC response in seconds</li>
    * <li>netconfTraceFile - Reference to a LoggerIf used for tracing.</li>
    * <li>subsystem - SSH subsystem - defaults to "netconf" if not specified.</li>
    * </ol>
    */
   public void setup(Properties properties, final HelloResponseProcessorIf hrp)
   {
      m_hrp = hrp;
      m_properties = properties;
      
      if (m_nexus == null)
         m_nexus = new SyncSshConnection(m_properties, m_hrp, false);

   }
   
   private boolean connected = false;
   
   public boolean isConnected()
   {
       return connected;
   }
   
   /**
    * Create the Synchronous SSH connection (or just connect it if alread created), and establish sessions
    */
   public void connect()
   {
       if (m_nexus == null)
           m_nexus = new SyncSshConnection(m_properties, m_hrp, true);
       else
           m_nexus.connect();

       //m_nexus.establishSession();   // this is done in the sshConnection.connect
       connected = true;
   }

   ////////////////////////////   Notification API
   @Override
   public synchronized void startNotifications(final String stream, final Timestamp startTime, final NotificationListenerIf listener) throws RuntimeException
   {
      AsyncSshConnection connection = new AsyncSshConnection(m_properties, listener, true);  // this should connect immediately upon instantiation
      
      m_streamConnections.put(stream, connection);
      //connection.establishSession();
      
      connection.startNotifications(stream, startTime);
   }

   public synchronized void startNotifications(final String stream, final Element filter, final String startTime, final String stopTime,
		   final NotificationListenerIf listener) throws RuntimeException
   {
      AsyncSshConnection connection = new AsyncSshConnection(m_properties, listener, true);  // this should connect immediately upon instantiation
      
      m_streamConnections.put(stream, connection);
      //connection.establishSession();
      
      connection.startNotifications(stream, filter, startTime, stopTime);
   }
   
   @Override
   public /*synchronized*/ void stopNotifications(final String stream)
   {
      AsyncSshConnection connection = m_streamConnections.remove(stream);
      if (connection != null)
      {
         connection.disconnect();
      }
   }

   @Override
   public /*synchronized*/ void stopAllNotifications()
   {
      ArrayList<String> streams = new ArrayList<String>();
      for (String stream : m_streamConnections.keySet())
      {
         streams.add(stream);
      }
      for (String s : streams)
      {
         AsyncSshConnection connection = m_streamConnections.remove(s);
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }

   ///////////////////////////  Synchronous request-response API

   @Override
   public Element send(Element data, String xid) throws RuntimeException
   {
      boolean destroy = false;
      // Make sure the session is available and grab it
      acquireSession();
      // Create a session (if necessary) and send the stuff
      try {
    	  obtainConnection(xid);
    	  s_logger.debug("About to send request..");
      } catch (Exception ex) {
    	  m_busy = false;
    	  destroyConnection();
    	  throw ex;
      }
      try
      {
         String timeoutStr = m_properties.getProperty("responseTimeout", "-1");
         Sender s = new Sender(m_nexus, data, Integer.parseInt(timeoutStr));
         Element response = s.getResponse();
         return response;
      }
      catch (final Exception ex)
      {
         // TODO: Optimization - Figure out if this was an IO (i.e. a socket exception), only then set destroy to true
         destroy = true;
         s_logger.error("Exception during NETCONF rpc send", ex);
         if (s_logger.isDebugEnabled())
         {
            s_logger.error(ex, ex);
         }
         throw new RuntimeException("An error occured sending in the SSH transport layer: " + ex.toString());
      }
      finally
      {
         if (destroy)
         {
            destroyConnection();
         }
         m_busy = false;
      }
   }

   private boolean acquireSession() throws RuntimeException
   {
      boolean acquired = false;
      // TODO; Make this a configurable parameter
      final int CLIENT_WAIT_QUANTUM = 100;
      int quantumCount = 100;                 // 10 seconds total wait
      while (m_busy && quantumCount > 0)
      {
         try
         {
            Thread.sleep(CLIENT_WAIT_QUANTUM);
         }
         catch (InterruptedException iex)
         {
            throw new RuntimeException("Waiting for session available - interrupted");            
         }
         quantumCount--;
      }
      synchronized (this)
      {
         if (!m_busy)
         {
            m_busy = true;
            acquired = true;
         }
         // TODO: How is this not always acquired??  all threads will come through the sync block eventually
      }
      if (acquired)
      {
         s_logger.debug("Session acquired");
      }
      else
      {
         throw new RuntimeException("Timed out waiting for session to be available");
      }
      return acquired;
   }

   /**
    * Obtains an SSH connection, with which to perform configuration. The connection can be obtained in one of two ways:<ul>
    * <li>From a connection pool, if there is no transaction currently running in this thread</li>
    * <li>From the transaction context, if there is a transaction in progress</li>
    * </ul>
    *
    * @param xid                 Transaction ID (if required), or NULL if not in transaction.
    * @return                    Synchronous SSH connection to be used to configure the device.
    * @throws RuntimeException   if an error occurred obtaining a connection.
    */
   private synchronized SyncSshConnection obtainConnection(String xid) throws RuntimeException
   {
      /*
      if (m_nexus == null)
      {
         m_nexus = new SyncSshConnection(m_properties, m_hrp, false);
         m_nexus.establishSession();
      }
      */
       
      if (!isConnected())
          connect();
      
      if (xid != null)
      {
         // Compare current XID (which is just the connection) to the XID passed in
         if (!xid.equals(m_nexus.toString()))
         {
            throw new RuntimeException("Transaction ID mismatch");
         }
      }
      return m_nexus;
   }

   /**
    * Destroys the connection.
    */
   private /*synchronized*/ void destroyConnection()
   {
      if (m_nexus!=null)
      {
          m_nexus.disconnect();
          m_nexus = null;
          connected = false;
      }
   }

   //////////////////////////////////////////  Transaction management ////////////////////////////////////
   @Override
   public String startTransaction() throws RuntimeException
   {
      boolean success = acquireTransaction();
      if (!success)
      {
         throw new RuntimeException("Timed out waiting to acquire transaction");
      }
      //m_transactionStarted = true;
      obtainConnection(null);
      return m_nexus.toString();
   }

   @Override
   public void commitTransaction(String transactionId) throws RuntimeException
   {
      // Conpare current XID (which is just the connection) to the XID passed in
      if (!transactionId.equals(m_nexus.toString()))
      {
         throw new RuntimeException("Transaction ID mismatch");
      }
      // No longer in transaction; however, we can STILL use the same SSH session
      m_transactionStarted = false;
   }

   @Override
   public void rollbackTransaction(String transactionId) throws RuntimeException
   {
      // Release the SSH connection back to the pool, and destroy it (to make sure the session closes and nothing gets committed)
      destroyConnection();
      // No longer in transaction
      m_transactionStarted = false;
   }

   private boolean acquireTransaction()
   {
      boolean acquired = false;
      // TODO; Make this a configurable parameter
      final int XACTION_WAIT_QUANTUM = 100;
      int quantumCount = 300;                 // 30 seconds total wait
      while (m_transactionStarted && quantumCount > 0)
      {
         try
         {
            Thread.sleep(XACTION_WAIT_QUANTUM);
         }
         catch (InterruptedException iex)
         {
            throw new RuntimeException("Waiting for transaction available interrupted");            
         }
         quantumCount--;
      }
      synchronized (this)
      {
         if (!m_transactionStarted)
         {
            m_transactionStarted = true;
         }
         acquired = true;
      }
      if (acquired)
      {
         s_logger.debug("Transaction acquired");
      }
      return acquired;
   }

   @Override
   public /*synchronized*/ void shutdown()
   {
      try
      {
         this.stopAllNotifications();
         this.destroyConnection();
         m_transactionStarted = false;
         m_busy = false;
      }
      catch (final Exception ex)
      {
         s_logger.error("Exception shutting down client: " + ex.getMessage());
         //if (s_logger.isDebugEnabled())
         {
            s_logger.error(ex, ex);
         }
      }
   }

   /**
    * The Sender class is a helper that helps to convert the full-duplex behavior of SSH to one that works more like a request-response
    * (or half-duplex) protocol, which is what NETCONF RPC behaves like.
    *
    * @author  Subramaniam Aiylam
    * @since   1.6
    */
   static class Sender implements ResponseCallbackIf
   {
      /** Time to wait for a NETCONF response from the device before giving up  - defaults to 5 seconds */
      private static final int NETCONF_RESPONSE_TIMEOUT = 5 * 1000;

      /** The NETCONF RPC response from the device */
      private Element m_response = null;


      /**
       * Creates a Sender with the specified parameters.
       *
       * @param nexus            SSH connection being used for device communications.
       * @param request          NETCONF RPC request.
       * @param timeoutInSeconds seconds for which to wait for response before declaring failure.
       * @throws Exception       if an error occurred.
       */
      Sender(final SyncSshConnection nexus, final Element request, final int timeoutInSeconds) throws Exception
      {
         nexus.send(request, this);
         try
         {
            int timeout = NETCONF_RESPONSE_TIMEOUT;
            if (timeoutInSeconds > 0)
            {
               timeout = timeoutInSeconds * 1000;
            }
            synchronized (this)
            {
               if (m_response == null)
               {
                  this.wait(timeout);
               }
            }
            if (m_response == null)
            {
               s_logger.warn("No response from device - timed out.");
               throw new Exception("Timed out waiting for response.");
            }
         }
         catch (final InterruptedException ie)
         {
            s_logger.warn("SSH Sender interrupted");
         }
      }

      /** */
      Element getResponse()
      {
         return m_response;
      }

      @Override
      public void processResponse(Element response)
      {
         m_response = response;
         // Release the waiting thread
         synchronized (this)
         {
            this.notify();
         }
      }
   }   

}
