package org.columba.core.gui.htmlviewer;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.columba.core.io.DiskIO;
import org.columba.core.io.TempFileStore;
import org.columba.core.main.Main;
import org.jdesktop.jdic.browser.WebBrowser;

/**
 * JDIC-enabled web browser component used by the Message Viewer in component
 * mail.
 * <p>
 * TODO: how to use images in Message Viewer, we can't set a base URL and load
 * images from columba.jar?
 * 
 * @author Frederik Dietz
 * 
 */
public class JDICHTMLViewerPlugin extends JComponent implements
		IHTMLViewerPlugin {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.htmlviewer");

	private WebBrowser browser;

	public JDICHTMLViewerPlugin() {
		super();

		browser = new WebBrowser();

		setLayout(new BorderLayout());
		add(browser, BorderLayout.CENTER);
	}

	public void view(String htmlSource) {
		// TODO: update JDIC to version 0.9 which has a setContent(String)
		// implementation
		try {
			// create temporary file
			File inputFile = TempFileStore.createTempFileWithSuffix("html");
			// save bodytext to file
			DiskIO.saveStringInFile(inputFile, htmlSource);
			// get URL of file
			URL url = inputFile.toURL();

			// browser.setContent(htmlSource);

			browser.setURL(url);
		} catch (MalformedURLException e) {
			LOG.severe("Error while viewing HTML page: " + e.getMessage());
			if (Main.DEBUG)
				e.printStackTrace();
		} catch (IOException e) {
			LOG.severe("Error while viewing HTML page: " + e.getMessage());
			if (Main.DEBUG)
				e.printStackTrace();
		}
	}

	public void view(URL url) {
		browser.setURL(url);
	}

	public JComponent getView() {
		return this;
	}

	public String getSelectedText() {
		return "getSelected() not yet supported by JDIC";
	}

	public boolean initialized() {
		// TODO: update JDIC to version 0.9 and check status with
		// WebBrowser.isInitialized() instead
		return browser.getStatus().isInitialized();
	}
}
