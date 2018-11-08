/* 
  * SSHManager
  * 
  * @author cabbott
  * @version 1.0
  */
//  package cabbott.net;
package com.cmb.api.sfg;

  import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import static org.junit.Assert.fail;

import java.io.IOException;
  import java.io.InputStream;
import java.util.Vector;
import java.util.logging.Level;
  import java.util.logging.Logger;
//  import com.vordel.trace.Trace;
  
  
  public class SSHManager2
  {
  private static final Logger LOGGER = 
      Logger.getLogger(SSHManager2.class.getName());
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

  public SSHManager2(String userName, String password, 
     String connectionIP, String knownHostsFileName)
  {
     doCommonConstructorActions(userName, password, 
                connectionIP, knownHostsFileName);
     intConnectionPort = 22;
     intTimeOut = 60000;
  }

  public SSHManager2(String userName, String password, String connectionIP, 
     String knownHostsFileName, int connectionPort)
  {
     doCommonConstructorActions(userName, password, connectionIP, 
        knownHostsFileName);
     intConnectionPort = connectionPort;
     intTimeOut = 60000;
  }
  
  public SSHManager2(String userName, String connectionIP, 
		     String knownHostsFileName, int connectionPort, String certFile)
  {
		     doCommonConstructorActionsKey(userName, certFile, connectionIP, 
		        knownHostsFileName);
		     intConnectionPort = connectionPort;
		     intTimeOut = 60000;
  }

  public SSHManager2(String userName, String password, String connectionIP, 
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
//	Trace.debug("********preparing to send command********");
//	Trace.debug(command);

     try
     {
//        Channel channel = sesConnection.openChannel("exec");
        Channel channel = sesConnection.openChannel("sftp");

        ((ChannelExec)channel).setCommand(command);
        InputStream commandOutput = channel.getInputStream();
        channel.connect();
//    	Trace.debug("********channel connect********");

        int readByte = commandOutput.read();

        while(readByte != 0xffffffff)
        {
           outputBuffer.append((char)readByte);
           readByte = commandOutput.read();
        }
        
//    	Trace.debug("readbyte: " +readByte);

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

  
  public String mkdir(String path)
  {
    StringBuilder outputBuffer = new StringBuilder();
//	Trace.debug("********preparing to send command********");
//	Trace.debug(path);
    String errorMessage = null;
	ChannelSftp channelSftp = null;
	Channel channel = null;
     try
     {
//      Channel channel = sesConnection.openChannel("exec");
        channel = sesConnection.openChannel("sftp");

//      ((ChannelExec)channel).setCommand(command);
//      InputStream commandOutput = channel.getInputStream();
        channel.connect();
//    	Trace.debug("********channel connect********");

    	channelSftp = (ChannelSftp)channel;
        channelSftp.mkdir(path);
        
//        int readByte = commandOutput.read();
//
//        while(readByte != 0xffffffff)
//        {
//           outputBuffer.append((char)readByte);
//           readByte = commandOutput.read();
//        }
        
//    	Trace.debug("the end of mkdir command...");

        channel.disconnect();
     }
     catch(JSchException jschX)
     {
//        logWarning(jschX.getMessage());
		errorMessage = jschX.getMessage();
        return errorMessage;
     } catch (SftpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
//    	Trace.debug("Sftp Error..." + e.toString());
		System.out.println("Sftp exception...." + e);
		errorMessage = e.toString();
        return errorMessage;
	}finally {
		if(channel != null) channel.disconnect();
	}

     return errorMessage;
  	}
  
  public String rmdir(String path)
  {
    StringBuilder outputBuffer = new StringBuilder();
//	Trace.debug("********preparing to send command********");
//	Trace.debug(path);
    String errorMessage = null;
	ChannelSftp channelSftp = null;
	Channel channel = null;
     try
     {
//      Channel channel = sesConnection.openChannel("exec");
        channel = sesConnection.openChannel("sftp");

//      ((ChannelExec)channel).setCommand(command);
//      InputStream commandOutput = channel.getInputStream();
        channel.connect();
//    	Trace.debug("********channel connect********");

    	channelSftp = (ChannelSftp)channel;
        channelSftp.rmdir(path);
        
//        int readByte = commandOutput.read();
//
//        while(readByte != 0xffffffff)
//        {
//           outputBuffer.append((char)readByte);
//           readByte = commandOutput.read();
//        }
        
//    	Trace.debug("the end of mkdir command...");

        channel.disconnect();
     }
     catch(JSchException jschX)
     {
//        logWarning(jschX.getMessage());
		errorMessage = jschX.getMessage();
        return errorMessage;
     } catch (SftpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
//    	Trace.debug("Sftp Error..." + e.toString());
		System.out.println("Sftp exception...." + e);
		errorMessage = e.toString();
        return errorMessage;
	}finally {
		if(channel != null) channel.disconnect();
	}

     return errorMessage;
  	}
  
  
  public String rm(String path)
  {
    StringBuilder outputBuffer = new StringBuilder();
//	Trace.debug("********preparing to send command********");
//	Trace.debug(path);
    String errorMessage = null;
	ChannelSftp channelSftp = null;
	Channel channel = null;
     try
     {
//      Channel channel = sesConnection.openChannel("exec");
        channel = sesConnection.openChannel("sftp");

//      ((ChannelExec)channel).setCommand(command);
//      InputStream commandOutput = channel.getInputStream();
        channel.connect();
//    	Trace.debug("********channel connect********");

    	channelSftp = (ChannelSftp)channel;
        channelSftp.rm(path);
        
//        int readByte = commandOutput.read();
//
//        while(readByte != 0xffffffff)
//        {
//           outputBuffer.append((char)readByte);
//           readByte = commandOutput.read();
//        }
        
//    	Trace.debug("the end of mkdir command...");

        channel.disconnect();
     }
     catch(JSchException jschX)
     {
//        logWarning(jschX.getMessage());
		errorMessage = jschX.getMessage();
        return errorMessage;
     } catch (SftpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
//    	Trace.debug("Sftp Error..." + e.toString());
		System.out.println("Sftp exception...." + e);
		errorMessage = e.toString();
        return errorMessage;
	}finally {
		if(channel != null) channel.disconnect();
	}

     return errorMessage;
  	}
  
  
  public String ls(String path)
  {
    StringBuilder outputBuffer = new StringBuilder();
//	Trace.debug("********preparing to send command********");
//	Trace.debug(path);
    String errorMessage = null;
	ChannelSftp channelSftp = null;
	Channel channel = null;
	String resMessage = "";
	
     try
     {
//      Channel channel = sesConnection.openChannel("exec");
        channel = sesConnection.openChannel("sftp");

//      ((ChannelExec)channel).setCommand(command);
//      InputStream commandOutput = channel.getInputStream();
        channel.connect();
//    	Trace.debug("********channel connect********");

    	channelSftp = (ChannelSftp)channel;
//    	resMessage = channelSftp.ls(path);
    	
    	Vector filelist = channelSftp.ls(path);
    	System.out.println("the nubmer of the file is: " + filelist.size());
        for(int i=0; i<filelist.size();i++){
        	
            LsEntry entry = (LsEntry) filelist.get(i);
            if(entry.getFilename().equalsIgnoreCase(".") || entry.getFilename().equalsIgnoreCase("..") ) continue;
            
            SftpATTRS attrs = entry.getAttrs();

            resMessage = resMessage+ "{\"isDir\": \"" + attrs.isDir() +"\",";
            
            resMessage = resMessage+ "\"name\": \"" + entry.getFilename() +"\",";
            
            if(!attrs.isDir()) {
            	resMessage = resMessage+ "\"size\": " + attrs.getSize() +",";
            }

            resMessage = resMessage+ "\"lastModifiedTime\": \"" + attrs.getMtimeString() +"\"},";
//            System.out.println(entry);
            
//            System.out.println(attrs.getMtimeString());
//            System.out.println(attrs.getSize());

//            if(i == 0) {
//                resMessage = resMessage+ "\"" + entry.getFilename() +"\",";
//            }else if (i == filelist.size() -1) {
//                resMessage = resMessage+ "\"" + entry.getFilename() +"\"";
//            }else {
//                resMessage = resMessage+ "\"" + entry.getFilename() +"\",";
//            }
//          System.out.println(entry.getFilename());
//            System.out.println(resMessage);
        }
        
        //remove the unwanted last comma
        resMessage = resMessage.substring(0, resMessage.length() -1);
        
//        int readByte = commandOutput.read();
//
//        while(readByte != 0xffffffff)
//        {
//           outputBuffer.append((char)readByte);
//           readByte = commandOutput.read();
//        }
        
//    	Trace.debug("the end of mkdir command...");

        channel.disconnect();
     }
     catch(JSchException jschX)
     {
//        logWarning(jschX.getMessage());
//		errorMessage = jschX.getMessage();
        return "false";
     } catch (SftpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
//    	Trace.debug("Sftp Error..." + e.toString());
		System.out.println("Sftp exception...." + e);
//		errorMessage = e.toString();
        return "false";
	}finally {
		if(channel != null) channel.disconnect();
	}
     
         //no error, return response from ls command
     System.out.println("list folder success...");
     return resMessage;
  }
  
  
  public String lstat(String path)
  {
    StringBuilder outputBuffer = new StringBuilder();
//	Trace.debug("********preparing to send command********");
//	Trace.debug(path);
    String errorMessage = null;
	ChannelSftp channelSftp = null;
	Channel channel = null;
	String resMessage = "";
	
     try
     {
//      Channel channel = sesConnection.openChannel("exec");
        channel = sesConnection.openChannel("sftp");

//      ((ChannelExec)channel).setCommand(command);
//      InputStream commandOutput = channel.getInputStream();
        channel.connect();
//    	Trace.debug("********channel connect********");

    	channelSftp = (ChannelSftp)channel;
//    	resMessage = channelSftp.ls(path);
    	
    	SftpATTRS attrs  = channelSftp.lstat(path);
    	
        System.out.println(attrs.getMtimeString());
        System.out.println(attrs.getSize());
        
      resMessage = attrs.getMtimeString()+ "," + attrs.getSize() ;

//    	System.out.println("the nubmer of the file is: " + filelist.size());
//        for(int i=0; i<filelist.size();i++){
//        	
//            LsEntry entry = (LsEntry) filelist.get(i);
//            if(entry.getFilename().equalsIgnoreCase(".") || entry.getFilename().equalsIgnoreCase("..") ) continue;
//            
//            resMessage = resMessage+ "\"" + entry.getFilename() +"\",";
////            System.out.println(entry);
//
//            SftpATTRS attrs = entry.getAttrs();
//            
//            System.out.println(attrs.getMtimeString());
//            System.out.println(attrs.getSize());
//
////            if(i == 0) {
////                resMessage = resMessage+ "\"" + entry.getFilename() +"\",";
////            }else if (i == filelist.size() -1) {
////                resMessage = resMessage+ "\"" + entry.getFilename() +"\"";
////            }else {
////                resMessage = resMessage+ "\"" + entry.getFilename() +"\",";
////            }
////          System.out.println(entry.getFilename());
////            System.out.println(resMessage);
//        }
        
        
//        int readByte = commandOutput.read();
//
//        while(readByte != 0xffffffff)
//        {
//           outputBuffer.append((char)readByte);
//           readByte = commandOutput.read();
//        }
        
//    	Trace.debug("the end of mkdir command...");

        channel.disconnect();
     }
     catch(JSchException jschX)
     {
//        logWarning(jschX.getMessage());
//		errorMessage = jschX.getMessage();
        return "false";
     } catch (SftpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
//    	Trace.debug("Sftp Error..." + e.toString());
		System.out.println("Sftp exception...." + e);
//		errorMessage = e.toString();
        return "false";
	}finally {
		if(channel != null) channel.disconnect();
	}
     
     //no error, return response from ls command
     System.out.println("list file success...");
     return resMessage;
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
	     SSHManager2 instance = new SSHManager2(userName, password, connectionIP, "");
	     
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
  
  public static String createFolder(String command, String userName, String key, String connectionIP, int port, String certPath) {
	  
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
	    SSHManager2 instance = new SSHManager2(userName, connectionIP, "", port, key);
//		Trace.debug("********begin to connect********");
	     
	     //SSHManager for key authentication
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

	        System.out.println(errorMessage);
	        return "false";
	     }
	     
	     System.out.println("going to create folder");
	     String result = instance.mkdir(command);

	     if(result != null)
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

//	        System.out.println(result);
		    instance.close();
	        return "false";
	     }
	     
	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
//	     String result = instance.sendCommand(command);
	     
//	     System.out.println(result);
	     // close only after all commands are sent
	     instance.close();
	  
	  return "success";
  }
  
 public static String deleteFolder(String command, String userName, String key, String connectionIP, int port, String certPath) {
	  
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
	    SSHManager2 instance = new SSHManager2(userName, connectionIP, "", port, key);
//		Trace.debug("********begin to connect********");
	     
	     //SSHManager for key authentication
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

	        System.out.println(errorMessage);
	        return "false";
	     }
	     
	     System.out.println("going to create folder");
	     String result = instance.rmdir(command);

	     if(result != null)
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

//	        System.out.println(result);
		    instance.close();
	        return "false";
	     }
	     
	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
//	     String result = instance.sendCommand(command);
	     
//	     System.out.println(result);
	     // close only after all commands are sent
	     instance.close();
	  
	  return "success";
  }
 
 public static String deleteFile(String command, String userName, String key, String connectionIP, int port, String certPath) {
	  
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
	    SSHManager2 instance = new SSHManager2(userName, connectionIP, "", port, key);
//		Trace.debug("********begin to connect********");
	     
	     //SSHManager for key authentication
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

	        System.out.println(errorMessage);
	        return "false";
	     }
	     
	     System.out.println("going to create folder");
	     String result = instance.rm(command);

	     if(result != null)
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

//	        System.out.println(result);
		    instance.close();
	        return "false";
	     }
	     
	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
//	     String result = instance.sendCommand(command);
	     
//	     System.out.println(result);
	     // close only after all commands are sent
	     instance.close();
	  
	  return "success";
 }
 
 
 public static String listFile(String command, String userName, String key, String connectionIP, int port, String certPath) {
	  
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
	    SSHManager2 instance = new SSHManager2(userName, connectionIP, "", port, key);
//		Trace.debug("********begin to connect********");
	     
	     //SSHManager for key authentication
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

	        System.out.println(errorMessage);
	        return "false";
	     }
	     
	     
	     System.out.println("going to list folder");
	     String result = instance.lstat(command);
	     System.out.println(result);

	     
	     if(result == "false")
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

	        System.out.println(result);
		    instance.close();
	        return "false";
	     }
	     
	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
//	     String result = instance.sendCommand(command);
	     
//	     System.out.println(result);
	     // close only after all commands are sent
	     instance.close();
	  
	  return result;
}
 
 public static String listFolder(String command, String userName, String key, String connectionIP, int port, String certPath) {
	  
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
	    SSHManager2 instance = new SSHManager2(userName, connectionIP, "", port, key);
//		Trace.debug("********begin to connect********");
	     
	     //SSHManager for key authentication
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

	        System.out.println(errorMessage);
	        return "false";
	     }
	     
	     System.out.println("going to create folder");
	     String result = instance.ls(command);

	     if(result == "false")
	     {
//	 		Trace.debug("********connection failed********");
//	 		Trace.debug(errorMessage);

//	        System.out.println(result);
		    instance.close();
	        return "false";
	     }
	     
	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
//	     String result = instance.sendCommand(command);
	     
//	     System.out.println(result);
	     // close only after all commands are sent
	     instance.close();
	  
	  return result;
}
 
 
  
  public static void main(String[] args) {
	  
//	  boolean b = execPwd("ls", "axway","axway","192.168.47.131","");
//	  boolean b = execKey("mkdir 30102018", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");

//	  String res = execKey("ls", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");
//	  String res = createFolder("success2", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");
//	  String res = deleteFolder("success2", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");
//	  String res = deleteFile("success/customizing-your-apis-lifecycle-3.png", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");
	  
	  String res = listFolder(".", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");
	  
//	  String res = listFile("success/customizing-your-apis-lifecycle-3.png", "axway","C:\\Users\\dwang\\Downloads\\temp4\\id_rsa","192.168.47.131", 22, "");

	  if(res == "false") {
		  //this is error
		  System.out.println("can't create folder");
	  }else {
		  //this is success
		  System.out.println("create folder successfully");
	  }
	  System.out.println("The response is: "+res);
	  
	  System.out.println("printing more logs...");
  }
  
}

  