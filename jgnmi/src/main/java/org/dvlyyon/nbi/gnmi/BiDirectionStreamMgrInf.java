package org.dvlyyon.nbi.gnmi;

import java.util.List;

public interface BiDirectionStreamMgrInf <T1, T2>{
	public void push(T1 content);
	public void complete();
	public void error(Throwable t);
	public boolean isInComplete();
	public boolean isInError();
	public String  getInError();
	public List<T2>      popInEvent();
}
