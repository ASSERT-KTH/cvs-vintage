package org.columba.core.io;

import java.io.File;
import java.net.URL;

public class ColumbaDesktop implements DesktopInterface {

	private static ColumbaDesktop instance = new ColumbaDesktop();
	
	DesktopInterface activeDesktop;
	
	protected ColumbaDesktop() {
		activeDesktop = new DefaultDesktop();
	}

	public String getMimeType(File file) {
		return activeDesktop.getMimeType(file);
	}

	public String getMimeType(String ext) {
		return activeDesktop.getMimeType(ext);
	}

	public boolean supportsOpen() {
		return activeDesktop.supportsOpen();
	}

	public boolean open(File file) {
		return activeDesktop.open(file);
	}

	public boolean openAndWait(File file) {
		return activeDesktop.open(file);
	}

	public boolean supportsBrowse() {
		return activeDesktop.supportsBrowse();
	}

	public void browse(URL url) {
		activeDesktop.browse(url);
	}

	/**
	 * @return Returns the activeDesktop.
	 */
	public DesktopInterface getActiveDesktop() {
		return activeDesktop;
	}

	/**
	 * @param activeDesktop The activeDesktop to set.
	 */
	public void setActiveDesktop(DesktopInterface activeDesktop) {
		this.activeDesktop = activeDesktop;
	}

	/**
	 * @return Returns the instance.
	 */
	public static ColumbaDesktop getInstance() {
		return instance;
	}

}
