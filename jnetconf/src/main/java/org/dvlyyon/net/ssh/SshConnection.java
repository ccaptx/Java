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
package org.dvlyyon.net.ssh;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.ethz.ssh2.Connection;


/**
 * The SshConnection class represents a client connection to an SSH server. It mainly provides derived classes the ability to connect to an
 * SSH server and authenticate itself using keys or passwords.
 * <p>
 * The Ganymed SSH-2 stack is used by this class as the SSH client.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
public class SshConnection
{

   /** Logger for tracing */
   private final static Log s_logger = LogFactory.getLog(SshConnection.class);

   /** Default SSH port */
   public static final int DEFAULT_SSH_PORT = 22;

   /** The type of authentication */
   public enum AuthType
   {
      /** Private-public key-based */
      Key,

      /** Username/password -based */
      Password
   };

   /** The Ganymed SSH connection */
   private Connection m_nexus;


   /**
    * Creates an SshConnection.
    */
   public SshConnection()
   {
      //
   }
   
   protected void createConnection(final String address, final int port)
   {
       m_nexus = new Connection(address, port);
   }

   /**
    * Logs in to an SSH server, authenticating the user in the process. We first try key-based authentication (if available); if this fails,
    * we drop down to the simpler password-based authentication.
    *
    * @param address             Address of the SSH server to connect to.
    * @param port                SSH server port.
    * @param username            SSH user-name (used for login).
    * @param certFileName        Name of (private) certificate file used for authentication, if applicable.
    * @param passPhrase          Pass-phrase used to decrypt password if encrypted certificate file.
    * @param password            SSH password, if user-name/password login.
    * @return                    Type of authentication that succeeded.
    * @throws RuntimeException   if an error occurs authentication.
    */
   protected AuthType connectAndAuthenticate(final String address, final int port,
                                             final int socketTimeout, final int socketLinger,
                                             final String username, final String certFileName,
                                             final String passPhrase, final String password) throws RuntimeException
  {
      // First try with certificate
      boolean authenticated = false;
      AuthType ret = AuthType.Key;
      try
      {
         connect(address, port, socketTimeout, socketTimeout);
         authenticated = authenticateWithKey(username, certFileName, passPhrase);
      }
      catch (final Exception ex)
      {
         // If certificate authentication fails, disconnect and reconnect. Not sure why this is required, the GanyMed library likely has
         // some issues.
         disconnect();
      }
      // Key-based authentication failed (if any), go with password
      if (!authenticated)
      {
         ret = AuthType.Password;
         
         // not sure how we could have a null nexus at this point.  But I MAY need to conect.
         if (m_nexus == null)
         {
            connect(address, port, socketTimeout, socketTimeout);
         }
         authenticateWithPassword(username, password);
      }
      return ret;
  }

   /**
    * Establishes an SSH connection to the specified host and port.
    *
    * @param address             Host name or IP address of the device.
    * @param port                Port to connect to.
    * @param socketTimeout       Socket timeout to use (in milliseconds).
    * @param socketLinger        Socket linger time to use (in milliseconds).
    * @throws RuntimeException   if an error occurs during connection.
    */
   private void connect(final String address, final int port, int socketTimeout, int socketLinger) throws RuntimeException
   {
      try
      {
         s_logger.debug("Connecting to host: " + address + " on port: " + port);
         if (m_nexus==null)
            createConnection(address, port);
         //boolean linger = socketLinger != 0;
         //m_nexus.setSocketParameters(socketTimeout, linger, socketLinger);
         m_nexus.connect(null, socketTimeout, socketLinger);
         s_logger.debug("Connected successfully to " + address + ":" + port);
      }
      catch (final Exception ex)
      {
         // Translate to NeException
         disconnect();
         s_logger.error("Error connecting to device SSH server: " + ex.getMessage());
         throw new RuntimeException("Failed to connect to SSH server: " + address + " at port:" + port);
      }
   }

   /**
    * Logs in to an SSH server, authenticating the user using key-based authentication.
    *
    * @param username            SSH user-name (used for login).
    * @param certFileName        Name of (private) certificate file used for authentication.
    * @param passPhrase          Pass-phrase used to decrypt password if encrypted certificate file.
    * @return                    true if authentication succeeded, false if no certificate file provided.
    * @throws RuntimeException   if an error occurs during certificate authentication.
    */
   boolean authenticateWithKey(final String username, final String certFileName, final String passPhrase) throws RuntimeException
   {
      boolean isAuthenticated = false;
      // Try certificate-based authentication if a cert file has been specified
      if (certFileName != null && !certFileName.equals(""))
      {
         s_logger.debug("Trying to authenticate user:" + username + " using certificate at: " + certFileName);
         try
         {
            File certFile = new File(certFileName);
            String decryptPasswd = passPhrase;
            if (decryptPasswd != null && decryptPasswd.equals(""))
            {
               decryptPasswd = null;
            }
            isAuthenticated = m_nexus.authenticateWithPublicKey(username, certFile, decryptPasswd);
         }
         catch (final Exception ex)
         {
            s_logger.error("Error authenticating with key: " + ex.getMessage());
            if (s_logger.isDebugEnabled())
            {
               s_logger.error(ex, ex);
            }
         }
         // That's it for now - we've tried everything
         if (!isAuthenticated)
         {
            String msg = "Failed to authenticate user: " + username + " on host: " + m_nexus.getHostname() + " using certificates";
            s_logger.error(msg);
            throw new RuntimeException(msg);
         }
         s_logger.debug("Certificate authentication successful");
      }
      return isAuthenticated;
   }

   /**
    * Logs in to an SSH server, authenticating the user using the simpler password-based authentication.
    *
    * @param username            SSH user-name (used for login).
    * @param password            SSH password, password login.
    * @throws RuntimeException   if an error occurs during password authentication.
    */
   private void authenticateWithPassword(final String username, final String password) throws RuntimeException
   {
      boolean isAuthenticated = false;
      try
      {
         s_logger.debug("Using password authentication with username: " + username);
         isAuthenticated = m_nexus.authenticateWithPassword(username, password);
      }
      catch (final Exception ex)
      {
         s_logger.error("Error authenticating with password: " + ex.getMessage());
         if (s_logger.isDebugEnabled())
         {
            s_logger.error(ex, ex);
         }
      }
      // That's it for now - we've tried everything
      if (!isAuthenticated)
      {
         String msg = "Failed to authenticate user: " + username + " on host: " + m_nexus.getHostname();
         s_logger.error(msg);
         throw new RuntimeException(msg);
      }
      s_logger.debug("Passowrd Authentication successful");
   }

   /**
    * Returns the underlying Ganymed SSH connection.
    *
    * @return  SSH connection if established (or NULL if not).
    */
   protected Connection getConnection()
   {
      return m_nexus;
   }

   /**
    * Disconnects from the server - tears down the SSH connection.
    */
   public void disconnect()
   {
       s_logger.debug("disconnecting");
  

      if (m_nexus != null)
      {
          s_logger.debug("closing Ganymed connection");

         m_nexus.close();
         m_nexus = null;
      }
      else
          s_logger.debug("ganymed connection was null, can't close()");

   }

}