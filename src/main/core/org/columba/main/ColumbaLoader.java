// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.main;

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