package org.dvlyyon.nbi.gnmi;

public interface GnmiStreamListenerInf<T> {

	void onNext(T value, String valueOf);

	void onError(Throwable t, String valueOf);

	void onCompleted(String valueOf);

}
