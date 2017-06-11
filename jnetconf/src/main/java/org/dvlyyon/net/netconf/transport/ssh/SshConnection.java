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
package org.dvlyyon.net.netconf.transport.ssh;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.util.XmlUtils;
import org.dvlyyon.net.netconf.Capabilities;
import org.dvlyyon.net.netconf.transport.HelloResponseProcessorIf;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;


/**
 * The SshConnection class represents a NETCONF SSH connection. There are two types of SSH connections - synchronous (used for regular
 * NETCONF RPC calls to a device) and asynchronous (used to handle notifications from a device). This class provides the basic code to
 * support both types. The RFC it implements is 6242.
 * <p>
 * The Ganymed SSH-2 stack is used by this class as the SSH client.
 *
 * @author  Subramaniam Aiylam
 * @since   1.6
 */
abstract class SshConnection extends org.dvlyyon.net.ssh.SshConnection
{

   /** Logger for tracing */
   private final static Log s_logger = LogFactory.getLog(SshConnection.class);

   /** Default NETCONF SSH port */
   private static final int DEFAULT_SSH_PORT = 830;

   /** Pattern that terminates a NETCONF SSH message */
   private static final String SSH_NETCONF_TERMINATOR = "]]>]]>";

   /** End of line marker */
   private static final String s_eol = System.getProperty("line.separator");

   /** The application's hello response processor */
   private HelloResponseProcessorIf m_helloResponseProcessor;

   /** The Ganymed SSH connection */
   private Connection m_nexus;

   /** The Ganymed SSH session */
   private Session m_session;

   /** The input stream associated with this connection (used to receive data) */
   private BufferedInputStream m_inputStream;

   /** The output stream associated with this connection (used to send data) */
   private BufferedOutputStream m_outputStream;

   /** Capabilities that the device supports */
   private Capabilities m_deviceCaps = new Capabilities();

   private String subsystem;
   
   /** this will be set true by disconnect rendering this connection unusable. */
   private boolean shutdown = false;

   /**
    * Creates an SshConnection with the specified parameters and connects to the SSH server on the device.
    *
    * @param connectionProperties   Properties used to set up the SSH connection. These are:<ol>
    *                               <li>host - Name or IP address of device</li>
    *                               <li>port - Port over which to do SSH</li>
    *                               <li>username - SSH login user name</li>
    *                               <li>password - SSH password (for user or to decrypt certificate)</li>
    *                               <li>certificate - Fully-qualified path-name of file containing private-key certificate.
    *                                   Note that the file MUST be in (OpenSSH) PEM format - either DSA or RSA.</li>
    *                               <li>socketTimeout - socket timeout in seconds</li>
    *                               <li>netconfTraceFile - Reference to a LoggerIf used for tracing.</li>
    *                               <li>subsystem - SSH subsystem - defaults to "netconf" if not specified.</li>
    *                               </ol>
    * @param hrp                    The application's HelloResponseProcessor.
    * @throws RuntimeException      if an error occurred during creation.
    */
   SshConnection(Properties connectionProperties, final HelloResponseProcessorIf hrp) throws RuntimeException
   {
       this(connectionProperties, hrp, true);
       
   }
   

   protected String address;
   protected String portStr;
   protected String username;
   protected String password;
   protected String certificateFileName;
   protected String passPhrase;
   protected int socketTimeout;
   protected int port;
   
   SshConnection(Properties connectionProperties, final HelloResponseProcessorIf hrp, boolean connectNow) throws RuntimeException
   {
      m_helloResponseProcessor = hrp;
      address = connectionProperties.getProperty("host", "localhost");
      portStr = connectionProperties.getProperty("port", "" + DEFAULT_SSH_PORT);
      username = connectionProperties.getProperty("username", "admin");
      password = connectionProperties.getProperty("password");
      certificateFileName = connectionProperties.getProperty("certificate");
      passPhrase = connectionProperties.getProperty("passphrase");
      subsystem = connectionProperties.getProperty("subsystem");
      if (subsystem == null)
      {
         subsystem = "netconf";
      }
      final String socketTimeoutStr = connectionProperties.getProperty("socketTimeout", "0");
      port = DEFAULT_SSH_PORT;
      socketTimeout = 0;
      try
      {
         port = Integer.parseInt(portStr);
         socketTimeout = Integer.parseInt(socketTimeoutStr) * 1000;
      }
      catch (final Exception ex)
      {
         s_logger.warn("Error parsing port parameter: " + portStr + "; using default port: " + DEFAULT_SSH_PORT);
      }
      
      createConnection(address, port);

      if (connectNow)
      {
         connect();
      }
   }
   
   public void connect()
   {
        AuthType authType = super.connectAndAuthenticate(address, port, socketTimeout, 0,
               username, certificateFileName, passPhrase, password);
        s_logger.debug("Connected to: " + address + ":" + port + " using " + authType + " authentication");
        m_nexus = super.getConnection();
        
        try
        {
            m_session = m_nexus.openSession();
            m_session.startSubSystem(subsystem);
            m_inputStream = new BufferedInputStream(m_session.getStdout()); //new BufferedInputStream(new StreamGobbler(m_session.getStdout()));
            m_outputStream =  new BufferedOutputStream((m_session.getStdin()));
        }
        catch (final Exception ex)
        {
        // Translate to NetconfException
        disconnect();
        s_logger.error("Error creating streams for device: " + ex.getMessage());
        throw new RuntimeException("Failed to create streams to SSH server: " + m_nexus.getHostname() + " at port:" + m_nexus.getPort());
        }
        
        if (m_nexus!=null)  // if I am connected
           establishSession();
        
        // did shutdown happen while I was stuck waiting?
        if (shutdown)
        {
            s_logger.warn("Disconnect detected after creating streams.  disconnecting");
            disconnect();
        }

   }
   
   
   /**
    * Establishes a session with the NETCONF device at the other end, exchanging "hello" messages in the process.
    *  If you sent in  a "true" for connectNow on the constructor (or did not specify), you will be connected automatically.  
    *  DO NOT call this method in that case
    * @param subsystem           SSH subsystem.
    * @throws RuntimeException   if a communications error occurred.
    */
   private void establishSession() throws RuntimeException
   {
      try
      {
         exchangeHellos();
      }
      catch (final Exception ex)
      {
         // Translate to NetconfException
         disconnect();
         s_logger.error("Error connecting to device SSH server: " + ex.getMessage());
         throw new RuntimeException("Failed to connect to SSH server: " + m_nexus.getHostname() + " at port:" + m_nexus.getPort());
      }
   }

   /**
    * Returns the capabilities of the device.
    *
    * @return  Device capabilities.
    */
   public Capabilities getDeviceCapabilities()
   {
      return m_deviceCaps;
   }

   /**
    * Sends the <i>hello</i> handshake, which is the first data exchange on a new NETCONF connection.
    */
   private void exchangeHellos()
   {
      // Send the "hello" message to the other end
      Namespace ns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");
      final Element hello = new Element("hello", ns);
      final Element caps = new Element("capabilities", ns);
      hello.addContent(caps);
      addCapabilities(caps, ns);
      
      // send this way to force non-chunked.  Hello's must be non-chunked
      streamDataOutToWire(hello, false, false);
      streamDataInFromWire(false, false);

     // syncSend(hello, true, false);
   }

   /**
    * Adds the client's capabilities to the specified XML node. Currently, we support only the base capability.
    *
    * @param root    XML element to add the capabilities to.
    * @param ns      XML namespace used for the hello message.
    */
   private void addCapabilities(final Element root, final Namespace ns)
   {
      final Element cap = new Element("capability", ns);
      cap.setText("urn:ietf:params:netconf:base:1.1");
      root.addContent(cap);
      // TODO: Any other capabilities to add ? Should we give the subclass a chance to add its own?
   }

   /**
    * Disconnects from the device. Tears down the SSH connection.
    */
   public void disconnect()
   {
       s_logger.debug("NC ssh connection disconnect called");
       
       // set shutdown in case there is a thread in connect().
      shutdown=true;
       
      if (m_session != null)
      {
          s_logger.debug("NC sshconnection - closing session");

         // TODO: Should we send an RPC <close-session> message?
         // Guess not, since closing the connection is pretty explicit
         m_session.close();
         m_session = null;
      }
      else
          s_logger.debug("NC sshconnection  - session null, cant close.");

      
      try
      {
          if (m_inputStream!=null)
          {
              m_inputStream.close();   
              s_logger.debug("closed input stream");
          }

          
          if (m_inputStream!=null)
          {
              m_outputStream.close();
              s_logger.debug("closed output stream");
          }

      }
      catch(Exception e)
      {
      }
      finally
      {
          m_inputStream = null;
          m_outputStream = null;
     }
      
      super.disconnect();
   }

   /**
    * Sends the specified XML synchronously over the wire to the NETCONF-capable device, tracing the
    * over=the-wire traffic.
    *
    * @param data                XML to be sent to the device.
    * @param waitForResponse     true to wait for a response from the device, false if no response is expected.
    * @throws RuntimeException   if an error occurs during the data transfer.
    */
   protected void syncSend(Element data, boolean waitForResponse) throws RuntimeException
   {
      syncSend(data, waitForResponse, true);
   }

   /**
    * Sends the specified XML synchronously over the wire to the NETCONF-capable device.
    *
    * @param data                XML to be sent to the device.
    * @param waitForResponse     true to wait for a response from the device, false if no response is expected.
    * @param traceWireData       true to trace the over-the-wire data, false if no tracing.
    * @throws RuntimeException   if an error occurs during the data transfer.
    */
   protected void syncSend(Element data, boolean waitForResponse, boolean traceWireData) throws RuntimeException
   {
      streamDataOutToWire(data, traceWireData);
      if (waitForResponse)
      {
         streamDataInFromWire(traceWireData);
      }
   }

   /**
    * Sends the specified data over the wire to the device, adding the appropriate protocol-specific termination.
    * Uses the chunked format or the EOM terminator, depending upon the device's capabilities.
    *
    * @param data                XML to be sent over the wire.
    * @param traceWireData       true to trace the over-the-wire data, false if no tracing.
    * @throws RuntimeException   if an error occurred during the data write.
    */
   protected void streamDataOutToWire(Element data, boolean traceWireData) throws RuntimeException
   {
       if (!m_deviceCaps.supportsChunkedFraming())
          streamDataOutToWire(data, traceWireData, false);
       else
           streamDataOutToWire(data, traceWireData, true);

   }
   
   
   protected void streamDataOutToWire(Element data, boolean traceWireData, boolean chunked) throws RuntimeException
   {
      try
      {
         StringBuilder buf = new StringBuilder("");
         // Dump the thing out to the output buffer
         if (!chunked)
         {
            buf.append(XmlUtils.toXmlString(data, false, true));
            buf.append(SSH_NETCONF_TERMINATOR);            
         }
         else
         {
            String dataStr = XmlUtils.toXmlString(data, false, true);
            long count = dataStr.length();
            buf.append("\n#");
            buf.append(""+count);
            buf.append("\n");
            buf.append(dataStr);
            buf.append("\n##\n");
         }
         //if (s_logger.isDebugEnabled())
         //{
         //   s_logger.debug("Request: " + buf);
         //}
         byte[] stringAsBytes = buf.toString().getBytes();
         m_outputStream.write(stringAsBytes);
         m_outputStream.flush();
         // Log the request if desired
         if (traceWireData)
         {
            log(buf.toString(), false);
         }
      }
      catch (final Exception ex)
      {
         if (s_logger.isDebugEnabled())
         {
            s_logger.error(ex, ex);
         }
         throw new RuntimeException("An error occured in the transport layer: " + ex.getMessage());
      }
   }

   /**
    * Logs the specified XML to the trace file.
    *
    * @param rawData       The raw data on the wire which will be logged.
    * @param isResponse    true if the XML is a response, false if it is a request.
    */
   protected void log(String rawData, boolean isResponse)
   {
      if (s_logger.isDebugEnabled())
      {
         StringBuilder buf = new StringBuilder("");
         Date ts = new Date(System.currentTimeMillis());
         String prefix = isResponse ? "Response received " : "Request sent" ;
         buf.append(prefix);
         buf.append(" (at: " + ts + ")" + s_eol);
         //buf.append(XmlUtils.toXmlString(xml, true) + s_eol);
         buf.append(rawData + s_eol);
         s_logger.debug(buf);
      }
   }

   /**
    * Reads data sent from the device over the wire, looking for the protocol-specific termination to detect packet boundaries.
    *
    * @param traceWireData       true to trace the over-the-wire data, false if no tracing.
    * @throws RuntimeException   if an error occurred during the data read.
    */
   protected void streamDataInFromWire(boolean traceWireData) throws RuntimeException
   {
       if (m_deviceCaps.supportsChunkedFraming())
           streamDataInFromWire(traceWireData, true);
       else
           streamDataInFromWire(traceWireData, false);
   }
   
   
   protected void streamDataInFromWire(boolean traceWireData, boolean chunked) throws RuntimeException
   {
      try
      {
         StringBuilder logStr = new StringBuilder();
         String responseStr = null;
         if (!chunked)
         {
            responseStr = getDataInEndOfMessageFormat(logStr);
         }
         else
         {
            responseStr = getDataInChunkedFormat(logStr);
         }
         // Make XML out of the string
         Document responseDoc = new SAXBuilder().build(new java.io.StringReader(responseStr));
         Element response = responseDoc.getRootElement();
         // Log the request if desired
         if (traceWireData)
         {
            log(logStr.toString(), true);
         }
         processResponseInternal(response);
         // Dump to debug (if set)
         //if (s_logger.isDebugEnabled())
         //{
         //   s_logger.debug("Response: " + XmlUtils.toXmlString(response));
         //}
      }
      catch (final Exception ex)
      {
         if (s_logger.isDebugEnabled())
         {
            s_logger.error(ex, ex);
         }
         throw new RuntimeException("An error occured in the transport layer: " + ex.getMessage());
      }
   }

   /**
    * Processes the input data stream for messages using the (older) EndOfMessage format - as specified by the now-
    * obsoleted RFC 4742.
    *
    * @param logStr        StringBuilder used to accumulate trace data.
    * @return              String containing response message.
    * @throws Exception    if an error occurred.
    */
   String getDataInEndOfMessageFormat(StringBuilder logStr) throws Exception
   {
      Terminator terminator = new Terminator();
      // Wait for the response
      byte[] byteBuff = new byte[1024];
      int count = 0;
      StringBuilder respStr = new StringBuilder("");
      while (true)
      {
         // append
         int c = m_inputStream.read();
         logStr.append((char)c);
         if (c != -1)
         {
            //byte b = (byte) c;
            byte[] b = terminator.filter(c);
            if (terminator.isAtEnd())
            {
               String appendStr = new String(byteBuff, 0, count);
               respStr.append(appendStr);
               break;                  
            }
            if (b != null)
            {
               for (int i=0; i< b.length; i++)
               {
                  byteBuff[count] = b[i];
                  count++;
                  if (count == 1024)
                  {
                     String appendStr = new String(byteBuff, 0, count);
                     respStr.append(appendStr);
                     count = 0;
                  }
               }
            }
         }
         else
         {
            break;
         }
      }
      return respStr.toString();
   }

   /**
    * Processes the input data stream for messages using the (newer) chunked framing format - as specified by the
    * newer RFC 6262.
    *
    * @param logStr        StringBuilder used to accumulate trace data.
    * @return              String containing response message.
    * @throws Exception    if an error occurred.
    */
   private String getDataInChunkedFormat(StringBuilder logStr) throws Exception
   {
	   StringBuilder respStr = new StringBuilder("");
	   try {
	      byte[] buf = new byte[1024];
	      long count = getCount(m_inputStream, logStr);
	      long toRead = count;
	      while (toRead > 0) {
	    	  int num = 0;
	    	  if (toRead >= 1024)
	    		  num = m_inputStream.read(buf,0,1024);
	    	  else 
	    		  num = m_inputStream.read(buf,0,(int)toRead);
	    	  String appendStr = new String(buf, 0, num);
	    	  respStr.append(appendStr);
	    	  logStr.append(appendStr);
	    	  toRead -= num;
	      }
	      count = getCount(m_inputStream, logStr);
	   } catch (Exception ex) {
		   if (this.s_logger.isDebugEnabled()) {
			   s_logger.debug("response:" + logStr.toString());
			   s_logger.error(ex, ex);
			   throw ex;
		   }
	   }
	   return respStr.toString();
//      while (count > 0)
//      {
//         int iterations = (int) count/1024;
//         int extra = (int) count%1024;
//         for (int i=0; i<iterations; i++)
//         {
//            int num = m_inputStream.read(buf,0,1024);
//            System.out.println("number of byte read:" + num);
//            String appendStr = new String(buf, 0, 1024);
//            respStr.append(appendStr);
//            logStr.append(appendStr);
//         }
//         if (extra > 0)
//         {
//            for (int i=0; i<extra; i++)
//            {
//               m_inputStream.read(buf, i, 1);
//            }
//            String appendStr = new String(buf, 0, extra);
//            respStr.append(appendStr);
//            logStr.append(appendStr);
//         }
//         System.out.println(logStr);
//         count = getCount(m_inputStream, logStr);
//      }
   }

   /**
    * Given an input stream (in the chunked framing format), extracts the chunk size.
    *
    * @param inputStream   Input stream containing response data.
    * @param logStr        StringBuilder used to accumulate trace data.
    * @return              Chunk size (in bytes), or -1 for end of message.
    * @throws Exception    if an error occurred.
    */
   private long getCount(BufferedInputStream inputStream, StringBuilder logStr) throws Exception
   {
      long count = -1;
      StringBuilder countStr = new StringBuilder("");
      int in = inputStream.read();
//      if (in != -1)
//      {
//         throw new Exception("Got unexpected end of stream");
//      }
      logStr.append((char)in);
      if (in != '\n')
      {
         throw new Exception("Expected \\n; got: " + in);
      }
      in = inputStream.read();
      logStr.append((char)in);
      if (in != '#')
      {
         throw new Exception("Expected #; got: " + in);
      }
      in = inputStream.read();
      while (in != '#' && in != '\n')
      {
         char b = (char) in;
         countStr.append(b);
         logStr.append(b);
         in = inputStream.read();
      }
      logStr.append((char)in);
      if (in == '#')
      {
         // We are done with the chunks - read the last line-feed
         in = inputStream.read();
         logStr.append((char)in);
      }
      else
      {
         count = Long.parseLong(countStr.toString());
      }
      return count;
   }

   /**
    * Handles the response to an RPC request. A response can be one of two types:<ol>
    * <li>A "hello" message (in response to a connection being created)</li>
    * <li>A regular "rpc-reply" message (in response to a NETCONF rpc request)</li>
    * <ol>
    * <p>
    * If a hello is received, the registered HelloResponseProcesser (if any) is invoked.
    *
    * @param response            XML representation of a response message from the device.
    * @throws RuntimeException   if an error occurred processing the response.
    */
   private void processResponseInternal(Element response) throws RuntimeException
   {
      // Depending upon the response type, handle it
      if (!"hello".equals(response.getName()))
      {
         s_logger.debug("Got a non-hello response");
         // Any non-hello messages just call the derived implementation
         handleResponse(response);
      }
      else
      {
         s_logger.debug("Got a hello response, getting device capabilities");
         Element capsRoot = response.getChild("capabilities", HelloResponseProcessorIf.CAPABILITIES_NAMESPACE);
         m_deviceCaps.fromNetconfXml(capsRoot);
         // Hello messages are passed down to the derived class for any decision making
         handleHelloResponse(response);
         if (m_helloResponseProcessor != null)
         {
            // and also sent to the client registered by the high-level app
            m_helloResponseProcessor.processHelloResponse(response);
         }
      }
   }

   /**
    * Called to handle a hello message received from the device.
    *
    * @param response   the XML message representing the "hello" message.
    */
   protected abstract void handleHelloResponse(Element response);

   /**
    * Called to handle a non-hello response (e.g. a regular NETCONF rpc-reply) message from the device. 
    *
    * @param response   the XML message representing the NETCONF response.
    */
   protected abstract void handleResponse(Element response);


   /**
    * The Terminator class hunts for the NETCONF SSH termination sequence in an incoming stream. When reading an XML stream
    * from the NETCONF device, filter every character that comes in through this class in order to determine if we have
    * reached the end of the transmission.
    * 
    * @author  Subramaniam Aiylam
    * @since   1.6
    */
   private static class Terminator
   {
      /** Accumulated bytes so far that are part of a potential termination */
      private byte[] m_accumulator = new byte[6];

      /** Index into the accumulated bytes */
      private int m_index = 0;

      /** True if the accumulated bytes indicate the end of a NETCONF SSH transmission */
      private boolean m_endOfTransmission;

      /**
       * Filters the character passed in to determine whether it is part of a termination sequence.
       *
       * @param input               Input character that is part of the NETCONF stream.
       * @return                    Byte array that contains the set of characters that are part of the sequence and
       *                            <b>NOT</b> part of the termination sequence. A NULL may be returned here if there is
       *                            a potential termination, but we are not quite sure yet.
       * @throws RuntimeException   if an invalid index is encountered.
       */
      byte[] filter(int input) throws RuntimeException
      {
         byte[] ret = null;
         boolean accumulate = false;
         switch (m_index)
         {
            case 0:
               if (input == ']')
               {
                  accumulate = true;
               }
               break;
            case 1:
               if (input == ']')
               {
                  accumulate = true;
               }
               break;
            case 2:
               if (input == '>')
               {
                  accumulate = true;
               }
               break;
            case 3:
               if (input == ']')
               {
                  accumulate = true;
               }
               break;
            case 4:
               if (input == ']')
               {
                  accumulate = true;
               }
               break;
            case 5:
               if (input == '>')
               {
                  accumulate = true;
                  m_endOfTransmission = true;
               }
               break;
            default:
               throw new RuntimeException("Invalid index value: " + m_index);
         }
         if (accumulate)
         {
            m_accumulator[m_index++] = (byte) input;
         }
         else
         {
            // Return all the bytes (the accumulated ones plus the one passed in)
            ret = new byte[m_index+1];
            for (int i=0; i<m_index; i++)
            {
               ret[i] = m_accumulator[i];
            }
            ret[m_index] = (byte) input;
            m_index = 0;
         }
         return ret;
      }

      /**
       * Returns the current termination state of the input stream.
       *
       * @return  true if we have detected the termination sequence, false if not.
       */
      boolean isAtEnd()
      {
         return m_endOfTransmission;
      }
   }

}
