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
package org.columba.core.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * ideas taken from www.jext.org (author Roman Guy)
 */
public class ColumbaLoader implements Runnable
{
	public final static int COLUMBA_PORT = 50000;
	private int File;
	private String key;
	private Thread thread;
	private ServerSocket serverSocket;

	public ColumbaLoader()
	{
		try
		{
			serverSocket = new ServerSocket(COLUMBA_PORT);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop()
	{
		thread.interrupt();
		thread = null;

		try
		{
			if (serverSocket != null)
				serverSocket.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public synchronized boolean isRunning(){
		return thread != null;
	}

	public void run()
	{
		while (isRunning())
		{
			try
			{
				// does a client trying to connect to server ?
				Socket client = serverSocket.accept();
				if (client == null)
					continue;

				// only accept client from local machine
				String host = client.getLocalAddress().getHostAddress();
				if (!(host.equals("127.0.0.1")))
				{
					// client isn't from local machine
					client.close();
				}

				// try to read possible arguments
				BufferedReader reader =
					new BufferedReader(new InputStreamReader(client.getInputStream()));

				StringBuffer arguments = new StringBuffer();
				arguments.append(reader.readLine());

				if (!(arguments.toString().startsWith("columba:")))
				{
					// client isn't a Columba client
					client.close();
				}

				// do something with the arguments..
				System.out.println("arguments received...");

				handleArgs(arguments.toString());

				client.close();

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	protected void handleArgs(String argumentString)
	{
		System.out.println("argument string=" + argumentString);

		Vector v = new Vector();

		StringTokenizer st =
			new StringTokenizer(argumentString.substring(8, argumentString.length()), "%");
		while (st.hasMoreTokens())
		{
			String tok = (String) st.nextToken();
			v.addElement(tok);
		}

		String[] args = new String[v.size()];
		v.copyInto(args);

		new CmdLineArgumentHandler( args );

	}

}