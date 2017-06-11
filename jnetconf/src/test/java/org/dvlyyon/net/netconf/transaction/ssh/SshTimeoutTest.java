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
package org.dvlyyon.net.netconf.transaction.ssh;

import java.util.Properties;

import junit.framework.TestCase;

import org.dvlyyon.common.util.CommandLine;
import org.dvlyyon.net.netconf.transport.ssh.SyncSshConnection;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The SshTimeoutTest acts as a test harness for testing NETCONF SSH transport timeout behavior. Socket timeouts are highly
 * inconsistent across platforms; this class can be used to establish platform behavior. The code runs in a loop allowing the
 * developer to pull out cables, turn off device power, etc. and observe how SSH times out.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class SshTimeoutTest extends TestCase
{

   /** The base NETCONF XML namespace - see RFC 6241 */
   private static Namespace s_xmlns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");

   /** The NETCONF namespace used for notifications - see RFC 5277 */
   private static Namespace s_notificationNs = Namespace.getNamespace("urn:ietf:params:xml:ns:netmod:notification");


   /**
    * This is just a place-holder to make the build happy, since this does not really have any JUnit tests.
    */
   public void testDummy()
   {
   }

   /**
    * The entry point to the NETCONF SSH timeout test.
    *
    * @param args Parameters to the test. To see what these are, run the test like so:
    *                   java com.centeredlogic.net.netconf.transport.ssh.SshTimeoutTest ?
    */
   public static void main(String[] args)
   {
      try
      {
         CommandLine cmdline = CommandLine.getInstance("SshTimeoutTest", "1.0");
         cmdline.initArg("host", true, 1, "", "Hostname or IP Address of device");
         cmdline.initArg("port", false, 1, "830", "SSH port on device");
         cmdline.initArg("username", true, 1, "", "SSH user name (for authentication)");
         cmdline.initArg("password", false, 1, "", "SSH password name (if password authentication is used)");
         cmdline.initArg("certificate", false, 1, "", "Fully qualified path name to certificate file (if certificate authentication is used)");
         cmdline.initArg("passphrase", false, 1, "", "Passphrase for certificate authentication");
         cmdline.initArg("timeout", false, 1, "", "Timeout (in seconds) to declare connection failure");
         cmdline.setArgs(args);
         Properties properties = new Properties();
         properties.put("host", cmdline.getArgument("host"));
         properties.put("socketTimeout", cmdline.getArgument("timeout"));
         if (!"".equals(cmdline.getArgument("port")))
         {
            properties.put("port", cmdline.getArgument("port"));
         }
         properties.put("username", cmdline.getArgument("username"));
         if (!cmdline.getArgument("certificate").equals(""))
         {
            properties.put("certificate", cmdline.getArgument("certificate"));
         }
         if (!cmdline.getArgument("password").equals(""))
         {
            properties.put("password", cmdline.getArgument("password"));
         }
         if (!cmdline.getArgument("passphrase").equals(""))
         {
            properties.put("passphrase", cmdline.getArgument("passphrase"));
         }
         System.out.println("Setting up SSH connection with timeout");
         SyncSshConnection nexus = new SyncSshConnection(properties, null);
         System.out.println("SSH connection setup complete");

         while (true)
         {
            System.out.print("Sending ping....");
            boolean success = ping(nexus);
            if (success)
            {
               System.out.println("  Success.");
            }
            else
            {
               System.out.println("  ERROR!");
               break;
            }
            Thread.sleep(5000);
         }
         System.exit(0);
      }
      catch (final Exception ex)
      {
         ex.printStackTrace();
         System.exit(1);
      }
   }

   /**
    * Sends a <get-stream> message to the host (without tracing the request-response interaction).
    *
    * @param nexus      the SSH connection.
    * @throws Exception if an error occurred sending a message.
    */
   private static boolean ping(SyncSshConnection nexus) throws Exception
   {
      boolean ret = false;
      try
      {
         Element req = createGetStreamRequest(true);
         nexus.quietSend(req, null);
         ret = true;
      }
      catch (final Exception ex)
      {
         ex.printStackTrace();
      }
      return ret;
   }

   /**
    * Creates a the <get-stream> request.
    *
    * @param wrap    true if the request is to be wrapped with a NETCONF RPC element, false if not.
    * @return        The get-steam request that can be sent over-the-wire.
    */
   private static Element createGetStreamRequest(boolean wrap)
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

}
