/* 
  * SSHManager
  * 
  * @author cabbott
  * @version 1.0
  */
//  package cabbott.net;
package com.cmb.api.sfg;

  import com.jcraft.jsch.*;

import static org.junit.Assert.fail;

import java.io.IOException;
  import java.io.InputStream;
  import java.util.logging.Level;
  import java.util.logging.Logger;
  import com.vordel.trace.Trace;
  
  
  public class SSHManager
  {
  private static final Logger LOGGER = 
      Logger.getLogger(SSHManager.class.getName());
  private JSch jschSSHChannel;
  private String strUserName;
  private String strConnectionIP;
  private int intConnectionPort;
  private String strPassword;
  private Session sesConnection;
  private int intTimeOut;
  private String strAuthentication;
  private String strKey;


  private void doCommonConstructorActions(String userName, 
       String password, String connectionIP, String knownHostsFileName)
  {
     jschSSHChannel = new JSch();

     try
     {
        jschSSHChannel.setKnownHosts(knownHostsFileName);
     }
     catch(JSchException jschX)
     {
        logError(jschX.getMessage());
     }
     
     strAuthentication = "pwd";
     strUserName = userName;
     strPassword = password;
     strConnectionIP = connectionIP;
  }
  
  private void doCommonConstructorActionsKey(String userName, 
	       String key, String connectionIP, String knownHostsFileName)
	  {
	     jschSSHChannel = new JSch();

	     try
	     {
	        jschSSHChannel.setKnownHosts(knownHostsFileName);
		    jschSSHChannel.addIdentity(key);
	     }
	     catch(JSchException jschX)
	     {
	        logError(jschX.getMessage());
	     }
	     
	     strAuthentication = "key";
	     strUserName = userName;
	     strKey = key;
	     strConnectionIP = connectionIP;
	  }

  public SSHManager(String userName, String password, 
     String connectionIP, String knownHostsFileName)
  {
     doCommonConstructorActions(userName, password, 
                connectionIP, knownHostsFileName);
     intConnectionPort = 22;
     intTimeOut = 60000;
  }

  public SSHManager(String userName, String password, String connectionIP, 
     String knownHostsFileName, int connectionPort)
  {
     doCommonConstructorActions(userName, password, connectionIP, 
        knownHostsFileName);
     intConnectionPort = connectionPort;
     intTimeOut = 60000;
  }
  
  public SSHManager(String userName, String connectionIP, 
		     String knownHostsFileName, int connectionPort, String certFile)
  {
		     doCommonConstructorActionsKey(userName, certFile, connectionIP, 
		        knownHostsFileName);
		     intConnectionPort = connectionPort;
		     intTimeOut = 60000;
  }

  public SSHManager(String userName, String password, String connectionIP, 
      String knownHostsFileName, int connectionPort, int timeOutMilliseconds)
  {
     doCommonConstructorActions(userName, password, connectionIP, 
         knownHostsFileName);
     intConnectionPort = connectionPort;
     intTimeOut = timeOutMilliseconds;
  }

  public String connect()
  {
     String errorMessage = null;

     try
     {
        sesConnection = jschSSHChannel.getSession(strUserName, 
        strConnectionIP, intConnectionPort);
        //if the authentication is set to password, then use password. Otherwise, the private key file is already set in the constructor.
        if(strAuthentication.equalsIgnoreCase("pwd")) {
            sesConnection.setPassword(strPassword);
        }
        
        // UNCOMMENT THIS FOR TESTING PURPOSES, BUT DO NOT USE IN PRODUCTION
        sesConnection.setConfig("StrictHostKeyChecking", "no");
        sesConnection.connect(intTimeOut);
     }
     catch(JSchException jschX)
     {
        errorMessage = jschX.getMessage();
     }

     return errorMessage;
  }

  private String logError(String errorMessage)
  {
     if(errorMessage != null)
     {
        LOGGER.log(Level.SEVERE, "{0}:{1} - {2}", 
            new Object[]{strConnectionIP, intConnectionPort, errorMessage});
     }

     return errorMessage;
  }

  private String logWarning(String warnMessage)
  {
     if(warnMessage != null)
     {
        LOGGER.log(Level.WARNING, "{0}:{1} - {2}", 
           new Object[]{strConnectionIP, intConnectionPort, warnMessage});
     }

     return warnMessage;
  }

  public String sendCommand(String command)
  {
     StringBuilder outputBuffer = new StringBuilder();
	Trace.debug("********preparing to send command********");
	Trace.debug(command);

     try
     {
//        Channel channel = sesConnection.openChannel("exec");
        Channel channel = sesConnection.openChannel("sftp");

        ((ChannelExec)channel).setCommand(command);
        InputStream commandOutput = channel.getInputStream();
        channel.connect();
    	Trace.debug("********channel connect********");

        int readByte = commandOutput.read();

        while(readByte != 0xffffffff)
        {
           outputBuffer.append((char)readByte);
           readByte = commandOutput.read();
        }
        
    	Trace.debug("readbyte: " +readByte);

        channel.disconnect();
     }
     catch(IOException ioX)
     {
        logWarning(ioX.getMessage());
        return null;
     }
     catch(JSchException jschX)
     {
        logWarning(jschX.getMessage());
        return null;
     }

     return outputBuffer.toString();
  }

  public void close()
  {
     sesConnection.disconnect();
  }
  
  public static boolean execPwd(String command, String userName, String password, String connectionIP, String certPath) {
	  
	  System.out.println("sendCommand");

	     /**
	      * YOU MUST CHANGE THE FOLLOWING
	      * FILE_NAME: A FILE IN THE DIRECTORY
	      * USER: LOGIN USER NAME
	      * PASSWORD: PASSWORD FOR THAT USER
	      * HOST: IP ADDRESS OF THE SSH SERVER
	     **/
//	     String command = "ls license.xml";
//	     String userName = "axway";
//	     String password = "axway";
//	     String connectionIP = "192.168.47.131";
	  
	  	//SSHManager for password authentication
	     SSHManager instance = new SSHManager(userName, password, connectionIP, "");
	     
	     //SSHManager for key authentication
	    
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
	        System.out.println(errorMessage);
	        return false;
	     }

	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
	     String result = instance.sendCommand(command);
	     System.out.println(result);
	     // close only after all commands are sent
	     instance.close();
	  
	  return true;
  }
  
  public static String execKey(String command, String userName, String key, String connectionIP, int port, String certPath) {
	  
	  System.out.println("sendCommand...");

	     /**
	      * YOU MUST CHANGE THE FOLLOWING
	      * FILE_NAME: A FILE IN THE DIRECTORY
	      * USER: LOGIN USER NAME
	      * PASSWORD: PASSWORD FOR THAT USER
	      * HOST: IP ADDRESS OF THE SSH SERVER
	     **/
//	     String command = "ls license.xml";
//	     String userName = "axway";
//	     String password = "axway";
//	     String connectionIP = "192.168.47.131";
	  
	  	//SSHManager for password authentication
	    SSHManager instance = new SSHManager(userName, connectionIP, "", port, key);
		Trace.debug("********begin to connect********");
	     
	     //SSHManager for key authentication
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
	 		Trace.debug("********connection failed********");
	 		Trace.debug(errorMessage);

	        System.out.println(errorMessage);
	        return "false";
	     }

	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
	     String result = instance.sendCommand(command);
	     System.out.println(result);
	     // close only after all commands are sent
	     instance.close();
	  
	  return result;
  }
  
  public static void main(String[] args) {
	  
//	  boolean b = execPwd("ls", "axway","axway","192.168.47.131","");
//	  boolean b = execKey("mkdir 30102018", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");

	  String res = execKey("ls", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");

	  System.out.print(res);
  }
}

  