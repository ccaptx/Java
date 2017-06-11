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
package org.dvlyyon.net.netconf.marshalling;

import java.util.List;

import junit.framework.TestCase;

import org.dvlyyon.common.util.CommandLine;
import org.dvlyyon.common.util.XmlUtils;
import org.dvlyyon.net.netconf.common.Path;
import org.dvlyyon.net.netconf.marshalling.DataModel;
import org.dvlyyon.net.netconf.marshalling.NetconfUtil;
import org.dvlyyon.net.netconf.marshalling.NetconfUtil.EditOperation;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The MarshalTest is a test harness that allows you to convert a POJO to its NETCONF-XML equivalent and vice-versa. You can generate
 * XML corresponding to the POJO of interest, save the file, then edit it and read it back in to verify the marshalling/un-marshalling
 * process.
 * <p>
 * Typically, you would use it after some modification to make sure the transformations work with <b>your>/b> classes.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class MarshalTest extends TestCase
{

   /**
    * Just a place-holder to make the build happy, since this does not really have any JUnit tests
    */
   public void testDummy()
   {
   }

   /**
    * Main method, which acts as the test harness.
    *
    * @param args Command-line parameters (use ? for usage information).
    */
   public static void main(String[] args)
   {
      try
      {
         CommandLine cmdline = CommandLine.getInstance("Netconf marshaling test", "1.0");
         cmdline.initArg("op", false, 1, "read", "Operation to perform <read|write>");
         cmdline.initArg("model", true, 1, "mapping.xml", "XML File containing mapping model");
         cmdline.initArg("class", false, 1, "com.centeredlogic.net.netconf.toaster.Toaster", "Java Class name of class being processed");
         cmdline.initArg("file", false, 1, "", "XML File containing netconf RPC response to process (required for a read operation)");
         cmdline.initArg("path", false, 1, "/toaster", "Path to class (for a write operation)");
         cmdline.setArgs(args);
 
         // Load up the data model mappings
         Element modelXml = XmlUtils.fromXmlFile(cmdline.getArgument("model"));
         DataModel dm = new DataModel();
         dm.fromXml(modelXml);
         NetconfUtil nu = new NetconfUtil(dm);

         // Read the RPC response file
         String classType = cmdline.getArgument("class");
         String op = cmdline.getArgument("op");
         if ("read".equals(op))
         {
            Element rpcReply = org.dvlyyon.common.util.XmlUtils.fromXmlFile(cmdline.getArgument("file"));
            Namespace ns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");
            Element data = rpcReply.getChild("data", ns);
            List<Object> objects = nu.fromXml(data, classType);
            for (Object object : objects)
            {
               System.out.println("Got object: " + object);
            }
            System.out.println("Retrieved: " + objects.size() + " objects.");
         }
         else // if op = write
         {
            // Instantiate the class
            Class<?> cls = Class.forName(classType);
            Object o = cls.newInstance();
            Path path = Path.fromString(cmdline.getArgument("path"));
            Element objectRoot = nu.toXml(path, null, o, EditOperation.Merge);
            System.out.println(org.dvlyyon.common.util.XmlUtils.toXmlString(objectRoot));
         }
      }
      catch (final Exception ex)
      {
         ex.printStackTrace();
      }
   }

}
