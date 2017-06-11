package org.dvlyyon.nbi;

import java.io.PrintStream;

public class CommonCatsAgent {
	public PrintStream out = null;
	
	public CommonCatsAgent() {
		try {
			out = new PrintStream(System.out, true, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getErrorMsg(String err) {
	return "<##CATS##>Failed: " + err + " <##CATS##>\n";
	}

	public String getExecuteOKMsg() {
		return "<##CATS##>OK<##CATS##>\n";
	}

	public String getExpectOKMsg(String msg) {
		return "<##CATS##>" + msg + "<##CATS##>\n";
	}
	
	public String getOKMsg(String msg) {
		return "<##CATS##>" + msg + "<##CATS##>\n";
	}
	
	public void replyFunction(String msg) {
		out.println(getOKMsg(msg));
	}

	public void reply(String ret) {
		try {
		if (ret !=null) {
			if (ret.startsWith("OK")) {
				out.println(getOKMsg(ret));
			}
			else {
				out.println(getErrorMsg(ret));
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public String response(String ret) {
//		if (ret!=null) {
//			if (ret.startsWith("OK")) {
//				return getOKMsg(ret);
//			}
//			else 
//				return getErrorMsg(ret);			
//		}
//		return getExecuteOKMsg();
//	}
	
	public void ready(String platform) {
		System.out.println("cats_"+platform+">");
	}

	public void tellModelRevision(String revision) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<25; i++) sb.append("-");
		System.out.format("%s%-32s%s%n",sb.toString()," Object Model Revision:"+revision,sb.toString());
	}

	public void tellDriverRevision() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<25; i++) sb.append("-");
		System.out.format("%s%-32s%s%n",sb.toString()," Driver Release Number:" + this.getClass().getPackage().getImplementationVersion(),sb.toString());
	}
}
