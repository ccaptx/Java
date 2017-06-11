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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.net.netconf.transport.HelloResponseProcessorIf;
import org.jdom2.Element;


/**
 * The SyncSshConnection class handles synchronous NETCONF RPC calls. In other words, NETCONF communications that involve a
 * request-response paradigm use this class. Calls make using this class block until a response is received.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class SyncSshConnection extends SshConnection
{

   /** Logger for tracing */
   protected final static Log s_logger = LogFactory.getLog(SyncSshConnection.class);

   /** Flag that is used to track whether a call is currently in progress */
   private boolean m_callInProgress;

   /** Handle to callback that is to be invoked with response data */
   private ResponseCallbackIf m_callback;


   /**
    * Creates a SyncSshConnection with the specified parameters.
    *
    * @param connectionProperties   Properties containing connection information.
    * @param hrp                    The application's HelloResponseProcessor.
    * @throws RuntimeException      if an error occurred during creation.
    */
   public SyncSshConnection(Properties connectionProperties, final HelloResponseProcessorIf hrp) throws RuntimeException
   {
       this(connectionProperties, hrp, true);
   }
   
   public SyncSshConnection(Properties connectionProperties, final HelloResponseProcessorIf hrp, boolean connectNow) throws RuntimeException
   {
      super(connectionProperties, hrp, connectNow);
   }

   @Override
   protected void handleHelloResponse(Element response)
   {
      // Nothing to do for a sync SSH connection
      m_callInProgress = false;
   }

   @Override
   protected void handleResponse(Element response)
   {
      if (m_callback != null)
      {
         m_callback.processResponse(response);
      }
      m_callInProgress = false;
   }

   /**
    * Sends an XML request, logging the transmission data.
    *
    * @param data                XML data to send.
    * @param callback            Callback invoked when the response is received.
    * @throws RuntimeException   if an error occurred.
    */
   public void send(Element data, ResponseCallbackIf callback) throws RuntimeException
   {
      send(data, callback, true);
   }

   /**
    * Sends an XML request without logging transmission data.
    *
    * @param data                XML data to send.
    * @param callback            Callback invoked when the response is received.
    * @throws RuntimeException   if an error occurred.
    */
   public void quietSend(Element data, ResponseCallbackIf callback) throws RuntimeException
   {
      send(data, callback, false);
   }

   /**
    * Called to perform a NETCONF RPC-type operation synchronously. This call blocks until a response is received.
    *
    * @param data                XML representing the NETCONF request.
    * @param callback            Handle to callback which is to be invoked with the response.
    * @param traceWireData       true to log the XML over-the-wire, false if no logging.
    * @throws RuntimeException   if an error occurs while sending the request. An exception is also thrown if a
    *                            synchronous transmission is currently going on.
    */
   public synchronized void send(Element data, ResponseCallbackIf callback, boolean traceWireData) throws RuntimeException
   {
      // We have an interlock here to ensure request/response framework is maintained
      if (m_callInProgress)
      {
         throw new RuntimeException("A synchronous call is already in progress");
      }
      m_callInProgress = true;

      m_callback = callback;
      try
      {
         syncSend(data, true, traceWireData);
      }
      finally
      {
         // If there was any exception sending or receiving data (before the callback has been invoked), mark our flag
         m_callInProgress = false;
      }
   }

}
