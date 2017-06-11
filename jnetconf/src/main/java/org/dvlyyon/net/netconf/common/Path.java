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

import java.util.ArrayList;

import org.dvlyyon.common.util.StringUtils;


/**
 * The Path class represents an XPath (as defined in the NETCONF RFC 6241). A path contains a set of ordered path segments and is typically
 * used as a key to identify an entity on a device.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class Path
{

   /** List of segments that constitute a path */
   private ArrayList<PathSegment> m_segments = new ArrayList<PathSegment>();

   /**
    * Constructs an empty Path object.
    */
   public Path()
   {
   }

   /**
    * Constructs a Path with the single specified path segment.
    *
    * @param pathSegment   the only segment in the path.
    */
   public Path(PathSegment pathSegment)
   {
      addSegment(pathSegment);
   }

   /**
    * Constructs a Path based upon the specified path segments.
    *
    * @param pathSegments  Path segments that constitute the Path.
    */
   public Path(ArrayList<PathSegment> pathSegments)
   {
      m_segments = pathSegments;
   }

   /**
    * Compares two Paths. Paths are equal if their string representations are equal.
    *
    * @param obj  Other Path to compare to.
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof Path))
      {
         return false;
      }
      if (this == obj || toString().equals(obj.toString()))
      {
         return true;
      }
      return false; 
   }

   @Override
   public int hashCode()
   {
      return toString().hashCode();
   }

   /**
    * Returns a clone of path.
    *
    * @return     Cloned path - this is a "deep" clone.
    */
   public Path deepClone()
   {
      Path ret = new Path();
      for (PathSegment seg : getSegments())
      {
         PathSegment mySeg = seg.deepClone();
         ret.addSegment(mySeg);
      }
      return ret;
   }

   /**
    * Adds the specified path segment to the end of the path.
    *
    * @param pathSegment   Path segment to append to path.
    */
   public void addSegment(PathSegment pathSegment)
   {
      m_segments.add(pathSegment);
   }

   /** */
   public void setSegments(ArrayList<PathSegment> pathSegments)
   {
      m_segments = pathSegments;
   }

   /** */
   public ArrayList<PathSegment> getSegments()
   {
      return m_segments;
   }

   /**
    * Returns the string representation of the Path. A Path is represented in string form as:
    * <pathSegment1><pathSegment2>.....<pathSegmenntN>.
    *
    * @return  the Path as a string.
    */
   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer("");
      for (PathSegment ps : m_segments)
      {
         sb.append("<"+ps.toString()+">");
      }
      return sb.toString();
   }

   /**
    * Given the string representation of a Path, creates and returns it.
    *
    * @param path    The Path as a string (the format is described in the <b>toString</b> method.
    * @return        newly created Path, based upon the string.
    */
   public static Path fromString(String path)
   {
      ArrayList<PathSegment> pathSegments = new ArrayList<PathSegment>();
      String segments[] = StringUtils.tokenizeToStringArray(path, "<>");
      for (String segment : segments)
      {
         pathSegments.add(PathSegment.fromString(segment));
      }
      return new Path(pathSegments);
   }

   /**
    * Given the path as a slash('/')-separated string (as in a regular XPath expression), returns the fully
    * qualified path.
    *
    * @param path       The Path as a simple Xpath string (e.g. '/demodevice/shelf[shelf_number="2"]').
    * @param namespace  The namespace of the module used for path instantiation
    *                   (e.g. 'http://centeredlogic.com/ns/demodevice').
    * @return           newly created Path, based upon the string.
    */
   public static Path fromBareXpathString(String path, String namespace)
   {
      Path ret = new Path();
      if (path != null)
      {
         String segments[] = Path.getSegmentsFromPathSpecifier(path);
         for (String segment : segments)
         {
            PathSegment ps = new PathSegment(namespace, segment);
            ret.addSegment(ps);
         }
      }
      return ret;
   }

   /**
    * Given the path as a slash('/')-separated string (as in a regular XPath expression), returns the constituent path
    * segments as an array.
    *
    * @param pathString    XPath- (or Netconf-)like path representation.
    * @return              Path segments that form the Xpath.
    */
   public static String[] getSegmentsFromPathSpecifier(String pathString)
   {
      return StringUtils.tokenizeToStringArray(pathString, "/");
   }

}
