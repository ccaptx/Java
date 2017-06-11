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
package org.dvlyyon.net.netconf.toaster;


/**
 * Top-level container for all toaster database objects..
 *
 * @author  Model Based Management Technologies Code Generator.
 * @since   1.6
 */
public class Toaster
{

   private String toasterManufacturer = "DefaultManufacturer";

   private String toasterModelNumber = "2.3.4";

   private String toasterStatus = "Up";

   
   /**
    * Creates a default Toaster instance.
    */
   public Toaster()
   {
   }

   /**
    * Returns the value of the toasterManufacturer.
    *
    * @return  The current value of toasterManufacturer.
    */
   public java.lang.String getToasterManufacturer()
   {
      return toasterManufacturer;
   }

   /**
    * Sets toasterManufacturer to the specified value.
    *
    * @param x  The new value of toasterManufacturer.
    */
   public void setToasterManufacturer(java.lang.String p_toasterManufacturer)
   {
      toasterManufacturer = p_toasterManufacturer;
   }

   /**
    * Returns the value of the toasterModelNumber.
    *
    * @return  The current value of toasterModelNumber.
    */
   public java.lang.String getToasterModelNumber()
   {
      return toasterModelNumber;
   }

   /**
    * Sets toasterModelNumber to the specified value.
    *
    * @param x  The new value of toasterModelNumber.
    */
   public void setToasterModelNumber(java.lang.String p_toasterModelNumber)
   {
      toasterModelNumber = p_toasterModelNumber;
   }

   /**
    * Returns the value of the toasterStatus.
    *
    * @return  The current value of toasterStatus.
    */
   public java.lang.String getToasterStatus()
   {
      return toasterStatus;
   }

   /**
    * Sets toasterStatus to the specified value.
    *
    * @param x  The new value of toasterStatus.
    */
   public void setToasterStatus(java.lang.String p_toasterStatus)
   {
      toasterStatus = p_toasterStatus;
   }

}
