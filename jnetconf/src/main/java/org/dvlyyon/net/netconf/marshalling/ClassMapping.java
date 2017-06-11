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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.util.StringUtils;
import org.jdom2.Element;


/**
 * The ClassMapping class represents the mapping between a Java class and its representation as NETCONF XML. This class is used to marshal
 * data between Java classes and NETCONF XML. The XML representation of a class mapping looks like so:
 * <pre>
 * <class name="com.xxx.Abc" xmlPath="<http:://com.xxx.ns#system><http://com.xxx.ns#config>"
 *                           xmlNamespace="<http://com.xxx.ns>" xmlTag="abc"
 *                           keys="attr1,attr2"
 *                           parentClass="com.xxx.AAA" />
 *   <attribute name="attr1" ... />
 *   <attribute name="attr2" ... />
 *   <attribute name="description" ... />
 * </class>
 * </pre>
 * <p>
 * All path and namespace specifications follow the format specified in the PathSegment class; in other words, a path looks like:
 * <pre>   <completeNamespace#tag> </pre>
 * 
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class ClassMapping
{

   /** Logger for tracing */
   private static final Log s_logger = LogFactory.getLog(ClassMapping.class.getName());

   /** The type of the class - it represents one of the following:
    * <ul>
    * <li>A regular class</li>
    * <li>A union</li>
    * <li>A choice</li>
    * <li>A case in a choice</li>
    * </ul>
    */
   public enum Type
   {
      Normal,
      Union,
      Choice,
      Case
   };

   /** Complete XML path on how to get to this class */
   private String m_xmlPath = "";

   /** NETCONF XML tag that represents this class */
   private String m_xmlTag;

   /** Netconf XML namespace for this class */
   private String m_xmlNamespace;

   /** Parent class (in hierarchy) */
   private ClassMapping m_parentClass;

   /** Fully qualified Java class name */
   private String m_javaClassName;

   /** List of attribute names that act as keys (in order) */
   private String[] m_keyAttributes = new String[0];

   /** Mapping from attribute name (java member name) to Attribute mapping */
   private LinkedHashMap<String, AttributeMapping> m_attributes = new LinkedHashMap<String, AttributeMapping>();

   /** List of attributes (java member name) in the order in which they must be emitted (keys first) */
   private ArrayList<AttributeMapping> m_attributesInOrder = new ArrayList<AttributeMapping>();

   /** Type of class mapping */
   private Type m_type = Type.Normal;

   /**
    * Creates a ClassMapping.
    */
   public ClassMapping()
   {
   }

   /**
    * Loads up the class mapping from its XML representation.
    *
    * @param classRoot  Root XML node representing the class mapping.
    */
   @SuppressWarnings("unchecked")
   public void fromXml(Element classRoot)
   {
      m_javaClassName = classRoot.getAttributeValue("name");
      m_xmlPath = classRoot.getAttributeValue("xmlPath");
      m_xmlTag = classRoot.getAttributeValue("xmlTag");
      m_xmlNamespace = classRoot.getAttributeValue("xmlNamespace");
      m_type = Type.Normal;
      String typeStr = classRoot.getAttributeValue("type");
      if (typeStr != null && typeStr.length() > 0)
      {
         m_type = Type.valueOf(typeStr);
      }
      String keys = classRoot.getAttributeValue("keys");
      List<Element> kids = (List<Element>) classRoot.getChildren("attribute");
      for (Element kid : kids)
      {
         AttributeMapping am = new AttributeMapping();
         am.fromXml(kid);
         addAttributeMapping(am);
      }
      loadKeys(keys);
   }

   /**
    * Adds an attribute mapping to this class mapping.
    *
    * @param am   The attribute mapping to add.
    */
   public void addAttributeMapping(final AttributeMapping am)
   {
      // Add it to the combined map
      AttributeMapping old = m_attributes.put(am.getJavaMemberName(), am);
      if (old != null)
      {
         s_logger.warn("Replacing attribute mapping for: " + am.getJavaMemberName());
      }
   }

   /**
    * Returns an AttributeMapping, given the (java member) name of the attribute.
    *
    * @param attributeName    Java name of the attribute.
    * @return                 the corresponding AttributeMapping, or NULL if none exists.
    */
   public AttributeMapping getAttributeMapping(final String attributeName)
   {
      return m_attributes.get(attributeName);
   }

   /**
    * Returns the set of Attribute Mappings for this class.
    *
    * @return  all the attribute mappings for the class.
    */
   public Collection<AttributeMapping> getAttributeMappings()
   {
      return m_attributesInOrder;
   }

   /**
    * Loads up and validates the keys specified in the key set.
    * 
    * @param keySet              Comma-separated list of key attributes.
    * @throws RuntimeException   if an error occurred during key processing.
    */
   private void loadKeys(final String keySet) throws RuntimeException
   {
      m_keyAttributes = StringUtils.tokenizeToStringArray(keySet, ",");
      validateAndSortKeys();
   }

   /**
    * Validates the key attributes - makes sure mappings for these attributes actually exist. Also sorts them so that, given the set of
    * attributes, the key attributes are the <b>first</b> ones in the list, in order.
    * <p>
    * Some implementations of NETCONF agents require that these MUST be the first attributes passed down in the XML.
    *
    * @throws RuntimeException   if a key attribute is not found.
    */
   private void validateAndSortKeys() throws RuntimeException
   {
      // Make sure there is an attribute with the name
      ArrayList<AttributeMapping> keysInOrder = new ArrayList<AttributeMapping>();
      for (int i=0; i<m_keyAttributes.length; i++)
      {
         AttributeMapping am = m_attributes.get(m_keyAttributes[i]);
         if (am == null)
         {
            s_logger.error("Could not find attribute named: " + m_keyAttributes[i] + " in class: " + getJavaClassName());
            throw new RuntimeException("Attribute with name: " + m_keyAttributes[i] + " not defined");
         }
         keysInOrder.add(am);
      }
      LinkedHashMap<String, AttributeMapping> newMap = new LinkedHashMap<String, AttributeMapping>();
      // Add the keya sttributes to the new map from the old
      for (AttributeMapping am : keysInOrder)
      {
         m_attributes.remove(am.getJavaMemberName());
         s_logger.debug("Added key attribute: " + am.getJavaMemberName());
         m_attributesInOrder.add(am);
         newMap.put(am.getJavaMemberName(), am);
      }
      // Now add the rest of the attributes remaining in the old map
      for (Entry<String, AttributeMapping> oldEntry : m_attributes.entrySet())
      {
         // to the array
         m_attributesInOrder.add(oldEntry.getValue());
         // as well as the new map
         newMap.put(oldEntry.getKey(), oldEntry.getValue());
      }
      m_attributes = newMap;
   }

   /**
    * Converts this ClassMapping to its XML representation.
    * 
    * @return  Class mapping as XML.
    */
   public Element toXml()
   {
      Element ret = new Element("class");
      ret.setAttribute("name", m_javaClassName);
      ret.setAttribute("xmlPath", m_xmlPath);
      ret.setAttribute("xmlTag", m_xmlTag);
      ret.setAttribute("xmlNamespace", m_xmlNamespace);
      ret.setAttribute("type", "" + m_type.name());
      if (m_parentClass != null)
      {
         ret.setAttribute("parentClass", m_parentClass.getJavaClassName());
      }
      for (AttributeMapping am : m_attributes.values())
      {
         ret.addContent(am.toXml());
      }
      String keys = "";
      for (int i = 0; i < m_keyAttributes.length; i++)
      {
         if (i == 0)
         {
            keys += m_keyAttributes[i];
         }
         else
         {
            keys += "," + m_keyAttributes[i];
         }
      }
      ret.setAttribute("keys", keys);
      return ret;
   }

   /** */
   public String getXmlPath()
   {
      return m_xmlPath;
   }

   /** */
   public void setXmlPath(String xmlpath)
   {
      m_xmlPath = xmlpath;
   }

   /** */
   public String getXmlTag()
   {
      return m_xmlTag;
   }

   /** */
   public void setXmlTag(String xmlTag)
   {
      m_xmlTag = xmlTag;
   }

   /** */
   public String getXmlNamespace()
   {
      return m_xmlNamespace;
   }

   /** */
   public void setXmlNamespace(String xmlNamespace)
   {
      m_xmlNamespace = xmlNamespace;
   }

   /** */
   public ClassMapping getParentClass()
   {
      return m_parentClass;
   }

   /** */
   public void setParentClass(ClassMapping parentClass)
   {
      m_parentClass = parentClass;
   }

   /** */
   public String getJavaClassName()
   {
      return m_javaClassName;
   }

   /** */
   public void setJavaClassName(String javaClassName)
   {
      m_javaClassName = javaClassName;
   }

   /** */
   public Type getType()
   {
      return m_type;
   }

   /** */
   public void setType(Type type)
   {
      m_type = type;
   }

   /** */
   public String[] getKeyAttributes()
   {
      return m_keyAttributes;
   }

   /**
    * Sets the key attributes for this class, When the key attributes are set, the set of attributes are also
    * re-sorted so that the key attributes are the first ones processed.
    *
    * @param keyAttributes       List of key attribute names.
    * @throws RuntimeException   if a specified attributes is not found.
    */
   public void setKeyAttributes(String[] keyAttributes) throws RuntimeException
   {
      m_keyAttributes = keyAttributes;
      validateAndSortKeys();
   }

}
