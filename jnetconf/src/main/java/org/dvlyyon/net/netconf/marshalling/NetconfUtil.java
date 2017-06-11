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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.beans.BeanUtil;
import org.dvlyyon.net.netconf.common.Path;
import org.dvlyyon.net.netconf.common.PathSegment;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The NetconfUtil class provides methods that perform the transformation between POJOs (Plain Oild Java Objects) and corresponding NETCONF XML.
 * It uses the metadata that is provided in the mapping XML to do the actual marsahlling and unmarsahlling.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public final class NetconfUtil
{

   /** Logger for tracing */
   static protected final Log s_logger = LogFactory.getLog(NetconfUtil.class.getName());

   /**
    * Defines the NETCONF operation to be applied during an <edit-config>.
    */
   public static enum EditOperation
   {
      /** Not relevant */
      NotApplicable,
      /** Specified for object creation */
      Create,
      /** Specified for object replacement */
      Replace,
      /** Specified for object merge (modification of an existing object) */
      Merge,
      /** Specified for object deletion */
      Delete
   };

   /** The data model, which represents the metadata driving the transformation */
   private DataModel m_model;

   /** Helper class used to marshal choices and unions */
   private UnionChoiceHelper m_unionChoiceHelper;


   /**
    * Creates a NetconfUtil object.
    *
    * @param dataModel  Model containing transformation metadata.
    */
   public NetconfUtil(DataModel dataModel)
   {
      m_model = dataModel;
      m_unionChoiceHelper = new UnionChoiceHelper(this, m_model);
   }

   /**
    * Given a POJO, converts it to the corresponding (valid) NETCONF XML, which can then be sent over the wire.
    *
    * @param parentXpathOid      XPATH representing the path to the parent object of the POJO.
    * @param oldObject           the old POJO, in case the edit operation specified is an update. We need to pass this in, since
    *                            there may be a specific need to "unset" attributes that were previously set.
    * @param object              the POJO that needs to be converted to NETOCNF XML.
    * @param editOperation       The type of operation to be performed.
    * @return                    XML node representing the data to be sent via NETCONF.
    * @throws RuntimeException   if an error occurred.
    */
   public Element toXml(final Path parentXpathOid, final Object oldObject, final Object object, final EditOperation editOperation) throws RuntimeException
   {
      // Create an xpath based upon the parent's OID, my xmltag and my keys
      Element ret = null;
      ClassMapping classUnderConsideration = m_model.getClassMappingByName(object.getClass().getName());
      if (classUnderConsideration == null)
      {
         throw new RuntimeException("Class metadata not found for: " + object.getClass().getName());
      }
      Element current = null;
      ClassMapping cm = classUnderConsideration.getParentClass();
      if (cm == null)
      {
         s_logger.debug("Parent class metadata not found for: " + object.getClass().getName());
      }
      else
      {
         //Namespace ns = Namespace.getNamespace(cm.getXmlNamespace());
         final ArrayList<PathSegment> segments = parentXpathOid.getSegments();
         final ArrayList<ClassMapping> mappings = Filter.getClassHierarchy(cm);
         int index = 0;
         for (PathSegment segment : segments)
         {
            ClassMapping theClass = mappings.get(index);
            Element elem = Filter.processToken(segment.getTag(), theClass);
            if (ret == null)
            {
               ret = elem;
               current = elem;
            }
            else
            {
               current.addContent(elem);
               current = elem;
            }
            index++;
         }
      }
      // Now we have got all the XML up to our parent in the hierarchy, now for the class itself
      Element classElem = new Element(classUnderConsideration.getXmlTag(), Namespace.getNamespace(classUnderConsideration.getXmlNamespace()));
      // Here is where we set the operation to being create, update or delete
      if (editOperation == EditOperation.Delete)
      {
         classElem.setAttribute("operation", "delete");
      }
      else if (editOperation == EditOperation.Create)
      {
         classElem.setAttribute("operation", "create");
      }
      else if (editOperation == EditOperation.Replace)
      {
         classElem.setAttribute("operation", "replace");
      }
      else if (editOperation == EditOperation.Merge)
      {
         classElem.setAttribute("operation", "merge");
      }
      if (ret == null)
      {
         ret = classElem;
      }
      else
      {
         current.addContent(classElem);
      }
      dumpAttributesToXml(classElem, classUnderConsideration, oldObject, object, editOperation);
      return ret;
   }

   /**
    * Transform all the attributes of the specified POJO to NETCONF XML and add these to the XML node representing the object itself.
    *
    * @param classElem           XML node representing the object.
    * @param cm                  Metadata representing the class being transformed.
    * @param oldObject           the old POJO, in case the edit operation specified is an update. We need to pass this in, since
    *                            there may be a specific need to "unset" attributes that were previously set.
    * @param object              the POJO that needs to be converted to NETOCNF XML.
    * @param editOperation       The type of operation to be performed.
    * @throws RuntimeException   if an error occurred.
    */
   private void dumpAttributesToXml(final Element classElem, final ClassMapping cm, final Object oldObject,
                                    final Object object, final EditOperation editOperation) throws RuntimeException
   {
      // Need to dump all the keys first (BEFORE anything else); this should be done during creation
      for (AttributeMapping am : cm.getAttributeMappings())
      {
         dumpAttributeToXml(am, classElem, cm, oldObject, object, editOperation);
      }
   }

   /**
    * Given an attribute mapping for a class, convert it to NETCONF XML and add them to the root XML node.
    *
    * @param am                  Attribute mappings defining an attribute of the class.
    * @param classElem           Root XML node representing the object.
    * @param cm                  ClassMapping representing the class being transformed.
    * @param oldObject           the old POJO, in case the edit operation specified is an update. We need to pass this in, since
    *                            there may be a specific need to "unset" attributes that were previously set.
    * @param object              the POJO that needs to be converted to NETOCNF XML.
    * @param editOperation       The type of operation to be performed.
    * @throws RuntimeException   if an error occurred.
    */
   @SuppressWarnings("unchecked")
   void dumpAttributeToXml(AttributeMapping am, final Element classElem, final ClassMapping cm, final Object oldObject,
                           final Object object, final EditOperation editOperation) throws RuntimeException
   {
      Namespace namespace = Namespace.getNamespace(cm.getXmlNamespace());
      Namespace ns = namespace;
      String attrNs = am.getXmlNamespace();
      if (attrNs != null)
      {
         ns = Namespace.getNamespace(attrNs);
      }
      if (am.isReadOnly())
      {
         // We do not stream read-only values out
         return;
      }
      if (am.getType() == AttributeMapping.Type.Class)
      {
         Path childPath = Path.fromString(cm.getXmlPath());
         PathSegment ps = new PathSegment(cm.getXmlNamespace(), cm.getXmlTag());
         childPath.addSegment(ps);
         ps = new PathSegment(am.getXmlNamespace(), am.getXmlTag());
         childPath.addSegment(ps);
         ClassMapping childClass = m_model.getClassMappingByPath(childPath);
         if (childClass == null)
         {
            throw new RuntimeException("Class mapping not found for path: " + childPath);
         }
         if (childClass.getType() == ClassMapping.Type.Union)
         {

            Object childValue = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), object);
            if (am.isList())
            {
               if (editOperation == EditOperation.Merge && oldObject != null)
               {
                  List<Object> oldUnions = (List<Object>) BeanUtil.getDirectListValue(am.getJavaMemberName(), oldObject);
                  for (Object oldUnion : oldUnions)
                  {
                     Element unionAttrib = new Element(am.getXmlTag(), ns);
                     unionAttrib.setAttribute("operation", "delete");
                     // TODO: Optimize later - only dump out the index attributes
                     m_unionChoiceHelper.unionToXml(childClass, oldUnion, unionAttrib);
                     classElem.addContent(unionAttrib);
                  }
               }
               // Stick the new ones in
               List<Object> unionList = (List<Object>) childValue;
               for (Object unionObject : unionList)
               {
                  Element attrib = new Element(am.getXmlTag(), ns);
                  m_unionChoiceHelper.unionToXml(childClass, unionObject, attrib);
                  classElem.addContent(attrib);
               }
            }
            else
            {
               Element attrib = new Element(am.getXmlTag(), ns);
               m_unionChoiceHelper.unionToXml(childClass, childValue, attrib);
               classElem.addContent(attrib);
            }
         }
         else if (childClass.getType() == ClassMapping.Type.Choice)
         {
            Object choiceValue = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), object);
            m_unionChoiceHelper.choiceToXml(cm, childClass, choiceValue, classElem, editOperation);           
         }
         else if (!am.isList())
         {
            boolean add = false;
            Object value = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), object);
            Element childClassElem = new Element(childClass.getXmlTag(), Namespace.getNamespace(childClass.getXmlNamespace()));
            if (value != null)
            {
               childClassElem.setAttribute("operation", "replace");
               add = true;
               dumpAttributesToXml(childClassElem, childClass, null, value, EditOperation.NotApplicable);
            }
            else
            {
               // Remove the child if there used to be one there originally
               if (editOperation == EditOperation.Merge && oldObject != null)
               {
                  Object oldValue = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), oldObject);
                  if (oldValue != null)
                  {
                     childClassElem.setAttribute("operation", "delete");
                     add = true;
                  }
               }
            }
            if (add)
            {
               classElem.addContent(childClassElem);
            }
         }
         else
         {
            // Need to delete the old objects first (if there is an old object)
            if (oldObject != null)
            {
               List<Object> oldVos = (List<Object>) BeanUtil.getDirectListValue(am.getJavaMemberName(), oldObject);
               for (Object vo : oldVos)
               {
                  Element childClassElem = new Element(childClass.getXmlTag(), Namespace.getNamespace(childClass.getXmlNamespace()));
                  classElem.addContent(childClassElem);
                  childClassElem.setAttribute("operation", "delete");
                  // TODO: Optimize later - only dump out the index attributes
                  dumpAttributesToXml(childClassElem, childClass, null, vo, EditOperation.Create);                  
               }
            }
            // Then, add the current ones - aka "create"
            List<Object> vos = (List<Object>) BeanUtil.getDirectListValue(am.getJavaMemberName(), object);
            for (Object vo : vos)
            {
               Element childClassElem = new Element(childClass.getXmlTag(), Namespace.getNamespace(childClass.getXmlNamespace()));
               classElem.addContent(childClassElem);
               childClassElem.setAttribute("operation", "replace");
               dumpAttributesToXml(childClassElem, childClass, null, vo, EditOperation.Create);
            }
         }
      }
      else
      {
         // Check for a list here, too (this corresponds to a leaf-list)
         if (am.isList())
         {
            // Delete all the existing leaf-list attributes if the operation is an edit
            if (editOperation == EditOperation.Merge && oldObject != null)
            {
               List<Object> oldPrimitives = (List<Object>) BeanUtil.getDirectListValue(am.getJavaMemberName(), oldObject);
               for (Object prim : oldPrimitives)
               {
                  Element attrib = new Element(am.getXmlTag(), ns);
                  attrib.setAttribute("operation", "delete");
                  String strVal = am.convertValueToString(prim);
                  if (strVal != null)
                  {
                     attrib.setText(strVal);
                  }
                  classElem.addContent(attrib);
               }                  
            }
            // Stick the new ones in
            List<Object> primitives = (List<Object>) BeanUtil.getDirectListValue(am.getJavaMemberName(), object);
            for (Object prim : primitives)
            {
               Element attrib = new Element(am.getXmlTag(), ns);
               String strVal = am.convertValueToString(prim);
               if (strVal != null)
               {
                  attrib.setText(strVal);
               }
               classElem.addContent(attrib);
            }
         }
         else
         {
            Element targetNode = classElem;
            boolean add = false;
            Element attrib = null;;
            if (am.isSynthetic() && am.getType() != AttributeMapping.Type.Primitive_Boolean)
            {
               // No extra XML node here - just set the content directly (unless it is "empty")
               attrib = classElem;
               targetNode = null;
            }
            else
            {
               attrib = new Element(am.getXmlTag(), ns);
            }
            Object value = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), object);
            if (value != null)
            {
               String strVal = am.convertValueToString(value);
               if (strVal != null)
               {
                  attrib.setText(strVal);
                  add = true;
               }
            }
            else
            {
               // "Unset" the attribute by setting delete = true
               if (editOperation == EditOperation.Merge && oldObject != null)
               {
                  Object oldValue = BeanUtil.getDirectFieldValue(am.getJavaMemberName(), oldObject);
                  if (oldValue != null)
                  {
                     attrib.setAttribute("operation", "delete");
                     add = true;
                  }
               }
            }
            if (add && targetNode != null)
            {
               targetNode.addContent(attrib);
            }
         }
      }
   }

   /**
    * Give the XML node that represents a response from a NETCONF device, extracts and creates the list of POJOs that
    * correspond to the specified Java class.
    *
    * @param root                Root node of NETCONF XML returned from a query.
    * @param javaClass           Fully qualified class name of POJO.
    * @return                    List of objects of type 'javaClass', that are contained within the specified node.
    * @throws RuntimeException   if an error occurred.
    */
   public List<Object> fromXml(final Element root, final String javaClass) throws RuntimeException
   {
      ClassMapping cm = m_model.getClassMappingByName(javaClass);
      if (cm == null)
      {
         throw new RuntimeException("Class mapping not found: " + javaClass);
      }
      return fromXml(root, cm, false);
   }

   /**
    * Give the XML node that represents a response from a NETCONF device, extracts and creates the list of POJOs that
    * correspond to the specified Java class.
    *
    * @param root                Root node of NETCONF XML returned from a query.
    * @param cm                  Class mapping that represents the desired objects.
    * @param ignoreContainment   true to ignore intermediate containers, false to take them into account.
    * @return                    List of objects of the desired type, that are contained within the specified node.
    * @throws RuntimeException   if an error occurred.
    */
   @SuppressWarnings("unchecked")
   private List<Object> fromXml(final Element root, ClassMapping cm, final boolean ignoreContainment) throws RuntimeException
   {
      List<Object> ret = new ArrayList<Object>();
      final Path xmlpath = Path.fromString(cm.getXmlPath());
      final String namespace = cm.getXmlNamespace();
      if (xmlpath == null)
      {
         throw new RuntimeException("XML container not found for class: " + cm.getJavaClassName());
      }
      Element elementToProcess = root;
      if (!ignoreContainment)
      {
         Element current = root;
         // We are searching from the ROOT node for this class
         // final String[] xpathTokens = StringUtils.tokenizeToStringArray(xmlpath, "/");
         final ArrayList<PathSegment> xpathTokens = xmlpath.getSegments();
         int index = 0;
         //for (String token : xpathTokens)
         for (PathSegment token : xpathTokens)
         {
            current = current.getChild(token.getTag(), Namespace.getNamespace(token.getNamespaceUri()));
            if (current == null)
            {
               // We have gotten only through part of the hierarchy (after which it has apparently ended).
               // We take this to mean there is no data for the required objects; so return the empty list
               //throw new RuntimeException("Unexpected XML tag - expected: " + token.getTag() + ", got: null");
               s_logger.warn("Unexpected XML tag - expected: " + token.getTag() + ", got: null");
               return ret;
            }
            index++;
         }
         elementToProcess = current;
      }
      // Now we are pointing to the PARENT node of the class we are interested in
      // Get the list of classes that we are looking for
      String tag = cm.getXmlTag();
      List<Element> classes = (List<Element>) elementToProcess.getChildren(tag, Namespace.getNamespace(namespace));
      for (Element theClass : classes)
      {
         //VersionedObject o = ModelUtil.getInstance().createObject(m_model.getModuleName()+"/"+cd.getName(), m_version);
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
         setAttributesFromXml(o, theClass, cm);
         ret.add(o);
      }
      return ret;
   }

   /**
    * Processes the specified element and extracts all the attribute information from it; then sets these attributes
    * in the specified instance of the class represented by its mapping.
    * 
    * @param vo               POJO whose attributes are being converted from their XML representations.
    * @param classElement     Root XML node representing the instance.
    * @param cm               Class mapping that contains metadata about the class and its attributes.
    */
   @SuppressWarnings("unchecked")
   void setAttributesFromXml(final Object vo, final Element classElement, final ClassMapping cm)
   {
      //Namespace namespace = Namespace.getNamespace(cm.getXmlNamespace());
      Element elementToProcess = classElement;
      for (AttributeMapping am : cm.getAttributeMappings())
      {
         Namespace attribNs = Namespace.getNamespace(am.getXmlNamespace());
         if (am.getType() == AttributeMapping.Type.Class)
         {
            Path childXmlPath = Path.fromString(cm.getXmlPath());
            PathSegment ps = new PathSegment(cm.getXmlNamespace(), cm.getXmlTag());
            PathSegment attrPs = new PathSegment(am.getXmlNamespace(), am.getXmlTag());
            childXmlPath.addSegment(ps);
            childXmlPath.addSegment(attrPs);
            ClassMapping childClass = m_model.getClassMappingByPath(childXmlPath);
            if (childClass == null)
            {
               throw new RuntimeException("Class mapping not found for path : " + childXmlPath);
            }
            if (childClass.getType() == ClassMapping.Type.Union)
            {
               List<Element> attribElements = elementToProcess.getChildren(am.getXmlTag(), attribNs);
               if (am.isList())
               {
                  List<Object> listVal = new ArrayList<Object>();
                  for (Element attribElement : attribElements)
                  {
                     Object unionValue = m_unionChoiceHelper.xmlToUnion(cm, attribElement);
                     listVal.add(unionValue);
                  }
                  BeanUtil.setDirectListValue(am.getJavaMemberName(), vo, listVal, "java.util.ArrayList");
               }
               else
               {
                  Element attribElement = attribElements.get(0);
                  if (attribElement != null)
                  {
                     Object unionValue = m_unionChoiceHelper.xmlToUnion(cm, attribElement);
                     BeanUtil.setDirectFieldValue(am.getJavaMemberName(), vo, unionValue);
                  }
               }
            }
            else if (childClass.getType() == ClassMapping.Type.Choice)
            {
               Object choiceClass = m_unionChoiceHelper.xmlToChoice(cm, am.getXmlTag(), classElement);
               BeanUtil.setDirectFieldValue(am.getJavaMemberName(), vo, choiceClass);
            }
            else
            {
               //  Pass in an empty xpathOid and ignore containment, since we are rooted right here
               List<Object> kids = this.fromXml(elementToProcess, childClass, true);
               if (kids != null)
               {
                  if (am.isList())
                  {
                     BeanUtil.setDirectListValue(am.getJavaMemberName(), vo, kids, childClass.getJavaClassName());
                  }
                  else if (kids.size() > 0)
                  {
                     BeanUtil.setDirectFieldValue(am.getJavaMemberName(), vo, kids.get(0));
                  }
               }
            }
         }
         else
         {
            // Built-in, enumeration and user-defined
            if (am.isList())
            {
               // This is a leaf-list; get all the values and set the thing
               List<Element> attribElements = elementToProcess.getChildren(am.getXmlTag(), attribNs);
               List<Object> primitives = new ArrayList<Object>();
               String primitiveClass = "java.lang.String";
               for (Element attribElement : attribElements)
               {
                  Object value = am.convertStringToValue(attribElement.getText());
                  primitiveClass = value.getClass().getName();
                  primitives.add(value);
               }
               BeanUtil.setDirectListValue(am.getJavaMemberName(), vo, primitives, primitiveClass);
            }
            else
            {
               Element sourceNode = elementToProcess;
               if (!am.isSynthetic() || am.getType() == AttributeMapping.Type.Primitive_Boolean)
               {
                  Element attribElement = elementToProcess.getChild(am.getXmlTag(), attribNs);
                  if (attribElement == null)
                  {
                     s_logger.debug("Attribute with tag: " + am.getXmlTag() + " not received.");
                     continue;
                  }
                  sourceNode = attribElement;
               }
               // This will take care of transformations from String to any primitive Java type
               BeanUtil.setDirectFieldValue(am.getJavaMemberName(), vo, am.convertStringToValue(sourceNode.getText()));
            }
         }
      }
   }

}
