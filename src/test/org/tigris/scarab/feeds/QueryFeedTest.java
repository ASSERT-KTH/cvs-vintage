/*
 * Created on Nov 20, 2004
 *
 */
package org.tigris.scarab.feeds;

import org.apache.torque.om.NumberKey;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.test.BaseScarabTestCase;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedOutput;


/**
 * @author Eric Pugh
 *
 */
public class QueryFeedTest extends BaseScarabTestCase {
    
    public void testCreatingFeed() throws Exception{
        Query query = QueryManager.getInstance(new Long(280));
        ScarabUser user1 = ScarabUserManager.getInstance(new NumberKey(1), false);
        assertNotNull(query);
        QueryFeed feed = new QueryFeed(query,user1);
        SyndFeed syndFeed = feed.getFeed();
        syndFeed.setFeedType("rss_2.0");
        SyndFeedOutput out = new SyndFeedOutput();
        String stringOutput = out.outputString(syndFeed);
        System.out.println(stringOutput);
    }

}
