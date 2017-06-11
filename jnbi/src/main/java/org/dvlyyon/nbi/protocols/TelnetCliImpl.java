package org.dvlyyon.nbi.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

public abstract class TelnetCliImpl extends CommonCliImpl implements TelnetNotificationHandler {
	
	protected TelnetClient telnetClient = null;

	public String login(String hostname, int port, RunState state) {
        telnetClient = new TelnetClient();
        
        TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, true);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(false, false, false, false);
        StreamPair pair=null;
        try {
	        telnetClient.addOptionHandler( ttopt );
	        telnetClient.addOptionHandler( echoopt );
	        telnetClient.addOptionHandler( gaopt );
	        
	        telnetClient.connect(hostname, port);
	        InputStream is =  new FromNetASCIIInputStream( telnetClient.getInputStream() ); // null until client connected
	        OutputStream os = new ToNetASCIIOutputStream( telnetClient.getOutputStream() );
	        
	        pair = new StreamPair(is, os) {
	            public void close() {
	                //super.close();
	                try {
	                    if( telnetClient != null ) telnetClient.disconnect();
	                }catch(IOException ioe) {
	                    
	                }
	            }
	        };
        } catch (Exception e) {
        	state.setResult(State.EXCEPTION);
        	state.setExp(e);
        	return "Can't connect to remote IP "+hostname+" with port "+port+": "+e.toString();
        }
        state.setResult(State.NORMAL);
        stopped = false;
		consumer = new BlockingConsumer(pair);
        consumerThread = new Thread(consumer);
        consumerThread.setName("TelnetClient");
        consumerThread.setDaemon(true);
        consumerThread.start();        
        return "OK";
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void beforeSendCommands(String cmds) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getErrorInfo(String str, String cmds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void receivedNegotiation(int negotiation_code, int option_code)
    {
        String command = null;
        if(negotiation_code == TelnetNotificationHandler.RECEIVED_DO)
        {
            command = "DO";
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_DONT)
        {
            command = "DONT";
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_WILL)
        {
            command = "WILL";
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_WONT)
        {
            command = "WONT";
        }
        System.err.println("Received " + command + " for option code " + option_code);
   }

}
