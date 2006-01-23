package org.gjt.sp.jedit.help;

import java.awt.Component;
import java.beans.PropertyChangeListener;

/**
 * Interface supported by all HelpViewer classes.
 * Currently used by @ref infoviewer.InfoViewerPlugin and @ref HelpViewer 
 * 
 * @since Jedit 4.2pre3
 * @version $Id: HelpViewerInterface.java,v 1.5 2006/01/23 23:04:25 ezust Exp $
 */
public interface HelpViewerInterface 
{
	public void addPropertyChangeListener(PropertyChangeListener l);
	public void dispose();
	public String getBaseURL();
	public Component getComponent();
	public String getShortURL();
	public void gotoURL(String url, boolean addToHistory);
	public void queueTOCReload();
	public void setTitle(String newTitle);
}
