
package org.tigris.scarab.feeds;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.fulcrum.parser.StringValueParser;
import org.apache.torque.TorqueException;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabToolManager;
import org.tigris.scarab.util.IteratorWithSize;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.util.word.IssueSearchFactory;
import org.tigris.scarab.util.word.MaxConcurrentSearchException;
import org.tigris.scarab.util.word.QueryResult;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;

/**
 * Converts a query to an RSS feed.  The private methods are mostly ripped off
 * of ScarabRequestTool and there should be some refactoring done here.
 * 
 * @author Eric Pugh
 *  
 */
public class QueryFeed implements Feed {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private Query query;
    private ScarabUser user;
    private ScarabLink scarabLink;
	private ScarabToolManager scarabToolManager;
    

    public QueryFeed(Query query, ScarabUser user,ScarabToolManager scarabToolManager, ScarabLink scarabLink) {
        this.query = query;
        this.user = user;
        this.scarabLink = scarabLink;
        this.scarabToolManager = scarabToolManager;
    }

    public SyndFeed getFeed() throws IOException, FeedException, TorqueException, Exception {
        DateFormat dateParser = new SimpleDateFormat(DATE_FORMAT);


        SyndFeed feed = new SyndFeedImpl();

        MITList mitList = query.getMITList();
        boolean showModuleName = !mitList.isSingleModule();
        boolean showIssueType = !mitList.isSingleIssueType();
        String currentQueryString = query.getValue();
        IssueSearch search = getPopulatedSearch(currentQueryString,mitList,user);
        
        IteratorWithSize queryResults = null;

        // Do search

        if (search == null) {
            // an alert message should have been set while attempting
            // to populate the search.
            queryResults = IteratorWithSize.EMPTY;
        } else {
            queryResults = search.getQueryResults();

        }

        feed.setTitle(query.getName());
        String link = scarabLink.setAction("Search").addPathInfo("go",query.getQueryId()).toString();
        feed.setLink(link);
        feed.setDescription(query.getDescription());

        List entries = new ArrayList();

        for (Iterator i = queryResults; i.hasNext();) {
            SyndEntry entry;
            SyndContent description;
            //MITListItem item = (MITListItem) i.next();
            QueryResult queryResult = (QueryResult)i.next();

            
            
            entry = new SyndEntryImpl();            
            String title = queryResult.getUniqueId();
            if(showModuleName){
                title = title + " ("+ queryResult.getModule().getRealName() + ")";
            }
            if(showIssueType){
                title = title + " ("+ queryResult.getRModuleIssueType().getDisplayName() + ")";
            }
            entry.setTitle(title);
            
            Issue issue = IssueManager.getInstance(Long.valueOf(queryResult.getIssueId()));

            link = scarabLink.getIssueIdAbsoluteLink(issue).toString();
            entry.setLink(link);
            
            Date publishedDate = null;
            if(issue.getModifiedDate()!= null){
                publishedDate = issue.getModifiedDate();
            }
            else {
                publishedDate = issue.getCreatedDate();
            }
            entry.setPublishedDate(publishedDate);
            

            description = new SyndContentImpl();
            description.setType("text/html");
            String desc = "";
            Iterator avIteratorCSV = queryResult.getAttributeValuesAsCSV().iterator();
            Iterator avIterator = search.getIssueListAttributeColumns().iterator();
            for(;avIterator.hasNext();){
                String value = (String)avIteratorCSV.next();
                RModuleUserAttribute av = (RModuleUserAttribute)avIterator.next();
                desc = desc + "<b>" + av.getAttribute().getName()+":</b>" + value +"<br/>";
            }
            description.setValue(desc);

            entry.setDescription(description);
            entries.add(entry);

        }
     

        feed.setEntries(entries);
        
        search.close();
        IssueSearchFactory.INSTANCE.notifyDone();

        return feed;
    }

    /**
     * Get an IssueSearch object based on a query string. Copied from
     * ScarabRequestTool
     * 
     * @return a <code>Issue</code> value
     */
    private IssueSearch getPopulatedSearch(String query, MITList mitList, ScarabUser searcher) throws Exception {
        IssueSearch search = getNewSearch(mitList,searcher);
        Module module = mitList.getModule();   
        IssueType issueType = null;
        // this sucks, but seems required...
        if(mitList.isSingleIssueType()){
        	issueType = mitList.getIssueType();
        }
        else {
        	issueType = mitList.getFirstItem().getIssueType();
        }
        List listUserAttributes = scarabToolManager.getRModuleUserAttributes(user,module,issueType);
        search.setIssueListAttributeColumns(listUserAttributes);

       //Intake intake = null;

        if (query == null) {
            throw new Exception("Query was null");
        }

        // If they have entered users to search on, add them to the search
        StringValueParser parser = new StringValueParser();
        parser.parse(query, '&', '=', true);
        String[] userList = parser.getStrings("user_list");
        if (userList != null && userList.length > 0) {
            for (int i = 0; i < userList.length; i++) {
                String userId = userList[i];
                String[] attrIds = parser.getStrings("user_attr_" + userId);
                if (attrIds != null) {
                    for (int j = 0; j < attrIds.length; j++) {
                        search.addUserCriteria(userId, attrIds[j]);
                    }
                }
            }
        }

        // Set intake properties
        /*
         * boolean searchSuccess = true; Group searchGroup =
         * intake.get("SearchIssue", search.getQueryKey());
         * 
         * Field minDate = searchGroup.get("MinDate"); if (minDate != null &&
         * minDate.toString().length() > 0) { searchSuccess = checkDate(search,
         * minDate.toString()); }
         * 
         * Field maxDate = searchGroup.get("MaxDate"); if (maxDate != null &&
         * maxDate.toString().length() > 0) { searchSuccess = checkDate(search,
         * maxDate.toString()); }
         * 
         * Field stateChangeFromDate = searchGroup.get("StateChangeFromDate");
         * if (stateChangeFromDate != null &&
         * stateChangeFromDate.toString().length() > 0) { searchSuccess =
         * checkDate(search, stateChangeFromDate.toString()); }
         * 
         * Field stateChangeToDate = searchGroup.get("StateChangeToDate"); if
         * (stateChangeToDate != null && stateChangeToDate.toString().length() >
         * 0) { searchSuccess = checkDate(search, stateChangeToDate.toString()); }
         * 
         * if (!searchSuccess) { setAlertMessage(l10n.format("DateFormatPrompt",
         * L10NKeySet.ShortDateDisplay)); return null; }
         * 
         * try { searchGroup.setProperties(search); } catch (Exception e) {
         * setAlertMessage(l10n.getMessage(e)); return null; }
         * 
         * Integer oldOptionId = search.getStateChangeFromOptionId(); if
         * (oldOptionId != null && oldOptionId.intValue() != 0 &&
         * oldOptionId.equals(search.getStateChangeToOptionId())) {
         * setAlertMessage(L10NKeySet.StateChangeOldEqualNew); return null; }
         */
        // Set attribute values to search on
        LinkedMap avMap = search.getCommonAttributeValuesMap();
        Iterator i = avMap.mapIterator();
        while (i.hasNext()) {
            AttributeValue aval = (AttributeValue) avMap.get(i.next());
         //   Group group = intake.get("AttributeValue", aval.getQueryKey());
          //  if (group != null) {
          //      group.setProperties(aval);
          //  }
        }

        // If user is sorting on an attribute, set sort criteria
        // Do not use intake, since intake parsed from query is not the same
        // As intake passed from the form
        //    String sortColumn = data.getParameters().getString("sortColumn");
        //      if (sortColumn != null && sortColumn.length() > 0 &&
        // StringUtils.isNumeric(sortColumn)) {
        //      search.setSortAttributeId(new Integer(sortColumn));
        // }

        //        String sortPolarity = data.getParameters().getString("sortPolarity");
        //      if (sortPolarity != null && sortPolarity.length() > 0) {
        //        search.setSortPolarity(sortPolarity);
        //  }

        return search;
    }

    /**
     * Get a new IssueSearch object. Copied from ScarabRequestTool
     * 
     * @return a <code>Issue</code> value
     */
    private IssueSearch getNewSearch(MITList mitList, ScarabUser searcher) throws Exception,
            MaxConcurrentSearchException {

        IssueSearch issueSearch = IssueSearchFactory.INSTANCE.getInstance(mitList, searcher);
        // issueSearch.setLocale(getLocalizationTool().getPrimaryLocale());

        return issueSearch;
    }

}