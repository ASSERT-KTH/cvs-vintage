
package org.tigris.scarab.feeds;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.tools.ScarabToolManager;
import org.tigris.scarab.util.ScarabLink;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;

/**
 * Converts a Issue to an RSS feed.
 * 
 * @todo improve what is shown to a user
 * @author Eric Pugh
 *  
 */
public class IssueFeed implements Feed{
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private Issue issue;

    private ScarabLink scarabLink;
    private ScarabToolManager scarabToolManager;

    public IssueFeed(Issue issue,ScarabLink scarabLink,ScarabToolManager scarabToolManager) {
        this.issue = issue;
        this.scarabLink = scarabLink;
        this.scarabToolManager = scarabToolManager;
    }

    public SyndFeed getFeed() throws IOException, FeedException, TorqueException, Exception {
        DateFormat dateParser = new SimpleDateFormat(DATE_FORMAT);

        SyndFeed feed = new SyndFeedImpl();
       
        String title = issue.getUniqueId() + ": " + issue.getDefaultText();
        feed.setTitle(title);
        String link = scarabLink.getIssueIdAbsoluteLink(issue).toString();
        feed.setLink(link);
        feed.setDescription(title);

        List entries = new ArrayList();
        List allActivities = issue.getActivity(true);
        

        for (Iterator i = allActivities.iterator(); i.hasNext();) {
            SyndEntry entry;
            SyndContent description;
            
            Activity activity = (Activity)i.next();
            
            ActivitySet activitySet = activity.getActivitySet();
            Date date =activitySet.getCreatedDate();
            entry = new SyndEntryImpl();
            
            entry.setTitle(title);

            entry.setPublishedDate(date);
            
            description = new SyndContentImpl();
            description.setType("text/html");
            
            StringBuffer desc = new StringBuffer();            
            desc.append("<b>Description:</b>" + activity.getDescription() +"<br/>");
            desc.append("<b>Reason:</b>" + scarabToolManager.getActivityReason(activitySet,activity) +"<br/>");                

            description.setValue(desc.toString());            

            entry.setDescription(description);
            
            entries.add(entry);

        }
     

        feed.setEntries(entries);
        
        return feed;
    }

}