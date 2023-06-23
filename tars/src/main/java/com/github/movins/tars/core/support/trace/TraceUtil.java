package com.github.movins.tars.core.support.trace;

import com.github.movins.tars.api.client.CommunicatorConfig;
import com.github.movins.tars.api.client.util.ParseTools;
import com.github.movins.tars.api.server.config.ConfigurationManager;
import com.github.movins.tars.api.server.config.ServerConfig;

public class TraceUtil {
	
	public static boolean checkServant(String servant) {
		
    	//node
    	ServerConfig serverConfig = ConfigurationManager.getInstance().getServerConfig();
    	if (serverConfig == null) {
    		return true;
    	}
    	String node = ParseTools.parseSimpleObjectName(serverConfig.getNode());
    	if (servant.equals(node)) {
    		return false;
    	}
    	
    	//notify
    	String notify = ParseTools.parseSimpleObjectName(serverConfig.getNotify());
    	if (servant.equals(notify)) {
    		return false;
    	}
    	
    	//config
    	String config = ParseTools.parseSimpleObjectName(serverConfig.getConfig());
    	if (servant.equals(config)) {
    		return false;
    	}
    	
    	//log
    	String log = ParseTools.parseSimpleObjectName(serverConfig.getLog());
    	if (servant.equals(log)) {
    		return false;
    	}
    	
    	CommunicatorConfig clientConfig = serverConfig.getCommunicatorConfig();
    	if (clientConfig == null) {
    		return true;
    	}
    	
    	//stat
    	String stat = ParseTools.parseSimpleObjectName(clientConfig.getStat());
    	if (servant.equals(stat)) {
    		return false;
    	}
    	
    	//query
    	String query = ParseTools.parseSimpleObjectName(clientConfig.getLocator());
    	if (servant.equals(query)) {
    		return false;
    	}
    	
       	//property
    	String property = ParseTools.parseSimpleObjectName(clientConfig.getProperty());
    	if (servant.equals(property)) {
    		return false;
    	}
    	return true;
    	
    }

}
