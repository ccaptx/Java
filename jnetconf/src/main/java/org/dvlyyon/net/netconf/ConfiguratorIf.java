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
package org.dvlyyon.net.netconf;

import org.jdom2.Element;


/**
 * The ConfiguratorIf interface defines the NETCONF interface for all configuration operations, as specified by RFC 6241.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public interface ConfiguratorIf
{

   public enum EditOperation
   {
      None,
      Merge,
      Replace,
      Create,
      Delete,
      Remove
   };

   public enum TestOption
   {
      None,
      TestThenSet,
      Set,
      TestOnly
   };

   public enum ErrorOption
   {
      None,
      StopOnError,
      ContinueOnError,
      RollbackOnError
   };

   /**
    * Executes a <b>get</b> NETCONF request.
    * <p>
    * Note that only the XML tree form of the filter is supported - the XPATH form is not.
    *
    * @param filter              XML node representing the filter containing the search parameters.
    * @param xid                 Transaction Id or NULL if not in transaction context.
    * @return                    XML node representing the data returned from the call.
    * @throws RuntimeException   if an error occurred.
    */
   Element get(final Element filter, final String xid) throws RuntimeException;

   /**
    * Executes a <b>get-config</b> NETCONF request.
    * <p>
    * Note that only the XML tree form of the filter is supported - the XPATH form is not.
    *
    * @param configurationName   Name of the source configuration on the device.
    * @param filter              XML node representing the filter containing the search parameters.
    * @param xid                 Transaction Id or NULL if not in transaction context.
    * @return                    XML node representing the data returned from the call.
    * @throws RuntimeException   if an error occurred.
    */
   Element getConfig(final String configurationName, final Element filter, final String xid) throws RuntimeException;

   /**
    * Executes an <b>edit-config</b> NETCONF request.
    *
    * @param configurationName      Name of the target configuration being edited on the device.
    * @param xml                    XML node representing the data that is being modified.
    * @param defaultEditOperation   What to do when an error occurs on the device.
    * @param testOption             What to do when an error occurs on the device.
    * @param errorOption            What to do when an error occurs on the device.
    * @param xid                    Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException      if an error occurred.
    */
   void editConfig(final String configurationName, final Element xml, final EditOperation defaultEditOperation,
                   final TestOption testOption, final ErrorOption errorOption, final String xid) throws RuntimeException;

   /**
    * Executes a <b>copy-config</b> NETCONF request.
    *
    * @param sourceConfigurationName   Name of the source configuration on the device.
    * @param targetConfigurationName   Name of the target configuration on the device.
    * @param xid                       Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException         if an error occurred.
    */
   void copyConfig(final String sourceConfigurationName, final String targetConfigurationName, final String xid) throws RuntimeException;

   /**
    * Executes a <b>delete-config</b> NETCONF request.
    *
    * @param configurationName      Name of the target configuration to be deleted on the device.
    * @param xid                    Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException      if an error occurred.
    */
   void deleteConfig(final String configurationName, final String xid) throws RuntimeException;

   /**
    * Executes a <b>lock</b> NETCONF request.
    *
    * @param configurationName      Name of the target configuration to be locked out to prevent concurrent modifications.
    * @param xid                    Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException      if an error occurred.
    */
   void lock(final String configurationName, final String xid) throws RuntimeException;

   /**
    * Executes an <b>unlock</b> NETCONF request.
    *
    * @param configurationName      Name of the target configuration to be unlocked.
    * @param xid                    Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException      if an error occurred.
    */
   void unlock(final String configurationName, final String xid) throws RuntimeException;

   /**
    * Executes a <b>validate</b> operation; this validates the specified configuration for errors.
    *
    * @param configurationName      Name of the configuration to be validated.
    * @param xid                    Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException      if an error occurred or the configuration is invalid.
    */
   void validate(final String configurationName, final String xid) throws RuntimeException;

   /**
    * Executes an <b>commit</b> NETCONF request. This sets the running configuration to the candidate configuration.
    *
    * @param persistId              token that identifies this commit operation (similar to a transaction ID), or NULL
    *                               if the commit is restricted to the same session.
    * @param confirm                true if confirmed commit, false if immediate config.
    * @param timeoutInSeconds       timeout for confirmed commit operation.
    * @param xid                    Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException      if an error occurred or the configuration is invalid.
    */
   void commit(final String persistId, final boolean confirm, final int timeoutInSeconds, final String xid) throws RuntimeException;

   /**
    * Executes an <b>cancelCommit</b> NETCONF request. This cancels the current confirmed commit operation.
    *
    * @param persistId              Persistence (or transaction) identifier.
    * @param xid                    Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException      if an error occurred.
    */
   void cancelCommit(final String persistId, final String xid) throws RuntimeException;

   /**
    * Executes the <b>discard-changes</b> NETCONF request. This discards all changes to the candidate configuration and reverts
    * it to match the running configuration.
    *
    * @param xid                    Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException      if an error occurred.
    */
   void discardChanges(final String xid) throws RuntimeException;

   /**
    * Called to close a specific NETCONF session. However, since the underlying transport may use pooled connections, we cannot tie a
    * single session to this client; hence this call is not supported.
    *
    * @throws RuntimeException    Always.
    */
   void closeSession() throws RuntimeException;

   /**
    * Called to kill a specific NETCONF session. However, since the underlying transport may use pooled connections, we cannot tie a
    * single session to this client; hence this call is not supported.
    *
    * @param sessionId           Id of the session to the killed.
    * @param xid                 Transaction Id or NULL if not in transaction context.
    * @throws RuntimeException   Always.
    */
   public void killSession(String sessionId, final String xid) throws RuntimeException;

}
