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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.columba.core.externaltools.ExternalToolsNotFoundException;
import org.columba.core.io.StreamUtils;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.ExternalToolsPluginHandler;
import org.columba.mail.config.PGPItem;

/**
 * Default class that implements methods which handles pgp functions like singing, encrypting, verify and decrypting.
 * For each pgp tool like gpg a derived class should be extend this class and implementing all abstract methods. Methods
 * of the instantiated class (inclusive this class) are called normally from the PGPController. All real Implementation for
 * PGP-Tools like GPG should extending this class.
 * <p>
 * This class is used by {@link org.columba.mail.pgp.PGPController PGPController}.
 * @author tstich, waffel
 *
 */
public abstract class DefaultUtil {
	/**
	 * The output String which holds the output from the whole PGP tool.
	* @deprecated After using ristretto use only the outputStream.
	 */
	protected String outputString;

	/**
	 * The error String which holds the error-output from the whole PGP tool.
	* @deprecated After using ristretto use only the errorStream.
	 */
	protected String errorString;

	/**
	 * The outputStream which holds the output from the whole PGP tool.
	 */
	protected InputStream outputStream;

	/**
	 * The errorStream which holds the error output from the whole PGP tool.
	 */
	protected InputStream errorStream;

	/** Executes the given command and the returnes the connected process.
	 * @param cmd Command to be executed
	 * @return Process which is connected with the executed command
	 * @throws Exception if the command cannot be executed. @see Runtime.getRuntime().exec(String[])
	 */
	protected Process executeCommand(String[] cmd) throws Exception {
		Process p= Runtime.getRuntime().exec(cmd);

		return p;
	}

	/**
	* This method should parse the given String and for example remove all "gpg:" strings from the given String.
	 * @param str String which should be parsed
	 * @return a parsed String, not null
	 */
	protected abstract String parse(String str);

	/**
	* Returnes the error string which is created from the execution of a extern process. Only if the extern execution
	 * tool like gpg creates a error string on system.error the method is returning a error string. Else the error string
	 * is empty. Before using this method it should be checked, if the exitValue from the executeion process is not 0.
	 * @return Returnes the error string which is created from the execution of a extern process.
	* @deprecated since using ristretto and intensive using Stream you should use the {@link #getErrorStream()} method.
	 */
	public String getErrorString() {
		ByteArrayOutputStream out= new ByteArrayOutputStream();

		try {
			StreamUtils.streamCopy(this.errorStream, out);
		} catch (IOException e) {
			return "";
		}

		return out.toString();
	}

	/**
	* Retruns the output String which is created from the execution of the whole PGP implementation. Only if the PGP tool
	* creates a Output the method returns a not empty String, else the String is empty.
	 * @return The output String which is created from the whole PGP tool which implements the pgp stuff.
	* @deprecated since using ristretto and intensive using Stream you should use the {@link #getStreamResult()} method.
	 */
	public String getOutputString() {
		ByteArrayOutputStream out= new ByteArrayOutputStream();

		try {
			StreamUtils.streamCopy(this.outputStream, out);
		} catch (IOException e) {
			return "";
		}

		return out.toString();
	}

	/**
	 * Returns the result of a operation like singing, encrypting and so on.
	 * @return the result of one of the oprations like signing, encrypting and so on.
	 * @deprecated this is deprecated since the new ristretto-implementation which use Streams instead of Strings.
	 * Use {@link getStreamResult()}.
	 */
	public String getResult() {
		return this.outputString;
	}

	/**
	 * Returns the restult of a operation like sign, encryypt, verify and so on as an InputStream from which the result can be
	 * read.
	* @return The Restult of a operation like sign, encryypt, verify and so on as an InputStream from which the result can be
	 * read.
	*/
	public InputStream getStreamResult() {
		return this.outputStream;
	}

	/**
	* Returns the result of a operation like sign, encrypt and so on as an InputStream from which the error result can be read.
	 * @return The result of a operation like sign, encrypt and so on as an InputStream from which the error result can be read.
	 */
	public InputStream getErrorStream() {
		return this.errorStream;
	}

	/**
	 * Returns the Command-line for a tool like gpg for the given type of operation like ENCRYPT, SIGN and so on.
	* @param type for which type the commandline should be returned. The types are defined in {@link PGPController}.
	* @return a String Array which holds all necessary command line argument for the instancieated tool, like gpg.
	* @see PGPController
	*/
	protected abstract String[] getRawCommandString(int type);

	/**
	 * Gets the path to the commandline tool.
	 * @see org.columba.core.plugin.ExternalToolsPluginHandler
	 * @param type                id of commandline tool
	 * @return                        absolut path of tool or null if the path cannot be obtained.
	 */
	protected String getPath(String type)
		throws ExternalToolsNotFoundException {
		ExternalToolsPluginHandler handler= null;

		try {
			handler=
				(
					ExternalToolsPluginHandler) MainInterface
						.pluginManager
						.getHandler(
					"org.columba.core.externaltools");

			return handler.getLocationOfExternalTool(type).getPath();
		} catch (Exception e) {

			if (e instanceof ExternalToolsNotFoundException)
				throw (ExternalToolsNotFoundException) e;

			if (MainInterface.DEBUG)
				e.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns the command-line that should be given for the pgp-tool, like gpg. In the command-line all items in
	 * % chatacters are replaced with the source from item, if there is a item entry with the id in the % characters. If there
	 * is not a entry found the value is replaced with a null-entry. The returned command line is executed by a
	 * Runtime-Process with the given command - line. The command-line is expanded with the item-entry "path" which
	 * holds the path to the pgp-tool. For example /usr/bin/pgp.
	 * @param type Type for which the command-line should be returned. For example PGPController.ENCRYPT_ACTION
	 * @param item Item which holds all necessary datas for the command - line. For example the entry path MUST be
	 * given.
	 * @return The command - line that should be executed by a Runtime - Process.
	 */
	protected String[] getCommandString(int type, PGPItem item)
		throws Exception {
		String[] rawCommand= getRawCommandString(type);
		List commandList= new ArrayList();

		String path=null;
		try {
			path= getPath("gpg");
		} catch (ExternalToolsNotFoundException e) {
			
			// change this exception to a pgp exception
			throw new ProgramNotFoundException(e.getMessage());
		}

		if (path == null) {
			path= item.get("path");
		}

		commandList.add(path);

		for (int i= 0; i < rawCommand.length; i++) {
			StringBuffer command= new StringBuffer();
			String rawArg= rawCommand[i];
			int varStartIndex= rawArg.indexOf("%");
			int varEndIndex= -1;
			String varName;

			while (varStartIndex != -1) {
				command.append(
					rawArg.substring(varEndIndex + 1, varStartIndex));
				varEndIndex= rawArg.indexOf("%", varStartIndex + 1);

				varName= rawArg.substring(varStartIndex + 1, varEndIndex);

				command.append(getValue(varName, item));

				varStartIndex= rawArg.indexOf("%", varEndIndex + 1);
			}

			command.append(rawArg.substring(varEndIndex + 1));
			commandList.add(command.toString());
		}

		return (String[]) commandList.toArray(new String[0]);
	}

	/**
	 * Replace the given name with the entry in the given PGPItem
	 * @param name The name that should be replaced
	 * @param item The item which should hold the entry for the given name
	 * @return The entry for the given name or null, if no entry is found.
	 */
	private String getValue(String name, PGPItem item) {
		if (name.equals("user")) {
			return item.get("id");
		}

		if (name.equals("recipients")) {
			return item.get("id");
		}

		if (name.equals("sigfile")) {
			return item.get("sigfile");
		}

		if (name.equals("digest-algo")) {
			return item.getDigestAlgorithm();
		}

		return null;
	}

	/**
	 * Verify a given message with a given signature. The given item should holding the path to the pgp-tool. While gpg
	 * dosn't yet supporting a real stream based process to verify a given detached signature with a message the method
	 * creates a temporary file which holds the signature. After the verify process the temporary file is deleted.
	 * @param item PGPItem wich should holding the path to the pgp-tool
	 * @param message The message for wich the given signature should be verify.
	 * @param signature Signature wich should be verify for the given message
	 * @return the exit status from the pgp-tool for the veify process.
	 * @throws Exception If the temporary file cannot be created or the verify process cannot be run.
	 */
	public int verify(PGPItem item, InputStream message, InputStream signature)
		throws Exception {
		int exitVal= -1;
		File tempFile= File.createTempFile("columbaSig", null);

		//		make sure file is deleted automatically when closing VM
		tempFile.deleteOnExit();

		FileOutputStream fout= new FileOutputStream(tempFile);
		StreamUtils.streamCopy(signature, fout);
		fout.flush();
		item.set("sigfile", tempFile.getAbsolutePath());

		Process p=
			executeCommand(getCommandString(PGPController.VERIFY_ACTION, item));

		//write the pgpMessage out
		StreamUtils.streamCopy(message, p.getOutputStream());
		p.getOutputStream().close();

		exitVal= p.waitFor();
		this.errorStream= StreamUtils.streamClone(p.getErrorStream());
		this.outputStream= StreamUtils.streamClone(p.getInputStream());
		p.destroy();
		fout.close();
		tempFile.delete();

		return exitVal;
	}

	/**
	 * Signes a given message. The tool to be used for singing is defined in the PGPItem. There should be also userID and
	 * passphrase stored in the PGPItem. It returns the exit Value from the process that is executed the command. The
	 * command to be executed self is defined in the instantiated class, for example GnuPGUtil. The result is stored in
	 * a intern Buffer and should be get with #getStreamResult().
	 * @param item PGPItem which holds necessary datas
	 * @param pgpMessage The message to be signed
	 * @return the exit value from the process that executes the whole singing command.
	 * @throws Exception If the process cannot execute the command.
	 */
	public int sign(PGPItem item, InputStream pgpMessage) throws Exception {
		int exitVal= -1;
		Process p=
			executeCommand(getCommandString(PGPController.SIGN_ACTION, item));

		p.getOutputStream().write(item.getPassphrase().getBytes());
		p.getOutputStream().write(
			System.getProperty("line.separator").getBytes());

		// send return after passphrase
		//write the pgpMessage out
		StreamUtils.streamCopy(pgpMessage, p.getOutputStream());
		p.getOutputStream().close();

		exitVal= p.waitFor();

		this.errorStream= StreamUtils.streamClone(p.getErrorStream());
		this.outputStream= StreamUtils.streamClone(p.getInputStream());
		p.destroy();

		return exitVal;
	}

	/**
	 * Signes a given message. The tool to be used for singing is defined in the PGPItem. There should be also userID and
	 * passphrase stored in the PGPItem. It returns the exit Value from the process that is executed the command. The
	 * command to be executed self is defined in the instantiated class, for example GnuPGUtil. The result is strored in a
	 * intern Buffer. Use #getResult() to get the signed data.
	 * @param item PGPItem which holds necessary data
	 * @param input The message to be signed
	 * @return the exit value from the process that executes the whole singing command.
	 * @throws Exception If the process cannot execute the command.
	 * @deprecated this is deprecated since the new ristretto-implementation which use Streams instead of Strings.
	 * Use {@link #sign(PGPItem, InputStream)}.
	 */
	public int sign(PGPItem item, String input) throws Exception {
		int exitVal= -1;

		Process p=
			executeCommand(getCommandString(PGPController.SIGN_ACTION, item));
		p.getOutputStream().write(item.getPassphrase().getBytes());
		p.getOutputStream().write(
			System.getProperty("line.separator").getBytes());
		p.getOutputStream().write(input.getBytes());
		p.getOutputStream().close();

		exitVal= p.waitFor();

		this.errorStream= StreamUtils.streamClone(p.getErrorStream());
		this.outputStream= StreamUtils.streamClone(p.getInputStream());
		p.destroy();

		return exitVal;
	}

	/**
	 * Encrypting of a given message. The tool to be used for encrypting is defined in the PGPItem. There should be also
	 * userID, passphrase and recipients stored in the PGPItem. The recipients must be given as String seperated by spaces.
	 * It returns the exit Value from the process that is executed the command. The command to be executed self is defined
	 * in the instantiated class, for example GnuPGUtil. The result is strored in a intern Buffer. Use #getResult() to get the
	 * encrypted data.
	 * @param item PGPItem which holds necessary data
	 * @param message The message to be encrypt
	 * @return the exit value from the process that executes the whole singing command.
	 * @throws Exception If the process cannot execute the command.
	 */
	public int encrypt(PGPItem item, InputStream message) throws Exception {
		int exitVal= -1;

		Process p=
			executeCommand(
				getCommandString(PGPController.ENCRYPT_ACTION, item));

		StreamUtils.streamCopy(message, p.getOutputStream());
		p.getOutputStream().close();

		exitVal= p.waitFor();

		this.errorStream= StreamUtils.streamClone(p.getErrorStream());
		this.outputStream= StreamUtils.streamClone(p.getInputStream());
		p.destroy();

		return exitVal;
	}

	/**
	* Decrypts the given crypted Stream and returns the exit status of the whole PGP tool. The given Item must have the
	* passphrase to decrypt the Stream. Error and output are stored in the intern errorStream and outputStream and can be
	* accessed with {@link #getErrorStream()} and {@link #getStreamResult()}.
	* <p>
	* The decrypting process is runnning in a process. The commandline for the process is getted from the real
	* implementation of the PGP tool.
	* @return The exit value from the whole PGP tool.
	* @see PGPController#decrypt(InputStream, PGPItem)
	 */
	public int decrypt(PGPItem item, InputStream cryptMessage)
		throws Exception {
		int exitVal= -1;
		Process p=
			executeCommand(
				getCommandString(PGPController.DECRYPT_ACTION, item));

		p.getOutputStream().write(item.getPassphrase().getBytes());
		p.getOutputStream().write(
			System.getProperty("line.separator").getBytes());

		// send return after passphrase
		StreamUtils.streamCopy(cryptMessage, p.getOutputStream());
		cryptMessage.close();
		p.getOutputStream().close();

		exitVal= p.waitFor();

		this.errorStream= StreamUtils.streamClone(p.getErrorStream());
		this.outputStream= StreamUtils.streamClone(p.getInputStream());
		p.destroy();

		return exitVal;
	}
}
