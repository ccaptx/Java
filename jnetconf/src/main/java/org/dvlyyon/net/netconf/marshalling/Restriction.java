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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.dvlyyon.common.util.StringUtils;
import org.jdom2.Element;


/**
 * The Restriction class represents a constraint applied to an attribute - to see more about the kind of restrictions applicable,
 * see <a href="http://tools.ietf.org/html/rfc6020">RFC 6020</a>. The XML representation of a restriction mapping looks like so:
 * <pre>
 * <attribute name="attr1" ... >
 *   <restriction type="Range">
 *     <value>0 *</value>
 *     <value>2048 4191</value>
 *   </restriction>
 *   <restriction type="FractionDigits"/>
 *     <value>2</value>
 *   </restriction>
 * </attribute>
 * </pre>
 * <p>
 * 
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class Restriction
{

   /** Definition of possible restriction types */
   public enum Type
   {
      None,
      Range,
      FractionDigits,
      Length,
      Pattern,
      MinElements,
      MaxElements
   };

   /** Type of Restriction */
   private Type m_type = Type.None;

   /** Possible values for the restriction */
   private ArrayList<String> m_values = new ArrayList<String>();


   /**
    * Creates a Restriction.
    */
   public Restriction()
   {
   }

   /**
    * Loads up the Restriction from its XML representation.
    *
    * @param restrictionElement  Root XML node representing the restriction.
    */
   @SuppressWarnings("unchecked")
   public void fromXml(Element restrictionElement)
   {
      m_type = Type.valueOf(restrictionElement.getAttributeValue("type"));
      List<Element> valueElems = (List<Element>) restrictionElement.getChildren("value");
      for (Element valElem : valueElems)
      {
         m_values.add(valElem.getText().trim());
      }
   }

   /**
    * Converts this Restriction to its XML representation.
    * 
    * @return  Restriction as XML.
    */
   public Element toXml()
   {
      Element ret = new Element("restriction");
      ret.setAttribute("type", m_type.name());
      for (String value : m_values)
      {
         ret.addContent(new Element("value").setText(value));
      }
      return ret;
   }

   /** */
   public ArrayList<String> getValues()
   {
      return m_values;
   }

   /** */
   public void setValues(List<String> values)
   {
      m_values.clear();
      m_values.addAll(values);
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

   @Override
   public String toString()
   {
      return "Restriction: type = " + m_type + "; value = " + m_values;
   }

   boolean validate(Object value)
   {
      boolean ret = true;
      if (value != null)
      {
         switch (m_type)
         {
         case Range:
            ret = checkRange(value);
            break;

         case FractionDigits:
            ret = checkFractionDigits(value);
            break;

         case Length:
            ret = checkLength(value);
            break;

         case Pattern:
            ret = checkPattern(value);
            break;

         default:
            break;
         }
      }
      return ret;
   }

   private boolean checkRange(Object value)
   {
      // TODO: We go only as far as longs here (in reality we should convert all possible types)
      boolean valid = true;
      if (value instanceof Number)
      {
         Number n = (Number) value;
         for (String constraintValue : m_values)
         {
            String[] tokens = StringUtils.tokenizeToStringArray(constraintValue, " ");
            BigInteger minValue = null;
            if (!tokens[0].equals("*"))
            {
               minValue = new BigInteger(tokens[0]);
            }
            BigInteger maxValue = null;
            if (!tokens[tokens.length-1].equals("*"))
            {
               maxValue = new BigInteger(tokens[tokens.length-1]);
            }
            boolean lowValid = (minValue == null || n.longValue() >= minValue.longValue());
            boolean hiValid = (maxValue == null || n.longValue() <= maxValue.longValue());
            valid = lowValid && hiValid;
            if (valid)
            {
               break;
            }
         }
      }
      return valid;
   }

   private boolean checkFractionDigits(Object value)
   {
      if (value instanceof Double)
      {
         // TODO: Someday ....
         return true;
      }
      return false;
   }

   private boolean checkLength(Object value)
   {
      boolean valid = true;
      int actualLength = -1;
      if (value instanceof String)
      {
         String str = (String) value;
         actualLength = str.length();
      }
      else if (value instanceof byte[])
      {
         byte[] bytes = (byte[]) value;
         actualLength = bytes.length;
      }
      if (actualLength != -1)
      {
         for (String constraintValue : m_values)
         {
            String[] tokens = StringUtils.tokenizeToStringArray(constraintValue, " ");
            Integer minLength = null;
            if (!tokens[0].equals("*"))
            {
               minLength = Integer.parseInt(tokens[0]);
            }
            Integer maxLength = null;
            if (!tokens[tokens.length-1].equals("*"))
            {
               maxLength = Integer.parseInt(tokens[tokens.length-1]);            
            }
            boolean minValid = (minLength == null || actualLength >= minLength);
            boolean maxValid = (maxLength == null || actualLength <= maxLength);
            valid = minValid && maxValid;
            if (valid)
            {
               break;
            }
         }
      }      
      return valid;
   }

   private boolean checkPattern(Object value)
   {
      boolean valid = true;
      if (value instanceof String)
      {
         String str = (String) value;
         for (String pattern : m_values)
         {
            valid = str.matches(pattern);
            if (valid)
            {
               break;
            }
         }
      }
      return valid;
   }

}
