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

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.dvlyyon.common.io.FileUtils;
import org.dvlyyon.common.util.CommandLine;
import org.dvlyyon.common.util.StringUtils;
import org.dvlyyon.common.util.XmlUtils;
import org.dvlyyon.net.netconf.Client;
import org.dvlyyon.net.netconf.NetconfLocalTransactionContext;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The ClientTest acts as a test harness for testing atomic operation using the NETCONF client. It performs a series of
 * operations and tests that they all work or all fail as a set.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class ClientTest extends TestCase
{

   /** Command-line with all its parameters */
   static CommandLine s_commandLine;


   /**
    * This is just a place-holder to make the build happy, since this does not really have any JUnit tests.
    */
   public void testDummy()
   {
   }

   /**
    * The entry point to the NETCONF client atomic operation test.
    *
    * @param args Parameters to the test. To see what these are, run the test like so:
    *                   java com.centeredlogic.net.netconf.ClientTest ?
    */
   public static void main(String[] args)
   {
      try
      {
         CommandLine cmdline = CommandLine.getInstance("ClientTest", "1.0");
         cmdline.initArg("protocol", false, 1, "ssh", "Transport protocol (defaults to SSH)");
         cmdline.initArg("host", true, 1, "", "Hostname or IP Address of device");
         cmdline.initArg("port", false, 1, "", "SSH port on device");
         cmdline.initArg("username", false, 1, "admin", "SSH user name (for authentication)");
         cmdline.initArg("password", true, 1, "", "SSH password name (for authentication)");
         cmdline.initArg("namespace", true, 1, "", "XML Nanespace to be used for the test");
         cmdline.initArg("tag", true, 1, "", "Tag identigying the attribute being modified for the test");
         cmdline.initArg("editConfigXml", true, 1, "editConfig.xml", "File containing XML for configuration edit");
         cmdline.initArg("filterXml", true, 1, "filter.xml", "File containing filter XML for configuration get");
         cmdline.setArgs(args);
         System.out.println("Setting up NETCONF client");
         Properties properties = new Properties();
         properties.put("protocol", cmdline.getArgument("protocol"));
         properties.put("host", cmdline.getArgument("host"));
         if (!"".equals(cmdline.getArgument("port")))
         {
            properties.put("port", cmdline.getArgument("port"));
         }
         properties.put("username", cmdline.getArgument("username"));
         properties.put("password", cmdline.getArgument("password"));
         s_commandLine = cmdline;

         Client client = new Client();
         client.setup(properties);
         System.out.println("NETCONF client setup complete");

         String label2 = getCurrentValue(client);
         System.out.println("Label at begining: " + label2);

         // Set it to a known value before we start
         client.editConfig("running", getEditConfigXml("beforeTest"), null, null, null, null);

         // Test atomic operation that is successful
         AtomicThread atomic = new AtomicThread(client, false);
         atomic.start();
         ExtraneousThread extraneous = new ExtraneousThread(client);
         new Thread(extraneous).start();
         atomic.join(20000);
         extraneous.join(20000);
         if (!extraneous.getStatus().equals(ExtraneousThread.OperationStatus.Failure))
         {
            System.out.println("Unexpected success performing extraneous operation");
            throw new Exception("Unexpected success");
         }
         String value = getCurrentValue(client);
         if (!value.equals("atomic2"))
         {
            System.out.println("Unexpected value - expected: 'atomic2'; actual: " + value);
            throw new Exception("Unexpected value obtained");
         }
         System.out.println("Successful atomic operation complete");

         // Test atomic operation that fails and lets the other one in
         atomic = new AtomicThread(client, true);
         atomic.start();
         atomic.join(5000);
         extraneous = new ExtraneousThread(client);
         new Thread(extraneous).start();
         extraneous.join(20000);
         if (extraneous.getStatus().equals(ExtraneousThread.OperationStatus.Success))
         {
            System.out.println("Unexpected failure performing extraneous operation");
            throw new Exception("Unexpected failure");
         }
         Thread.sleep(5000);
         value = getCurrentValue(client);
         if (!value.equals("extraneous"))
         {
            System.out.println("Unexpected value - expected: 'extraneous'; actual: " + value);
            throw new Exception("Unexpected value obtained");
         }
         System.out.println("Failed atomic operation test complete");
      }
      catch (final Exception ex)
      {
         ex.printStackTrace();
         System.exit(1);
      }
   }

   /**
    * Reads the filter XML file and sends out the request to the target. Processes the response and extracts the
    * value of the specified tag, when it is encountered for the first time.
    *
    * @param client        Client used to communicate with the target.
    * @return              Value of the attribute identified by the specified tag.
    * @throws Exception    if an error occurred.
    */
   private static String getCurrentValue(Client client) throws Exception
   {
      Element getFilter = XmlUtils.fromXmlFile(s_commandLine.getArgument("filterXml"));
      Element response = client.get(getFilter, null);
//      Element response = client.getConfig("running", getFilter, null);
      String s = response.toString();
      Namespace ns = Namespace.getNamespace(s_commandLine.getArgument("namespace"));
      String tag = s_commandLine.getArgument("tag");
      Element attrNode = getNode(response, ns, tag);;
      if (attrNode == null)
      {
         throw new Exception("No attribute found for tag: " + tag);
      }
      return attrNode.getText();
   }

   /**
    * Gets the value of the first node encountered while traversing (depth-first) the specified root node.
    *
    * @param root    Root node to start traversal. 
    * @param ns      Namespace of XML hierarchy.
    * @param tag     Name (or tag) or node we are searching for.
    * @return        First node found, or NULL if no node found.
    */
   @SuppressWarnings("unchecked")
   public static Element getNode(Element root, Namespace ns, String tag)
   {
      List<Element> kids = root.getChildren();
      for (Element kid : kids)
      {
         if (kid.getNamespaceURI().equals(ns.getURI()))
         {
            if (kid.getName().equals(tag))
            {
               return kid;
            }
            Element ret = getNode(kid, ns, tag);
            if (ret != null)
            {
               return ret;
            }
         }
      }
      return null;
   }

   /**
    * Creates the XML required to send to the target to modify a configuration attribute, by reading the base XML from a file
    * and substituting the "@Tag" place-holder in it with the specified value.
    *
    * @param value         Value to replace the @Tag place-holder.
    * @return              XML that can be sent over-the-wire as part of the <edit-config> command.
    * @throws Exception    if an error occurred.
    */
   private static Element getEditConfigXml(String value) throws Exception
   {
      System.out.println("Value to substitute: " + value);
      String requestString = FileUtils.getFileAsString(s_commandLine.getArgument("editConfigXml"));
      requestString = StringUtils.replaceTag(requestString, "@Tag", value);
      Element ret = XmlUtils.fromXmlString(requestString);
      ret.detach();
      return ret;
   }


   /**
    * The AtomicThread class provides the context in which to attempt run an atomic operation.
    */
   private static class AtomicThread extends Thread
   {

      /** NETCONF Client used to talk to the target */
      Client m_client;

      /** True if this thread is to simulate a failure */
      boolean m_fail;

      /**
       * Creates an AtomicThread.
       *
       * @param client  the NETCONF client.
       * @param fail    true to force a failure, false to run without forced failures.
       */
      AtomicThread(Client client, boolean fail)
      {
         m_client = client;
         m_fail = fail;
      }

      @Override
      public void run()
      {
         try
         {
            NetconfLocalTransactionContext ctxt = (NetconfLocalTransactionContext) m_client.startTransaction();
            String xid = ctxt.getTransactionId(); 
            m_client.editConfig("running", getEditConfigXml("atomic1"), null, null, null, xid);
            if (m_fail)
            {
               throw new Exception("Failing because we were requested to fail");
            }
            Thread.sleep(10000);
            m_client.editConfig("running", getEditConfigXml("atomic2"), null, null, null, xid);
            m_client.commitTransaction(ctxt);
         }
         catch (final Exception ex)
         {
            System.out.println("Exception encountered: " + ex.getMessage());
         }
      }

   }

   private static class ExtraneousThread extends Thread
   {

      private enum OperationStatus
      {
         NotStarted,
         Success,
         Failure
      };

      private OperationStatus m_status = OperationStatus.NotStarted;

      private Client m_client;

      ExtraneousThread(Client client)
      {
         m_client = client;
      }

      OperationStatus getStatus()
      {
         return m_status;
      }

      public void run()
      {
         try
         {
            m_client.editConfig("running", getEditConfigXml("extraneous"), null, null, null, null);
            m_status = OperationStatus.Success;
         }
         catch (final Exception ex)
         {
            System.out.println("Exception during operation: " + ex.getMessage());
            m_status = OperationStatus.Failure;
         }
      }
      
   }

}
