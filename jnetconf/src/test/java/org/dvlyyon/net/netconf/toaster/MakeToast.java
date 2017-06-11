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
 * Make some toast.
          The toastDone notification will be sent when 
          the toast is finished.
          An 'in-use' error will be returned if toast
          is already being made.
          A 'resource-denied' error will be returned 
          if the toaster service is disabled..
 *
 * @author  Model Based Management Technologies Code Generator.
 * @since   1.6
 */
public class MakeToast
{

   private Long toasterDoneness;

   private String toasterToastType;


   /**
    * Creates a default MakeToast instance.
    */
   public MakeToast()
   {
   }

   /**
    * Returns the value of the toasterDoneness.
    *
    * @return  The current value of toasterDoneness.
    */
   public java.lang.Long getToasterDoneness()
   {
      return toasterDoneness;
   }

   /**
    * Sets toasterDoneness to the specified value.
    *
    * @param x  The new value of toasterDoneness.
    */
   public void setToasterDoneness(java.lang.Long p_toasterDoneness)
   {
      toasterDoneness = p_toasterDoneness;
   }

   /**
    * Returns the value of the toasterToastType.
    *
    * @return  The current value of toasterToastType.
    */
   public java.lang.String getToasterToastType()
   {
      return toasterToastType;
   }

   /**
    * Sets toasterToastType to the specified value.
    *
    * @param x  The new value of toasterToastType.
    */
   public void setToasterToastType(java.lang.String p_toasterToastType)
   {
      toasterToastType = p_toasterToastType;
   }

}
