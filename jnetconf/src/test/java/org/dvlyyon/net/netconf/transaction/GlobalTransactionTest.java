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
package org.dvlyyon.net.netconf.transaction;

import java.util.ArrayList;
import java.util.Properties;

import junit.framework.TestCase;

import org.dvlyyon.common.io.FileUtils;
import org.dvlyyon.common.transaction.LocalTransactionContextIf;
import org.dvlyyon.common.transaction.TransactionManager;
import org.dvlyyon.common.transaction.TransactionalResourceIf;
import org.dvlyyon.common.util.CommandLine;
import org.dvlyyon.common.util.StringUtils;
import org.dvlyyon.common.util.XmlUtils;
import org.dvlyyon.net.netconf.Client;
import org.dvlyyon.net.netconf.ClientTest;
import org.dvlyyon.net.netconf.ConfiguratorIf;
import org.dvlyyon.net.netconf.NetconfLocalTransactionContext;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The GlobalTransactionTest acts as a test harness for testing NETCONF global transactions. It will perform a distributed transaction
 * across multiple devices, making the same configuration change across all of them.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class GlobalTransactionTest extends TestCase
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
    * The entry point to the NETCONF client test.
    *
    * @param args Parameters to the test. To see what these are, run the test like so:
    *                   java com.centeredlogic.net.netconf.GlobalTransactionTest ?
    */
   public static void main(String[] args)
   {
      try
      {
         CommandLine cmdline = CommandLine.getInstance("NETCONF Global Transaction Test", "1.0");
         cmdline.initArg("host", true, -1, null, "List of device hostname or IP Address");
         cmdline.initArg("port", false, 1, "22", "SSH port on device");
         cmdline.initArg("username", false, 1, "admin", "SSH user name (for authentication)");
         cmdline.initArg("password", true, 1, "", "SSH password name (for authentication)");
         cmdline.initArg("namespace", true, 1, "", "XML Nanespace to be used for the test");
         cmdline.initArg("tag", true, 1, "", "Tag identifying the attribute being modified for the test");
         cmdline.initArg("editConfigXml", true, 1, "editConfig.xml", "File containing XML for configuration edit");
         cmdline.initArg("filterXml", true, 1, "filter.xml", "File containing filter XML for configuration get");
         cmdline.setArgs(args);
         s_commandLine = cmdline;

         int count = cmdline.getArgumentCount("host");
         System.out.println("Setting up " + count + " NETCONF clients ...");
         ArrayList<Client> clients = new ArrayList<Client>();
         String port = null;
         if (!"".equals(cmdline.getArgument("port")))
         {
            port = cmdline.getArgument("port");
         }
         for (int i=0; i<count; i++)
         {
            String host = cmdline.getArgument("host", i);
            Properties properties = new Properties();
            properties.put("host", host);
            properties.put("port", port);
            properties.put("username", cmdline.getArgument("username"));
            properties.put("password", cmdline.getArgument("password"));
            Client client = new Client();
            client.setup(properties);
            clients.add(client);
         }
         System.out.println("NETCONF client setup complete.");

         listValues(clients);
         for (Client client : clients)
         {
            // Set it to a known value before we start
            NetconfLocalTransactionContext context = (NetconfLocalTransactionContext) client.startTransaction();
            client.editConfig("candidate", getEditConfigXml("1111"), null, null, null, context.getTransactionId());
            client.commitTransaction(context);
         }

         listValues(clients);

         System.out.println("Testing global transaction (with success)");
         testTransaction(clients, false);
         System.out.println("Global transaction (success) complete");

         listValues(clients);
         // TODO: You are currently expected to check whether the test worked by
         // actually visually inspecting the values printed out. Later just add a check in
         // code to verify correct behavior.

         System.out.println("Testing global transaction (with failure)");
         testTransaction(clients, true);
         System.out.println("Global transaction (failure) complete");

         listValues(clients);
         // TODO: You are currently expected to check whether the test worked by
         // actually visually inspecting the values printed out. Later just add a check in
         // code to verify correct behavior.
      }
      catch (final Exception ex)
      {
         ex.printStackTrace();
         System.exit(1);
      }
   }

   /**
    * Tests global (or distributed) transaction behavior as specified.
    *
    * @param clients       Clients that are part of the distributed transaction.
    * @param forceFailure  true if we are testing for transaction failure case, false if we are
    *                      testing for the success case. 
    */
   private static void testTransaction(ArrayList<Client> clients, boolean forceFailure)
   {
      String newLabel = forceFailure ? "1166" : "2222";
      TransactionManager txmgr = TransactionManager.getInstance();
      String xid = txmgr.createTransaction(500, 120);
      ArrayList<TransactionalResourceIf> resources = new ArrayList<TransactionalResourceIf>();
      for (Client client : clients)
      {
         resources.add(client);
      }
      txmgr.startTransaction(xid, resources);
      // Test failure in the last one
      int cfgCount = clients.size();
      int index = 0;
      try
      {
         for (Client client : clients)
         {
            if (forceFailure && index == cfgCount-1)
            {
               throw new Exception("ForcedFailureException");
            }
            NetconfLocalTransactionContext resourceContext = (NetconfLocalTransactionContext) txmgr.getResourceContext(xid, client);
            String localXid = resourceContext.getTransactionId();
            client.editConfig("candidate", getEditConfigXml(newLabel), null, null, null, localXid);
            index++;
         }
         txmgr.commitTransaction(xid);
         System.out.println("Committed transaction");
      }
      catch (final Exception ex)
      {
         System.out.println("Exception: " + ex.getMessage());
         txmgr.rollbackTransaction(xid);
         System.out.println("Rolled back transaction");
      }
   }

   /**
    * Displays the values of the relevant entity being modified on all the specified clients.
    *
    * @param clients       Clients that are part of the distributed transaction.
    * @throws Exception    if an error occurred.
    */
   private static void listValues(ArrayList<Client> clients) throws Exception
   {
      for (Object c : clients)
      {
         ConfiguratorIf client = (ConfiguratorIf) c;
         String label2 = getCurrentValue(client);
         System.out.println("Value: " + label2);
      }
   }

   /**
    * Reads the filter XML file and sends out the request to the target. Processes the response and extracts the
    * value of the specified tag, when it is encountered for the first time.
    *
    * @param client        Configurator used to communicate with the target.
    * @return              Value of the attribute identified by the specified tag.
    * @throws Exception    if an error occurred.
    */
   private static String getCurrentValue(ConfiguratorIf client) throws Exception
   {
      Element getFilter = XmlUtils.fromXmlFile(s_commandLine.getArgument("filterXml"));
      Element response = client.getConfig("running", getFilter, null);
      Namespace ns = Namespace.getNamespace(s_commandLine.getArgument("namespace"));
      String tag = s_commandLine.getArgument("tag");
      Element attrNode = ClientTest.getNode(response, ns, tag);;
      if (attrNode == null)
      {
         throw new Exception("No attribute found for tag: " + tag);
      }
      return attrNode.getText();
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

}
