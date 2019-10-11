package org.dvlyyon.nbi.gnmi;

import org.dvlyyon.nbi.gnmi.GnmiCommonClient.DefaultSubscriptionMgr;

public interface BiDirectionStreamClientInf<T1,T2> {
	BiDirectionStreamMgrInf<T1,T2> getMgr() throws Exception;
	BiDirectionStreamMgrInf<T1,T2> getMgr(int capacity) throws Exception;
}
