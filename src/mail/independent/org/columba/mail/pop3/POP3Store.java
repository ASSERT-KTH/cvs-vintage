package org.columba.mail.pop3;

import java.util.Vector;

import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.config.PopItem;
import org.columba.mail.gui.util.PasswordDialog;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.Message;
import org.columba.mail.parser.Rfc822Parser;
import org.columba.mail.pop3.parser.SizeListParser;
import org.columba.mail.pop3.parser.UIDListParser;
import org.columba.mail.pop3.protocol.POP3Protocol;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class POP3Store {

	public static final int STATE_NONAUTHENTICATE = 0;
	public static final int STATE_AUTHENTICATE = 1;

	private int state = STATE_NONAUTHENTICATE;

	private POP3Protocol protocol;

	private PopItem popItem;

	/**
	 * Constructor for POP3Store.
	 */
	public POP3Store(PopItem popItem) {
		super();
		this.popItem = popItem;

		protocol = new POP3Protocol();
	}

	/**
	 * Returns the state.
	 * @return int
	 */
	public int getState() {
		return state;
	}

	/**
	 * Sets the state.
	 * @param state The state to set
	 */
	public void setState(int state) {
		this.state = state;
	}

	public Vector fetchUIDList( int totalMessageCount, WorkerStatusController worker ) throws Exception {

		isLogin( worker );

		String str = protocol.fetchUIDList(totalMessageCount, worker);

		// need to parse here
		Vector v = UIDListParser.parse(str);

		return v;
	}
	


	public Vector fetchMessageSizeList(WorkerStatusController worker) throws Exception {

		isLogin( worker );

		String str = protocol.fetchMessageSizes();

		// need to parse here
		Vector v = SizeListParser.parse(str);

		return v;
	}

	public int fetchMessageCount(WorkerStatusController worker) throws Exception 
	{
		isLogin( worker );
		
		int messageCount = protocol.fetchMessageCount();
		
		return messageCount;
		
	}
	
	public void deleteMessage(int index, WorkerStatusController worker) throws Exception {

		isLogin( worker );

		boolean b = protocol.deleteMessage(index);

	}

	public Message fetchMessage( int index, WorkerStatusController worker ) throws Exception {
		ColumbaHeader header = new ColumbaHeader();
		Rfc822Parser parser = new Rfc822Parser();
		
		isLogin( worker );

		String rawString = protocol.fetchMessage( new Integer(index).toString() ,  worker);

		int i = rawString.indexOf("\n\n");
		String headerString = rawString.substring(0, i);

		header = parser.parseHeader(rawString);
		
		Message m = new Message(header);
		ColumbaHeader h = (ColumbaHeader) m.getHeader();
		m.setSource(rawString);
		
		parser.addColumbaHeaderFields(h);
		
		h.set("columba.host", popItem.get("host"));
		
		h.set("columba.fetchstate", new Boolean(true));
		
		

		//h.set("columba.pop3uid", (String) uids.get(number - 1));

		return m;
	}

	public void logout() throws Exception
	{
		boolean b = protocol.logout();
		
		protocol.close();
		
		state = STATE_NONAUTHENTICATE;
	}
	
	public void close() throws Exception
	{
		protocol.close();
		
		state = STATE_NONAUTHENTICATE;
	}
	
	public void login( WorkerStatusController worker) throws Exception {
		PasswordDialog dialog;
		boolean login = false;

		boolean b =
			protocol.openPort(
				popItem.get("host"),
				popItem.getInteger("port"));

		String password = new String("");
		String user = new String("");
		String method = new String("");
		boolean save = false;

		while (login == false) {
			if (popItem.get("password").length() == 0) {
				dialog = new PasswordDialog();

				dialog.showDialog(
					popItem.get("host"),
					popItem.get("password"),
					popItem.getBoolean("save_password"));

				char[] name;

				if (dialog.success() == true) {
					// ok pressed
					name = dialog.getPassword();
					password = new String(name);
					//user = dialog.getUser();
					save = dialog.getSave();
					//method = dialog.getLoginMethod();

					//System.out.println("pass:<"+password+">");
					//setCancel(false);
				} else {
					// cancel pressed
					worker.cancel();
					throw new CommandCancelledException();
				}
			} else {
				password = popItem.get("password");
				//user = popItem.getUser();
				save = popItem.getBoolean("save_password");
				//method = popItem.getLoginMethod();
			}

		
			/*
			if (getCancel() == false) {
			*/
			//System.out.println("trying to login");
			//setText(popServer.getFolderName() + " : Login...");
			//stopTimer();

			//startTimer();
			// authenticate

			//pop3Connection.openPort();

			protocol.setLoginMethod(popItem.get("login_method"));
			login = protocol.login(popItem.get("user"), password);
			//stopTimer();

			if (login == false) {
				//JOptionPane.showMessageDialog(popServer.getFrame(), "Authorization failed!");

				popItem.set("password","");
				state = STATE_NONAUTHENTICATE;

			}

			/*
			}
			*/
		}

		if (login) {
			//popItem.setUser(user);
			popItem.set("save_password",save);
			//popItem.setLoginMethod( method );
			state = STATE_AUTHENTICATE;

			if (save) {

				// save plain text password in config file
				// this is a security risk !!!
				popItem.set("password", password);
			}
		}

	}

	public boolean isLogin( WorkerStatusController worker ) throws Exception {
		if (state == STATE_AUTHENTICATE)
			return true;
		else {
			login( worker );

			return false;
		}
	}

}
