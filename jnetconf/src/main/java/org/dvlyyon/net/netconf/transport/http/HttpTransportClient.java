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
package org.dvlyyon.net.netconf.transport.http;

import java.sql.Timestamp;
import java.util.Properties;

import org.dvlyyon.net.netconf.NotificationListenerIf;
import org.dvlyyon.net.netconf.exception.NetconfException;
import org.dvlyyon.net.netconf.transport.HelloResponseProcessorIf;
import org.dvlyyon.net.netconf.transport.TransportClientIf;
import org.jdom2.Element;


/**
 * This class needs to be implemented in case we ever support NETCONF over HTTP. Currently, all its methods are stubs.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class HttpTransportClient implements TransportClientIf
{

   public void setup(final Properties properties) throws NetconfException
   {
      // TODO: Add code here
   }

   public void setHelloResponseProcessor(HelloResponseProcessorIf hrp)
   {
      // TODO: Deal with this later      
   }

   public Element send(Element data, String xid) throws RuntimeException
   {
      // TODO: Add code here
      return null;
   }

   public void shutdown()
   {
      // TODO: Add code here
   }

   @Override
   public void startNotifications(String stream, Timestamp startTime, NotificationListenerIf listener)
   {
      // TODO Auto-generated method stub
   }

   @Override
   public void stopNotifications(String stream)
   {
      // TODO Auto-generated method stub
   }

   @Override
   public void stopAllNotifications()
   {
      // TODO Auto-generated method stub
   }

   @Override
   public void commitTransaction(String transactionId) throws RuntimeException
   {
      throw new RuntimeException("Unsupported operation");
   }

   @Override
   public void rollbackTransaction(String transactionId) throws RuntimeException
   {
      throw new RuntimeException("Unsupported operation");
   }

   @Override
   public String startTransaction() throws RuntimeException
   {
      throw new RuntimeException("Unsupported operation");
   }

   @Override
   public void setup(Properties connectionProperties, HelloResponseProcessorIf hrp)
   {
      // TODO Auto-generated method stub
      
   }

	@Override
	public void startNotifications(String stream, Element filter, String startTime,
			String stopTime, NotificationListenerIf listener)
			throws RuntimeException {
		// TODO Auto-generated method stub
		
	}

}
