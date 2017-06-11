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
package org.dvlyyon.net.netconf.transport;

import java.sql.Timestamp;
import java.util.Properties;

import org.dvlyyon.net.netconf.NotificationListenerIf;
import org.jdom2.Element;


/**
 * The TransportClientIf interface defines the API provided by any NETCONF transport layer. It covers both synchronous (RPC) style calls
 * to the target NETCONF device as well as asynchronous notifications that are sent by the target device.
 * <p>
 * For more information about the NETCONF configuration protocol, see <a href="http://tools.ietf.org/html/rfc6241">RFC 6241</a>.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public interface TransportClientIf
{

   /**
    * Called to set up the transport connection. Also sets the listener that handles the hello message from the device.
    * The listener can then make decisions based upon the device's capabilities.
    *
    * @param connectionProperties   Connection (and pool) properties.
    * @param hrp                    Processor that is called when a hello message is sent by the device.
    */
   void setup(final Properties connectionProperties, HelloResponseProcessorIf hrp);

   /**
    * Requests the device to start sending asynchronous notifications.
    *
    * @param stream              Stream from which notifications are desired.
    * @param startTime           Time from which notifications are required. Specify NULL to indicate that you need notifications
    *                            from the beginning of time.
    * @param listener            Listener that is invoked whenever a notification arrives from the device.
    * @throws RuntimeException   if there was an error during notification registration.
    */
   void startNotifications(final String stream, final Timestamp startTime, final NotificationListenerIf listener) throws RuntimeException;

   /**
    * Requests the device to start sending asynchronous notifications.
    *
    * @param stream              Stream from which notifications are desired.
    * @param filter				 What will be in response
    * @param startTime           Time from which notifications are required. Specify NULL to indicate that you need notifications
    *                            from the beginning of time.
    * @param stopTime			 Time before which notification are required. Specify NULL to continue until subscription is terminated
    * @param listener            Listener that is invoked whenever a notification arrives from the device.
    * @throws RuntimeException   if there was an error during notification registration.
    */
   void startNotifications(final String stream, final Element filter, final String startTime, final String stopTime, final NotificationListenerIf listener) throws RuntimeException;
   
   /**
    * Terminates receipt of notifications from the device on the specified stream.
    *
    * @param stream              Stream from which notifications are to be stopped.
    */
   void stopNotifications(final String stream);

   /**
    * Terminates receipt of all notifications from the device.
    */
   void stopAllNotifications();

   /**
    * Called to start a NETCONF transaction - this is an atomic NETCONF operation.
    *
    * @return                    String that is used subsequently as a transaction ID.
    * @throws RuntimeException   if an error occurred starting the transaction.
    */
   String startTransaction() throws RuntimeException;

   /**
    * Send a NETCONF message synchronously - this follows the request/response methodology.
    *
    * @param data                Root element representing the data to be sent.
    * @param xid                 Transaction ID, if this operation is associated with a transaction, NULL if not.
    * @return                    Element representing the response data.
    * @throws RuntimeException   if an error occurred during data exchange.
    */
   Element send(Element data, String xid) throws RuntimeException;

   /**
    * Called to commit a currently running NETCONF transaction.
    *
    * @param xid                 String that specifies the transaction ID.
    * @throws RuntimeException   if an error occurred committing the transaction.
    */
   void commitTransaction(String xid) throws RuntimeException;

   /**
    * Called to roll-back a currently running NETCONF transaction. All resources that have been previously reserved
    * for the transaction will be released.
    *
    * @param xid                 String that specifies the transaction ID.
    * @throws RuntimeException   if an error occurred during the rollback.
    */
   void rollbackTransaction(String xid) throws RuntimeException;

   /**
    * Called to shutdown the transport.
    */
   void shutdown();

}
