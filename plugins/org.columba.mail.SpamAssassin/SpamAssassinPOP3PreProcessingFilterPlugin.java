/*
 * Created on Apr 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

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

	protected IPCHelper ipcHelper;

	public SpamAssassinPOP3PreProcessingFilterPlugin(XmlElement rootElement) {
		super(rootElement);

		ipcHelper = new IPCHelper();
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

			ipcHelper.executeCommand(cmd);

			ColumbaLogger.log.debug("sending to stdin..");

			exitVal = ipcHelper.send(rawString);

			ColumbaLogger.log.debug("exitcode=" + exitVal);

			ColumbaLogger.log.debug("retrieving output..");
			result = ipcHelper.getOutputString();

			ipcHelper.waitForThreads();

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

		rawString = null;

		// free memory
		rawString = null;
		result = null;

		return buf.toString();
		//return result;
	}

}
