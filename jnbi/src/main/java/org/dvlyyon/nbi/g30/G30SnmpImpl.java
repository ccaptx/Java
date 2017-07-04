package org.dvlyyon.nbi.g30;

import static org.dvlyyon.nbi.CommonMetadata.NBI_TYPE_NETCONF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.CliInterface;
import org.dvlyyon.nbi.CliStub;
import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.util.RunState;

public class G30SnmpImpl extends CliStub implements CliInterface, NBIAdapterInf  {
	public final Log log = LogFactory.getLog(G30SnmpImpl.class);
	public final String COMMAND = "NETCONF_CMD:-> ";
	public final String RESPONSE = "NETCONF_RESP:<- ";

	@Override
	public CommandPatternListInf parseAction(NBIMultiProtocolsObjectInf obj,
			String actionName, String[] params, RunState state, int actType) {
		return obj.parseAction(actionName, params, state, actType, NBI_TYPE_NETCONF);
	}

	@Override
	public String toGetResponse(NBIMultiProtocolsObjectInf obj, String actionName,
			CommandPatternListInf cmd, RunState state) {
		return obj.toGetResponse(actionName, cmd, state, NBI_TYPE_NETCONF);
	}
	
}
