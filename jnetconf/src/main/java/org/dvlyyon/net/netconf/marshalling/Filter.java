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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.util.StringUtils;
import org.dvlyyon.common.util.XmlUtils;
import org.dvlyyon.net.netconf.common.Path;
import org.dvlyyon.net.netconf.common.PathSegment;
import org.jdom2.Element;
import org.jdom2.Namespace;


/**
 * The Filter class provides the NETCONF filter mechanism used during <get> and <getConfig> operations. A Filter is effectively a template
 * that you can specify to a query; the results of the query are those entities that match the template.
 * <p>
 * Although NETCONF specifies a capability of using an XPATH as the actual filter XML request (instead of the complete XML tree representation),
 * we do not support this; the reason being that we need to specify explicitly every attribute in an object for get and set operations; the XPATH
 * filter is restrictive in that sense.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class Filter
{

   /** Logger for tracing */
   static protected final Log s_logger = LogFactory.getLog(Filter.class.getName());

   /** The base NETCONF namespace */
   protected static Namespace s_rpcNs = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");

   /** The XML representation of the filter */
   private Element m_xmlFilter;

   /** The marshaling model */
   private DataModel m_model;


   /**
    * Creates a filter object based upon the specified parameters. Specifically, this filter says - "get me all the objects of type
    * 'chidClass' whose parent type is 'parentClass' and parentId is 'parentIdAsXpath'.
    *
    * @param model            The marshaling model.
    * @param parentClass         Class mapping representing the parent class.
    * @param parentIdAsXPath     XPath representing the parent ID.
    * @param childClass          Class representing the objects of interest.
    */
   public Filter(DataModel model, ClassMapping parentClass, String parentIdAsXPath, ClassMapping childClass)
   {
      createXmlFilter(model, parentIdAsXPath, parentClass, childClass, false);
   }

   /**
    * Creates a filter object based upon the specified parameters. Specifically, this filter says - "get me all the objects of type
    * 'classToFilter' whose ID is 'idAsXpath'. In reality, we expect only one object, assuming the ID is unique.
    *
    * @param model            The marshaling model.
    * @param classToFilter    Class representing the object of interest.
    * @param idAsXpath        XPath representing the ID of the object.
    */
   public Filter(DataModel model, ClassMapping classToFilter, String idAsXpath)
   {
      this(model, classToFilter, idAsXpath, false);
   }

   /**
    * Creates a filter object based upon the specified parameters. Specifically, this filter says - "get me the XML corresponding to
    * an object of type 'classToFilter' whose ID is 'idAsXpath'.
    *
    * @param model            The marshaling model.
    * @param classToFilter    Class representing the object of interest.
    * @param idAsXpath        XPath representing the ID of the object.
    * @param delete           true if the filter is being used for a delete operation on the object.
    */
   public Filter(DataModel model, ClassMapping classToFilter, String idAsXpath, boolean delete)
   {
      createXmlFilter(model, idAsXpath, classToFilter, null, delete);      
   }

   /**
    * Creates a NETCONF XML filter with the specified parameters. Actually constructs the complete XML tree, including all the attributes of
    * the desired object type and set the xmlFilter member variable.
    *
    * @param model         The marshaling model.
    * @param xpathId       XPath representing the ID of the object.
    * @param theClass      Class representing the parent object.
    * @param childClass    Class representing the object of interest.
    * @param delete        true if a filter is required for a delete operation.
    */
   private void createXmlFilter(DataModel model, final String xpathId, final ClassMapping theClass, final ClassMapping childClass, final boolean delete)
   {
      m_model = model;
      Element xml = null;
      Element current = null;
      String[] xpathTokens = new String[0];
      if (xpathId != null)
      {
         xpathTokens = StringUtils.tokenizeToStringArrayQuotes(xpathId, "/");         
      }
      //final ArrayList<PathSegment> segments = xpath.getSegments();
      final ArrayList<ClassMapping> mappings = getClassHierarchy(theClass);
      int index = 0;
      for (String token : xpathTokens)
      //for (PathSegment seg : segments)
      {
         ClassMapping clsMapping = mappings.get(index);
         Element elem = processToken(token, clsMapping);
         if (xml == null)
         {
            xml = elem;
            current = elem;
         }
         else
         {
            current.addContent(elem);
            current = elem;
         }
         index++;
      }
      if (childClass != null)
      {
         Namespace ns = Namespace.getNamespace(childClass.getXmlNamespace());
         // Add the tag for the child class
         Element elem = new Element(childClass.getXmlTag(), ns);
         if (current != null)
         {
            current.addContent(elem);
            current = elem;
         }
         else
         {
            current = elem;
            xml = elem;
         }
      }
      if (delete)
      {
         current.setAttribute("operation", "delete");
      }
      else
      {
         // It's not a delete; restrict the set of attributes to the ones we are interested in
         if (childClass != null)
         {
            addAttributeSet(childClass, current);
         }
         else
         {
            addAttributeSet(theClass, current);
         }
      }
      if (delete)
      {
         m_xmlFilter = xml;
      }
      else
      {
         m_xmlFilter = new Element("filter", s_rpcNs);
         m_xmlFilter.setAttribute("type", "subtree");
         m_xmlFilter.addContent(xml);
      }
      if (s_logger.isDebugEnabled())
      {
         s_logger.debug(XmlUtils.toXmlString(m_xmlFilter, true));
      }
   }

   /**
    * Given a class mapping, walks up the parent class hierarchy and creates the list of parent class mappings.
    *
    * @param theClass   Class mapping for class we are interested in.
    * @return           List of ClassMappings, starting at the root class all the way down to the class we are interested in
    *                   (including the specifed class itself).
    */
   static ArrayList<ClassMapping> getClassHierarchy(ClassMapping theClass)
   {
      ArrayList<ClassMapping> parentage = new ArrayList<ClassMapping>();
      ClassMapping current = theClass;
      if (current != null)
      {
         parentage.add(current);
      }
      while (current != null)
      {
         current = current.getParentClass();
         if (current != null)
         {
            parentage.add(0, current);
         }
      }
      return parentage;
   }

   /**
    * Given a string representing a path end-point, and the class that we are interested in, converts the attributes specified in the
    * end-point as XML entities used in a Filter. For example, if you have a path like 'myClass[key1="val1"][key2="val2"]', an XML
    * node is created for each key and added as a child to the root node called 'myClass'.
    *
    * @param pathToken  Path end-point (this is a path-segment). 
    * @param theClass   Class mapping representing the class we are dealing with.
    * @return           XML node containing Filter XML equivalent of the specified path end-point.
    */
   static Element processToken(final String pathToken, final ClassMapping theClass)
   //static Element processToken(final PathSegment pathSegment, final ClassMapping theClass)
   {
      // in the form elementTag [attribute1="value1"][attribute2="value2"]
      Element ret = null;
      //final String[] tokens = StringUtils.tokenizeToStringArray(pathSegment.getTag(), " []");
      // ** mike ** final String[] tokens = StringUtils.tokenizeToStringArray(pathToken, " []");
      final String[] tokens = StringUtils.tokenizeToStringArrayQuotes(pathToken, "[]");
      int index = 0;
      for (String token : tokens)
      {
         Namespace ns = Namespace.getNamespace(theClass.getXmlNamespace());
         //Namespace ns = Namespace.getNamespace(pathSegment.getNamespaceUri());
         if (index == 0)
         {
            ret = new Element(token, ns);
         }
         else
         {
            // This one is name="value"
            String name = token.substring(0, token.indexOf('=')).trim();
            // Actually we should look for the "" (and not assume lack of spaces)
            String value = token.substring(token.indexOf('\"')+1, token.lastIndexOf('\"'));
            // TODO: Deal with the possibility that the KEY ATTRIBUTE has a different namespace - a bit paranoid here
            Element attrib = new Element(name, ns);
            attrib.setText(value);
            ret.addContent(attrib);
         }
         index++;
      }
      return ret;
   }

   /**
    * Given an starting XML node and a container path representing a set of empty containers, creates all the XML elements required to
    * get to the lowest level container in the container hierarchy.
    * <p> 
    * This is not really used (it was used originally to skip empty containers, but it is recommended that you treat every container as
    * a distinct class in the hierarchy).
    *
    * @param top              Root XML node at which to start.
    * @param containerPath    Empty container hierarchy as a path (that looks like "a/b/c").
    * @param namespace        Namespace of the containers (it is assumed here that the namespace does not change).
    * @return                 XML node representing the lowest level container ("c" in the example above).
    */
   static Element getContainerHierarchy(final Element top, final String containerPath, final Namespace namespace)
   {
      Element ret = null;
      final String[] tokens = StringUtils.tokenizeToStringArray(containerPath, "/");
      int index = 0;
      for (String token : tokens)
      {
         if (index == 0)
         {
            top.setName(token);
            ret = top;
         }
         else
         {
            Element bottom = new Element(token, namespace);
            ret.addContent(bottom);
            ret = bottom;
         }
         index++;
      }
      return ret;
   }

   /**
    * 
    * @param classMapping
    * @param root
    */
   private void addAttributeSet(final ClassMapping classMapping, final Element root)
   {
      Namespace classNs  = Namespace.getNamespace(classMapping.getXmlNamespace());
      for (AttributeMapping am : classMapping.getAttributeMappings())
      {
         Namespace ns = classNs;
         if (am.getXmlNamespace() != null)
         {
            ns = Namespace.getNamespace(am.getXmlNamespace());
         }
         if (!am.isSynthetic() || (am.isSynthetic() && am.getType() == AttributeMapping.Type.Primitive_Boolean))
         {
            Element attrib = new Element(am.getXmlTag(), ns);
            root.addContent(attrib);
         }
         else if (am.getType() == AttributeMapping.Type.Class)
         {
            // It's a synthetic choice or case or union - recurse down the tree until you hit anything non-synthetic and add it
            Path childPath = Path.fromString(classMapping.getXmlPath());
            PathSegment ps = new PathSegment(classMapping.getXmlNamespace(), classMapping.getXmlTag());
            childPath.addSegment(ps);
            ps = new PathSegment(am.getXmlNamespace(), am.getXmlTag());
            childPath.addSegment(ps);
            ClassMapping childClass = m_model.getClassMappingByPath(childPath);
            if (childClass.getType() == ClassMapping.Type.Union)
            {
               // A union is treated as just an attribute
               Element attrib = new Element(am.getXmlTag(), ns);
               root.addContent(attrib);
            }
            else
            {
               addAttributeSet(childClass, root);
            }
         }
      }
   }

   /**
    * Returns the Filter as an XML node, which can be sent as over-the-wire NETCONF.
    *
    * @return  Filter as XML.
    */
   public Element getAsXml()
   {
      return m_xmlFilter;
   }

}
