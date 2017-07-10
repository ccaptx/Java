package org.dvlyyon.nbi.protocols.cli;

import java.io.InputStream;
import java.io.OutputStream;

public interface TransceiverInf {
	public void setIOStream(InputStream in, OutputStream out);
	public void setEndPattern(String endPattern);
	public void setErrorPattern(String errPattern);
	public void setLoginOKPattern(String endPattern);
	public String  signedIn() throws TransceiverException;
	public String  sendCommand(String cmd) throws TransceiverException;
	public void close();
}
