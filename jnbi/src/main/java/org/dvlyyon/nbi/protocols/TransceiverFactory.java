package org.dvlyyon.nbi.protocols;

public class TransceiverFactory {
	public static TransceiverInf get(String className) {
		return new SimpleTransceiver();
	}
}
