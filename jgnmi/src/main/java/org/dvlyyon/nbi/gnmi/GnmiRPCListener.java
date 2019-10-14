package org.dvlyyon.nbi.gnmi;

public interface GnmiRPCListener<T> {

	void onNext(T value, String string);

	void onError(Throwable t, String valueOf);

	void onCompleted(String valueOf);

}
