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


/**
 * The special class witch handles the commandline parameters to sign, verify,
 * encrypt and decrypt messages with the gnu pgp tool named gpg.
 * @author waffel
 *
 */

public class GnuPGUtil extends DefaultUtil {
	static String[] cmd =
		{
			"--batch --no-tty --passphrase-fd 0 -d",
			"--batch --no-tty --armor --encrypt",
			"--no-secmem-warning --no-greeting --batch --yes --no-tty --armor --passphrase-fd 0 --output - --detach-sign -u %user% ",
			"--batch --no-tty --verify" };

	/**
	 * @see org.columba.mail.pgp.DefaultUtil#getRawCommandString(int)
	 */
	protected String getRawCommandString(int type) {
		return cmd[type];
	}

	/*
	  public int decrypt( String path, String pgpMessage, String passphrase ) throws Exception
	  {
	      File tempFile = File.createTempFile("columba"+System.currentTimeMillis(),null);
	      tempFile.deleteOnExit();
	      DiskIO.saveStringInFile( tempFile, pgpMessage );
	
	      setCommandString( path + " " + cmd[PGPController.DECRYPT_ACTION] + " " + tempFile.toString() );
	
	      int exitVal = super.decrypt( path, pgpMessage, passphrase );
	
	      if ( exitVal == 0 )
	      {
	          // everything worked ok
	          return 0;
	      }
	      else if ( exitVal == 1 )
	      {
	          // this means: decrypted message successfully, but
	          // was not able to verify the signature
	          return 1;
	      }
	      else if ( exitVal == 2 )
	      {
	          // unknown error
	          return 2;
	      }
	      else
	          return 2;
	
	  }
	
	
	  public int verify( String path, String pgpMessage, String signatureString, String passphrase ) throws Exception
	  {
	      int exitVal = -1;
	
	      File tempFile1 = File.createTempFile("columba"+System.currentTimeMillis(),null);
	      tempFile1.deleteOnExit();
	      DiskIO.saveStringInFile( tempFile1, pgpMessage );
	
	      File tempFile2 = File.createTempFile("columba"+System.currentTimeMillis(),null);
	      tempFile2.deleteOnExit();
	      DiskIO.saveStringInFile( tempFile2, signatureString );
	
	      setCommandString( path + " " + cmd[PGPController.VERIFY_ACTION] + " " + tempFile2.toString() + " " + tempFile1.toString() );
	
	      exitVal = super.verify( path, pgpMessage, signatureString, passphrase );
	
	      return exitVal;
	  }
	
	  public int sign( String path, String pgpMessage, String passphrase, String id ) throws Exception
	  {
	      int exitVal = -1;
	
	      File tempFile = File.createTempFile("columba"+System.currentTimeMillis(),null);
	      tempFile.deleteOnExit();
	      DiskIO.saveStringInFile( tempFile, pgpMessage );
	
	      outputFile = File.createTempFile( "columba"+System.currentTimeMillis(), null );
	      outputFile.deleteOnExit();
	      outputFile.delete();
	
	
	      String str = path + " --detach-sign " + tempFile +"--armor --output " + outputFile;
	
	      setCommandString( str );
	
	      exitVal = super.sign( path, pgpMessage, passphrase, signValue, recipient, id );
	
	      return exitVal;
	  }
	
	  public int encrypt( String path, String pgpMessage, String passphrase, boolean signValue,  Vector recipient, String id ) throws Exception
	  {
	      int exitVal = -1;
	
	      File tempFile = File.createTempFile("columba"+System.currentTimeMillis(),null);
	      tempFile.deleteOnExit();
	      DiskIO.saveStringInFile( tempFile, pgpMessage );
	
	      outputFile = File.createTempFile( "columba"+System.currentTimeMillis(), null );
	      outputFile.deleteOnExit();
	      outputFile.delete();
	
	      StringBuffer rcpt = new StringBuffer();
	      for ( int i=0; i<recipient.size(); i++ )
	      {
	          rcpt.append( (String) recipient.get(i) );
	          if ( i != recipient.size()-1 ) rcpt.append(" ");
	      }
	
	      String str = path + " --recipient " + rcpt.toString() +" --output " + outputFile
	                   + " " + cmd[PGPController.ENCRYPT_ACTION] + " " + tempFile.toString();
	
	      if ( signValue == true )
	      {
	          str = path + " --recipient " + rcpt.toString() +" --output " +
	                outputFile + " " + cmd[PGPController.SIGN_ACTION] + " " + id
	                + " "  + cmd[PGPController.ENCRYPT_ACTION] + " " + tempFile.toString();
	      }
	
	      setCommandString( str );
	
	      exitVal = super.encrypt( path, pgpMessage, passphrase, signValue, recipient, id );
	
	      return exitVal;
	  }
	*/
	/**
	 * every line of the error stream starts with "gpg"; remove these characters
	 */
	protected String parse(String s) {
		StringBuffer str = new StringBuffer(s);
		// remove on the start position of the string the "gpg:" string

		int pos = 0;
		if (pos + 3 < str.length()) {
			if ((str.charAt(pos) == 'g')
				&& (str.charAt(pos + 1) == 'p')
				&& (str.charAt(pos + 2) == 'g')
				&& (str.charAt(pos + 3) == ':')) {
				str.delete(pos, pos + 4);
			}
		}

		pos++;
		// remove on each beginning of an new line the start string "gpg:" from this line
		while (pos < str.length()) {
			if (str.charAt(pos) == '\n') {
				pos++;
				if (pos + 3 < str.length()) {
					if ((str.charAt(pos) == 'g')
						&& (str.charAt(pos + 1) == 'p')
						&& (str.charAt(pos + 2) == 'g')
						&& (str.charAt(pos + 3) == ':')) {
						str.delete(pos, pos + 4);
					}
				}
			}

			pos++;
		}

		return str.toString();
	}

}
