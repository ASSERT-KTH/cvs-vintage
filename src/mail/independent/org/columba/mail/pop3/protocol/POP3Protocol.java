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
package org.columba.mail.pop3.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.StatusObservable;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.ssl.SSLProvider;

/**
 * @author fdietz
 *
 * This is an implementation of the POP3 protocol as defined
 * in RFC 1939: http://www.ietf.org/rfc/rfc1939.txt
 * 
 * Generally every command has its corresponding method in this
 * class.
 * 
 */
public class POP3Protocol {
	
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	private boolean statuschecked;
	
	private String security;
	public String answer;
	private int logMethod;
		
	private String user;
	private String password;
	private String server;
	private int port;

	public static final int DEFAULT_PORT = 110;

	public static final int USER = 1;
	public static final int APOP = 2;
	private boolean useSSL;
	
	private StatusObservable observable;

	/**
	 * @param user
	 * @param password
	 * @param server
	 * @param port
	 * @param useSSL
	 */
	public POP3Protocol(
		String user,
		String password,
		String server,
		int port,
		boolean useSSL) {
		this.user = user;
		this.port = port;
		this.password = password;
		this.server = server;
		this.useSSL = useSSL;

		logMethod = USER;
		
	}

	/**
	 * 
	 */
	public POP3Protocol() {
		logMethod = USER;
	}

	/**
	 * 
	 * @param str	possible arguments are "USER" and "APOP"
	 *              (hopefully more to come ;-)
	 * 
	 */
	public void setLoginMethod(String str) {
		if (str.equalsIgnoreCase("USER"))
			logMethod = USER;
		else if (str.equalsIgnoreCase("APOP"))
			logMethod = APOP;
		else
			logMethod = USER;
	}

	/**
	 * @param s
	 * @throws IOException
	 */
	public void sendString(String s) throws IOException {
		ColumbaLogger.log.debug("CLIENT:" + s);

		out.print(s + "\r\n");
		out.flush();
	}

	/**
	 * @return	response from server
	 * 
	 * @throws IOException
	 */
	public String getServerResponse() throws IOException {
		return answer;
	}

	/**
	 * 
	 * Responses in the POP3 consist of a status indicator and a keyword
	 * possibly followed by additional information.  All responses are
	 * terminated by a CRLF pair.  Responses may be up to 512 characters
	 * long, including the terminating CRLF.  There are currently two status
	 * indicators: positive ("+OK") and negative ("-ERR").  Servers MUST
	 * send the "+OK" and "-ERR" in upper case. 
	 * 
	 * 
	 * @return	true, if server answered with "+OK", false otherwise
	 * 
	 * @throws IOException
	 */
	public boolean getAnswer() throws IOException {

		answer = in.readLine();

		ColumbaLogger.log.debug("SERVER:" + answer);
		// bug fixed for bug id 619290 
		if (answer != null) {
			return (answer.startsWith("+OK"));
		} else {
			return false;
		}
	}

	/**
	 * Read next line from server
	 * 
	 * @throws IOException
	 */
	public void getNextLine() throws IOException {
		answer = in.readLine();

		ColumbaLogger.log.debug("SERVER:" + answer);
	}

	/**
	 * 
	 * Establish SSL encrypted connection
	 * 
	 * see RFC 2595: http://www.faqs.org/rfcs/rfc2595.html
	 * 
	 * @throws Exception
	 */
	protected void initSSL() throws Exception {
		sendString("STLS");

		// server doesn't seem to support STARTTLS extension
		if (getAnswer() == false)
			return;

		// create SSLSocket using already established socket
		SSLSocketFactory factory = SSLProvider.createSocketFactory();

		socket = factory.createSocket(socket, server, port, true);

		// handshake (which cyper algorithms are used?)
		 ((SSLSocket) socket).startHandshake();

		// create streams
		in =
			new BufferedReader(
				new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
		out =
			new PrintWriter(
				new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"));
	}

	/**
	 * Open port to POP3 server
	 * 
	 * Once the TCP connection has been opened by a POP3 client, the POP3
	 * server issues a one line greeting.  This can be any positive
	 * response.  An example might be:
	 * 
	 * S: +OK POP3 server ready
	 * 
	 * The POP3 session is now in the AUTHORIZATION state.  The client must
	 * now identify and authenticate itself to the POP3 server.
	 * 
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean openPort() throws Exception {
		socket = new Socket(server, port);

		// All Readers shall use ISO8859_1 Encoding in order to ensure
		// 1) ASCII Chars represented right to ensure working parsers
		// 2) No mangling of the received bytes to be able to convert
		//    the received bytes to another charset

		in =
			new BufferedReader(
				new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
		out =
			new PrintWriter(
				new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"));

		//connected = true;
		security = null;

		if (getAnswer()) {
			int i = answer.indexOf("<");
			if (i != -1) {
				security = answer.substring(i, answer.indexOf(">") + 1);

			}

			if (useSSL)
				initSSL();

			return true;
		} else
			return false;
	}

	

	/**
	 * Login to the POP3 server
	 * 
	 * Two possible mechanisms for doing this are implemented,
	 * the USER and PASS command combination and the APOP command.
	 * 
	 * 
	 * @param u
	 * @param pass
	 * @return
	 * @throws IOException
	 */
	public boolean login(String u, String pass) throws IOException {

		switch (logMethod) {

			case USER :
				return userPass(u, pass);

			case APOP :
				return apop(u, pass);
		}

		return false;
	}

	/**
	 * command syntax:
	 * 
	 * USER name
	 * 
	 * Arguments : 
	 *   a string identifying a mailbox(required), which is of
	 *   significance ONLY to the server
	 * 
	 * Possible Responses for "USER":
	 *   +OK name is a valid mailbox
	 *   -ERR never heard of mailbox name
	 * 
	 * Possible Responses for "PASS":
	 *   +OK maildrop locked and ready
	 *   -ERR invalid password
	 *   -ERR unable to lock maildrop
	 * 
	 * Restrictions:
	 *   may only be given in the AUTHORIZATION state after the POP3
	 *   greeting or after an unsuccessful USER or PASS command
	 * 
	 * Discussion:
	 *   To authenticate using the USER and PASS command
	 *   combination, the client must first issue the USER
	 *   command.  If the POP3 server responds with a positive
	 *   status indicator ("+OK"), then the client may issue
	 *   either the PASS command to complete the authentication,
	 *   or the QUIT command to terminate the POP3 session.  If
	 *   the POP3 server responds with a negative status indicator
	 *   ("-ERR") to the USER command, then the client may either
	 *   issue a new authentication command or may issue the QUIT
	 *   command.
	 * 
	 *   The server may return a positive response even though no
	 *   such mailbox exists.  The server may return a negative
	 *   response if mailbox exists, but does not permit plaintext
	 * 
	 * 
	 * @param usr	username
	 * @param pass	password
	 * @return		true, if login was successful, false otherwise
	 * 
	 * @throws IOException
	 */
	private boolean userPass(String usr, String pass) throws IOException {
		sendString("USER " + usr);
		if (getAnswer()) {
			sendString("PASS " + pass);
			return getAnswer();
		}
		return false;
	}

	/**
	 * 
	 * Arguments:
	 *   a string identifying a mailbox and a MD5 digest string
	 *   (both required)
	 * 
	 * Possible Responses:
	 *   +OK maildrop locked and ready
	 *   -ERR invalid password
	 * 
	 * @param user		username
	 * @param pass		password
	 * @return			true if login was successful, false otherwise
	 * @throws IOException
	 */
	private boolean apop(String user, String pass) throws IOException {
		if (security != null) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(security.getBytes());
				if (pass == null)
					pass = "";
				byte[] digest = md.digest(pass.getBytes());
				sendString("APOP " + user + " " + digestToString(digest));
				return getAnswer();
			} catch (NoSuchAlgorithmException e) {
			}
		}
		return false;
	}

	/**
	 * Logout from POP3 server
	 * 
	 * Possible Responses:
	 *  +OK
	 *  -ERR some deleted messages not removed
	 * 
	 * Discussion:
	 *  The POP3 server removes all messages marked as deleted
	 *  from the maildrop and replies as to the status of this
	 *  operation.  If there is an error, such as a resource
	 *  shortage, encountered while removing messages, the
	 *  maildrop may result in having some or none of the messages
	 *  marked as deleted be removed.  In no case may the server
	 *  remove any messages not marked as deleted.
	 * 
	 *  Whether the removal was successful or not, the server
	 *  then releases any exclusive-access lock on the maildrop
	 *  and closes the TCP connection.
	 * 
	 * @return	true if successfull, false otherwise
	 * 
	 * @throws IOException
	 */
	public boolean logout() throws IOException {

		sendString("QUIT");

		return getAnswer();

	}

	/**
	 * 
	 * Explicitly close all streams and the socket.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		in.close();
		out.close();
		socket.close();
	}


	/**
	 * 
	 * The POP3 server issues a positive response with a line
	 * containing information for the maildrop.  This line is
	 * called a "drop listing" for that maildrop.
	 * 
	 * Possible Responses:
	 *  +OK nn mm
	 * 
	 * Examples:
	 *  C: STAT
	 *  S: +OK 2 320
	 * 
	 * @return
	 * @throws IOException
	 */
	public int fetchMessageCount() throws IOException {
		String dummy;

		sendString("STAT");
		if (getAnswer()) {
			try {
				dummy = answer.substring(answer.indexOf(' ') + 1);

				int totalMessages =
					Integer.parseInt(dummy.substring(0, dummy.indexOf(' ')));

				dummy = dummy.substring(dummy.indexOf(' ') + 1);

				statuschecked = true;
				return totalMessages;

			} catch (NumberFormatException e) {
			}
		}

		return -1;
	}

	/**
	 * Fetch list with message sizes
	 * 
	 * After the initial +OK, for each message in the maildrop,
	 * the POP3 server responds with a line containing
	 * information for that message.  This line is also called a
	 * "scan listing" for that message.  If there are no
	 * messages in the maildrop, then the POP3 server responds
	 * with no scan listings--it issues a positive response
	 * followed by a line containing a termination octet and a
	 * CRLF pair.  
	 * 
	 * Possible Responses:
	 *  +OK scan listing follows
	 *  -ERR no such message
	 *     
	 * @return	string containing server response
	 * 
	 * @throws IOException
	 */
	public String fetchMessageSizes() throws IOException {
		int size = -1;
		String dummy;
		StringBuffer buf = new StringBuffer();

		sendString("LIST");
		if (getAnswer()) {

			getNextLine();
			int i = 0;
			while (!answer.equals(".")) {
				buf.append(answer + "\n");

				getNextLine();
			}

			return buf.toString();

		} else
			System.out.println("getAnswer == false");

		return null;
	}

	/**
	 * Fetch a list of all message UIDs
	 * 
	 * If no argument was given and the POP3 server issues a positive
	 * response, then the response given is multi-line.  After the
	 * initial +OK, for each message in the maildrop, the POP3 server
	 * responds with a line containing information for that message.
	 * This line is called a "unique-id listing" for that message.
	 * 
	 * In order to simplify parsing, all POP3 servers are required to
	 * use a certain format for unique-id listings.  A unique-id
	 * listing consists of the message-number of the message,
	 * followed by a single space and the unique-id of the message.
	 * No information follows the unique-id in the unique-id listing.
	 * 
	 * Possible Responses:
	 *  +OK unique-id listing follows
	 *  -ERR no such message
	 *   
	 * @param totalMessageCount
	 * @param worker
	 * @return
	 * @throws Exception
	 */
	public String fetchUIDList(
		int totalMessageCount)
		throws Exception {
		StringBuffer buffer = new StringBuffer();
		Integer parser = new Integer(0);
		int progress = 0;

		sendString("UIDL");
		if (getAnswer()) {

			//worker.setProgressBarMaximum(totalMessageCount);
			//worker.setProgressBarValue(0);
			observable.setMax(totalMessageCount);
			observable.setCurrent(0);
			
			getNextLine();
			while (!answer.equals(".")) {

				/*
				if (worker.cancelled() == true)
					throw new CommandCancelledException();
				*/
				if ( observable.isCancelled() )
					throw new CommandCancelledException();
					
				buffer.append(answer + "\n");
				progress++;

				//worker.setProgressBarValue(progress);
				observable.setCurrent(progress);
				getNextLine();
			}
		}

		return buffer.toString();
	}

	/**
	 * Fetch message with "number"
	 * 
	 * 
	 * Arguments:
	 *  a message-number (required) which may NOT refer to a
	 *  message marked as deleted
	 * 
	 * Discussion:
	 *  If the POP3 server issues a positive response, then the
	 *  response given is multi-line.  After the initial +OK, the
	 *  POP3 server sends the message corresponding to the given
	 *  message-number, being careful to byte-stuff the termination
	 *  character (as with all multi-line responses).
	 * 
	 * Possible Responses:
	 *  +OK message follows
	 *  -ERR no such message
	 * 
	 * @param messageNumber		number of messages
	 * @param worker			worker for updating the statusbar
	 * @return					message source as string
	 * @throws Exception
	 */
	public String fetchMessage(
		String messageNumber)
		throws Exception {
		StringBuffer messageBuffer = new StringBuffer();
		Integer parser = new Integer(0);
		int total = 0;
		int progress = 0;
		String sizeline = new String();
		int test;
		boolean progressBar = true;

		sendString("RETR " + messageNumber);
		if (getAnswer()) {
			// Try to get messages size to ensure capacity of the messageBuffer
			int spacePos = answer.indexOf(' ');
			if (spacePos != -1) {
				int space2Pos = answer.indexOf(spacePos + 1, ' ');
				if (space2Pos != -1) {
					try {
						int messageSize =
							Integer.parseInt(
								answer.substring(spacePos, space2Pos));
						messageBuffer.ensureCapacity(messageSize);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}

			getNextLine();
			while (!answer.equals(".")) {

				/*
				if (worker.cancelled() == true)
					throw new CommandCancelledException();
				*/
				if ( observable.isCancelled() )
				throw new CommandCancelledException();
				
				messageBuffer.append(answer + "\n");

				progress = answer.length() + 2;

				total += progress;
				
				//worker.incProgressBarValue(progress);
				//worker.setProgressBarValue(total);
				observable.setCurrent(total);

				getNextLine();
			}

		}

		return messageBuffer.toString();
	}

	/**
	 * Arguments:
	 *  a message-number (required) which may NOT refer to to a
	 *  message marked as deleted, and a non-negative number
	 *  of lines (required)
	 * 
	 * Discussion:
	 *  If the POP3 server issues a positive response, then the
	 *  response given is multi-line.  After the initial +OK, the
	 *  POP3 server sends the headers of the message, the blank
	 *  line separating the headers from the body, and then the
	 *  number of lines of the indicated message's body, being
	 *  careful to byte-stuff the termination character (as with
	 *  all multi-line responses).
	 *  Note that if the number of lines requested by the POP3
	 *  client is greater than than the number of lines in the
	 *  body, then the POP3 server sends the entire message.
	 * 
	 * Possible Responses:
	 *  +OK top of message follows
	 *  -ERR no such message
	 *       
	 * @param 		messageNumber	
	 * @return		message header as string
	 * @throws IOException
	 */
	public String getMessageHeader(int messageNumber) throws IOException {
		StringBuffer messageBuffer = new StringBuffer();
		Integer parser = new Integer(0);
		//int progress = 0;

		sendString("TOP " + messageNumber + " " + 0);
		if (getAnswer()) {
			getNextLine();
			while (!answer.equals(".")) {
				messageBuffer.append(answer + "\n");
				getNextLine();
			}
		}

		return messageBuffer.toString();
	}

	/**
	 * 
	 * Arguments:
	 *  a message-number (required) which may NOT refer to a
	 *  message marked as deleted
	 * 
	 * Discussion:
	 *  The POP3 server marks the message as deleted.  Any future
	 *  reference to the message-number associated with the message
	 *  in a POP3 command generates an error.  The POP3 server does
	 *  not actually delete the message until the POP3 session
	 *  enters the UPDATE state.
	 * 
	 *  Possible Responses:
	 *   +OK message deleted
	 *   -ERR no such message
	 *     
	 * @param messageNumber
	 * @return
	 * @throws IOException
	 */
	public boolean deleteMessage(int messageNumber) throws IOException {

		sendString("DELE " + messageNumber);
		if (getAnswer())
			return true;

		return false;
	}

	/**
	 * 
	 * Discussion:
	 *  The POP3 server does nothing, it merely replies with a
	 *  positive response.
	 * 
	 * Possible Responses:
	 *  +OK
	 *      
	 * @return	returns true if server returns a positive response
	 * 
	 * @throws IOException
	 */
	public boolean noop() throws IOException {

		sendString("NOOP");
		if (getAnswer())
			return true;

		return false;
	}

	/**
	 * 
	 * If any messages have been marked as deleted by the POP3
	 * server, they are unmarked.  The POP3 server then replies
	 * with a positive response.
	 *       
	 * @return		true if successful, false otherwise
	 * 
	 * @throws IOException
	 */
	public boolean reset() throws IOException {

		sendString("RSET");
		if (getAnswer())
			return true;

		return false;
	}

	/**
	 * 
	 * helper method for APOP authentication
	 * 
	 * @param digest
	 * @return
	 */
	private String digestToString(byte[] digest) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 16; ++i) {
			if ((digest[i] & 0xFF) < 0x10) {
				sb.append("0");
			}
			sb.append(Integer.toHexString(digest[i] & 0xFF));
		}
		return sb.toString();
	}
	
	/**
	 * @return
	 */
	public StatusObservable getObservable() {
		return observable;
	}

	/**
	 * @param observable
	 */
	public void setObservable(StatusObservable observable) {
		this.observable = observable;
	}

}
