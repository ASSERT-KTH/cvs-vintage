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

package org.columba.mail.smtp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.coder.Base64Decoder;
import org.columba.mail.coder.Base64Encoder;
import org.columba.mail.ssl.SSLProvider;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author fdietz
 *
 * This is an implementation of the SMTP protocol as specified by 
 * RFC 821: http://www.ietf.org/rfc/rfc0821.txt
 * 
 * Generally you will find one method for every SMTP command.
 * 
 *  
 */
public class SMTPProtocol {
	/**
	 *	server supports regular login only
	 */
	public final static int SMTP = 0;
	/**
	 * server supports ESMTP
	 */
	public final static int ESMTP = 1;

	/**
	 *	name of host
	 */
	private String host;
	
	/**
	 *	server port number
	 */
	private int port;

	/**
	 *	Socket
	 */
	private Socket socket;
	
	/**
	 * output stream
	 */
	private DataOutputStream out;
	
	/**
	 * input stream
	 */
	private BufferedReader in;

	/**
	 * map of capabilites
	 */
	private Hashtable capabilities;

	/**
	 * decoder is needed for authentication
	 */
	private Base64Decoder decoder;
	
	/**
	 * encoder is needed for authentication
	 */
	private Base64Encoder encoder;

	/**
	 * is ESMTP enabled
	 */
	private boolean isEsmtp;

	/**
	 * is SSL enabled
	 */
	private boolean useSSL;

	/**
	 * @param hostName
	 * @param portNr
	 * @param useSSL
	 */
	public SMTPProtocol(String hostName, int portNr, boolean useSSL) {
		capabilities = new Hashtable();

		decoder = new Base64Decoder();
		encoder = new Base64Encoder();

		host = hostName;
		port = portNr;

		this.useSSL = useSSL;
	}

	/**
	 * @param hostName
	 * @param portNr
	 */
	public SMTPProtocol(String hostName, int portNr) {
		capabilities = new Hashtable();

		decoder = new Base64Decoder();
		encoder = new Base64Encoder();

		host = hostName;
		port = portNr;

		this.useSSL = false;
	}

	/**
	 * @param hostName
	 */
	public SMTPProtocol(String hostName) {
		this(hostName, 25);
	}

	/**
	 * 
	 * Check error code of answer
	 * 
	 * @param answer	server answer
	 * @param start		first number of error code we want to check
	 * 
	 * @throws SMTPException
	 */
	private void checkAnswer(String answer, String start)
		throws SMTPException {

		// throw Exception if command failed
		if (!answer.startsWith(start)) {
			throw (new SMTPException(answer));
		}
	}

	/**
	 * SMTP commands are character strings terminated by <CRLF>.  
	 * The command codes themselves are alphabetic characters terminated 
		 * by <SP> if parameters follow and <CRLF> otherwise.
		 * 
	 * @param parameter	command parameter
	 * @throws Exception
	 */
	private void sendString(String parameter) throws Exception {

		out.writeBytes(parameter + "\r\n");
		out.flush();
	}

	/**
	 * @param username
	 * @param password
	 * @param loginMethod
	 * @throws Exception
	 */
	public void authenticate(
		String username,
		String password,
		String loginMethod)
		throws Exception {
		String answer;
		
		StringWriter decoded = new StringWriter();
		
		String methods = (String) capabilities.get("AUTH");

		// Try LOGIN
		if (loginMethod.equalsIgnoreCase("LOGIN") == true) {
			
			authenticateLogin(username, password);

		} else if (loginMethod.equalsIgnoreCase("PLAIN") == true) {
			
			authenticatePlain(username, password);

		} else {
			throw new SMTPException("Unsupported authentication type!");
		}
	}

	/**
	 * 
	 * Authenticate using PLAIN
	 * 
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	protected void authenticatePlain(String username, String password)
		throws Exception {
		sendString("AUTH PLAIN");

		String answer = in.readLine();

		checkAnswer(answer, "3");

		String authenticateID = new String("");

		StringBuffer buf = new StringBuffer();
		buf.append(authenticateID);
		buf.append('\0');
		buf.append(username);
		buf.append('\0');
		buf.append(password);

		sendString(encoder.encode(buf.toString(), null));

		checkAnswer(in.readLine(), "2");
	}

	/**
	 * Authenticate using LOGIN
	 * 
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	protected void authenticateLogin(String username, String password)
		throws Exception {
		sendString("AUTH LOGIN");
		String answer = in.readLine();

		checkAnswer(answer, "3");

		answer = answer.substring(4);

		sendString(encoder.encode(username, null));

		answer = in.readLine();
		checkAnswer(answer, "3");

		answer = answer.substring(4);

		sendString(encoder.encode(password, null));

		checkAnswer(in.readLine(), "2");
	}

	/**
	 * command syntax: STARTTLS <CLRF>
	 * 
	 * possible server answers:
	 * 
	 * 220 Ready to start TLS
	 * 501 Syntax error (no parameters allowed)
	 * 454 TLS not available due to temporary reason
	 * 
	 * @throws Exception
	 */
	protected void initSSL() throws Exception {
		sendString("STARTTLS");

		String answer = in.readLine();

		// server doesn't seem to support STARTTLS extension
		if (!answer.startsWith("2"))
			return;

		// create SSLSocket using already established socket
		SSLSocketFactory factory = SSLProvider.createSocketFactory();

		socket = factory.createSocket(socket, host, port, true);

		// handshake (which cyper algorithms are used?)
		 ((SSLSocket) socket).startHandshake();

		// create streams
		out = new DataOutputStream(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	/**
	 * 
	 * Open port to server and create the input-/output streams. 
	 * 
	 * Possible answers:
	 * S: 220	<domain> Service ready
	 * F: 421	<domain> Service not available, closing transmission channel
	 *          [This may be a reply to any command if the service knows it
	 *           must shut down]
	 * 
	 * 
	 * @return	either SMTP or ESMTP, which will be later used
	 *          when trying to authenticate
	 * 
	 * @throws Exception
	 */
	public int openPort() throws Exception {
		String answer;

		// open socket
		socket = new Socket(host, port);

		// create streams
		out = new DataOutputStream(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		// read server greeting
		// value starting with "2" means success
		checkAnswer(in.readLine(), "2");

		// check if we should use SSL
		if (useSSL) {
			// use SSL
			initSSL();
		}

		// send initial greeting
		return helo();
	}

	/**
	 * 
	 * HELO command syntax: HELO <SP> <domain> <CRLF>
	 * 
	 * This command is used to identify the sender-SMTP to the 
	 * receiver-SMTP.  The argument field contains the host name of
	 * the sender-SMTP.
	 * The receiver-SMTP identifies itself to the sender-SMTP in
	 * the connection greeting reply, and in the response to this
	 * command.
	 * This command and an OK reply to it confirm that both the
	 * sender-SMTP and the receiver-SMTP are in the initial state,
	 * that is, there is no transaction in progress and all state
	 * tables and buffers are cleared.
	 * 
	 * Possible return codes for the HELO command:
	 * 
	 * S: 250 Requested mail action okay, completed
	 * E: 500 Syntax error, command unrecognized
	          [This may include errors such as command line too long]
	 * E: 501 Syntax error in parameters or arguments
	 * E: 504 Command parameter not implemented
	 * E: 421 <domain> Service not available, closing transmission channel
	 *        [This may be a reply to any command if the service knows it 
	 *         must shut down]
	 * 
	 * @return		either SMTP or ESMTP, which will be later used
	 *          	when trying to authenticate
	 * 
	 * @throws Exception
	 */
	protected int helo() throws Exception {
		// we send a FQDN EHLO/HELO that works
		// sending the actual hostname could be considered spyware
		// and it doesn't work with machines without a FQDN name,
		// or using NAT or a TCP tunnel to another host
		sendString("EHLO localhost.localdomain");

		String answer = in.readLine();

		if (answer.startsWith("2")) {
			// server supports ESMTP authentification

			isEsmtp = true;

			// receive capabilities list
			getCapabilities();

			return ESMTP;
		}

		// server doesn't support ESMTP
		isEsmtp = false;

		// use HELO as fallback
		sendString("HELO localhost.localdomain");

		// return code starting with "2" means server is ready
		checkAnswer(in.readLine(), "2");

		return SMTP;
	}

	/**
	 * 
	 * Get list of capabilites from server.
	 * 
	 * 
	 * @throws Exception
	 */
	private void getCapabilities() throws Exception {
		String answer;
		String key;
		String value;
		int separator;

		// capability list is returned automatically after login
		answer = in.readLine();

		while (answer != null) {
			if (answer.startsWith("2")) {

				// parse answer and put capabilites in map
				separator = answer.indexOf(' ');
				if (separator > 4) {
					key = answer.substring(4, separator);
					value = answer.substring(separator + 1);
					capabilities.put(key, value);
				} else
					capabilities.put(answer.substring(4), new String());

				if (answer.charAt(3) != '-')
					break;
			} else {
				throw (new SMTPException(answer));
			}

			answer = in.readLine();
			checkAnswer(answer, "2");
		}
	}

	/**
	 * command syntax: MAIL <SP> FROM:<reverse-path> <CRLF>
	 *  
	 * This command is used to initiate a mail transaction in which
	 * the mail data is delivered to one or more mailboxes.  The
	 * argument field contains a reverse-path.
	 * 
	 * possible server responses:
	 * S: 250	Requested mail action okay, completed 
	 * F: 552 	Requested mail action aborted: exceeded storage allocation
	 * F: 451	Requested action aborted: error in processing
	 * F: 452	Requested action not taken: insufficient system storage
	 * E: 500	Syntax error, command unrecognized
	 *          [This may include errors such as command line too long]
	 * E: 501	Syntax error in parameters or arguments
	 * E: 421	<domain> Service not available, closing transmission channel
	 *          [This may be a reply to any command if the service knows it
	 *           must shut down]
	 * 
	 * command syntax: RCPT <SP> TO:<forward-path> <CRLF>
	 * 
	 * This command is used to identify an individual recipient of
	 * the mail data; multiple recipients are specified by multiple
	 * use of this command.
	 * 
	 * possible server responses:
	 * S: 250	Requested mail action okay, completed
	 * S: 251	User not local; will forward to <forward-path>
	 * F: 550	Requested action not taken: mailbox unavailable
	 *          [E.g., mailbox not found, no access]
	 * F: 551	User not local; please try <forward-path>
	 * F: 552	Requested mail action aborted: exceeded storage allocation
	 * F: 553	Requested action not taken: mailbox name not allowed
	 *          [E.g., mailbox syntax incorrect]
	 * F: 450	Requested mail action not taken: mailbox unavailable
	 *          [E.g., mailbox busy]
	 * F: 451	Requested action aborted: error in processing
	 * F: 452	Requested action not taken: insufficient system storage
	 * E: 500	Syntax error, command unrecognized
	 *          [This may include errors such as command line too long]
	 * E: 501	Syntax error in parameters or arguments
	 * E: 503	Bad sequence of commands
	 * E: 421	<domain> Service not available, closing transmission channel
	 *          [This may be a reply to any command if the service knows it
	 *           must shut down]
	 * 
	 * @param from
	 * @param to
	 * @throws Exception
	 */
	public void setupMessage(String from, List to) throws Exception {
		int anzAdresses;

		// send command
		sendString("MAIL FROM: " + from);

		// check if answers succeded
		checkAnswer(in.readLine(), "2");

		// send all recipients one by one
		for (Iterator it = to.iterator(); it.hasNext();) {

			// send command
			sendString("RCPT TO: " + it.next());

			// check if answers succeded
			checkAnswer(in.readLine(), "2");
		}
	}

	/**
	 * command syntax: DATA <CRLF>
	 * 
	 * The receiver treats the lines following the command as mail
	 * data from the sender.  This command causes the mail data
	 * from this command to be appended to the mail data buffer.
	 * The mail data may contain any of the 128 ASCII character
	 * codes.
	 * The mail data is terminated by a line containing only a
	 * period, that is the character sequence "<CRLF>.<CRLF>".  
	 * This is the end of mail data indication.
	 * 
	 * @param message	String containing the message source
	 * 
	 * @param workerStatusController
	 * 
	 * @throws Exception
	 */
	public void sendMessage(
		String message,
		WorkerStatusController workerStatusController)
		throws Exception {

		BufferedReader messageReader =
			new BufferedReader(new StringReader(message));
		String line;

		int progressCounter = 0;

		// display status message
		workerStatusController.setDisplayText(
			MailResourceLoader.getString(
				"statusbar",
				"message",
				"send_message"));

		// init progress bar
		workerStatusController.setProgressBarMaximum(message.length() / 1024);

		// send client command
		out.writeBytes("DATA\r\n");
		out.flush();

		// check if answer succedded
		checkAnswer(in.readLine(), "3");

		line = messageReader.readLine();

		// write the data 
		while (line != null) {
			progressCounter += line.length();

			// update progressbar
			if (progressCounter > 1024) {
				workerStatusController.incProgressBarValue();
				progressCounter %= 1024;
			}

			if (line.equals("."))
				line = new String(" .");

			out.writeBytes(line + "\r\n");
			line = messageReader.readLine();
		}

		// send end of DATA command
		out.writeBytes("\r\n.\r\n");
		out.flush();

		// check if answer succedded
		checkAnswer(in.readLine(), "2");
	}

	/**
	 * command syntax: QUIT <CRLF>
	 * 
	 * This command specifies that the receiver must send an OK
	 * reply, and then close the transmission channel.     
	 * 
	 * @throws Exception
	 */
	public void closePort() throws Exception {
		// send QUIT command
		out.writeBytes("QUIT\r\n");
		out.flush();

		// check if answer succedded
		checkAnswer(in.readLine(), "2");
	}
}
