package com.cmb.api.sfg;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

public class ManageSftpConn {
	
	public static void main(String[] args) {
        System.out.print("Going to connect");

		
		JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession("axway", "192.168.47.131", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("axway");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            System.out.print("Connected");

//          sftpChannel.get("hello.txt", "localfile.txt");
            
          InputStream in = sftpChannel.get( "license.xml" );
          
          ByteArrayOutputStream result = new ByteArrayOutputStream();
          byte[] buffer = new byte[1024];
          int length;
          while ((length = in.read(buffer)) != -1) {
              result.write(buffer, 0, length);
          }
          
          System.out.print(result.toString("UTF-8"));
          
          sftpChannel.isConnected();
          System.out.print("The connection is " + sftpChannel.isConnected());
          
          sftpChannel.exit();
          session.disconnect();
            
        } catch (JSchException e) {
            e.printStackTrace();  
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
