package org.dvlyyon.study.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetAddress {
	public InetAddress getLocalHostLANAddress() {
	    try {
	        InetAddress candidateAddress = null;
	        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
	            NetworkInterface iface = ifaces.nextElement();
	            for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
	                InetAddress inetAddr = inetAddrs.nextElement();
	                if (!inetAddr.isLoopbackAddress()) {
	                    if (inetAddr.isSiteLocalAddress()) {	                  
	                        return inetAddr;
	                    } else if (candidateAddress == null) {
	                        candidateAddress = inetAddr;
	                    }
	                }
	            }
	        }
	        if (candidateAddress != null) {
	            return candidateAddress;
	        }
	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        return jdkSuppliedAddress;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}	

	public static void main(String [] argv) {
		NetAddress addr = new NetAddress();
		System.out.println(addr.getLocalHostLANAddress().getHostAddress());
	}
}
