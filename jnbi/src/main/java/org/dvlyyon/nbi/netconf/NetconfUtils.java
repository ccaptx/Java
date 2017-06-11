package org.dvlyyon.nbi.netconf;

import org.dvlyyon.nbi.util.CommonUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.dvlyyon.nbi.model.DObjectAttribute;
import org.dvlyyon.nbi.util.AttributeInfo;
import org.dvlyyon.net.netconf.ConfiguratorIf.EditOperation;
import org.dvlyyon.net.netconf.ConfiguratorIf.ErrorOption;
import org.dvlyyon.net.netconf.ConfiguratorIf.TestOption;
import org.dvlyyon.net.netconf.exception.NetconfException;

public class NetconfUtils {
	public static Namespace nb_xmlns = Namespace.getNamespace("urn:ietf:params:xml:ns:netconf:base:1.0");
	
	public static EditOperation convertOperation(String operation) {
		if (CommonUtils.isNullOrSpace(operation)) return null;
		else if (operation.equals("merge")) return EditOperation.Merge;
		else if (operation.equals("replace")) return EditOperation.Replace;
		else if (operation.equals("none")) return EditOperation.None;
		return null;
	}
	
	public static TestOption convertTestOption(String test) {
		if (CommonUtils.isNullOrSpace(test)) return null;
		else if (test.equals("set")) return TestOption.Set;
		else if (test.equals("test-only")) return TestOption.TestOnly;
		else if (test.equals("test-then-set")) return TestOption.TestThenSet;
		return null;		
	}

	public static ErrorOption convertErrorOption(String error) {
		if (CommonUtils.isNullOrSpace(error)) return null;
		else if (error.equals("continue-on-error")) return ErrorOption.ContinueOnError;
		else if (error.equals("rollback-on-error")) return ErrorOption.RollbackOnError;
		else if (error.equals("stop-on-error")) return ErrorOption.StopOnError;
		return null;		
	}
	
	public static boolean isMappedToEditConfig(int operation) {
		switch (operation) {
		case NetconfConstants.NETCONF_OPERATION_ADD:
		case NetconfConstants.NETCONF_OPERATION_DELETE:
		case NetconfConstants.NETCONF_OPERATION_SET:
			return true;
		}
		return false;
	}

	public static boolean isGetConfigOperation(String getType) {
		if (getType.equals(NetconfConstants.NETCONF_PROTOCOL_OPERATION_GET_CONFIG))
			return true;
		return false;
	}
	
	public static String getErrorInfo(Element rpcError) {
		NetconfException ex = NetconfException.fromXml(rpcError);
		return ex.getMessage();
	}

	public static boolean isSupportedRpcReplyType(String type) {
		// TODO Auto-generated method stub
		if (type.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LEAF) ||
			type.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LIST))
			return true;
		return false;
	}
	
	public static boolean isNetconfNodeTypeList(String name) {
		return name.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LIST);
	}
	
	public static boolean isNetconfNodeTypeLeaf(String name) {
		return name.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LEAF);
	}
	
	public static Namespace getNamespace(String namespace) {
		return Namespace.getNamespace(namespace);
	}
	
	public static String getNetconfAttributeName(AttributeInfo attrInfo) {
		DObjectAttribute attrObject = attrInfo.getAttrObject();
		String attrName = attrObject.getProperty(NetconfConstants.META_NETCONF_MAP2);
		return (attrName == null)?attrInfo.getName():attrName;
	}
}
