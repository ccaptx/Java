package org.dvlyyon.study.io.file;

import java.util.Collection;
import java.util.TreeMap;
import java.util.Vector;

public class Configuration {
	static final String name_prefix = "cx_";
	static final String convert_name_to = "internal__name";
	static final String value_closed_by = "vclosedby";
	static String[] neTypes = {"7090M5","7090M8","7090M80","7090M92","7090M240","7090M240-1","7090CEM60","7090CEM100"};
	static String[] ethTypes = {"fe","fx","ge","xg"};
	static String[] pdhTypes = {"e1","t1"};
	static String[] sdhTypes = {"stm1","stm4"};
	static String[] io_inTypes = {"io_in"};
	static String[] io_outTypes = {"io_out"};
	static String[] extclkTyeps = {"extclk"};
	static String[] todTypes = {"tod"};
	public static int actionTotal = 0;
	public static int actionParsed = 0;
	public static String [] objectDecompose = {
		"ne",
		"@ptn/@interfaces/@eth/@obj",
		"@ptn/@interfaces/@pdh/@obj",
		"@ptn/@interfaces/@sdh/@obj",
		"@interfaces/@io/@in_obj",
		"@interfaces/@io/@out_obj",
		"@interfaces/@extclk/@obj",
		"@interfaces/@tod/@obj"		
	};
	
	
	public static String[][] physicalPortMetaInfo = {//fname,hierarchy,rnrule,fnrule
		{"@ptn/@interfaces/@eth/@obj","interfaces/eth","2","1",""},
		{"@ptn/@interfaces/@pdh/@obj","interfaces/pdh","2","1",""},
		{"@ptn/@interfaces/@sdh/@obj","interfaces/sdh","2","1",""},
		{"@interfaces/@io/@in_obj","interfaces/io","2","1","in"},
		{"@interfaces/@io/@out_obj","interfaces/io","2","1","out"},
		{"@interfaces/@extclk/@obj","interfaces/extclk","3","1",""},
		{"@interfaces/@tod/@obj","interfaces/tod","3","1",""}
	};
	
	public static String [] getPhysicalPortMetaInfo(String fName) {
		for (String [] metaInfo:physicalPortMetaInfo) {
			if (fName.equals(metaInfo[0])) {
				return metaInfo;
			}
		}
		return null;
	}
	public static String [][] getProperties() {
		return properties;
	}
	public static final String [][] properties = {
		{"entry-point","java -jar cx7090M-1.0.jar"},
		{"ip-addr","localhost"},
		{"user-id","cxt7090m"},
		{"password","cxt7090m11"},
		{"system-prompt","cxt7090m"},
		{"timeout","200"},
		{"isStub","no"},
		{"isStateful","yes"},
		{"separator","#CX#"}
	};
	
	public static String [][] otherObjectMetaInfo = {//fname,hierary,rnrule,fnrule,rname-pre
		{"@ptn/@interfaces/@lag/@obj","","1","","lag"},
		{"@ptn/@interfaces/@l3vpnpeer/@obj","","1","","l3vpnpeer"},
		{"@ptn/@interfaces/@tunnel/@obj","","1","","tunnel"},
		{"@ptn/@interfaces/@acn/@obj","","1","","acn"},
		{"@ptn/@interfaces/@track/@obj","","1","","track"},
		{"@ptn/@interfaces/@veth/@l3obj","","1","","vethl3"},
		{"@ptn/@interfaces/@veth/@l2obj","","1","","vethl2"},
		{"@interfaces/@mcn/@obj","","1","","mcn"},
		{"@ptn/@interfaces/@peergrp/@obj","","1","","peergrp"},
		{"@ptn/@interfaces/@vrrpgrp/@obj","","1","","vrrpgrp"},
		{"@ptn/@interfaces/@ccn/@obj","","1","","ccn"},
		{"@ptn/@interfaces/@if_eth_ac","","4","",""},
		{"@ptn/@interfaces/@sdh/@obj_ac","","4","",""},
		{"@ptn/@interfaces/@pweth/@obj","","4","",""},
		{"@ptn/@interfaces/@pwpdh/@obj","","4","",""},
		{"@ptn/@interfaces/@pwsdh/@obj","","4","",""},
		{"@ptn/@l2vpn/@eline/@obj","","1","","eline"},
		{"@ptn/@l2vpn/@elan/@obj","","1","","elan"},
		{"@ptn/@l2vpn/@etree/@obj","","1","","etree"},
		{"@ptn/@l3vpn/@obj","","1","","l3vpn"},
		{"@ptn/@ccc/@obj","","1","","ccc"},
		{"@ptn/@ces/@obj","","1","","ces"}
	};
	public static String [] getMetaInfo(String fName) {
		for (String [] metaInfo:otherObjectMetaInfo) {
			if (fName.equals(metaInfo[0])) {
				return metaInfo;
			}
		}
		return null;
	}
	
	public static String [][] getOtherObjectMetaInfo () {
		return otherObjectMetaInfo;
	}
	public static final String [] getObjectDecomposed (String obj) {
		return objectsDecomposed.get(obj);
	}

	static TreeMap <String, String[]> objectsDecomposed = new TreeMap<String,String[]>();
	public static void addObjectsDecomposed(String obj, String[]objs) {
		objectsDecomposed.put(obj,objs);
	}
	
	public static void init() {
		addObjectsDecomposed("ne",neTypes);
		addObjectsDecomposed("@ptn/@interfaces/@eth/@obj",ethTypes);
		addObjectsDecomposed("@ptn/@interfaces/@pdh/@obj",pdhTypes);
		addObjectsDecomposed("@ptn/@interfaces/@sdh/@obj",sdhTypes);
		addObjectsDecomposed("@interfaces/@io/@in_obj",io_inTypes);
		addObjectsDecomposed("@interfaces/@io/@out_obj",io_outTypes);
		addObjectsDecomposed("@interfaces/@extclk/@obj",extclkTyeps);
		addObjectsDecomposed("@interfaces/@tod/@obj",todTypes);
	}

	static final String [][] callPrompts = {
		{"@ptn/@oam/@bfd/@lspid","ping","finished",""},
		{"@ptn/@oam/@bfd/@lspid","throughput","mean tx drop ratio",""},
		{"@ptn/@oam/@bfd/@pwid","ping","finished",""},
		{"@ptn/@oam/@bfd/@pwid","throughput","mean tx drop ratio",""},
		{"@ptn/@oam/@bfd/@secid","throughput","mean tx drop ratio",""},
		{"@ptn/@l2vpn/@elan/@obj", "QueryUCfdb","total", ""},
		{"@ptn/@l2vpn/@etree/@obj", "QueryUCfdb","total", ""},
		{"@ptn/@oam/@oam_mep_lsp", "loopback","timeOutCnt",""},
		{"@ptn/@oam/@oam_mep_pw", "loopback","timeOutCnt",""},
		{"@ptn/@oam/@oam_mep_sec", "loopback","timeOutCnt",""},
		{"@ptn/@oam/@oam_mep_eth_svc", "ethsvcloopback","timeOutCnt",""}
	};
	
	static String [] getCallPrompt(String fName, String actName) {
		for (String[] item:callPrompts) {
			if (item[0].equals(fName) && item[1].equals(actName)) {
				return item;
			}
		}
		return null;
	}
	
	static final String [][] callParameterTable = {//fname,action,parameter,parameter type, range,other
		{"@interfaces/@obj_efm","rmtlpb","en","U32","0|1","maptype=2"},
		{"@proc/@logsvr","clearall","logtype","TXT","",""},
		{"@protocols/@cfm/@cfm_ma","linktrace","ethac_name","TXT","",""},
		{"@protocols/@cfm/@cfm_ma","linktrace","destination_mac","MACADDR","",""},
		{"@protocols/@cfm/@cfm_ma","linktrace","service_priority","TXT","",""},
		{"@protocols/@cfm/@cfm_ma","linktrace","discard_priority","TXT","",""},
		{"@protocols/@cfm/@cfm_ma","linktrace","ttl","TXT","",""},
		{"@protocols/@cfm/@cfm_ma","loopback","ethac_name","TXT","",""},
		{"@protocols/@cfm/@cfm_ma","loopback","destination_mac","MACADDR","",""},
		{"@protocols/@cfm/@cfm_ma","loopback","service_priority","TXT","",""},
		{"@protocols/@cfm/@cfm_ma","loopback","discard_priority","TXT","",""},
		{"@protocols/@cfm/@cfm_ma","loopback","package_num","TXT","",""},
		{"@ptn/@l2vpn/@etree/@obj","QueryUCfdb","cardno", "U32", "1-32",""},
		{"@ptn/@l2vpn/@elan/@obj","QueryUCfdb","cardno", "U32", "1-32",""},
		{"@ptn/@l3vpn/@obj","ping","ip","IPADDR","","order=1"},
		{"@ptn/@l3vpn/@obj","ping","ttl","U32","1-255",""},
		{"@ptn/@l3vpn/@obj","ping","len","U32","0-1024",""},
		{"@ptn/@l3vpn/@obj","ping","srcip","IPADDR","",""},
		{"@ptn/@l3vpn/@obj","tracert","destip","IPADDR","",""},
		{"@ptn/@oam/@bfd/@lspid","ping","dnodeid","IPADDR","","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","ping","dtunnelnum","U32","1-4096","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","throughput","cos","ECOS","","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","throughput","color","EQOSCOLOR","","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","throughput","len","U32","128-1504","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","throughput","time","U32","60-180","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","throughput","dm","OAMDMMANNER","","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","throughput","bw","U32","","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","tracert","dnodeid","IPADDR","","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","tracert","igrifnum","U32","","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","tracert","egrifnum","U32","","maptype=2"},
		{"@ptn/@oam/@bfd/@lspid","tracert","dtunnelnum","U32","1-4096","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","ping","dnodeid","IPADDR","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","ping","dacid","U32","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","throughput","cos","ECOS","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","throughput","color","EQOSCOLOR","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","throughput","len","U32","128-1504","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","throughput","time","U32","60-180","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","throughput","dm","OAMDMMANNER","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","throughput","bw","U32","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","tracert","dnodeid","IPADDR","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","tracert","igrifnum","U32","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","tracert","egrifnum","U32","","maptype=2"},
		{"@ptn/@oam/@bfd/@pwid","tracert","dacid","U32","","maptype=2"},
		{"@ptn/@oam/@bfd/@secid","throughput","cos","ECOS","","maptype=2"},
		{"@ptn/@oam/@bfd/@secid","throughput","color","EQOSCOLOR","","maptype=2"},
		{"@ptn/@oam/@bfd/@secid","throughput","len","U32","128-1504","maptype=2"},
		{"@ptn/@oam/@bfd/@secid","throughput","time","U32","60-180","maptype=2"},
		{"@ptn/@oam/@bfd/@secid","throughput","dm","OAMDMMANNER","","maptype=2"},
		{"@ptn/@oam/@bfd/@secid","throughput","bw","U32","","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","ethsvcloopback","tgtmpid","U32","0-8191","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","ethsvcloopback","cnt","U32","1-5","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","ethsvcloopback","datalen","U32","0-64","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","ethsvcloopback","dmac","MACADDR","","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","ethsvcloopback","ifoutofsvc","U32","0|1","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","throughput","cos","ECOS","","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","throughput","color","EQOSCOLOR","","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","throughput","len","U32","128-1504","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","throughput","time","U32","60-180","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","throughput","dm","OAMDMMANNER","","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","throughput","bw","U32","","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","linktrace","ttl","U32","1-255","maptype=2"},
		{"@ptn/@oam/@oam_mep_eth_svc","linktrace","dmac","MACADDR","","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopback","tgtmpid","U32","0-8191","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopback","ttl","U32","1-255","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopback","cnt","U32","1-5","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopback","datalen","U32","0-64","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopback","ifoutofsvc","enum","0/1","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopbackmip","ttl","U32","1-255","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopbackmip","cnt","U32","1-5","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopbackmip","datalen","U32","0-64","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopbackmip","nodeid","IPADDR","","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopbackmip","ifnum","U32","INT32","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","loopbackmip","ifoutofsvc","U32","0|1","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","throughput","cos","ECOS","","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","throughput","color","EQOSCOLOR","","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","throughput","len","U32","128-1504","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","throughput","time","U32","60-180","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","throughput","dm","OAMDMMANNER","","maptype=2"},
		{"@ptn/@oam/@oam_mep_lsp","throughput","bw","U32","","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopback","tgtmpid","U32","0-8191","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopback","ttl","U32","1-255","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopback","cnt","U32","1-5","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopback","datalen","U32","0-64","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopback","ifoutofsvc","U32","0|1","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopbackmip","ttl","U32","1-255","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopbackmip","cnt","U32","1-5","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopbackmip","datalen","U32","0-64","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopbackmip","nodeid","IPADDR","","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopbackmip","ifnum","U32","INT32","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","loopbackmip","ifoutofsvc","U32","0|1","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","throughput","cos","ECOS","","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","throughput","color","EQOSCOLOR","","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","throughput","len","U32","128-1504","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","throughput","time","U32","60-180","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","throughput","dm","OAMDMMANNER","","maptype=2"},
		{"@ptn/@oam/@oam_mep_pw","throughput","bw","U32","","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","loopback","tgtmpid","U32","0-8191","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","loopback","ttl","U32","1-255","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","loopback","cnt","U32","1-5","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","loopback","datalen","U32","0-64","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","loopback","ifoutofsvc","U32","0|1","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","throughput","cos","ECOS","","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","throughput","color","EQOSCOLOR","","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","throughput","len","U32","128-1504","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","throughput","time","U32","60-180","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","throughput","dm","OAMDMMANNER","","maptype=2"},
		{"@ptn/@oam/@oam_mep_sec","throughput","bw","U32","","maptype=2"},
		{"ne","testmode","modestat","U32","0|1",""},
		{"ne","dropdb","rebootne","U32","0|1","order=1"},
		{"ne","dropdb","label","TXT","0|1",""},
		{"ne","reboot","chss","CARDRANGE","0|1-16|all",""},
		{"ne","reboot","type","enum","warm/cold/comm",""},
		{"ne","restoredb","fromfile","TXT","",""},
		{"ne","restoredb","dbname","enum","odc/secu",""},
		{"ne","shhwres","slot","CARDRANGE","1-16|all",""},
		{"ne","showmanu","cardno","U32","1-16",""}		
	};
	
	static Vector<String []> getCallActionParameter(String oFName, String action) {
		Vector<String[]> parameters = new Vector<String[]>();
		for (String [] item: callParameterTable) {
			if (item[0].equals(oFName) && item[1].equals(action)) {
				parameters.add(item);
			}
		}
		return parameters;
	}
	
	static final String [][] otherAttributesDefinedInOtherAPI = { 
		//some attribute is special for some types of NE other than 500e, we need add them here manually
		//the attributes here are only for set and create actions. Now we only add static attribute and only in set action
		{"@ptn/@cxt500e/@lg_line3","clock","EPORTTYPE","1: NONE 2: TOD 3: EXTCLK",""}		
	};
	
	static Vector<String []> getExtraAttributesDefinedInOtherAPI(String fName) {
		Vector<String[]> attrV = new Vector<String[]>();
		for (String [] item:otherAttributesDefinedInOtherAPI) {
			if (item[0].equals(fName)) {
				attrV.add(item);
			}
		}
		return attrV;
	}
	
	static String [][] modifiableAttributesInOtherAPI = {
		//some attributes can be set in Other API
		//objName, attrName, reserved
		{"@interfaces/@tod/@obj","mode",""}
	};
	
	static boolean isModifiableInOtherAPI(String fName, String attrName) {
		for (String[] attrItem: modifiableAttributesInOtherAPI) {
			if (attrItem[0].equals(fName) && attrItem[1].equals(attrName)) {
				return true;
			}
		}
		return false;
	}
	
	static final String [][] attributeOnlyForGet = {//The initial thought is to add get CATS attribute
		{"ne", "catsAddress",""},
		{Configuration.FNAMETOSLOT,"catsAddress",""},
		{"@ptn/@interfaces/@eth/@obj","catsAddress",""},
		{"@ptn/@interfaces/@pdh/@obj","catsAddress",""},
		{"@ptn/@interfaces/@sdh/@obj","catsAddress",""},
		{"@interfaces/@io/@in_obj","catsAddress",""},
		{"@interfaces/@io/@out_obj","catsAddress",""},
		{"@interfaces/@extclk/@obj","catsAddress",""},
		{"@interfaces/@tod/@obj","catsAddress",""}		
	};
	
	static Vector <String> getGetOnlyAttribute(String fName) {
		Vector <String> attributes = new Vector<String>();
		for (String[] attr:attributeOnlyForGet) {
			if (attr[0].equals(fName)) attributes.add(attr[1]);
		}
		return attributes;
	}
	
	static final String [][] objectCreatedWithSubNode = {
		{"@pmap/@vlanpri_obj","default"},
		{"@pmap/@l2pmapc_obj","default"},
		{"@pmap/@l3pmapc_obj","default"},
		{"@ptn/@protocols/@mpls/@xc_obj","1,2"}
	};
	
	static String [] needCreatedWithSubNode(String fName) {
		for (String [] item:objectCreatedWithSubNode) {
			if (fName.equals(item[0])) {
				return item;
			}
		}
		return null;	
	}
	
	static final String [] not_model_objects = {
		"@cmm/@portlist_ro",
		"@cmm/@phycard", //use @phy_sct
		"@cmm/@lg_fan", //use @lg_line3 
		"@cmm/@lg_pwn",
		"@ptn/@cxt500e/@lg_sct",
		"@ptn/@cxt500e/@lg_line1",
		"@ptn/@cxt500e/@lg_line2",
		}; //phycard with 

	public static final String FNAMETOSLOT = "@ptn/@cxt500e/@slots";
	static final String[][] nameMapper = {
		{"@almcfg/@define","alarm_define"},
		{"ne","7090MNode"},
		{"@ptn/@cxt500e/@slots","slot"},
		{"@ptn/@cxt500e/@phy_sct","phycard"},
		{"@ptn/@cxt500e/@slot_sct","slot"}, //Note that this slot is not model, we should move its children to @ptn/@cxt500e/@slots
		{"@ptn/@cxt500e/@lg_line3","lg"},
		{"@ptn/@cxt500e/@slot_line3","slot"}, //same as slot_sct
		{"@ptn/@protocols/@mpls/@ringpassprt", "ringpasslbl"},
		{"@ptn/@protocols/@mpls/@ringprt","ringlbl"},
		{"@ptn/@interfaces/@veth/@l2list","vethl2_list"},
		{"@ptn/@interfaces/@veth/@l2obj","vethl2_obj"},
		{"@ptn/@interfaces/@veth/@l3list","vethl3_list"},
		{"@ptn/@interfaces/@veth/@l3obj","vethl3_obj"},
		{"@ptn/@interfaces/@sdh/@obj_ac", "sdh_ac_obj"},
		{"@ptn/@interfaces/@if_eth_ac", "eth_ac_obj"},
		{"@ptn/@interfaces/@tunnel/@obj_lsp","lsp_obj"},
		{"@protocols/@ospf/@area_area_id","ospf_area_id"},
		{"@protocols/@ospf/@area_area_id_area_range","ospf_area_id_range"},
		{"@protocols/@ospf/@area_area_id_area_range_prefix", "ospf_area_id_range_prefix"},
		{"@protocols/@ospf/@area_area_id_virtual_link", "ospf_area_id_virtual_link"},
		{"@protocols/@ospf/@area_area_id_virtual_link_peer_id", "ospf_area_id_virtual_link_peer_id"},
		{"@protocols/@ospf/@redistribute_default", "ospf_redistribute_default"},
		{"@protocols/@ospf/@redistribute", "ospf_redistribute"},
		{"@protocols/@ospf/@redistribute_proto", "ospf_redistribute_proto"},
		{"@ptn/@oam/@bfd/@lspid","lsp_bfd"},
		{"@ptn/@oam/@bfd/@bfd_lspid","lsp_bfd_id"},
		{"@ptn/@oam/@bfd/@pwid","pw_bfd"},
		{"@ptn/@oam/@bfd/@bfd_pwid", "pw_bfd_id"},
		{"@cmap/@l2_obj", "cmap_l2_obj"},
		{"@cmap/@l3_obj", "cmap_l3_obj"},
		{"@pmap/@l2pmapc_obj", "pmap_ethac_l2_obj"},
		{"@pmap/@l3pmapc_obj", "pmap_ethac_l3_obj"},
		{"@pmap/@vlanpri_obj", "pmap_ethac_vlanpri_obj"},
		{"@ptn/@pmap/@ethac", "pmap_ethac_list"},
		{"@ptn/@pmap/@mpls", "pmap_mpls_list"},
		{"@ptn/@pmap/@eelsp_obj", "pmap_mpls_eelsp_obj"},
		{"@ptn/@pmap/@llsp_obj", "pmap_mpls_llsp_obj"},
		{"@ptn/@pmap/@elsp_obj", "pmap_mpls_elsp_obj"}
	};
	
	
	static boolean needModel (String fName) {
		if (fName.indexOf("@slot_")>0) return false;
		for (String on:not_model_objects) {
			if (on.equals(fName)) return false;
		}
		return true;
	}
	
	static Vector<String> interfaceObjects = new Vector<String>();
	public static void addInterfaceObjects(String ifObject) {
		interfaceObjects.addElement(ifObject);
	}
	public static Vector<String> getInterfaceObjects() {
		return interfaceObjects;
	}
	
	static final String [] knowned_type = {"BOOL","TXT"};
	static final String[][] types = {
		{"PREFIX","format","A.B.C.D/masklen"},
		{"IFNAME","object-name", "ifname"},
		{"INT16","integer","-32768-32767"},
		{"INT32","integer","-2147473648-2147483647"},
		{"INT8","integer","-128-127"},
		{"IPPREFIX","format","A.B.C.D/masklen"},
		{"LPTSTATUS","integer","bit0:lclfail,bit1:transitfail,bit2:rmtfail,0:idle"},
		{"TODSTATUS","integer","bit1:ante open,bit2:ante shorted,...,0:ok"},
		{"U16","integer","0-65535"},
		{"U32","integer","[1,10]|(90,100)|999"},
		{"U52","integer","0-2^52-1"},
		{"U8","integer","0-255"},
		{"VPNNAME","string",""},
		{"IPADDR","format","A.B.C.D"},
		{"MACADDR", "format", "HHHH.HHHH.HHHH"},
		{"OPERSTATUS","integer","bit0:tsf,bit1:linkdown,bit2:admindown,bit3:notPresent,bit5:datalinkdown,0:up"},
		{"WAVELEN","string",""},
		{"PERPARA","format","flag=%d,m15hh=%d,m15hl=%d,h24hh=%d,h24hl=%d"},
		{"_array", "integer", "1-80"},
		{"CARDRANGE","format","0-16|all"}
	};

	public static String [] getType (String type) {
		for (String[] t:types) {
			if (t[0].equals(type)) return t;
		}
		return null;
	}
	
	public static String getCustomizedObjectName (String fName) {
		for (String[] on:nameMapper) {
			if (on[0].equals(fName)) return on[1];
		}
		return null;
	}
	
	public static final int ALL_DYNAMIC = 0;
	public static final int ONE_STATIC_ONE_DYNAMIC = 1;
	public static final int ALL_STATIC = 3;
	
	public static String getDynamicFieldName(String type) {
		if (type.equals("_array")) {
			return "d_arrayIndex";
		} else if (type.equals("IFNAME")) {
			return "d_ifName";
		} else if (type.equals("MACADDR")) {
			return "d_macAddress";
		} else if (type.equals("PREFIX")) {
			return "d_prefix";
		} else if (type.equals("TXT")) {
			return "d_textValue";
		} else if (type.equals("INT32")) {
			return "d_number";
		} else if (type.equals("IPADDR")) {
			return "d_ipAddress";
		}
		return null;
	}
	public static String retrieveItem(String ass, int order) {
		String [] it = ass.split("]");
		return (it[order-1].substring(1));
	}
		
	
	public static final String [] objectsWithoutCreate = {
		"@ptn/@interfaces/@eth/@obj",
		"@interfaces/@extclk/@obj",
		"@interfaces/@io/@in_obj",
		"@interfaces/@io/@out_obj",
		"@ptn/@interfaces/@pdh/@obj",
		"@ptn/@interfaces/@sdh/@obj",
		"@interfaces/@tod/@obj"	
	};
	
	public static boolean isObjectWithoutCreate(String fName) {
		for (String n:objectsWithoutCreate) {
			if (n.equals(fName)) return true;
		}
		return false;
	}
	
	static final String [] eth_ac_pmap = {
		"@pmap/@vlanpri_obj", "@pmap/@l2pmapc_obj","@pmap/@l3pmapc_obj"
	};
	static final String [] pwmpls_pmap = {
		"@ptn/@pmap/@llsp_obj","@ptn/@pmap/@elsp_obj","@ptn/@pmap/@eelsp_obj"
	};
	public static String [] getQOSObjs(String fName) {
		if (fName.equals("@ptn/@interfaces/@if_eth_ac")) {
			return eth_ac_pmap;
		} else if (fName.equals("@ptn/@interfaces/@pweth/@obj") ||
				fName.equals("@ptn/@interfaces/@tunnel/@obj") ||
				fName.equals("@ptn/@protocols/@mpls/@xc_obj")) {
			return pwmpls_pmap;
		} else if (fName.equals("@ptn/@interfaces/@l3vpnpeer/@obj")) {
			return pwmpls_pmap;
		} 
		return null;
	}
	
	public static int getAssTypeForOPMR(String as) {
		String [] asL = as.split(",");
		boolean allStatic = true;
		boolean allDynamic = true;
		for (String a: asL) {
			if (a.indexOf("[S]")>=0) allDynamic = false;
			if (a.indexOf("[D]")>=0) allStatic = false;
 		}
		if (allDynamic) return Configuration.ALL_DYNAMIC;
		if (allStatic) return Configuration.ALL_STATIC;
		if (!allDynamic && !allStatic) {
			if (asL.length==2) return Configuration.ONE_STATIC_ONE_DYNAMIC;
		}
		return -1;
	}
	/**
	 * It is expected: 
	 * 		1.all parents only have one association
	 * 		2.all association have the same type S or D
	 * 		3.all association have the same type F0 or F1
	 * @param ass
	 * @return
	 */
	public static boolean checkForMPOREF(TreeMap <String,String> ass) {
		Collection <String> assC = ass.values();
		boolean allStatic = true;
		boolean allDynamic = true;
		boolean allFixed0 = true;
		boolean allFixed1 = true;
		for (String as:assC) {
			String [] asL = as.split(",");
			if (asL.length>1) return false;
			for (String a: asL) {
				if (a.indexOf("[S]")>=0) allDynamic = false;
				if (a.indexOf("[D]")>=0) allStatic = false;
				if (a.indexOf("[F1]")>=0) allFixed0 = false;
				if (a.indexOf("[F0]")>=0) allFixed1 = false;
	 		}
		}
		return (allStatic || allDynamic) && (allFixed0 || allFixed1);
	}
	
	static String [] vpnObject = {
		"@ptn/@l2vpn/@elan/@obj",
		"@ptn/@l2vpn/@eline/@obj",
		"@ptn/@l2vpn/@etree/@obj",
		"@ptn/@l3vpn/@obj",
		"@ptn/@ccc/@obj",
		"@ptn/@ces/@obj"
	};
	
	static String [][] serviceMapper = {
		{"@ptn/@interfaces/@if_eth_elan",vpnObject[0]},
		{"@ptn/@interfaces/@if_eth_pw_elan",vpnObject[0]},
		{"@ptn/@interfaces/@if_eth_eline", vpnObject[1]},
		{"@ptn/@interfaces/@if_eth_etree", vpnObject[2]},
		{"@ptn/@interfaces/@l3/@if_l3_cmm",vpnObject[3]},
		{"@ptn/@interfaces/@if_service_ccc", vpnObject[4]},
		{"@ptn/@interfaces/@if_service_ces",vpnObject[5]},
		{"@ptn/@interfaces/@l3vpnpeer/@obj",vpnObject[3]}	
	};
	
	public static String [] getAllVPNObject() {
		return vpnObject;
	}
	public static String [] getVPNObject(String fName) {
		for (String [] item:serviceMapper) {
			if (item[0].equals(fName))
				return item;
		}
		return null;
	}
	
	static String[][] referredObjects = {
		{"@acl/@ace","name","@cmap/@l2_obj,@cmap/@l3_obj"},
		{"@ptn/@interfaces/@if_eth_uni","vlanpri2cng","@ptn/@interfaces/@if_vlanpri2cng_obj"},
		{"@ptn/@interfaces/@if_eth_uni","cos2vlanpri","@interfaces/@if_cos2vlanpri_obj"}
	};
	
	public static String getRefferedObject(String fName, String attribute) {
		for (String[] item:referredObjects) {
			if (item[0].equals(fName) && item[1].equals(attribute))
				return item[2];
		}
		return null;
	}
	
	static String [] functionNames = {
		"getAddress"
	};
	
	static String [][] functionDefinitions = {
		{functionNames[0],"object","string","",""}	
	};
	
	static String [] getFunctions() {
		return functionNames;
	}
	
	static Vector<String[]> getFunctionDefinition(String functionName) {
		Vector <String[]> result = new Vector<String[]>();
		for (String[] item:functionDefinitions) {
			if (item[0].equals(functionName)) result.add(item);
		}
		return result;
	}
}
