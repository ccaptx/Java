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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.net.netconf.common.Path;
import org.dvlyyon.net.netconf.common.PathSegment;
import org.jdom2.Element;


/**
 * The DataModel class holds metadata that is used in marshaling data between NETCONF and Java objects. In conjunction with the NetconfUtil
 * and Filter classes, is is used to transform a Java object into its equivalent NETCONF XML and vice-versa. The format of the XML data
 * representing the data model is
 * <pre>
 * <netconfDataModel>
 *    <class name="com.xxx.Abc" ... />
 *    <class name="com.xxx.Xyz" ... />
 * </netconfDataModel>
 * </pre>
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class DataModel
{

   /** Logger for tracing */
   private static final Log s_logger = LogFactory.getLog(DataModel.class.getName());

   /** Special logger for dumping extended data */
   private static final Log s_dumplogger = LogFactory.getLog(DataModel.class.getName() + "_dump");

   /** Mapping from Java class name to its mapping metadata */
   private LinkedHashMap<String, ClassMapping> m_javaClassToMappings = new LinkedHashMap<String, ClassMapping>();

   /** Mapping from NETCONF XML path to the corresponding class mapping metadata */
   private LinkedHashMap<Path, ClassMapping> m_xmlPathToMappings = new LinkedHashMap<Path, ClassMapping>();

   /**
    * Constructs a DataModel.
    */
   public DataModel()
   {
      // Default public CTOR
   }

   /**
    * Loads the data model from its XML representation.
    *
    * @param modelRoot  XML element representing root of the data model.
    */
   @SuppressWarnings("unchecked")
   public void fromXml(Element modelRoot)
   {
      List<Element> kids = (List<Element>) modelRoot.getChildren("class");
      // Pass 1 - loads up each class from the XML model
      for (Element kid : kids)
      {
         ClassMapping cm = new ClassMapping();
         cm.fromXml(kid);
         addClassMapping(cm);
      }
      // Pass 2 - resolve references (specifically, parent class)
      for (Element kid : kids)
      {
         ClassMapping cm = getClassMappingByName(kid.getAttributeValue("name"));
         String parentClassName = kid.getAttributeValue("parentClass");
         if (parentClassName != null)
         {
            ClassMapping parent = getClassMappingByName(parentClassName);
            if (parent == null)
            {
               throw new RuntimeException("Parent class with name: " + parentClassName + " not found");
            }
            cm.setParentClass(parent);
         }
      }      
   }

   /**
    * Returns the XML representation of this data model.
    *
    * @return  Data model as XML.
    */
   public Element toXml()
   {
      Element ret = new Element("netconfDataModel");
      for (ClassMapping cm : getClassMappings())
      {
         ret.addContent(cm.toXml());
      }
      return ret;
   }

   /**
    * Adds a class mapping to the data model.
    *
    * @param cm                  Class mapping to add to model.
    * @throws RuntimeException   if an error occurred during addition.
    */
   public void addClassMapping(final ClassMapping cm) throws RuntimeException
   {
      ClassMapping old = m_javaClassToMappings.put(cm.getJavaClassName(), cm);
      if (old != null)
      {
         s_logger.warn("Replacing class mapping for: " + cm.getJavaClassName());
         throw new RuntimeException("A class mapping for the class: " + cm.getJavaClassName()+ " already exists.");
      }
      Path path = Path.fromString(cm.getXmlPath());
      PathSegment ps = new PathSegment(cm.getXmlNamespace(), cm.getXmlTag());
      path.addSegment(ps);
      old = m_xmlPathToMappings.put(path, cm);
      if (old != null)
      {
         s_logger.warn("Replaced class mapping: " + old.getJavaClassName());
         throw new RuntimeException("A class mapping for the XPATH: " + path + " already exists.");
      }
   }

   /**
    * Returns the set of class mappings for this data model.
    *
    * @return  All the class mappings for this data model.
    */
   public Collection<ClassMapping> getClassMappings()
   {
      return m_xmlPathToMappings.values();
   }

   /**
    * Given the name of a Java class, returns the class mapping corresponding to it.
    *
    * @param javaClassName    (Fully qualified) Java class name.
    * @return                 class mapping for this class, or NULL if none exists.
    */
   public ClassMapping getClassMappingByName(final String javaClassName)
   {
      return m_javaClassToMappings.get(javaClassName);
   }

   /**
    * Given a NETCONF XML path, returns the class mapping corresponding to it.
    *
    * @param path    XML path to get to a class.
    * @return        class mapping for the class, or NULL if none exists.
    */
   public ClassMapping getClassMappingByPath(final Path path)
   {
      dump();
      return m_xmlPathToMappings.get(path);
   }

   /**
    * Dumps class mapping information out to the dump logger stream.
    */
   public void dump()
   {
      if (s_dumplogger.isDebugEnabled())
      {
         StringBuffer dump = getDump();
         s_dumplogger.debug(dump);
      }
   }

   /**
    * Creates and returns an extended string containing all the mapping information between Java classes and NETCONF
    * XML paths.
    *
    * @return  Detailed mapping information contained in this data model.
    */
   public StringBuffer getDump()
   {
      StringBuffer dump = new StringBuffer("\n\n\nDataModel dump.\n\nCLASSMAPPINGS ****************************\n");
      Set<Entry<String, ClassMapping>> classentries = m_javaClassToMappings.entrySet();
      for (Entry<String, ClassMapping> entry : classentries)
      {
         dumpMapping(dump, entry.getKey(), entry.getValue());
      }

      dump.append("\n\nXMLMAPPINGS ****************************\n");
      Set<Entry<Path, ClassMapping>> xmlentries = m_xmlPathToMappings.entrySet();
      for (Entry<Path, ClassMapping> entry : xmlentries)
      {
         dumpMapping(dump, entry.getKey().toString(), entry.getValue());
      }
      return dump;
   }

   /**
    * Dumps the specified class mapping information to the specified string buffer.
    *
    * @param dump    String buffer to append to.
    * @param key     Key of mapping entry.
    * @param mapping Class Mapping corresponding to the key.
    */
   private void dumpMapping(StringBuffer dump, String key, ClassMapping mapping)
   {
      dump.append("\n------");
      dump.append("\nkey: ").append(key);
      dump.append("\njavaClass: ").append(mapping.getJavaClassName());
      dump.append("\nxmlNamespace: ").append(mapping.getXmlNamespace());
      dump.append("\nxmlPath: ").append(mapping.getXmlPath());
      dump.append("\nxmlTag: ").append(mapping.getXmlTag());
   }

}
