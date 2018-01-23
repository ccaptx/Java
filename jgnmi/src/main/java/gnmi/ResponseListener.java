package gnmi;

public interface ResponseListener<T> {
	public void onNext(T value);
}
