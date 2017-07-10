package org.dvlyyon.nbi.protocols.cli;

public class TransceiverFactory {
	public static TransceiverInf get(String className) {
		return new SimpleTransceiver();
	}
}
