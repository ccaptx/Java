package org.dvlyyon.nbi.gnmi;

public interface ResponseListener<T> {
	public void onNext(T value);
}
