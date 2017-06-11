package org.dvlyyon.nbi.cxt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.SimpleOptionHandler;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.dvlyyon.nbi.CliStub;
import org.dvlyyon.nbi.util.ThreadUtils;

public class TClient extends CliStub implements Runnable, TelnetNotificationHandler
{
    protected TelnetClient mTC = null;
    protected TerminalTypeOptionHandler mTtopt = null;
    protected EchoOptionHandler mEchoopt = null;
    protected SuppressGAOptionHandler mGaopt = null;
    protected Vector<String> mRemoteOutput = new Vector<String>();
    protected Boolean mStopped = false;
    protected String mStopReason = null;
    protected OutputStream mOutStream = null; 
    protected Thread mThread = null;
    protected String mIpAddr = null;
    protected String mPort = null;
    int mSize = 0; // in Bytes
    int mBufferSize = 1000000; // MB;
    public static final int mMaxBufferSize = 40; // in blocks of 100KB

    protected String mExpect = null;
    boolean mKeepExpect = false;
    
    protected void clearExpect() {
    	mKeepExpect = true;
    	mExpect = null;
    }
    
    protected String getExpect() {
    	mKeepExpect = false;
    	String str =  mExpect;
    	mExpect = null;
    	return str;
    }
    public void setBufferSize(int size) {
    	if (size <=0)
    		mBufferSize = 1000000;
    	else if (size < mMaxBufferSize) {
    		mBufferSize = size *  100000;
    	} else 
    		mBufferSize = mMaxBufferSize * 100000;
    }
   
     
    public String login(String ipAddr, String port) {
    	System.out.println("TClient.login >>>>>>>>: ipAddr= "+ipAddr+", port= "+port);
    	
    	boolean print = false;
    	if (mTC != null) {
    		synchronized(mStopped) {
    			if (!mStopped)
    				return "Session is still on ";
    		}
    	}
    	if (print) System.out.println("Before creating the client");
        mTC = new TelnetClient();
        mTtopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        mEchoopt = new EchoOptionHandler(true, false, true, false);
        mGaopt = new SuppressGAOptionHandler(true, true, true, true);
    	if (print) System.out.println("After creating the client");
        
        try {
        	mTC.addOptionHandler(mTtopt);
        	mTC.addOptionHandler(mEchoopt);
        	mTC.addOptionHandler(mGaopt);
        } catch (Exception e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
            return "Error registering option handlers: " + e.getMessage();
        }
        int remote_port = 23;
        if (port != null) {
        	try {
        		remote_port = Integer.parseInt(port);
        	} catch(Exception e) {
        		remote_port = 23;
        	}
        }
    	if (print) System.out.println("After setting options");
        
        try {
        	mTC.connect(ipAddr, remote_port);
        	mIpAddr = ipAddr;
        	mPort = Integer.toString(remote_port);
        } catch(Exception e) {
        	return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port+": "+e.toString();
        }
        if (!mTC.isConnected()) return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port;
    	if (print) System.out.println("After connecting");
        mStopped = false;
        mThread = new Thread (this);
        mThread.setName("TClient:"+ipAddr+":"+remote_port);
        mThread.start();
        mOutStream = mTC.getOutputStream();
        
        return "OK";
    }
    

    /***
     * Callback method called when TelnetClient receives an option
     * negotiation command.
     * <p>
     * @param negotiation_code - type of negotiation command received
     * (RECEIVED_DO, RECEIVED_DONT, RECEIVED_WILL, RECEIVED_WONT)
     * <p>
     * @param option_code - code of the option negotiated
     * <p>
     ***/
//    @Override
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
    
    public String getOutput() {
    	
    	return this.getOutput(1);
    	/*
    	synchronized(mRemoteOutput) {
    		if (mRemoteOutput.size() == 0) return null;
    		String ret = mRemoteOutput.firstElement();
    		for (int i=1; i<mRemoteOutput.size(); i++) ret += mRemoteOutput.elementAt(i);
    		mRemoteOutput.removeAllElements();
    		return ret;
    	}*/
    }
    
    public String getOutput(int fifty_ms) {
    	int num = 0;
    	while ((fifty_ms == 0) || (fifty_ms > 0 && num<fifty_ms)) {
	    	synchronized(mRemoteOutput) {
	    		if (mRemoteOutput.size() > 0) {
		    		String ret = mRemoteOutput.firstElement();
		    		for (int i=1; i<mRemoteOutput.size(); i++) ret += mRemoteOutput.elementAt(i);
		    		mRemoteOutput.removeAllElements();
		    		mSize = 0;
	    		
	    		return ret;
	    		}
	    	}
	    	ThreadUtils.sleep_ms(50);
	    	num++;
    	}
    	return null;
    	
    }    
    
    public boolean expect(String key, int seconds) {
    	return this.expect(key, seconds, false);
    }
    public boolean expect(String key, int seconds, boolean print) {
    	if (key == null || key.trim().equals("")) return true;
    	System.out.println("Expecting '"+key+"' ...................................................");
    	int num = 0;
    	int pos = 0;
    	String text = "";
    	while ((seconds == 0) || (seconds > 0 && num<seconds)) {
	    	synchronized(mRemoteOutput) {
	    		if (mRemoteOutput.size() > 0) {
		    		for (int i=pos; i<mRemoteOutput.size(); i++) {
		    			String ret = mRemoteOutput.elementAt(i);
		    			text += ret;
		    			if (mKeepExpect) {
		    				if (mExpect == null) mExpect = ret; else mExpect += ret;
		    			}
		    			if (print) System.out.println("TClient.expect: msg["+i+"]= "+ret);
		    			if (text != null && text.indexOf(key)>=0) {
		    				for (int j=0; j<=i; j++) mRemoteOutput.remove(0);
		    				return true;
		    			}
		    		}	
		    		pos = mRemoteOutput.size();
	    		}
	    	}
	    	ThreadUtils.sleep(2);
	    	num += 2;
    	}
    	return false;
    	
    }
    
    public void clearOutput() {
    	synchronized(mRemoteOutput) {
    		mRemoteOutput.removeAllElements();
    		mSize = 0;
    	}    	
    }
    
    /**
     * http://stackoverflow.com/questions/6410579/how-to-disable-echo-when-sending-a-terminal-command-using-apache-commons-net-teln
     */
    public void sendEnableWillEcho() {
    	synchronized(mOutStream) {
    		try {
    			mOutStream.write(0xFF);
    			mOutStream.write(0xFC);
    			mOutStream.write(0x01);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    public void sendSupressWillEcho() {
    	synchronized(mOutStream) {
    		try {
    			mOutStream.write(0xFF);
    			mOutStream.write(0xFB);
    			mOutStream.write(0x01);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}    	
    }
    
    public void sendSupressDoEcho() {
    	synchronized(mOutStream) {
    		try {
    			mOutStream.write(0xFF);
    			mOutStream.write(0xFD);
    			mOutStream.write(0x2D);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}    	
    }
    
    
    public String sendCommandLine(String cmdline) {
    	synchronized(mOutStream) {
    		if (cmdline == null) return "OK";
    		if (!mTC.isConnected()) return "TClient.sendCommandLine: connection is broken";
    		char[] ch = cmdline.toCharArray();
    		byte[] buf = new byte[ch.length];
    		for (int i=0; i<ch.length; i++) buf[i] = (byte)ch[i];
            try
            {
            	mOutStream.write(buf, 0 , buf.length);
                    mOutStream.flush();                   
            }
            catch (Exception e)
            {
                    return "sending command Exception - "+e.toString();
            }
    		
    	}
    	return "OK";
    }
    
    public void stop() {
    	if (mThread != null) {
    		try {
    			System.out.println("TClient.stop: stopping thread "+mThread.getName());
    			synchronized(mStopped) {
    				mStopped = true;
    				mStopReason = "Stopped normally thread "+mThread.getName();
    			}
    			mThread.stop();
    		} catch (Exception e) {
    			System.out.println("TClient.stop: failed to stop thread "+mThread.getName()+" - "+e.toString());
    		}
    		mThread = null;
    	}
    	ThreadUtils.sleep(5);
    	if (mTC != null) {
    		boolean stopped = false;
    		int num = 0;
    		while (!stopped && num < 5) {
    			num++;
	    		try {
	    			if (mTC.isConnected()) {
		    			System.out.println("TClient.stop: disconnecting connection "+mIpAddr+":"+mPort);
	    				mTC.disconnect();
	    			} else {
		    			System.out.println("TClient.stop: connection "+mIpAddr+":"+mPort+" already disconnected");	    				
	    			}
	    		    //mTC.stopSpyStream();
	    			stopped = true;
	    		} catch(Exception e) {
	    			if (num >= 5) {
	    				StackTraceElement[] s = e.getStackTrace();
	    				String trc = e.toString();
	    				for (int i=0; i<s.length; i++) {
	    					trc += "\n"+s[i].toString();
	    				}
	    				System.out.println("TClient.stop: failed to disconnect connection "+mIpAddr+":"+mPort+" - "+trc);
	    			} else
	    				ThreadUtils.sleep(2);
	    		}
    		}
    	}
    }

    /***
     * Reader thread.
     * Reads lines from the TelnetClient and echoes them
     * on the screen.
     ***/
//    @Override
    public void run()
    {
        InputStream instr = mTC.getInputStream();
        String msg = null;
        try
        {
            byte[] buff = new byte[1024];
            int ret_read = 0;

            do
            {
                ret_read = instr.read(buff);
                if(ret_read > 0)
                {
                	synchronized(mRemoteOutput) {
                		mRemoteOutput.add(new String(buff, 0, ret_read));
                		mSize += ret_read;
                		while (mSize > mBufferSize) { 
                			String str = mRemoteOutput.remove(0);
                			mSize -= str.length();
                		}
                	}
                    //System.err.print();
                }
            }
            while (ret_read >= 0);
        }
        catch (IOException e)
        {
        	msg = "Exception while reading socket:" + e.getMessage();
            System.err.println(msg);
        }

        try
        {
        	mTC.disconnect();
        	synchronized(mStopped) {
        		mStopped = true;
        		mStopReason = msg+", connection disconnected";
        	}
        }
        catch (IOException e)
        {
            System.err.println("Exception while closing telnet:" + e.getMessage());
            synchronized (mStopped) {
            	mStopped = true;
            	mStopReason = "Exception while closing telnet:" + e.getMessage();
            }
        }
    }
    
    public boolean isConnected() {
    	if (mTC != null) {
    		return mTC.isConnected();
    	}
    	return false;
    }
    
}
