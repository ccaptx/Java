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

import org.jdom2.Element;


/**
 * The NotificationListenerIf interface defines the API that is invoked when a NETCONF device sends an asynchronous notification to
 * the client. This is usually employed to process events and alarms from a device. 
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public interface NotificationListenerIf
{

   /**
    * Called to set the capabilities of the device as obtained from a hello exchange.
    *
    * @param caps Capabilities of the device that is generating the notifications.
    */
   void setDeviceCapabilities(Capabilities caps);

   /**
    * Called when the device sends an asynchronous notification.
    *
    * @param notificationTime Time at which the notification occurred.
    * @param notification     Element representing the data that comes along with the notification, or NULL if no data
    *                         was sent. The actual payload lives under an XML element called <data>.
    */
   void notify(Timestamp notificationTime, Element notification);

   /**
    * Called when the remote device terminates the underlying connection.
    */
   void connectionTerminated();

}
