package org.dvlyyon.nbi.protocols;

import org.dvlyyon.nbi.protocols.odl.OdlRestconfClientInf;
import org.dvlyyon.nbi.protocols.odl.OdlRestconfClientImpl;

public class OdlRestconfFactory {
	public static OdlRestconfClientInf get(String type) {
		return new OdlRestconfClientImpl();
	}
}
