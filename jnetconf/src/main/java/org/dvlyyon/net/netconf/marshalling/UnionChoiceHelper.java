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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.beans.BeanUtil;
import org.dvlyyon.net.netconf.common.Path;
import org.dvlyyon.net.netconf.common.PathSegment;
import org.dvlyyon.net.netconf.marshalling.NetconfUtil.EditOperation;
import org.jdom2.Element;


/**
 * TODO:
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class UnionChoiceHelper
{

   /** Logger for tracing */
   static protected final Log s_logger = LogFactory.getLog(UnionChoiceHelper.class.getName());

   NetconfUtil m_parent;

   DataModel m_model;


   UnionChoiceHelper(NetconfUtil parent, DataModel model)
   {
      m_parent = parent;
      m_model = model;
   }

   void unionToXml(ClassMapping cm, Object unionObject, Element targetNode)
   {
      // Take the union class and check out every attribute in it one by one
      for (AttributeMapping am : cm.getAttributeMappings())
      {
         // The first one that is a non-null is Xml-ized and returned
         // TODO: This could be a nested union, so we should probably recurse
         Object value = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), unionObject);
         if (value != null)
         {
            String strVal = am.convertValueToString(value);
            if (strVal != null)
            {
               targetNode.setText(strVal);
               break;
            }
         }
      }
   }

   public static String unionToStringValue(ClassMapping cm, Object unionObject)
   {
      String ret = null;
      // Take the union class and check out every attribute in it one by one
      for (AttributeMapping am : cm.getAttributeMappings())
      {
         // The first one that is a non-null is Xml-ized and returned
         // TODO: It COULD be a nested union; so we should probably recurse
         Object value = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), unionObject);
         if (value != null)
         {
            String strVal = am.convertValueToString(value);
            if (strVal != null)
            {
               ret = strVal;
               break;
            }
         }
      }
      return ret;
   }

   Object xmlToUnion(ClassMapping cm, Element unionAttributeNode)
   {
      // Get to the union class
      Path childPath = Path.fromString(cm.getXmlPath());
      PathSegment ps = new PathSegment(cm.getXmlNamespace(), cm.getXmlTag());
      childPath.addSegment(ps);
      ps = new PathSegment(unionAttributeNode.getNamespaceURI(), unionAttributeNode.getName());
      childPath.addSegment(ps);
      ClassMapping unionClass = m_model.getClassMappingByPath(childPath);
      if (unionClass == null)
      {
         throw new RuntimeException("Class mapping not found for path: " + childPath);
      }
      // Instantiate the union class
      Object ret = createInstance(unionClass);
      // Check out every attribute in it one by one
      for (AttributeMapping am : unionClass.getAttributeMappings())
      {
         Object attribValue = null;
         try
         {
            attribValue = am.convertStringToValue(unionAttributeNode.getText());
            if (attribValue != null)
            {
               boolean valid = am.isValid(attribValue);
               if (!valid)
               {
                  throw new RuntimeException("Invalid union assigment; constraint mismatch");
               }
            }
         }
         catch (final Exception ex)
         {
            s_logger.warn(ex.getMessage());
            attribValue = null;
         }
         if (attribValue != null)
         {
            // Yay - we found a match; set it
            BeanUtil.setDirectFieldValue(am.getJavaMemberName(), ret, attribValue);
            break;
         }
      }
      return ret;
   }

   void choiceToXml(ClassMapping parentCm, ClassMapping cm, Object choiceObject, Element targetNode, EditOperation editOperation)
   {
      // Take the choice class and check out every attribute in it one by one
      // Each attribute must be a case
      for (AttributeMapping am : cm.getAttributeMappings())
      {
         // The first one that is a non-null is Xml-ized and returned
         Object value = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), choiceObject);
         if (value != null)
         {
            // This must be a case; go down into it
            Path childPath = Path.fromString(cm.getXmlPath());
            PathSegment ps = new PathSegment(cm.getXmlNamespace(), cm.getXmlTag());
            childPath.addSegment(ps);
            ps = new PathSegment(am.getXmlNamespace(), am.getXmlTag());
            childPath.addSegment(ps);
            ClassMapping caseClass = m_model.getClassMappingByPath(childPath);
            if (caseClass == null)
            {
               throw new RuntimeException("Class mapping not found for path: " + childPath);
            }
            caseToXml(parentCm, caseClass, value, targetNode, editOperation);
            break;
         }
      }
   }

   void caseToXml(ClassMapping parentCm, ClassMapping cm, Object caseObject, Element targetNode, EditOperation editOperation)
   {
      // Just stuff in every attribute in the case into the target node
      for (AttributeMapping am : cm.getAttributeMappings())
      {
         //m_parent.dumpAttributeToXml(am, targetNode, parentCm, null, caseObject, editOperation);
         m_parent.dumpAttributeToXml(am, targetNode, cm, null, caseObject, editOperation);
      }
   }

   @SuppressWarnings("unchecked")
   Object xmlToChoice(ClassMapping cm, String tag, Element choiceParentNode) throws RuntimeException
   {
      // Get to the choice class
      Path childPath = Path.fromString(cm.getXmlPath());
      PathSegment ps = new PathSegment(cm.getXmlNamespace(), cm.getXmlTag());
      childPath.addSegment(ps);
      ps = new PathSegment(choiceParentNode.getNamespaceURI(), tag);
      childPath.addSegment(ps);
      ClassMapping choiceClass = m_model.getClassMappingByPath(childPath);
      if (choiceClass == null)
      {
         throw new RuntimeException("Class mapping not found for path: " + childPath);
      }
      // Instantiate the choice object
      Object choice = createInstance(choiceClass);
      ClassMapping caseClass = null;
      Object caseObject = null;
      List<Element> attribElements = choiceParentNode.getChildren();
      if (attribElements != null && attribElements.size() > 0)
      {
         for (Element potentialCaseAttribute : attribElements)
         {
            // Lookup the choice's case mappings and match it to ONE Of the attributes in the case node
     outer: for (AttributeMapping caseMapping : choiceClass.getAttributeMappings())
            {
               if (caseMapping.getType() != AttributeMapping.Type.Class)
               {
                  throw new RuntimeException("Invalid metadata - all attributes in a Choice class MUST be Case classes");
               }
               Path caseXmlPath = Path.fromString(choiceClass.getXmlPath());
               ps = new PathSegment(choiceClass.getXmlNamespace(), choiceClass.getXmlTag());
               PathSegment attrPs = new PathSegment(caseMapping.getXmlNamespace(), caseMapping.getXmlTag());
               caseXmlPath.addSegment(ps);
               caseXmlPath.addSegment(attrPs);
               caseClass = m_model.getClassMappingByPath(caseXmlPath);
               if (caseClass == null)
               {
                  throw new RuntimeException("Class mapping not found for path : " + caseXmlPath);
               }
               for (AttributeMapping caseAttrib : caseClass.getAttributeMappings())
               {
                  if (potentialCaseAttribute.getName().equals(caseAttrib.getXmlTag()) && potentialCaseAttribute.getNamespaceURI().equals(caseAttrib.getXmlNamespace()))
                  {
                     // We found the case - create the case object and set the choice's member to point to this
                     caseObject = createInstance(caseClass);
                     BeanUtil.setDirectFieldValue(caseMapping.getJavaMemberName(), choice, caseObject);
                     break outer;
                  }
               }
            }
            if (caseObject != null)
            {
               // do an XmlToCase, which is the same as fromXml - given the case class and instance
               m_parent.setAttributesFromXml(caseObject, choiceParentNode, caseClass);
               return choice;
            }
         }
      }
      return null;
   }

   Object createInstance(ClassMapping cm) throws RuntimeException
   {
      Object o = null;
      try
      {
         Class<?> cls = Class.forName(cm.getJavaClassName());
         o = cls.newInstance();
      }
      catch (final Exception ex)
      {
         throw new RuntimeException("Error instantiating class: " + cm.getJavaClassName() + " -- " + ex.getMessage());
      }
      return o;
   }

}
