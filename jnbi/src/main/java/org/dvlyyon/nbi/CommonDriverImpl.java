package org.dvlyyon.nbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dvlyyon.nbi.DriverEngineInf;
import org.dvlyyon.nbi.model.DObjectModel;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.LogUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;
import org.dvlyyon.nbi.SNIConstants;

import static org.dvlyyon.nbi.CommonConstants.*;

public class CommonDriverImpl {
	DriverEngineInf engine = null;
	String platformName = null;
	String objModelFileName = "cxt.xml";
	String separator = " ";
	String revision = "1.0.0";
	CommonCatsAgent writer = null;
	String fullFileName = null;
	int transID = 0;
	private static final Logger logger = Logger.getLogger(CommonDriverImpl.class.getName());
	
	public CommonDriverImpl() {
	}
	
	public void setCatsAgent(CommonCatsAgent agent) {
		this.writer = agent;
	}
	
	public void setFullFileName(String fileName) {
		fullFileName = fileName;
	}
	
	private void initWithProperties(DObjectModel objModel) {
		String value = objModel.getProperty("separator");
		if (value != null && !value.isEmpty()) {
			separator = value;
		}
		if (objModel.getProperty(OBJECT_MODEL_PROPERTY_REVISION) != null) {
			this.revision = objModel.getProperty(OBJECT_MODEL_PROPERTY_REVISION);
		}
		value = objModel.getProperty(OBJECT_MODEL_PROPERTY_LOG_LEVEL);
		if ( value != null) {
			LogUtils.setLevel(value);
			logger.info("set log level to " + value);
		}
		value = objModel.getProperty(OBJECT_MODEL_PROPERTY_LOG_SOURCE_FORMAT);
		if (value != null) {
			LogUtils.setSourceFormat(value);
			logger.info("set log source format to " + value);
		}
	}
	
	public InputStream getFile(RunState state) {
		//assume file in jar
		if (fullFileName == null) {
			fullFileName = objModelFileName;
		}
		if (!fullFileName.startsWith("/")) fullFileName = "/"+fullFileName;
		InputStream io=this.getClass().getResourceAsStream(fullFileName);
		if (io != null) return io;
		
		//assume file in a directory
		String filePath = CommonUtils.getPath();
		if (fullFileName == null)
			fullFileName = filePath+File.separator+objModelFileName;
		File file = new File(fullFileName);
		logger.fine("The full path file name is: " + fullFileName);
		if (!file.exists()) {
			fullFileName = filePath+File.separator+fullFileName;
			file = new File(fullFileName);
				if (!file.exists()) {
				logger.severe("Cannot find file with name "+fullFileName);
				state.setResult(State.ERROR);
				state.setErrorInfo("Cannot find file "+fullFileName);
				return null;
			}
		}
		try {
			io = new FileInputStream(file);
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
			state.setExp(e);
			return null;
		}
		return io;
	}
	
	public boolean init(RunState state) {
		this.writer = new CommonCatsAgent();
		logger.entering(CommonDriverImpl.class.getName(), "init");
		
		InputStream file = getFile(state);
		if (file == null) return false;
		
		DObjectModel objModel = new DObjectModel();
		objModel.retrieveProperties(file);
		String factoryName = objModel.getProperty(OBJECT_MODEL_PROPERTY_FACTORY);
		if (factoryName==null || factoryName.trim().isEmpty()) {
			logger.severe("Cannot find factory class name in XML file " + fullFileName);
			state.setResult(State.ERROR);
			state.setErrorInfo("Cannot find factory class name in XML file " + fullFileName);
			return false;
		}
		file = getFile(state);
		try {
			DriverFactoryInf factory = (DriverFactoryInf)Class.forName(factoryName).newInstance();
			objModel = factory.getObjectModel(file);
			platformName = objModel.getPlatformName();
			String nameSpace = objModel.getNameSpace();
			if (nameSpace!=null && !nameSpace.trim().isEmpty()) platformName = nameSpace;
			initWithProperties(objModel);
			engine = factory.createEngine(platformName, objModel);
			engine.setDriverFactory(factory);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Exception",e);
			state.setResult(State.EXCEPTION);
			state.setExp(e);
			state.setErrorInfo(e.getMessage());
			return false;
		}
		
		this.setCatsAgent(new CommonCatsAgent());
		logger.exiting(this.getClass().getName(),"init");
		return true;
	}
	
	private String replaceDoubleQuotes(String params) {
		if (params == null) return null;
		String str = params;
		int p = str.indexOf('"');
		while (p>0) {
			if (p==0) {
				str = "\\\""+str.substring(p+1);
			} else {
				str = str.substring(0, p)+"\\\""+str.substring(p+1);
			}
			p = str.indexOf('"', p+2);
		}
		return str;
	}
	
	public String doCmd(int transId, String cmd, String phyEntity, String actionName, String params) {
		if (!CommonUtils.isNullOrSpace(params) &&  params.equals("null")) params = null; 
    	String cmds = ""+transId+ " "+cmd+" \""+phyEntity+"\" \""+actionName+"\" \""+this.replaceDoubleQuotes(params)+"\"";
		String ret = null;
		Vector<String> err = new Vector<String>();
		if (SNIConstants.isDoCmd(cmd) || SNIConstants.isStubCmd(cmd)) {
			ret = engine.action(cmd, phyEntity, actionName, params);
		} else if (SNIConstants.isEndCmd(cmd)) {
			ret = engine.terminate();
		} else if (SNIConstants.isStartCaseCmd(cmd)) {
			ret = engine.startCase();
		} else if (SNIConstants.isEndCaseCmd(cmd)) {
			ret = engine.endCase();
		} else if (SNIConstants.isUndoCmd(cmd)) {
			ret = engine.unDoAction(phyEntity, actionName, params);
		} else if (SNIConstants.isDefineCmd(cmd)) {
			ret = engine.define(phyEntity);
		} else if (SNIConstants.isFunctionCmd(cmd)) {
			ret = engine.function(actionName, params, err);
			if (ret == null) {
				logger.info("Result of cmd "+cmd+", result:"+ret);
				return err.firstElement();
			}
			return ret;
		} else {
			ret = "Unknown driver command "+cmd;
		}
		logger.exiting(this.getClass().getName(), "doCmd");
		return ret;
	}
	
	public String preprocess(String cmd) {
		return CommonUtils.removeDualQuote(cmd);
	}
	
	public int getTransactionID() {
		return transID;
	}
	
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean first = true;
		while (true) {
			String line = "";
			String ret = "";
			try {
				writer.ready(platformName);
				logger.fine("ready for receiving command line for platform "+platformName+"......");
				line = in.readLine();
				if (line==null) continue;
				if (first) {
					writer.tellModelRevision(this.revision);
					writer.tellDriverRevision();
					first = false;
				}
				logger.info("receive command: \n\t"+line);
				logger.info("receive command: \n\t"+line.replaceAll(separator, "\t"));
				if (line != null && !(line.trim().isEmpty())) {
					String [] cmd = null;
					if (line.indexOf(separator) >= 0)
						cmd = line.split(separator);
					else 
						cmd = line.trim().split(" +");
					int transId = 0;
					try {
						transId = Integer.parseInt(cmd[0]);
						if (this.transID==0) {
							this.transID = transId;
							LogUtils.transID=cmd[0];
							logger.info("********************TRANSID: "+transID+"**************************");
						}
					}
					catch (NumberFormatException e) {
						logger.log(Level.SEVERE,"Invalid command",e);
						writer.reply("Invalid cmd");
						continue;
					}
					switch (cmd.length) {
						case 2: 
							ret = doCmd(transId, cmd[1].trim(), "", "", "");
							break;
						case 3:
							ret = doCmd(transId, cmd[1].trim(), preprocess(cmd[2].trim()), "" , "");
							break;
						case 4:
							ret = doCmd(transId, cmd[1].trim(), preprocess(cmd[2].trim()), preprocess(cmd[3].trim()), "");
							break;
						case 5:
							ret = doCmd(transId, cmd[1].trim(), preprocess(cmd[2].trim()), preprocess(cmd[3].trim()), preprocess(cmd[4].trim()));
							break;
						default:
							ret = "Invalid command format:" + line;								
					}
					if (cmd.length>1 && SNIConstants.isFunctionCmd(cmd[1].trim())) {
						if (ret==null) ret = " ";
						writer.replyFunction(ret);
					} else 
						writer.reply(ret);
					logger.info("sent result: "+ret);
					if (cmd.length>1 && SNIConstants.isEndCmd(cmd[1].trim())) {
						break;
					}
				}
			} catch (IOException e) {
				 e.printStackTrace();
				 logger.log(Level.SEVERE, "Run IOException: ", e);
				 ret = "Read command Exception - "+e.toString();
				 writer.reply(ret);
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE,"Run Exception: ", e);
				ret = "Exception - " + e.toString();
				writer.reply(ret);
			}
		}		
	}
	
	public void reply (String msg) {
		writer.reply(msg);
	}
	
	public static void main(String[] args) {
		RunState state = new RunState();
		LogUtils.initRollingFileLog("driver", state);
		logger.info("---------------------- DRIVER IS STARTING ------------------------");
		logger.info("Driver is starting...");
		CommonDriverImpl driver = new CommonDriverImpl();
		if (args.length > 0) {
			driver.setFullFileName(args[0]);
		}
		if (driver.init(state))
				driver.run();
			else
				driver.reply(state.getErrorInfo());
		logger.info("=====================DRIVER ("+driver.getTransactionID()+") IS END ===========================");
	}

}
