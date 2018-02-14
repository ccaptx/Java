package org.dvlyyon.nbi.gnmi;

import java.util.ArrayList;
import java.util.List;

import gnmi.Gnmi.Notification;
import gnmi.Gnmi.Path;
import gnmi.Gnmi.PathElem;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.Gnmi.TypedValue;
import gnmi.Gnmi.Update;


public class FakeData {
	
	public static Path getPath(String [] names) {
		ArrayList<PathElem> pl = new ArrayList<PathElem>();
		for (String n:names) {
			pl.add(PathElem
					.newBuilder()
					.setName(n)
					.build());
		}
		Path.Builder pb = Path.newBuilder();
		for (PathElem pe:pl) {
			pb.addElem(pe);
		}
		return pb.build();
				
	}
	
	public static  Update getUpdate (String[] names, String value, boolean useDep) {
		Path p;
		if (useDep) {
			Path.Builder pb = Path.newBuilder();
			for (String n:names)
				pb.addElement(n);
			p = pb.build();
		} else {
			p = getPath(names);
		}
		
		Update u = Update
				.newBuilder()
				.setPath(p)
				.setVal(TypedValue
						.newBuilder()
						.setStringVal(value)
						.build())
				.build();
		return u;
	}
	
	static class MyUpdate {
		public String [] path;
		public String value;
		public boolean useDep;
		
		public MyUpdate(String[] path, String value, boolean useDep) {
			this.path = path;
			this.value = value;
			this.useDep = useDep;
		}
	}
	
	public static  SubscribeResponse getResponse (MyUpdate[] updates) {
		Notification.Builder nb = Notification.newBuilder()
				.setTimestamp(System.currentTimeMillis());
		
		for (MyUpdate u:updates) {
			nb = nb.addUpdate(getUpdate(u.path,u.value, u.useDep));
		}
		SubscribeResponse r = SubscribeResponse
				.newBuilder()
				.setUpdate(nb.build())
				.build();
		return r;
	}

	public static  SubscribeResponse getSyncComplete() {
		return SubscribeResponse.newBuilder()
				.setSyncResponse(true)
				.build();
	}
	
	public static SubscribeResponse getOneUpdate(
			String abc,
			String abd,
			String abde) {
		MyUpdate [] u1 = {
				new MyUpdate(new String [] {"a","b","c"},    abc, true),
				new MyUpdate(new String[] {"a","b","d"},     abd, false),
				new MyUpdate(new String[] {"a","b","d","e"}, abde, true)
		};
		return getResponse(u1);
	}
	
	public static  List<SubscribeResponse> getAllCurrentData() {
		MyUpdate [] u1 = {
				new MyUpdate(new String [] {"a","b","c"}, "John Smith",true),
				new MyUpdate(new String[] {"a","b","d"},"Tom Smith",false),
				new MyUpdate(new String[] {"a","b","d","e"},"Hellow World",true)
		};

		MyUpdate [] u2 = {
				new MyUpdate(new String [] {"a","b1","c"}, "John Smith2",false),
				new MyUpdate(new String[] {"a","b2","e"},"Tom Smith2",false),
				new MyUpdate(new String[] {"a","b3","f","e"},"Hellow World2",false),
				new MyUpdate(new String[] {"a","b3","g","e"},"Hellow World2",false)
		};
		
		ArrayList<SubscribeResponse> rspL = new ArrayList<SubscribeResponse>();
		rspL.add(getResponse(u1));
		rspL.add(getResponse(u2));
		
		return rspL;
	}

}
