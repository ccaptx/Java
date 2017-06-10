package org.dvlyyon.study.lang.regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	
	private void match(String regExp, String result) {
		if (Pattern.compile(regExp,Pattern.DOTALL|Pattern.CASE_INSENSITIVE).matcher(result).matches()) {
			System.out.println("regExp:" + regExp + " match string: "+result);
		} else {
			System.out.println("regExp:" + regExp + " NOT match string: "+result);
		}
	}
	
	private void find(String regExp, String result) {
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(result);
		if (m.find()) {
			System.out.println("regExp:" + regExp + " find string: "+result);
		} else {
			System.out.println("regExp:" + regExp + " NOT find string: "+result);
		}
		
	}
	
	public void regularExp() {
		String regExp = "^(restart|clear -?f? ?database).*$";
		String result1 = "restart -f\n\r\r[NE]\r\r\n";
		String result2 = "clear -f database\r\r\n[NE]\r\r\n";
		String result3 = "clear database\r\n[NE]\n";
		String result4 = "restart \r\r\n[NE]\n";
		String result5 = "disldfak \nsdkfal\n";
		String result6 = "sdi \n restart \n[NE]";
		
		match(regExp,result1);
		match(regExp,result2);
		match(regExp,result3);
		match(regExp,result4);
		match(regExp,result5);
		match(regExp,result6);
		
		regExp = "AIS-L*.CL";
		match(regExp,"YANTAI 16-09-22 13:48:03\nA 44360 REPT EVT OC192 \n\"OC192-20-15-3:AIS-L,CL,09-22,13-48-03,NEND,TDTN,,,:\\\"Alarm Indication signal - Line\\\"\" \n;\n");
		find(regExp,"YANTAI 16-09-22 13:48:03\nA 44360 REPT EVT OC192 \n\"OC192-20-15-3:AIS-L,CL,09-22,13-48-03,NEND,TDTN,,,:\\\"Alarm Indication signal - Line\\\"\" \n;\n");
		find(regExp,"YANTAI 16-09-22 13:48:03\nA 44360 REPT EVT OC192 \n\"OC192-20-15-3:AIS-L,dsfaCL,09-22,13-48-03,NEND,TDTN,,,:\\\"Alarm Indication signal - Line\\\"\" \n;\n");
	}
	
	public void regularExp1() {
		String regExp = "CR.MS-BERSF.SA.*TDTC";
		String str    = "YANTAI 17-02-08 03:30:18 \nM 34841 COMPLD \n\"STM64-20-15-99-6,STM64:MN,MS-BERSD,NSA,02-08,03-28-07,NEND,TDTC:\"Bit Error Ratio Signal Degrade - Multiplex Section\"\" \n\"STM64-20-15-99-6,STM64:CR,MS-BERSF,SA,02-08,03-28-46,NEND,TDTC:\"Bit Error Ratio Signal Fail - Multiplex Section\"";
		find(regExp, str);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Main j = new Main();
		j.regularExp1();
	}

}
