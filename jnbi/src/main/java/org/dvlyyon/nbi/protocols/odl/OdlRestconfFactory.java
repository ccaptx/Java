package org.dvlyyon.nbi.protocols.odl;

public class OdlRestconfFactory {
	public static OdlRestconfClientInf get(String type) {
		return new OdlRestconfClientImpl();
	}
}
