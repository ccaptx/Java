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

import java.util.ArrayList;
import java.util.Properties;
import java.sql.Timestamp;

import junit.framework.TestCase;

import org.dvlyyon.common.util.CLRunnable;
import org.dvlyyon.common.util.CLThread;
import org.dvlyyon.common.util.CommandLine;
import org.dvlyyon.common.util.XmlUtils;
import org.dvlyyon.net.netconf.Capabilities;
import org.dvlyyon.net.netconf.NotificationListenerIf;
import org.dvlyyon.net.netconf.transport.ssh.SshTransportClient;
import org.jdom2.Element;


/**
 * The SshTransportClientTest acts as a test harness for testing NETCONF SSH transport. It can be used as a sample for
 * how to use the SshTransportClient for configuration as well as listening to notifications.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class SshTransportClientTest extends TestCase
{

   /**
    * This is just a place-holder to make the build happy, since this does not really have any JUnit tests.
    */
   public void testDummy()
   {
   }

   /**
    * The entry point to the NETCONF SSH client test.
    *
    * @param args Parameters to the test. To see what these are, run the test like so:
    *                   java com.centeredlogic.net.netconf.transport.ssh ?
    */
   public static void main(String[] args)
   {
      try
      {
         CommandLine cmdline = CommandLine.getInstance("SshTransportClientTest", "1.0");
         cmdline.initArg("host", true, 1, "", "Hostname or IP Address of device");
         cmdline.initArg("port", false, 1, "", "SSH port on device");
         cmdline.initArg("username", true, 1, "", "SSH user name (for authentication)");
         cmdline.initArg("password", false, 1, "", "SSH password name (if password authentication is used)");
         cmdline.initArg("certificate", false, 1, "", "Fully qualified path name to certificate file (if certificate authentication is used)");
         cmdline.initArg("passphrase", false, 1, "", "Passphrase for certificate authentication");
         cmdline.initArg("subsystem", false, 1, "", "SSH subsystem");
         cmdline.initArg("op", true, 1, "events", "Operation (events | config | stress)");
         cmdline.initArg("in", true, 1, "", "XML File containing netconf RPC (configuration) request");
         cmdline.initArg("out", false, 1, "", "XML File containing netconf RPC response");
         cmdline.initArg("stream", false, 1, "NETCONF", "Stream to subscribe to");
         cmdline.initArg("pollTime", false, 1, "5", "Time in minutes to wait before exit");
         cmdline.initArg("threadCount", false, 1, "4", "Number of concurrent configuration threads (for stress test)");
         cmdline.setArgs(args);
         System.out.println("Setting up transport client");
         SshTransportClient client = new SshTransportClient();
         Properties properties = new Properties();
         properties.put("host", cmdline.getArgument("host"));
         properties.put("socketTimeout", "10000");
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
         String subsystem = cmdline.getArgument("subsystem");
         if (subsystem != null && !subsystem.equals(""))
         {
            properties.put("subsystem", subsystem);
         }
         client.setup(properties, null);
         System.out.println("SSH transport client setup complete");

         boolean doEvents = false;
         int configThreads = 0;
         // Figure out what to do
         String op = cmdline.getArgument("op");
         if (op.equalsIgnoreCase("events"))
         {
            doEvents = true;
         }
         else if (op.equalsIgnoreCase("config"))
         {
            configThreads = 1;
         }
         else if (op.equalsIgnoreCase("stress"))
         {
            doEvents = true;
            configThreads = Integer.parseInt(cmdline.getArgument("threadCount"));
         }
         else
         {
            cmdline.showUsage();
            System.exit(1);
         }

         if (configThreads > 0)
         {
            boolean repeated = op.equals("stress");
            ArrayList<CLThread> threads = new ArrayList<CLThread>();
            for (int i=0; i<configThreads; i++)
            {
               ConfigRunner runner = new ConfigRunner(cmdline, client, repeated);
               CLThread thread = new CLThread("ConfigThread-" + i, runner);
               threads.add(thread);
            }
            for (CLThread t : threads)
            {
               t.startup();
            }
         }
         long waitTime = 1;
         if (doEvents)
         {
            String stream = cmdline.getArgument("stream");
            eventTest(client, stream);
            System.out.println("Monitoring netconf notifications ....");
            waitTime = Long.parseLong(cmdline.getArgument("pollTime"));
         }
         System.out.println("waiting for " + waitTime + " minute(s) to exit ....");
         long waitFor = waitTime * 60 * 1000;
         Thread.sleep(waitFor);
         System.out.println("Test complete - exiting.");

         client.shutdown();
         System.exit(0);
      }
      catch (final Exception ex)
      {
         ex.printStackTrace();
         System.exit(1);
      }
   }

   /**
    * Tests event notifications from the device.
    *
    * @param client     the SSH transport client.
    * @param stream     stream from which event notifications are desired.
    * @throws Exception if an error occurs registering for notifications.
    */
   private static void eventTest(SshTransportClient client, String stream) throws Exception
   {
      NotificationListenerIf listener = new EventListener();
      client.startNotifications(stream, null, listener);
   }

   /**
    * Returns the NETCONF RPC request from the specified file.
    *
    * @param fileName      name of file containing NETCONF XML request.
    * @return              XMLized request based upon file data.
    * @throws Exception    if an error occurred reading the file.
    */
   private static Element createXmlRequestFromFile(final String fileName) throws Exception
   {
      Element xml = XmlUtils.fromXmlFile(fileName);
      return xml;
   }

   /**
    * The ConfigRunner class makes a NETCONF RPC call in the context of a separate thread.
    */
   private static class ConfigRunner implements CLRunnable
   {

      /** The command line to the main program */
      CommandLine m_cmdline;

      /** The SSH transport client being tested */
      SshTransportClient m_client;

      /** Flag that indicates that the test should be run repeatedly */
      boolean m_repeat;


      /**
       * Creates a ConfigRunner,
       *
       * @param cmdline    Command line (that contains all the parameters passed in to the main method).
       * @param client     the SSH transport client.
       * @param repeated   false if the config operation is single-shot, true if it is repeated constantly.
       */
      ConfigRunner(CommandLine cmdline, SshTransportClient client, boolean repeated)
      {
         m_cmdline = cmdline;
         m_client = client;
         m_repeat = repeated;
      }

      @Override
      public void run() throws InterruptedException
      {
         try
         {
            do
            {
               Element request = createXmlRequestFromFile(m_cmdline.getArgument("in"));
               System.out.println("Sending XML request: \n" + XmlUtils.toXmlString(request));
               Element response = m_client.send(request, null);
               System.out.println("Received XML response: \n" + XmlUtils.toXmlString(response));
               if (!"".equals(m_cmdline.getArgument("out")))
               {
                  XmlUtils.toXmlFile(response, m_cmdline.getArgument("out"));
               }
            } while (m_repeat);
         }
         catch (final Exception ex)
         {
             System.out.println("Error during configuration: " + ex.getMessage());

            if (ex instanceof InterruptedException)
            {
               throw (InterruptedException) ex;
            }
         }
      }

   }


   /**
    * The EventListener class is used to process event notifications from a NETCONF device on behalf of the test harness.
    */
   private static class EventListener implements NotificationListenerIf
   {

      @Override
      public void notify(Timestamp time, Element notification)
      {
         System.out.println("Got notification: \n" + XmlUtils.toXmlString(notification));
      }

      @Override
      public void setDeviceCapabilities(Capabilities caps)
      {
         // ignore
      }

      @Override
      public void connectionTerminated()
      {
         System.out.println("Notification connection terminated by remote!!");
      }

   }

}
