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

package org.columba.core.dict;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.columba.core.util.SwingWorker;

public class DictLookup
{
	private static DictLookup instance = null;

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	boolean connected = false;

	static final String HOST = "dict.org";
	static final int PORT = 2628;

	String query = null;

	StringBuffer answer;

	public static DictLookup getInstance()
	{
		if (instance == null)
			instance = new DictLookup();

		return instance;
	}

	public void lookup(String str)
	{
		// FIXME
		/*
		this.query = str;
		LookupWorker worker = new LookupWorker();
		MainInterface.taskManager.register(worker, 30);
		worker.register(MainInterface.taskManager);
		worker.start();
		*/
	}

	class LookupWorker extends SwingWorker
	{
		public Object construct()
		{
			/*
			try
			{
				
				setText("Connecting with online dictionary: dict.org:2628 ... ");

				if (!connected) connect();

				setText("Query for word-definition of \"" + query + "\" ...");

				query();

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			unregister();
			*/
			
			return null;
		}

		/*
		public void connect() throws Exception
		{
			socket = new Socket(HOST, PORT);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			out.println("CLIENT Columba");
			out.flush();

			String str = in.readLine();

			if (str == null)
			{
				// disconnected ?

				socket = new Socket(HOST, PORT);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

				out.println("CLIENT Columba");
				out.flush();
			}

			if (str.startsWith("220"))
			{
				str = in.readLine();

				if (str.startsWith("250"))
					connected = true;
			}
		}

		public void query() throws Exception
		{
			out.println("DEFINE * " + query);
			out.flush();

			answer = new StringBuffer();
			String s;
			//while ( (!((s = in.readLine()).startsWith("25"))) || ( !((s = in.readLine()).startsWith("55"))))
			while ( (s=in.readLine()) != null )
			{

				answer.append(s + "\n");

				if ( s.startsWith("25") || s.startsWith("55") || s.startsWith("50") ) break;
			}
		}

		public void close() throws IOException
		{
			out.println("quit");
			out.flush();
			in.close();
			out.close();
			socket.close();
		}
		*/
		
		public void finished()
		{
			
			//DictionaryDialog dialog = new DictionaryDialog(MainInterface.mainFrame, answer.toString() );
		}
	}
}
