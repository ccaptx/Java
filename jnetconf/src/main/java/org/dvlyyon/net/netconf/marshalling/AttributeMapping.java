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
import java.util.List;

import org.jdom2.Element;

// The XML representation looks like so:
// <attribute name="" type="class' xmlTag=""
//            readOnly="true/false" mandatory="true/false" many="true/false" />
/**
 * The AttributeMapping class represents the mapping between a Java member (or attribute) and its representation as NETCONF XML.
 * This class is used to marshal data between Java classes and NETCONF XML.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class  AttributeMapping
{

   /** Definitions of the various attribute types we support */
   public enum Type
   {
      // Primitive -- or basically, Java types that correspond to built-in NETCONF types
      Primitive_Boolean,
      Primitive_Integer,
      Primitive_Long,
      Primitive_BigInteger,
      Primitive_Double,
      Primitive_ByteArray,
      Primitive_String,

      // Class -- if the attribute indicates a relationship
      // Note - the class may be a real one or a "synthetic" one (union, choice or case)
      Class,
      // For now, an enumeration is just a string
      //Enumeration,
      Bitset
   };

   /** XML tag that represents this attribute */
   private String m_xmlTag;

   /** Xml namespace for attribute - typically this makes sense if the attribute is a container or a list */
   private String m_xmlNamespace;

   /** Name of the java member */
   private String m_javaMemberName;

   /** Attribute type */
   private Type m_type = Type.Primitive_String;

   /** True if the attribute is read-only (e.g. non-config attribute) */
   private boolean m_readOnly;

   /** True if the attribute is mandatory (e.g. key attribute) */
   private boolean m_mandatory;

   /** True if the attribute is a list */
   private boolean m_isList;

   /** True if the attribute is "synthetic" - i.e. it represents one of the following:
    * <ul>
    * <li>A member of a union</li>
    * <li>A case in a choice</li>
    * <li>A boolean which is actually a NETCONF empty</b>
    * </ul>
    */
   private boolean m_synthetic;

   /** List of restrictions applicable for this attribute */
   private ArrayList<Restriction> m_restrictions = new ArrayList<Restriction>();


   /**
    * Constructs an AttributeMapping.
    */
   public AttributeMapping()
   {
      //
   }

   /**
    * Loads up the AttributeMapping from its XML representation.
    *
    * @param attrRoot   XML node representing the AttributeMapping.
    */
   @SuppressWarnings("unchecked")
   public void fromXml(final Element attrRoot)
   {
      m_javaMemberName = attrRoot.getAttributeValue("name");
      m_xmlTag = attrRoot.getAttributeValue("xmlTag");
      m_xmlNamespace = attrRoot.getAttributeValue("xmlNamespace");
      m_readOnly = "true".equals(attrRoot.getAttributeValue("readOnly"));
      m_mandatory = "true".equals(attrRoot.getAttributeValue("mandatory"));
      m_isList = "true".equals(attrRoot.getAttributeValue("many"));
      m_synthetic = "true".equals(attrRoot.getAttributeValue("synthetic"));
      boolean isClass = "class".equals(attrRoot.getAttributeValue("type"));
      if (isClass)
      {
         m_type = Type.Class;
      }
      boolean isBitset = "bits".equals(attrRoot.getAttributeValue("type"));
      if (isBitset)
      {
         m_type = Type.Bitset;
      }
      List<Element> restrictions = (List<Element>) attrRoot.getChildren("restriction");
      for (Element restriction : restrictions)
      {
         Restriction r = new Restriction();
         r.fromXml(restriction);
         m_restrictions.add(r);
      }
   }

   /**
    * Converts this AttributeMapping to its XML representation.
    *
    * @return  Element representing the AttributeMapping as XML.
    */
   public Element toXml()
   {
      Element ret = new Element("attribute");
      ret.setAttribute("name", m_javaMemberName);
      if (m_xmlTag != null)
      {
         ret.setAttribute("xmlTag", m_xmlTag);
      }
      if (m_xmlNamespace != null)
      {
         ret.setAttribute("xmlNamespace", m_xmlNamespace);
      }
      ret.setAttribute("readOnly", "" + m_readOnly);
      ret.setAttribute("mandatory", "" + m_mandatory);
      ret.setAttribute("many", "" + m_isList);
      ret.setAttribute("synthetic", "" + m_synthetic);
      if (m_type == Type.Class)
      {
         ret.setAttribute("type", "class");
      }
      else if (m_type == Type.Bitset)
      {
         ret.setAttribute("type", "bits");
      }
      ret.setAttribute("many", "" + m_isList);
      for (Restriction restriction : m_restrictions)
      {
         Element rElem = restriction.toXml();
         ret.addContent(rElem);
      }
      return ret;
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
   public String getJavaMemberName()
   {
      return m_javaMemberName;
   }

   /** */
   public void setJavaMemberName(String javaMemberName)
   {
      m_javaMemberName = javaMemberName;
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
   public boolean isReadOnly()
   {
      return m_readOnly;
   }

   /** */
   public void setReadOnly(boolean readOnly)
   {
      m_readOnly = readOnly;
   }

   /** */
   public boolean isMandatory()
   {
      return m_mandatory;
   }

   /** */
   public void setMandatory(boolean mandatory)
   {
      m_mandatory = mandatory;
   }

   /** */
   public boolean isSynthetic()
   {
      return m_synthetic;
   }

   /** */
   public void setSynthetic(boolean synthetic)
   {
      m_synthetic = synthetic;
   }

   /** */
   public boolean isList()
   {
      return m_isList;
   }

   /** */
   public void setList(boolean isList)
   {
      m_isList = isList;
   }

   /** */
   public ArrayList<Restriction> getRestrictions()
   {
      return m_restrictions;
   }

   /** */
   public void addRestriction(Restriction r)
   {
      m_restrictions.add(r);
   }

   /** */
   public void setRestrictions(ArrayList<Restriction> restrictions)
   {
      m_restrictions = restrictions;
   }

   ///////////////// Utility methods

   public String convertValueToString(Object value) throws RuntimeException
   {
      String ret = null;
      switch (m_type)
      {
      case Primitive_Boolean:
//         if (m_synthetic)
//         {
//            if ( ((Boolean)value).booleanValue())
//            {
//               ret = "";
//            }
//            else
//            {
//               ret = null;
//            }
//         }
//         else
//         {
            ret = ""+value;
//         }
         break;
      case Primitive_Integer:
      case Primitive_Long:
      case Primitive_BigInteger:
      case Primitive_Double:
      case Primitive_String:
         ret = value.toString();
         break;
      case Bitset:
         // TODO: Convert from byte array to string?
         throw new RuntimeException("Bitset type not yet supported");
      case Primitive_ByteArray:
         // Convert to Base 64
         ret = javax.xml.bind.DatatypeConverter.printBase64Binary((byte[])value);
         break;
      case Class:
      default:
         throw new RuntimeException("Unexpected call to convertValueToString() with type: " + m_type);
      }
      return ret;
   }

   public Object convertStringToValue(String value) throws RuntimeException
   {
      if (value == null)
      {
         return null;
      }
      Object ret = null;
      switch (m_type)
      {
      case Primitive_Boolean:
         // TODO: In case of empty, we need to check for EXISTENCE!
         ret = Boolean.valueOf(value);
         break;
      case Primitive_Integer:
         ret = Integer.valueOf(value);
         break;
      case Primitive_Long:
         ret = Long.valueOf(value);
         break;
      case Primitive_BigInteger:
         ret = new java.math.BigInteger(value);
         break;
      case Primitive_Double:
         ret = Double.valueOf(value);
         break;
      case Primitive_String:
         ret = value.toString();
         break;
      case Bitset:
         // TODO: Convert from byte array to string?
         throw new RuntimeException("Bitset type not yet supported");
      case Primitive_ByteArray:
         // Convert to Base 64
         ret = javax.xml.bind.DatatypeConverter.parseBase64Binary(value);
         break;
      case Class:
      default:
         throw new RuntimeException("Unexpected call to convertValueToString() with type: " + m_type);
      }
      return ret;
   }

   public boolean isValid(Object value)
   {
      boolean ret = true;
      for (Restriction r : m_restrictions)
      {
         ret = r.validate(value);
         if (!ret)
         {
            break;
         }
      }
      return ret;
   }

}
