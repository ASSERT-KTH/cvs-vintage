/*
 * Created on Apr 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.mail.pop3.plugins.AbstractPOP3PreProcessingFilter;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SpamAssassinPOP3PreProcessingFilterPlugin
	extends AbstractPOP3PreProcessingFilter {

	protected StreamThread outputStream = null;
	protected StreamThread errorStream = null;

	public SpamAssassinPOP3PreProcessingFilterPlugin(XmlElement rootElement) {
		super(rootElement);

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.pop3.plugins.AbstractPOP3PreProcessingFilter#modify(java.lang.String)
	 */
	public String modify(String rawString) {

		// this is for gathering configuration
		// but we don't need this right now
		String version = rootElement.getAttribute("version");

		//String cmd = "spamassassin -L";
		String cmd = "spamc -c";

		String result = null;
		int exitVal = -1;
		try {
			ColumbaLogger.log.debug("creating process..");
			Process p = executeCommand(cmd);

			errorStream = new StreamThread(p.getErrorStream());
			outputStream = new StreamThread(p.getInputStream());

			errorStream.start();
			outputStream.start();

			ColumbaLogger.log.debug("sending to stdin..");
			sendToStdin(p, rawString);

			exitVal = p.waitFor();
			ColumbaLogger.log.debug("exitcode=" + exitVal);

			ColumbaLogger.log.debug("retrieving output..");
			result = getOutputString();

			// wait for stream threads to die
			outputStream.join();
			errorStream.join();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (result == null)
			return rawString;

		StringBuffer buf = new StringBuffer();
		buf.append("X-Spam-Level: ");
		// result already contains "\n"
		buf.append(result);
		
		if (exitVal == 1)
			// spam found
			buf.append("X-Spam-Flag: YES\n");
		else
			buf.append("X-Spam-Flag: NO\n");
		buf.append(rawString);
		
		// free memory
		rawString=null;
		result=null;

		return buf.toString();
		//return result;
	}

	protected Process executeCommand(String cmd) throws Exception {
		Process p = Runtime.getRuntime().exec(cmd);

		return p;
	}

	// error gets parsed
	public String getErrorString() {
		String str = errorStream.getBuffer();
		return str;
	}

	public String getOutputString() {
		String str = outputStream.getBuffer();
		return str;
	}

	protected void sendToStdin(Process p, String str) throws Exception {
		PrintWriter out = new PrintWriter(p.getOutputStream());
		out.println(str);
		out.flush();
		out.close();
	}

	public class StreamThread extends Thread {
		InputStream is;

		StringBuffer buf;

		public StreamThread(InputStream is) {
			this.is = is;

			buf = new StringBuffer();
		}

		public void run() {

			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(">" + line);
					buf.append(line + "\n");
				}

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public String getBuffer() {
			return buf.toString();
		}

	}

}
