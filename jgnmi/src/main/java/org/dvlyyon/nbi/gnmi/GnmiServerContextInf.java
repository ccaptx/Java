/**
 * 
 */
package org.dvlyyon.nbi.gnmi;

/**
 * @author david
 *
 */
public interface GnmiServerContextInf extends GnmiCommonContextInf{
	public String	getServerKey();
	public boolean  requireClientCert();
}
