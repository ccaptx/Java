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
package org.dvlyyon.net.netconf.common;


/**
 * A Path segment is part of an XPath as used by NETCONF. A series of segments in order constitute a path.
 * Each segment in a path could be in a separate namespace.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class PathSegment
{

   /** The namespace of the path segment */
   String m_namespaceUri;

   /** The tag of the path segment */
   String m_tag;


   /**
    * Constructs a PathSegment with the specified parameters.
    *
    * @param namespaceUri  Namespace which the segment belongs to.
    * @param tag           the tag itself.
    */
   public PathSegment(String namespaceUri, String tag)
   {
      m_namespaceUri = namespaceUri;
      m_tag = tag;
   }

   /**
    * Returns a clone of the path segment.
    *
    * @return     Cloned path segment.
    */
   public PathSegment deepClone()
   {
      PathSegment ret = new PathSegment(getNamespaceUri(), getTag());
      return ret;
   }

   /** */
   public void setNamespaceUri(String namespaceUri)
   {
      m_namespaceUri = namespaceUri;
   }

   /** */
   public String getNamespaceUri()
   {
      return m_namespaceUri;
   }

   /** */
   public void setTag(String tag)
   {
      m_tag = tag;
   }

   /** */
   public String getTag()
   {
      return m_tag;
   }

   /**
    * Returns the path segment as a string. A PathSegment is represented as "namespace#tag".
    */
   public String toString()
   {
      return m_namespaceUri + '#' + m_tag;
   }

   /**
    * Given the string representation of a path segment, creates and returns it.
    *
    * @param segment String version of a PathSegment.
    * @return        newly created PathSegment.
    */
   public static PathSegment fromString(String segment)
   {
      String namespace = "";
      String tag = segment;
      int hashIndex = segment.indexOf('#');
      if (hashIndex != -1)
      {
         namespace = segment.substring(0, hashIndex);
         tag = segment.substring(hashIndex+1);
      }
      return new PathSegment(namespace, tag);
   }

}
