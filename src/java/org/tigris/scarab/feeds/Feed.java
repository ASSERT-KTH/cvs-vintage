/*
 * Created on Dec 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigris.scarab.feeds;

import java.io.IOException;

import org.apache.torque.TorqueException;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

/**
 * @author Eric Pugh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Feed {
	public abstract SyndFeed getFeed() throws IOException, FeedException,
			TorqueException, Exception;
}