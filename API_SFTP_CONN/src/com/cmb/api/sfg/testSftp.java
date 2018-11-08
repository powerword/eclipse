
package com.cmb.api.sfg;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class testSftp {

	@Test
	void test() {
		System.out.println("sendCommand");

	     /**
	      * YOU MUST CHANGE THE FOLLOWING
	      * FILE_NAME: A FILE IN THE DIRECTORY
	      * USER: LOGIN USER NAME
	      * PASSWORD: PASSWORD FOR THAT USER
	      * HOST: IP ADDRESS OF THE SSH SERVER
	     **/
//	     String command = "ls license.xml";
	     String command = "mkdir ./helloworld";

	     String userName = "axway";
	     String password = "axway";
	     String connectionIP = "192.168.47.131";
	     SSHManager instance = new SSHManager(userName, password, connectionIP, "");
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
	        System.out.println(errorMessage);
//	        fail("failed test case send command");
	     }

	     String expResult = "license.xml";
	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
	     String result = instance.sendCommand(command);
	     // close only after all commands are sent
	     instance.close();
	     assertEquals(expResult, result);
	}

		
	public void testSendCommand()
	  {
	     System.out.println("sendCommand");

	     /**
	      * YOU MUST CHANGE THE FOLLOWING
	      * FILE_NAME: A FILE IN THE DIRECTORY
	      * USER: LOGIN USER NAME
	      * PASSWORD: PASSWORD FOR THAT USER
	      * HOST: IP ADDRESS OF THE SSH SERVER
	     **/
	     String command = "ls license.xml";
	     String userName = "axway";
	     String password = "axway";
	     String connectionIP = "192.168.47.131";
	     SSHManager instance = new SSHManager(userName, password, connectionIP, "");
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
	        System.out.println(errorMessage);
	        fail("failed test case send command");
	     }

	     String expResult = "FILE_NAME\n";
	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
	     String result = instance.sendCommand(command);
	     // close only after all commands are sent
	     instance.close();
	     assertEquals(expResult, result);
	  }
}
