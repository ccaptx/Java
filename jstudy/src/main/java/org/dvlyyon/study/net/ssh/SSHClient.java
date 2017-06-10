package org.dvlyyon.study.net.ssh;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.oro.text.regex.MalformedPatternException;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.EofMatch;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import expect4j.matches.TimeoutMatch;

public class SSHClient {
    private static Logger log = LogManager.getLogger(SSHClient.class);
    
    private Session session;
    private ChannelShell channel;
    private static Expect4j expect = null;
    private static final long defaultTimeOut = 1000;
    private StringBuffer buffer=new StringBuffer();
    
    public static final int COMMAND_EXECUTION_SUCCESS_OPCODE = -2;
    public static final String BACKSLASH_R = "\r";
    public static final String BACKSLASH_N = "\n";
    public static final String COLON_CHAR = ":";
    public static String ENTER_CHARACTER = BACKSLASH_R;
    public static final int SSH_PORT = 22;
    

    public static String[] linuxPromptRegEx = new String[] { "~]#", "~#", "#",
            ":~#", "/$", ">" };
    
    public static String[] errorMsg=new String[]{"could not acquire the config lock "};
    

    private String ip;

    private int port;

    private String user;

    private String password;
    
    public SSHClient(String ip,int port,String user,String password) {
        this.ip=ip;
        this.port=port;
        this.user=user;
        this.password=password;
        expect = getExpect();
    }
    
    public void disconnect(){
        if(channel!=null){
            channel.disconnect();
        }
        if(session!=null){
            session.disconnect();
        }
    }
    
    public String getResponse(){
        return buffer.toString();
    }
    
     private Expect4j getExpect() {
        try {
            log.debug(String.format("Start logging to %s@%s:%s",user,ip,port));
            JSch jsch = new JSch();
            session = jsch.getSession(user, ip, port);
            session.setPassword(password);
            Hashtable<String, String> config = new Hashtable<String, String>();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
//            localUserInfo ui = new localUserInfo();
            UserInfo ui = new MyUserInfo(){
                public void showMessage(String message){
                  System.out.println(message);
                }
                public boolean promptYesNo(String message){
                	return true;
                }

                // If password is not given before the invocation of Session#connect(),
                // implement also following methods,
                //   * UserInfo#getPassword(),
                //   * UserInfo#promptPassword(String message) and
                //   * UIKeyboardInteractive#promptKeyboardInteractive()

              };
            session.setUserInfo(ui);
            session.connect(30000);
            channel = (ChannelShell) session.openChannel("shell");
            Expect4j expect = new Expect4j(channel.getInputStream(), channel
                    .getOutputStream());
            channel.connect();
            log.debug(String.format("Logging to %s@%s:%s successfully!",user,ip,port));
            return expect;
        } catch (Exception ex) {
            log.error("Connect to "+ip+":"+port+"failed,please check your username and password!");
            ex.printStackTrace();
        }
        return null;
    }

    public boolean executeCommands(String[] commands) {
        if(expect==null){
            return false;
        }
        
        log.debug("----------Running commands are listed as follows:----------");
        for(String command:commands){
            log.debug(command);
        }
        log.debug("----------End----------");
        
        Closure closure = new Closure() {
            public void run(ExpectState expectState) throws Exception {
                buffer.append(expectState.getBuffer());// buffer is string
                                                        // buffer for appending
                                                        // output of executed
                                                        // command
                expectState.exp_continue();
                
            }
        };
        List<Match> lstPattern = new ArrayList<Match>();
        String[] regEx = linuxPromptRegEx;
        if (regEx != null && regEx.length > 0) {
            synchronized (regEx) {
                for (String regexElement : regEx) {// list of regx like, :>, />
                                                    // etc. it is possible
                                                    // command prompts of your
                                                    // remote machine
                    try {
                        RegExpMatch mat = new RegExpMatch(regexElement, closure);
                        lstPattern.add(mat);
                    } catch (MalformedPatternException e) {
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                }
                lstPattern.add(new EofMatch(new Closure() { // should cause
                                                            // entire page to be
                                                            // collected
                            public void run(ExpectState state) {
                            }
                        }));
                lstPattern.add(new TimeoutMatch(defaultTimeOut, new Closure() {
                    public void run(ExpectState state) {
                    }
                }));
            }
        }
        try {
            boolean isSuccess = true;
            for (String strCmd : commands){
                isSuccess = isSuccess(lstPattern, strCmd);
            }
            isSuccess = !checkResult(expect.expect(lstPattern));
            
            String response=buffer.toString().toLowerCase();
            for(String msg:errorMsg){
                if(response.indexOf(msg)>-1){
                    return false;
                }
            }
            
            return isSuccess;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean isSuccess(List<Match> objPattern, String strCommandPattern) {
        try {
            boolean isFailed = checkResult(expect.expect(objPattern));
            if (!isFailed) {
                expect.send(strCommandPattern);
                expect.send("\r");
                return true;
            }
            return false;
        } catch (MalformedPatternException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkResult(int intRetVal) {
        if (intRetVal == COMMAND_EXECUTION_SUCCESS_OPCODE) {
            return true;
        }
        return false;
    }
    
    public static class localUserInfo implements UserInfo {
        String passwd;

        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str) {
        	System.out.println(str);
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
        	System.out.println(message);
            return false;
        }

        public boolean promptPassword(String message) {
        	System.out.println(message);
            return false;
        }

        public void showMessage(String message) {
            System.out.println(message);
        }
    }
    public static abstract class MyUserInfo
    	implements UserInfo, UIKeyboardInteractive{
    	public String getPassword(){ return null; }
    	public boolean promptYesNo(String str){ return false; }
    	public String getPassphrase(){ return null; }
    	public boolean promptPassphrase(String message){ return false; }
    	public boolean promptPassword(String message){ return false; }
    	public void showMessage(String message){ }
    	public String[] promptKeyboardInteractive(String destination,
                        String name,
                        String instruction,
                        String[] prompt,
                        boolean[] echo){
    		return null;
    	}
    }
   
    public static void main(String [] argv) {
    	SSHClient client = new SSHClient("172.21.3.9", 22, "djyang", "sycamore");
    	String [] cmdList = {"pwd","ls --color=never","mkdir ssh_tmp"};
    	client.executeCommands(cmdList);
    }
}
