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
