//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.pgp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.columba.core.io.DiskIO;
import org.columba.core.util.StreamThread;
import org.columba.mail.config.PGPItem;

public abstract class DefaultUtil {
	protected StreamThread outputStream = null;
	protected StreamThread errorStream = null;

	protected File outputFile;
	protected File inputFile;

	protected String outputString;

	protected Process executeCommand(String cmd) throws Exception {
		Process p = Runtime.getRuntime().exec(cmd);

		return p;
	}

	protected abstract String parse(String str);

	// error gets parsed
	public String getErrorString() {
		String str = parse(errorStream.getBuffer());
		return str;
	}

	public String getOutputString() {
		String str = outputStream.getBuffer();
		return str;
	}

	public String getResult() {
		return outputString;
	}

	protected abstract String getRawCommandString(int type);

	protected String getCommandString(int type, PGPItem item) {
		String rawCmd = getRawCommandString(type);
		StringBuffer command = new StringBuffer(item.get("path"));
		command.append(" ");

		int varStartIndex = rawCmd.indexOf("%");
		int varEndIndex = -1;
		String varName;

		while (varStartIndex != -1) {
			command.append(rawCmd.substring(varEndIndex + 1, varStartIndex));
			varEndIndex = rawCmd.indexOf("%", varStartIndex + 1);

			varName = rawCmd.substring(varStartIndex + 1, varEndIndex);

			command.append(getValue(varName, item));

			varStartIndex = rawCmd.indexOf("%", varEndIndex + 1);
		}

		command.append(rawCmd.substring(varEndIndex + 1));

		return command.toString();
	}

	private String getValue(String name, PGPItem item) {

		if (name.equals("user")) {
			return item.get("id");
		}
		if (name.equals("input_file")) {
			return inputFile.toString();
		}
		if (name.equals("output_file")) {
			return outputFile.toString();
		}

		return null;
	}

	protected void sendToStdin(Process p, String passphrase) throws Exception {
		PrintWriter out = new PrintWriter(p.getOutputStream());
		out.println(passphrase);
		out.flush();
		out.close();
	}

	/*
	  public int decrypt( String path, String pgpMessage, String passphrase ) throws Exception
	  {
	      int exitVal = -1;
	
	      Process p = executeCommand( getCommandString(PGPController.DECRYPT_ACTION) );
	
	      errorStream = new StreamThread(p.getErrorStream(), "ERROR");
	      outputStream = new StreamThread(p.getInputStream(), "OUTPUT");
	
	      sendPassphrase( p, passphrase );
	
	      errorStream.start();
	      outputStream.start();
	
	      exitVal = p.waitFor();
	
	      System.out.println("exitvalue: "+ exitVal );
	
	      // wait for stream threads to die
	      outputStream.join();
	      errorStream.join();
	
	      return exitVal;
	  }
	
	  public int verify( String path, String pgpMessage, String signatureString, String passphrase ) throws Exception
	  {
	      int exitVal = -1;
	
	      Process p = executeCommand( getCommandString(PGPController.VERIFY_ACTION) );
	
	      errorStream = new StreamThread(p.getErrorStream(), "ERROR");
	      outputStream = new StreamThread(p.getInputStream(), "OUTPUT");
	
	      sendPassphrase( p, passphrase );
	
	      errorStream.start();
	      outputStream.start();
	
	      exitVal = p.waitFor();
	
	      System.out.println("exitvalue: "+ exitVal );
	
	      // wait for stream threads to die
	      outputStream.join();
	      errorStream.join();
	
	      return exitVal;
	  }
	
	*/

	private File createTempFile(String contents) {
		try {
			File tempFile1 = File.createTempFile("columba" + System.currentTimeMillis(), null);
			tempFile1.deleteOnExit();
			DiskIO.saveStringInFile(tempFile1, contents);

			return tempFile1;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public int sign(PGPItem item, String input) throws Exception {
		int exitVal = -1;

		//System.out.println( getCommandString(PGPController.SIGN_ACTION, item) );

		Process p = executeCommand(getCommandString(PGPController.SIGN_ACTION, item));
		errorStream = new StreamThread(p.getErrorStream(), "ERROR");
		outputStream = new StreamThread(p.getInputStream(), "OUTPUT");

		sendToStdin(p, item.getPassphrase());

		sendToStdin(p, input);

		errorStream.start();
		outputStream.start();

		exitVal = p.waitFor();

		// wait for stream threads to die
		outputStream.join();
		errorStream.join();

		outputString = outputStream.getBuffer();

		return exitVal;
	}

	/*
	  public int encrypt( String path, String pgpMessage, String passphrase, boolean signValue,  Vector recipient, String id ) throws Exception
	  {
	      int exitVal = -1;
	
	      Process p = executeCommand( getCommandString(PGPController.ENCRYPT_ACTION) );
	
	      errorStream = new StreamThread(p.getErrorStream(), "ERROR");
	      outputStream = new StreamThread(p.getInputStream(), "OUTPUT");
	
	      if ( signValue == true )
	      {
	          sendPassphrase( p, passphrase );
	      }
	
	      errorStream.start();
	      outputStream.start();
	
	      exitVal = p.waitFor();
	
	      System.out.println("exitvalue: "+ exitVal );
	
	      // wait for stream threads to die
	      outputStream.join();
	      errorStream.join();
	
	      return exitVal;
	  }
	*/

}
