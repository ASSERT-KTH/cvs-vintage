package org.tigris.scarab.util.word;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

// JDK classes
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.adapter.DB;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.ComboKey;
import org.apache.torque.om.SimpleKey;
import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.collections.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.fulcrum.localization.Localization;
import org.apache.log4j.Logger;

// Scarab classes
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttachmentTypePeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ActivityPeer;
import org.tigris.scarab.om.ActivitySetPeer;
import org.tigris.scarab.om.RModuleOptionPeer;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleIssueTypeManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListItem;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.ScarabUser;

import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.StringAttribute;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.IteratorWithSize;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.security.ScarabSecurity;

/** 
 * A utility class to build up and carry out a search for 
 * similar issues.  It subclasses Issue for functionality, it is 
 * not a more specific type of Issue.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: IssueSearch.java,v 1.126 2004/05/07 05:57:48 dabbous Exp $
 */
public class IssueSearch 
    extends Issue
{
    private static final int MAX_INNER_JOIN = 
        ScarabConstants.QUERY_MAX_FILTER_CRITERIA;

    private static final int MAX_JOIN = 
        ScarabConstants.QUERY_MAX_JOIN;

    public static final String ASC = "asc";
    public static final String DESC = "desc";

    public static final String CREATED_BY_KEY = "created_by";
    public static final String ANY_KEY = "any";

    // column names only
    private static final String AV_OPTION_ID = 
        AttributeValuePeer.OPTION_ID.substring(
        AttributeValuePeer.OPTION_ID.indexOf('.')+1);
    private static final String AV_ISSUE_ID = 
        AttributeValuePeer.ISSUE_ID.substring(
        AttributeValuePeer.ISSUE_ID.indexOf('.')+1);
    private static final String AV_USER_ID =
        AttributeValuePeer.USER_ID.substring(
        AttributeValuePeer.USER_ID.indexOf('.')+1);

    private static final String ACTIVITYSETALIAS = "srchcobyactset";
    private static final String USERAVALIAS = "srchuav";
    private static final String ACTIVITYALIAS = "srchcobyact";

    private static final String CREATED_BY = "CREATED_BY";
    private static final String CREATED_DATE = "CREATED_DATE";
    private static final String ATTRIBUTE_ID = "ATTRIBUTE_ID";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String INNER_JOIN = " INNER JOIN ";
    private static final String ON = " ON (";
    private static final String IN = " IN (";
    private static final String IS_NULL = " IS NULL";
    private static final String LEFT_OUTER_JOIN = " LEFT OUTER JOIN ";
    private static final String SELECT_DISTINCT = "select DISTINCT ";
    
    private static final String ACT_TRAN_ID = 
        ActivityPeer.TRANSACTION_ID.substring(
        ActivityPeer.TRANSACTION_ID.indexOf('.')+1);
    private static final String ACTSET_TRAN_ID = 
        ActivitySetPeer.TRANSACTION_ID.substring(
        ActivitySetPeer.TRANSACTION_ID.indexOf('.')+1);
    private static final String 
        ISSUEPEER_TRAN_ID__EQUALS__ACTIVITYSETALIAS_TRAN_ID =
        IssuePeer.CREATED_TRANS_ID + '=' + 
        ACTIVITYSETALIAS + '.' + ACTSET_TRAN_ID;

    private static final String ACT_ISSUE_ID = 
        ActivityPeer.ISSUE_ID.substring(ActivityPeer.ISSUE_ID.indexOf('.')+1);
    private static final String ACTIVITYALIAS_ISSUE_ID =
        ACTIVITYALIAS + '.' + ACT_ISSUE_ID;
    private static final String 
        ACTIVITYALIAS_ISSUE_ID__EQUALS__ISSUEPEER_ISSUE_ID =
        ACTIVITYALIAS_ISSUE_ID + '=' + IssuePeer.ISSUE_ID;
    private static final String END_DATE = 
        ActivityPeer.END_DATE.substring(
        ActivityPeer.END_DATE.indexOf('.')+1);

    private static final String ACT_ATTR_ID = 
        ActivityPeer.ATTRIBUTE_ID.substring(
        ActivityPeer.ATTRIBUTE_ID.indexOf('.')+1);
    private static final String AV_ATTR_ID = 
        AttributeValuePeer.ATTRIBUTE_ID.substring(
        AttributeValuePeer.ATTRIBUTE_ID.indexOf('.')+1);
    private static final String ACTIVITYALIAS_ATTRIBUTE_ID =
        ACTIVITYALIAS + '.' + ACT_ATTR_ID;


    private static final String USERAVALIAS_ISSUE_ID =
        USERAVALIAS + '.' + AV_ISSUE_ID;

    private static final String ACT_NEW_USER_ID = 
        ActivityPeer.NEW_USER_ID.substring(
        ActivityPeer.NEW_USER_ID.indexOf('.')+1);
    private static final String ACTIVITYALIAS_NEW_USER_ID =
        ACTIVITYALIAS + '.' + ACT_NEW_USER_ID;

    private static final String WHERE = " WHERE ";
    private static final String FROM = " FROM ";
    private static final String ORDER_BY = " ORDER BY ";
    private static final String BASE_OPTION_SORT_LEFT_JOIN = 
        " LEFT OUTER JOIN " + RModuleOptionPeer.TABLE_NAME + " sortRMO ON " +
        '(' + IssuePeer.MODULE_ID + "=sortRMO.MODULE_ID AND " +
        IssuePeer.TYPE_ID + "=sortRMO.ISSUE_TYPE_ID AND sortRMO.OPTION_ID=";
    private static final String AV = "av";
    private static final String DOT_OPTION_ID_PAREN = ".OPTION_ID)";
    private static final String DOT_VALUE = ".VALUE";
    private static final String SORTRMO_PREFERRED_ORDER = 
        "sortRMO.PREFERRED_ORDER";


    private static final Integer NUMBERKEY_0 = new Integer(0);

    /**
     * The managed database connection used while iterating over large
     * query result sets using a cursor.  This connection <b>must</b>
     * be explicitly closed when done with it (e.g. at the end of the
     * request)!
     */
    private Connection conn;

    /**
     * The statement that will be used by the connection to get the issue
     * ids for the query.
     */
    private Statement searchStmt; 
    /**
     * The ResultSet(s) that contains the issue ids for a query.
     */
    private ResultSet searchRS;

    /**
     * The statement(s) that will be used by the connection to obtain the
     * ResultSet for column data to be shown for the query.  Statements should
     * be closed when no longer needed. Closing a Statement will release any 
     * associated ResultSets for a compliant jdbc driver, but we don't rely 
     * on this behavior.
     */
    private List stmtList;
    /**
     * The ResultSet(s) that contain query result column data.  We store the 
     * size along with the RS in a ResultSetAndSize object, where size is
     * the number of columns in the RS.
     */
    private List rsList;

    /**
     * used to track how long we hold the connection
     */
    private long connectionStartTime;

    private SimpleDateFormat formatter;

    private String searchWords;
    private String commentQuery;
    private Integer[] textScope;
    private String minId;
    private String maxId;
    private String minDate;
    private String maxDate;
    private int minVotes;
    
    private Integer stateChangeAttributeId;
    private Integer stateChangeFromOptionId;
    private Integer stateChangeToOptionId;
    private String stateChangeFromDate;
    private String stateChangeToDate;

    private Integer sortAttributeId;
    private String sortPolarity;
    private MITList mitList;

    private List userIdList;
    private List userSearchCriteriaList;
    private List lastUsedAVList;
    private boolean modified;

    private int lastTotalIssueCount = -1;
    private List lastMatchingIssueIds = null;
    private IteratorWithSize lastQueryResults = null;

    // the attribute columns that will be shown
    private List issueListAttributeColumns;

    // used to cache a few modules and issuetypes to make listing
    // a result set faster.
    private LRUMap moduleMap = new LRUMap(20);
    private LRUMap rmitMap = new LRUMap(20);

    private boolean isSearchAllowed = true;

    /** A counter of inner joins used in a query */
    private int joinCounter;
    
    /**
     * This is the locale that the search is currently running
     * under. We need it to parse the date attributes. It defaults
     * to the US locale as that was the behaviour before.
     * @todo Ideally, the minDate, maxDate and others should
     * be Date objects, with the user of this class doing the
     * parsing itself. However, the intake tool is currently
     * configured to use this class directly. Hopefully when
     * (if?) intake supports dates natively, we can drop the
     * date parsing from this class and use Dates instead. 
     */
    private Locale locale = Locale.US;

    IssueSearch(Issue issue, ScarabUser searcher)
        throws Exception
    {
        this(issue.getModule(), issue.getIssueType(), searcher);
        
        //
        // Make copies of the issue's attribute values so that
        // we can modify them later without affecting the issue
        // itself.
        //
        // @todo: This section of code is a result of SCB965.
        // However, I think a more significant problem is that
        // ReportIssue is modifying the search's attribute values
        // directly. I believe this breaks some OO principle or
        // other and should be resolved some time.
        //
        List issueAttributes = issue.getAttributeValues();
        List searchAttributes = this.getAttributeValues();
        
        for (Iterator iter = issueAttributes.iterator(); iter.hasNext(); ) {
            AttributeValue value = (AttributeValue) iter.next();
            searchAttributes.add(value.copy());
        }
    }

    IssueSearch(Module module, IssueType issueType, ScarabUser searcher)
        throws Exception
    {
        super(module, issueType);
        isSearchAllowed = 
            searcher.hasPermission(ScarabSecurity.ISSUE__SEARCH, module); 
    }

    IssueSearch(MITList mitList, ScarabUser searcher)
        throws Exception
    {
        super();
        if (mitList == null || mitList.size() == 0) 
        {
            throw new IllegalArgumentException("A non-null list with at" +
               " least one item is required."); //EXCEPTION
        }

        String[] perms = {ScarabSecurity.ISSUE__SEARCH};
        MITList searchableList = mitList
            .getPermittedSublist(perms, searcher);
        isSearchAllowed = searchableList.size() > 0;

        if (searchableList.isSingleModuleIssueType()) 
        {
            MITListItem item = searchableList.getFirstItem();
            setModuleId(item.getModuleId());
            setTypeId(item.getIssueTypeId());
        }
        else 
        {
            this.mitList = searchableList;   
            if (searchableList.isSingleModule()) 
            {
                setModule(searchableList.getModule());
            }
            if (searchableList.isSingleIssueType()) 
            {
                setIssueType(searchableList.getIssueType());
            }
        }        
    }

    public Locale getLocale() {
        return this.locale;
    }
    
    public void setLocale(Locale newLocale) {
        this.locale = newLocale;
    }

    public boolean isXMITSearch()
    {
        return mitList != null && !mitList.isSingleModuleIssueType();
    }

    /**
     * List of attributes to show with each issue.
     *
     * @param rmuas a <code>List</code> of RModuleUserAttribute objects
     */
    public void setIssueListAttributeColumns(List rmuas)
    {
        //FIXME! implement logic to determine if a new search is required.
        //HELP: John, would it be sufficient to set modified=true?
        issueListAttributeColumns = rmuas;
    }

    public List getIssueListAttributeColumns()
    {
        return issueListAttributeColumns;
    }

    public List getUserIdList()
    {
        return userIdList;
    }

    public SequencedHashMap getCommonAttributeValuesMap()
        throws Exception
    {
        SequencedHashMap result = null;
        if (isXMITSearch()) 
        {
            result = getMITAttributeValuesMap();
        }
        else 
        {
            result = super.getModuleAttributeValuesMap(false);
        }
        return result;
    }

    /**
     * AttributeValues that are relevant to the issue's current module.
     * Empty AttributeValues that are relevant for the module, but have 
     * not been set for the issue are included.  The values are ordered
     * according to the module's preference
     */
    private SequencedHashMap getMITAttributeValuesMap() 
        throws Exception
    {
        SequencedHashMap result = null;

        List attributes = mitList.getCommonAttributes(false);
        Map siaValuesMap = getAttributeValuesMap();
        if (attributes != null) 
        {
            result = new SequencedHashMap((int)(1.25*attributes.size() + 1));
            Iterator i = attributes.iterator();
            while (i.hasNext()) 
            {
                Attribute attribute = (Attribute)i.next();
                String key = attribute.getName().toUpperCase();
                if (siaValuesMap.containsKey(key)) 
                {
                    result.put(key, siaValuesMap.get(key));
                }
                else 
                {
                    AttributeValue aval = AttributeValue
                        .getNewInstance(attribute, this);
                    addAttributeValue(aval);
                    result.put(key, aval);
                }
            }
        }
        return result;
    }

    /**
     * @return The list of attributes of type "user" for the module(s)
     * to search in.
     */
    public List getUserAttributes()
        throws Exception
    {
        List result = null;
        if (isXMITSearch()) 
        {
            result = mitList.getCommonUserAttributes(false);
        }
        else 
        {
            result = getModule().getUserAttributes(getIssueType(), false);
        }
        return result;        
    } 

    public List getLeafRModuleOptions(Attribute attribute)
        throws Exception
    {
        List result = null;
        if (isXMITSearch()) 
        {
            result = mitList.getCommonLeafRModuleOptions(attribute);
        }
        else 
        {
            result = getModule()
                .getLeafRModuleOptions(attribute, getIssueType());
        }
        return result;        
    } 

    public List getCommonOptionTree(Attribute attribute)
        throws Exception
    {
        return mitList.getCommonRModuleOptionTree(attribute);
    }

    /**
     * Get the words for which to search.
     *
     * @return Value of {@link #searchWords}.
     */
    public String getSearchWords() 
    {
        return searchWords;
    }
    
    /**
     * Set the words for which to search.
     *
     * @param v Value to assign to {@link #searchWords}.
     */
    public void setSearchWords(String  v) 
    {
        if (!ObjectUtils.equals(v, this.searchWords)) 
        {
            modified = true;
            this.searchWords = v;
        }
    }

    
    /**
     * Get the value of commentQuery.
     * @return value of commentQuery.
     */
    public String getCommentQuery() 
    {
        return commentQuery;
    }
    
    /**
     * Set the value of commentQuery.
     * @param v  Value to assign to commentQuery.
     */
    public void setCommentQuery(String  v) 
    {
        if (!ObjectUtils.equals(v, this.commentQuery)) 
        {
            modified = true;
            this.commentQuery = v;
        }
    }
    
    /**
     * Get the value of textScope.  if the scope is not set then all
     * text attributes are returned.  if there are no relevant text
     * attributes null will be returned.
     * @return value of textScope.
     */
    public Integer[] getTextScope()
        throws Exception
    {
        if (textScope == null) 
        {
            textScope = getTextScopeForAll();
        }
        else
        {
            for (int i = textScope.length - 1; i >= 0; i--)
            {
                if (NUMBERKEY_0.equals(textScope[i])) 
                {
                    textScope = getTextScopeForAll();
                    break;
                }       
            }
        }
        return textScope;
    }


    /**
     * Sets the text search scope to all quick search text attributes.
     */
    private Integer[] getTextScopeForAll()
        throws Exception
    {
        Integer[] textScope = null;
        List textAttributes = getQuickSearchTextAttributeValues();
        if (textAttributes != null) 
        {
            textScope = new Integer[textAttributes.size()];
            for (int j=textAttributes.size()-1; j>=0; j--) 
            {
                textScope[j] = ((AttributeValue)
                                textAttributes.get(j)).getAttributeId();
            }
        }
        return textScope;
    }

    /**
     * Set the value of textScope.
     * @param v  Value to assign to textScope.
     */
    public void setTextScope(Integer[] v) 
        throws Exception
    {
        if (v != null) 
        {
            for (int i=v.length-1; i>=0; i--) 
            {
                if (v[i].equals(NUMBERKEY_0)) 
                {
                    v = getTextScopeForAll();
                    break;
                }       
            }
        }

        // note previous block may have made v == null though its not likely
        // (don't replace the if with an else)
        if (v == null) 
        {
            modified |= this.textScope != null;
            this.textScope = null;
        }
        else if (this.textScope != null && this.textScope.length == v.length)
        {
            for (int i=v.length-1; i>=0; i--) 
            {
                if (!v[i].equals(this.textScope[i])) 
                {
                    modified = true;
                    this.textScope = v;            
                    break;
                }       
            }
        }
        else 
        {
            modified = true;
            this.textScope = v;            
        }
    }


    /**
     * Get the value of minId.
     * @return value of minId.
     */
    public String getMinId() 
    {
        return minId;
    }
    
    /**
     * Set the value of minId.
     * @param v  Value to assign to minId.
     */
    public void setMinId(String  v) 
    {
        if (v != null && v.length() == 0) 
        {
            v = null;
        }
        if (!ObjectUtils.equals(v, this.minId)) 
        {
            modified = true;
            this.minId = v;
        }
    }

    
    /**
     * Get the value of maxId.
     * @return value of maxId.
     */
    public String getMaxId() 
    {
        return maxId;
    }
    
    /**
     * Set the value of maxId.
     * @param v  Value to assign to maxId.
     */
    public void setMaxId(String  v) 
    {
        if (v != null && v.length() == 0) 
        {
            v = null;
        }
        if (!ObjectUtils.equals(v, this.maxId)) 
        {
            modified = true;
            this.maxId = v;
        }
    }
    
    
    /**
     * Get the value of minDate.
     * @return value of minDate.
     */
    public String getMinDate() 
    {
        return this.minDate;
    }
    
    /**
     * Set the value of minDate.
     * @param newMinDate  Value to assign to minDate.
     */
    public void setMinDate(String newMinDate) 
    {
        if (newMinDate != null && newMinDate.length() == 0) 
        {
            newMinDate = null;
        }
        
        if (!ObjectUtils.equals(newMinDate, this.minDate)) 
        {
            this.modified = true;
            this.minDate = newMinDate;
        }
    }

    
    /**
     * Get the value of maxDate.
     * @return value of maxDate.
     */
    public String getMaxDate() 
    {
        return this.maxDate;
    }
    
    /**
     * Set the value of maxDate.
     * @param newMaxDate Value to assign to maxDate.
     */
    public void setMaxDate(String newMaxDate) 
    {
        if (newMaxDate != null && newMaxDate.length() == 0) 
        {
            newMaxDate = null;
        }
        
        if (!ObjectUtils.equals(newMaxDate, this.maxDate)) 
        {
            this.modified = true;
            this.maxDate = newMaxDate;
        }
    }
    
    /**
     * Get the value of minVotes.
     * @return value of minVotes.
     */
    public int getMinVotes() 
    {
        return minVotes;
    }
    
    /**
     * Set the value of minVotes.
     * @param v  Value to assign to minVotes.
     */
    public void setMinVotes(int  v) 
    {
        if (v != this.minVotes) 
        {
            modified = true;
            this.minVotes = v;
        }
    }    


    /**
     * Get the value of stateChangeAttributeId.
     * @return value of stateChangeAttributeId.
     */
    public Integer getStateChangeAttributeId() 
    {
        return stateChangeAttributeId;
    }
    
    /**
     * Set the value of stateChangeAttributeId.
     * @param v  Value to assign to stateChangeAttributeId.
     */
    public void setStateChangeAttributeId(Integer  v) 
    {
        if (!ObjectUtils.equals(v, this.stateChangeAttributeId)) 
        {
            modified = true;
            this.stateChangeAttributeId = v;
        }
    }
        
    /**
     * Get the value of stateChangeFromOptionId.
     * @return value of stateChangeFromOptionId.
     */
    public Integer getStateChangeFromOptionId() 
    {
        return stateChangeFromOptionId;
    }
    
    /**
     * Set the value of stateChangeFromOptionId.
     * @param v  Value to assign to stateChangeFromOptionId.
     */
    public void setStateChangeFromOptionId(Integer  v) 
    {
        if (!ObjectUtils.equals(v, this.stateChangeFromOptionId)) 
        {
            modified = true;
            this.stateChangeFromOptionId = v;
        }
    }
    
    /**
     * Get the value of stateChangeToOptionId.
     * @return value of stateChangeToOptionId.
     */
    public Integer getStateChangeToOptionId() 
    {
        return stateChangeToOptionId;
    }
    
    /**
     * Set the value of stateChangeToOptionId.
     * @param v  Value to assign to stateChangeToOptionId.
     */
    public void setStateChangeToOptionId(Integer  v) 
    {
        if (!ObjectUtils.equals(v, this.stateChangeToOptionId)) 
        {
            modified = true;
            this.stateChangeToOptionId = v;
        }
    }

    
    /**
     * Get the value of stateChangeFromDate.
     * @return value of stateChangeFromDate.
     */
    public String getStateChangeFromDate() 
    {
        return this.stateChangeFromDate;
    }
    
    /**
     * Set the value of stateChangeFromDate.
     * @param fromDate Value to assign to stateChangeFromDate.
     */
    public void setStateChangeFromDate(String fromDate) 
    {
        if (fromDate != null && fromDate.length() == 0) 
        {
            fromDate = null;
        }
        
        if (!ObjectUtils.equals(fromDate, this.stateChangeFromDate)) 
        {
            this.modified = true;
            this.stateChangeFromDate = fromDate;
        }
    }
    
    
    /**
     * Get the value of stateChangeToDate.
     * @return value of stateChangeToDate.
     */
    public String getStateChangeToDate()
    {
        return this.stateChangeToDate;
    }
    
    /**
     * Set the value of stateChangeToDate.
     * @param toDate Value to assign to stateChangeToDate.
     */
    public void setStateChangeToDate(String toDate) 
    {
        if (toDate != null && toDate.length() == 0) 
        {
            toDate = null;
        }
        
        if (!ObjectUtils.equals(toDate, this.stateChangeToDate)) 
        {
            this.modified = true;
            this.stateChangeToDate = toDate;
        }
    }
    
    
    /**
     * Get the value of sortAttributeId.
     * @return value of SortAttributeId.
     */
    public Integer getSortAttributeId() 
    {
        return sortAttributeId;
    }
    
    /**
     * Set the value of sortAttributeId.
     * @param v  Value to assign to sortAttributeId.
     */
    public void setSortAttributeId(Integer v) 
    {
        if (!ObjectUtils.equals(v, this.sortAttributeId)) 
        {
            modified = true;
            this.sortAttributeId = v;
        }
    }

    /**
     * Whether to do SQL sorting in <code>DESC</code> or
     * <code>ASC</code> order (the default being the latter).
     * @return value of sortPolarity.
     */
    public String getSortPolarity() 
    {
        return (DESC.equals(sortPolarity) ? DESC : ASC);
    }
    
    /**
     * Set the value of sortPolarity.
     * @param v  Value to assign to sortPolarity.
     */
    public void setSortPolarity(String  v) 
    {
        if (!ObjectUtils.equals(v, this.sortPolarity)) 
        {
            modified = true;
            this.sortPolarity = v;
        }
    }

    /**
     * Describe <code>addUserSearch</code> method here.
     *
     * @param userId a <code>String</code> represention of the PrimaryKey
     * @param searchCriteria a <code>String</code> either a String 
     * representation of an Attribute PrimaryKey, or the Strings "created_by" 
     * "any"
     */
    public void addUserCriteria(String userId, String searchCriteria)
    {
        if (userId == null) 
        {
            throw new IllegalArgumentException("userId cannot be null."); //EXCEPTION
        }
        if (searchCriteria == null) 
        {
            searchCriteria = ANY_KEY;
        }

        if (userIdList == null) 
        {
            userIdList = new ArrayList(4);
            userSearchCriteriaList = new ArrayList(4);
        }
        boolean newCriteria = true;
        for (int i=userIdList.size()-1; i>=0 && newCriteria; i--) 
        {
            Object attrId = userSearchCriteriaList.get(i);
            // not new if attrId already present or an ANY search has already
            // been specified
            newCriteria = !(userId.equals(userIdList.get(i)) && 
               (searchCriteria.equals(attrId) || ANY_KEY.equals(attrId))); 
        }
        
        if (newCriteria) 
        {
            modified = true;
            // if the new criteria is ANY, then remove old criteria 
            if (ANY_KEY.equals(searchCriteria)) 
            {
                for (int i=userIdList.size()-1; i>=0; i--) 
                {
                    if (userId.equals(userIdList.get(i)))
                    {
                        userIdList.remove(i);
                        userSearchCriteriaList.remove(i);
                    }
                }
            }
            userIdList.add(userId);
            userSearchCriteriaList.add(searchCriteria);
        }
    }

    private boolean isAVListModified()
        throws TorqueException
    {
        boolean result = false;
        if (lastUsedAVList == null) 
        {
            result = true;
        }
        else 
        {
            List avList = getAttributeValues();
            int max = avList.size();
            if (lastUsedAVList.size() == max) 
            {
                for (int i=0; i<max; i++) 
                {
                    AttributeValue a1 = (AttributeValue)avList.get(i);
                    AttributeValue a2 = (AttributeValue)lastUsedAVList.get(i);
                    if (!ObjectUtils.equals(a1.getOptionId(), a2.getOptionId())
                         || !ObjectUtils.equals(a1.getUserId(), a2.getUserId())
                         //|| a1.getNumericValue() != a2.getNumericValue()
                         || !ObjectUtils.equals(a1.getValue(), a2.getValue()))
                    {
                        result = true;
                    }
                }
            }
            else 
            {
                result = true;
            }
        }        
        return result;
    }

    /**
     * 
     *
     * @return a <code>boolean</code> value
     */
    private void checkModified()
        throws TorqueException
    {
        if (modified || isAVListModified()) 
        {
            modified = false;
            lastTotalIssueCount = -1;
            lastMatchingIssueIds = null;
            lastQueryResults = null;
        }
    }

    public Integer getALL_TEXT()
    {
        return NUMBERKEY_0;
    }

    public List getQuickSearchTextAttributeValues()
        throws Exception
    {
        return getTextAttributeValues(true);
    }

    public List getTextAttributeValues()
        throws Exception
    {
        return getTextAttributeValues(false);
    }

    private List getTextAttributeValues(boolean quickSearchOnly)
        throws Exception
    {
        SequencedHashMap searchValues = getCommonAttributeValuesMap();
        List searchAttributes = new ArrayList(searchValues.size());

        for (int i=0; i<searchValues.size(); i++) 
        {
            AttributeValue searchValue = 
                (AttributeValue)searchValues.getValue(i);
            if ((!quickSearchOnly || searchValue.isQuickSearchAttribute())
                 && searchValue.getAttribute().isTextAttribute()) 
            {
                searchAttributes.add(searchValue);
            }
        }

        return searchAttributes;
    }

    /**
     * Returns OptionAttributes which have been marked for Quick search.
     *
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getQuickSearchOptionAttributeValues()
        throws Exception
    {
        return getOptionAttributeValues(true);
    }

    /**
     * Returns OptionAttributes which have been marked for Quick search.
     *
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getOptionAttributeValues()
        throws Exception
    {
        return getOptionAttributeValues(false);
    }


    /**
     * Returns OptionAttributes which have been marked for Quick search.
     *
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    private List getOptionAttributeValues(boolean quickSearchOnly)
        throws Exception
    {
        SequencedHashMap searchValues = getCommonAttributeValuesMap();
        List searchAttributeValues = new ArrayList(searchValues.size());

        for (int i=0; i<searchValues.size(); i++) 
        {
            AttributeValue searchValue = 
                (AttributeValue)searchValues.getValue(i);
            if ((!quickSearchOnly || searchValue.isQuickSearchAttribute())
                 && searchValue instanceof OptionAttribute) 
            {
                searchAttributeValues.add(searchValue);
            }
        }

        return searchAttributeValues;
    }


    /**
     * remove unset AttributeValues.
     *
     * @param attValues a <code>List</code> value
     */
    private List removeUnsetValues(List attValues)
    {
        int size = attValues.size();
        List setAVs = new ArrayList(size);
        for (int i=0; i<size; i++) 
        {
            AttributeValue attVal = (AttributeValue) attValues.get(i);
            if (attVal.isSet())
            {
                setAVs.add(attVal);
            }
        }
        return setAVs;
    }


    private void addAnd(StringBuffer sb)
    {
        if (sb.length() > 0) 
        {
            sb.append(AND);
        }
    }

    private void addIssueIdRange(StringBuffer where)
        throws ScarabException, Exception
    {
        // check limits to see which ones are present
        // if neither are present, do nothing
        if ((minId != null && minId.length() != 0)
              || (maxId != null && maxId.length() != 0)) 
        {
            StringBuffer sb = new StringBuffer();
            String domain = null;
            String prefix = null;
            Issue.FederatedId minFid = null;
            Issue.FederatedId maxFid = null;
            if (minId == null || minId.length() == 0) 
            {
                maxFid = new Issue.FederatedId(maxId);
                setDefaults(null, maxFid);
                addAnd(sb);
                sb.append(IssuePeer.ID_COUNT).append("<=")
                    .append(maxFid.getCount());
                domain = maxFid.getDomain();
                prefix = maxFid.getPrefix();
            }
            else if (maxId == null || maxId.length() == 0) 
            {
                minFid = new Issue.FederatedId(minId);
                setDefaults(minFid, null);
                addAnd(sb);
                sb.append(IssuePeer.ID_COUNT).append(">=")
                    .append(minFid.getCount());
                domain = minFid.getDomain();
                prefix = minFid.getPrefix();
            }
            else 
            {
                minFid = new Issue.FederatedId(minId);
                maxFid = new Issue.FederatedId(maxId);
                setDefaults(minFid, maxFid);
                
                // make sure min id is less than max id and that the character
                // parts are equal otherwise skip the query, there are no 
                // matches
                if (minFid.getCount() <= maxFid.getCount() 
                  && StringUtils.equals(minFid.getPrefix(), maxFid.getPrefix())
                  && StringUtils.equals(minFid.getDomain(), maxFid.getDomain()))
                {
                    addAnd(sb);
                    sb.append(IssuePeer.ID_COUNT).append(">=")
                        .append(minFid.getCount()).append(AND)
                        .append(IssuePeer.ID_COUNT).append("<=")
                        .append(maxFid.getCount());
                    domain = minFid.getDomain();
                    prefix = minFid.getPrefix();
                }
                else 
                {
                    throw ScarabException.create(L10NKeySet.ExceptionIncompatibleIssueIds,
                                              minId,
                                              maxId);
                }
            }
            if (domain != null) 
            {
                sb.append(AND).append(IssuePeer.ID_DOMAIN).append("='")
                    .append(domain).append('\'');
            }
            if (prefix != null) 
            {
                sb.append(AND).append(IssuePeer.ID_PREFIX).append("='")
                    .append(prefix).append('\'');
            }
            where.append(AND).append(sb);
        }
    }


    /**
     * give reasonable defaults if module code was not specified
     */
    private void setDefaults(FederatedId minFid, 
                             FederatedId maxFid)
        throws Exception
    {
        Module module = getModule();
        if (module != null) 
        {
            if (minFid != null && minFid.getDomain() == null) 
            {
                minFid.setDomain(module.getDomain());
            }
            if (maxFid != null && maxFid.getDomain() == null) 
            {
                maxFid.setDomain(module.getDomain());
            }
            if (minFid != null && minFid.getPrefix() == null) 
            {
                minFid.setPrefix(module.getCode());
            }            
        }
        if (maxFid != null && maxFid.getPrefix() == null) 
        {
            if (minFid == null) 
            {
                maxFid.setPrefix(module.getCode());                
            }
            else 
            {
                maxFid.setPrefix(minFid.getPrefix());        
            }
        }
    }

    /**
     * Attempts to parse a atring as a date, first using the locale-sepcific
     * short date format, and then the ISO standard "yyyy-mm-dd". If it sees
     * a ':' character in the date string then the string will be interpreted
     * as a date <b>and</b> time. Throws a ParseException if the String does
     * not contain a suitable format.
     *
     * @param dateString a <code>String</code> value
     * @param locale the locale to use when determining the date patterns
     * to try.
     * @param addTwentyFourHours if no time is given in the date string and
     * this flag is true, then 24 hours - 1 msec will be added to the date.
     * @return a <code>Date</code> value
     */
    public Date parseDate(String dateString,
                          boolean addTwentyFourHours)
        throws ParseException
    {
        Date date = null;
        if (dateString != null) 
        {
            if (dateString.indexOf(':') == -1)
            {
                //
                // First try to parse the date using the current
                // locale. If that doesn't work, then try the
                // ISO format.
                //
                String[] patterns = {
                    Localization.getString(this.locale, "ShortDatePattern"),
                    ScarabConstants.ISO_DATE_PATTERN };
                date = parseDate(dateString, patterns);
                
                // one last try with the default locale format
                if (date == null) 
                {
                    //
                    // If this fails, then we want the parse exception
                    // to propogate. That's why we don't use
                    // parseDateWithFormat() here.
                    //
                    date = DateFormat.getDateInstance().parse(dateString);
                }

                // add 24 hours to max date so it is inclusive
                if (addTwentyFourHours) 
                {                
                    date.setTime(date.getTime() + 86399999);
                }
            }
            else
            {
                //
                // First try to parse the date using the current
                // locale. If that doesn't work, then try the
                // ISO format.
                //
                String[] patterns = {
                    Localization.getString(this.locale, "ShortDateTimePattern"),
                    ScarabConstants.ISO_DATETIME_PATTERN };
                date = parseDate(dateString, patterns);
        
                // one last try with the default locale format
                if (date == null) 
                {
                    date = DateFormat.getDateTimeInstance().parse(dateString);
                }
            }
        }
        
        return date;
    }
    
    /**
     * Attempts to parse a String as a Date, trying each pattern in
     * turn until the string is successfully parsed or all patterns
     * have been tried.
     *
     * @param s a <code>String</code> value that should be converted
     * to a <code>Date</code>.
     * @param patterns if no time is given in the date string and
     * this flag is true, then 24 hours - 1 msec will be added to the date.
     * @return the equivalent <code>Date</code> if the string could
     * be parsed. 
     * @throws ParseException if input String is null, or the string
     * could not be parsed.
     */
    private Date parseDate(String s, String[] patterns)
        throws ParseException
    {
        /* FIXME: the contract for this method is strange
           it is returning a null value when encountering a ParseException,
           and throwing a ParseException when having a wrong input*/
        if (s == null) 
        {
            throw new ParseException("Input string was null", -1); //EXCEPTION
        }

        if (formatter == null) 
        {
            formatter = new SimpleDateFormat();
        }
        
        for (int i = 0; i < patterns.length; i++) 
        {
            formatter.applyPattern(patterns[i]);
            Date date = parseDateWithFormat(s, formatter);
            
            if (date != null) 
            {
                return date;
            }
        }
        
        throw new ParseException("Date could not be parsed with any"
                                 + " of the provided date patterns.", -1); //EXCEPTION
    }
    
    private Date parseDateWithFormat(String dateString, DateFormat format) {
        try
        {
            return format.parse(dateString);
        }
        catch (ParseException ex)
        {
            return null;
        }
    }


    private void addDateRange(String column, Date minUtilDate,
                              Date maxUtilDate, StringBuffer sb)
        throws Exception
    {
        // check limits to see which ones are present
        // if neither are present, do nothing
        if (minUtilDate != null || maxUtilDate != null) 
        {
            DB adapter = Torque.getDB(Torque.getDefaultDB());
            if (minUtilDate == null) 
            {
                sb.append(column).append('<')
                    .append(adapter.getDateString(maxUtilDate));
            }
            else if (maxUtilDate == null) 
            {
                sb.append(column).append(">=")
                    .append(adapter.getDateString(minUtilDate));
            }
            else 
            {
                // make sure min id is less than max id and that the character
                // parts are equal otherwise skip the query, there are no 
                // matches
                if (minUtilDate.before(maxUtilDate))
                {
                    sb.append(column).append(">=")
                        .append(adapter.getDateString(minUtilDate));
                    sb.append(AND);
                    sb.append(column).append('<')
                        .append(adapter.getDateString(maxUtilDate));
                }
                else 
                {
                    throw ScarabException.create(L10NKeySet.ExceptionMaxdateBeforeMindate,
                            this.maxDate,
                            minUtilDate);
                }
            }
        }
    }


    /**
     * Returns a List of matching issues.  if no OptionAttributes were
     * found in the input list, criteria is unaltered.
     *
     * @param attValues a <code>List</code> value
     */
    private void addSelectedAttributes(StringBuffer fromClause,  
                                       List attValues, Set tableAliases)
        throws Exception
    {
        Map attrMap = new HashMap((int)(attValues.size()*1.25));
        for (int j=0; j<attValues.size(); j++) 
        {
            AttributeValue multiAV = (AttributeValue)attValues.get(j);
            if (multiAV instanceof OptionAttribute)
            {
                Integer index = multiAV.getAttributeId();
                List options = (List)attrMap.get(index);
                if (options == null) 
                {
                    options = new ArrayList();
                    attrMap.put(index, options);
                }
                
                //pull any chained values out to create a flat list
                List chainedValues = multiAV.getValueList();
                for (int i=0; i<chainedValues.size(); i++) 
                {
                    AttributeValue aval = (AttributeValue)chainedValues.get(i);
                    buildOptionList(options, aval);
                }
            }
        }

        for (Iterator i=attrMap.entrySet().iterator(); i.hasNext();) 
        {
            Map.Entry entry = (Map.Entry)i.next();
            String alias = "av" + entry.getKey();
            List options = (List)entry.getValue();
            String c2 = null;
            if (options.size() == 1) 
            {
                c2 = alias + '.' + AV_OPTION_ID + '=' 
                    + options.get(0);
            }
            else
            { 
                c2 = alias + '.' + AV_OPTION_ID + " IN ("
                    + StringUtils.join(options.iterator(), ",") + ')';
            }
            joinCounter++;
            String joinClause = INNER_JOIN + AttributeValuePeer.TABLE_NAME
                + ' ' + alias + " ON (" + 
                alias + '.' + AV_ISSUE_ID + '=' + IssuePeer.ISSUE_ID + 
                AND + c2 + AND + 
                alias + '.' + "DELETED=0" + ')';
            // might want to add redundant av2.ISSUE_ID=av5.ISSUE_ID. might
            // not be necessary with sql92 join format?
            fromClause.append(joinClause);
            tableAliases.add(alias);
        }
    }


    /**
     * <p>This method builds a Criterion for a single attribute value.
     * It is used in the addOptionAttributes method.</p>
     * <p>The attribute value is basically the attribute name/id
     * + its value. Since some option (picklist) attributes are
     * hierachical, we need to add any child values of the given
     * attribute value. For example, assume we have an attribute named
     * "Operating System". This might have values in a hierarchy like
     * so:</p>
     * <pre>
     *   All
     *     Windows
     *       NT
     *       2000
     *       XP
     *     Unix
     *       Linux
     *       Solaris
     *       Tru64
     * </pre>
     * <p>If the user selects the "Windows" value in a query, we want
     * to include any issues that have "Windows" as this attribute's
     * value, and also "NT", "2000", and "XP".</p>
     * <p>All the appropriate attribute values are added to the 'options'
     * list as RModuleOption objects.</p>
     *
     * @param aval an <code>AttributeValue</code> value
     * @return a <code>Criteria.Criterion</code> value
     */
    private void buildOptionList(List options, AttributeValue aval)
        throws Exception
    {
        List descendants = null;
        // it would be a more correct query to separate the descendant
        // options by module and do something like
        // ... (module_id=1 and option_id in (1,2,3)) OR (module_id=5...
        // but we are not checking which options are active here so i
        // don't think the complexity of the query is needed.  might want
        // to revisit, especially the part about ignoring active setting.
        if (isXMITSearch()) 
        {
            descendants = 
                mitList.getDescendantsUnion(aval.getAttributeOption());
        }
        else 
        {
            IssueType issueType = getIssueType();
            
            //
            // This call checks whether the attribute value is available
            // to the current module. If not, then no attribute options
            // are added to the list.
            //
            RModuleOption rmo = getModule()
                .getRModuleOption(aval.getAttributeOption(), issueType);
            if (rmo != null) 
            {
                descendants = rmo.getDescendants(issueType);
            }
        }
        
        //
        // Include the selected attribute value as one of the options
        // to search for.
        //
        options.add(aval.getOptionId());
        
        if (descendants != null && !descendants.isEmpty())
        {
            //
            // Add all applicable child attribute options to the list as well.
            //
            for (Iterator i = descendants.iterator(); i.hasNext();) 
            {
                options.add(((RModuleOption)i.next())
                    .getOptionId());
            }
        }
    }

    private void addUserAndCreatedDateCriteria(StringBuffer from, 
                                               StringBuffer where)
        throws Exception
    {
        String dateRangeSql = null;
        if (getMinDate() != null || getMaxDate() != null) 
        {
            StringBuffer sbdate = new StringBuffer();
            Date minUtilDate = parseDate(getMinDate(), false);
            Date maxUtilDate = parseDate(getMaxDate(), true);
            addDateRange(ACTIVITYSETALIAS + '.' + CREATED_DATE, 
                         minUtilDate, maxUtilDate, sbdate);
            dateRangeSql = sbdate.toString(); 
        }                
        
        if (userIdList == null || userIdList.isEmpty())
        {
            if (dateRangeSql != null) 
            {
                joinCounter++;
                // just dates
                from.append(INNER_JOIN).append(ActivitySetPeer.TABLE_NAME) 
                    .append(' ').append(ACTIVITYSETALIAS).append(ON).append(
                    ISSUEPEER_TRAN_ID__EQUALS__ACTIVITYSETALIAS_TRAN_ID)
                    .append(AND).append(dateRangeSql)
                    .append(')');
            }
        }
        else
        {
            List anyUsers = null;
            List creatorUsers = null;
            Map attrUsers = null;

            int maxUsers = userIdList.size();
            // separate users by attribute, Created_by, and Any
            for (int i =0; i<maxUsers; i++)
            {
                String userId = (String)userIdList.get(i);
                String attrId = (String)userSearchCriteriaList.get(i);
                if (attrId == null || ANY_KEY.equals(attrId)) 
                {
                    if (anyUsers == null) 
                    {
                        anyUsers = new ArrayList(maxUsers);
                    }
                    anyUsers.add(userId);
                }               
                else if (CREATED_BY_KEY.equals(attrId)) 
                {
                    if (creatorUsers == null) 
                    {
                        creatorUsers = new ArrayList(maxUsers);
                    }
                    creatorUsers.add(userId);
                }
                else 
                {
                    // using a map here seems like overkill, but it
                    // makes the logic easier
                    if (attrUsers == null) 
                    {
                        attrUsers = new HashMap(maxUsers);
                    }
                    List userIds = (List)attrUsers.get(attrId);
                    if (userIds == null) 
                    {
                        userIds = new ArrayList(maxUsers);
                        attrUsers.put(attrId, userIds);
                    }
                    userIds.add(userId);
                }
            }

            // All users are compared using OR, so use a single alias
            // for activities related to users.
            joinCounter++;
            StringBuffer fromClause = new StringBuffer(100);
            fromClause.append(INNER_JOIN).append(ActivityPeer.TABLE_NAME)
                .append(' ').append(ACTIVITYALIAS).append(ON)
                .append(ACTIVITYALIAS_ISSUE_ID__EQUALS__ISSUEPEER_ISSUE_ID);

            StringBuffer attrCrit = null;
            if (anyUsers != null) 
            {
                attrCrit = new StringBuffer(50);
                attrCrit.append('(');
                addUserActivityFragment(attrCrit, anyUsers);
                attrCrit.append(')');
            }
            
            // Add sql fragment for each attribute.  The sql is similar
            // to the one used for Any users with the addition of attribute 
            // criteria
            if (attrUsers != null) 
            {
                for (Iterator i = attrUsers.entrySet().iterator(); i.hasNext();)
                {
                    if (attrCrit == null) 
                    {
                        attrCrit = new StringBuffer();
                    }
                    else 
                    {
                        attrCrit.append(OR);
                    }
                
                    Map.Entry entry = (Map.Entry)i.next();
                    String attrId = (String)entry.getKey();
                    List userIds = (List)entry.getValue();
                    attrCrit.append('(');
                    addUserActivityFragment(attrCrit, userIds);
                    attrCrit.append(AND +
                        ACTIVITYALIAS + '.' + ATTRIBUTE_ID + '=' + attrId);
                    attrCrit.append(')');
                }
            }

            boolean isAddActivitySet = anyUsers != null || creatorUsers != null
                || dateRangeSql != null;
            String whereClause = null;
            if (isAddActivitySet)
            {
                if (attrCrit != null) 
                {
                    whereClause = '(' + attrCrit.toString() + ')';
                }

                joinCounter++;
                fromClause.append(')').append(INNER_JOIN)
                    .append(ActivitySetPeer.TABLE_NAME) 
                    .append(' ').append(ACTIVITYSETALIAS).append(ON).append(
                    ISSUEPEER_TRAN_ID__EQUALS__ACTIVITYSETALIAS_TRAN_ID);

                if (anyUsers != null || creatorUsers != null)
                {
                    List anyAndCreators = new ArrayList(maxUsers);
                    if (anyUsers != null) 
                    {
                        anyAndCreators.addAll(anyUsers);
                    }
                    if (creatorUsers != null) 
                    {
                        anyAndCreators.addAll(creatorUsers);
                    }

                    // we can add this to the join condition, if created-only
                    // query otherwise it needs to go in the where clause
                    String createdBySqlFragment =  
                        ACTIVITYSETALIAS + '.' + CREATED_BY;
                    if (anyAndCreators.size() == 1) 
                    {
                        createdBySqlFragment += 
                            '=' + anyAndCreators.get(0).toString();
                    }
                    else 
                    {
                        createdBySqlFragment += IN + 
                            StringUtils.join(anyAndCreators.iterator(), ",") 
                            + ')';
                    }
                
                    if (anyUsers != null || attrUsers != null) 
                    {
                        fromClause.append(')'); 
                        whereClause = '(' + whereClause + OR + 
                            createdBySqlFragment + ')';
                        if (dateRangeSql != null) 
                        {
                            whereClause += AND + dateRangeSql;
                        }
                    }
                    else 
                    {
                        fromClause.append(AND).append(createdBySqlFragment);
                        if (dateRangeSql != null) 
                        {
                            fromClause.append(AND).append(dateRangeSql);
                        }
                        fromClause.append(')'); 
                    }
                }
                else // dateRangeSql will not be null
                {
                    fromClause.append(AND).append(dateRangeSql).append(')'); 
                }                
            }
            else 
            {
                // we only had single-attribute users and no date criteria.
                // attrCrit will not be null, because we had to have at
                // least one user or we'd not be here
                fromClause.append(AND).append('(').append(attrCrit)
                    .append("))");
            }

            from.append(fromClause.toString());
            if (whereClause != null) 
            {
                where.append(AND).append(whereClause);
            }
        }
    }

    private void addUserActivityFragment(StringBuffer sb, List userIds)
    {
        sb.append(ACTIVITYALIAS + '.' + END_DATE + 
                  IS_NULL + AND + ACTIVITYALIAS_NEW_USER_ID);
        if (userIds.size() == 1) 
        {
            sb.append('=').append(userIds.get(0));
        }
        else 
        {
            sb.append(IN + 
                       StringUtils.join(userIds.iterator(), ",") + ')');
        }
    }


    private Long[] getTextMatches(List attValues)
        throws Exception
    {
        boolean searchCriteriaExists = false;
        Long[] matchingIssueIds = null;
        SearchIndex searchIndex = SearchFactory.getInstance();
        if (searchIndex == null)
        {
            // Check your configuration.
            throw new Exception("No index available to search"); //EXCEPTION
        }
        if (getSearchWords() != null && getSearchWords().length() != 0)
        {
            searchIndex.addQuery(getTextScope(), getSearchWords());
            searchCriteriaExists = true;
        }
        else 
        {
            for (int i=0; i<attValues.size(); i++) 
            {
                AttributeValue aval = (AttributeValue)attValues.get(i);
                if (aval instanceof StringAttribute 
                     && aval.getValue() != null 
                     && aval.getValue().length() != 0)
                {
                    searchCriteriaExists = true;
                    Integer[] id = {aval.getAttributeId()};
                    searchIndex
                        .addQuery(id, aval.getValue());
                }
            }
        }

        // add comment attachments
        String commentQuery = getCommentQuery();
        if (commentQuery != null && commentQuery.trim().length() > 0) 
        {
            Integer[] id = {AttachmentTypePeer.COMMENT_PK};
            searchIndex.addAttachmentQuery(id, commentQuery);            
            searchCriteriaExists = true;
        }

        if (searchCriteriaExists) 
        {
            matchingIssueIds = searchIndex.getRelatedIssues();    
        }

        return matchingIssueIds;
    }

    private void addStateChangeQuery(StringBuffer from)
        throws Exception
    {
        Integer oldOptionId = getStateChangeFromOptionId();
        Integer newOptionId = getStateChangeToOptionId();
        Date minUtilDate = parseDate(getStateChangeFromDate(), false);
        Date maxUtilDate = parseDate(getStateChangeToDate(), true);
        if ((oldOptionId != null &&  !oldOptionId.equals(NUMBERKEY_0))
            || (newOptionId != null && !newOptionId.equals(NUMBERKEY_0))
            || minUtilDate != null || maxUtilDate != null)
        {
            joinCounter++;
            from.append(INNER_JOIN + ActivityPeer.TABLE_NAME + ON +
                        ActivityPeer.ISSUE_ID + '=' + IssuePeer.ISSUE_ID);

            if (oldOptionId == null && newOptionId == null)
            {
                from.append(AND).append(ActivityPeer.ATTRIBUTE_ID)
                    .append('=').append(getStateChangeAttributeId());
            }
            else
            {
                if (newOptionId != null && !newOptionId.equals(NUMBERKEY_0)) 
                {
                    from.append(AND).append(ActivityPeer.NEW_OPTION_ID)
                        .append('=').append(newOptionId);
                }
                if (oldOptionId != null && !oldOptionId.equals(NUMBERKEY_0))
                {
                    from.append(AND).append(ActivityPeer.OLD_OPTION_ID)
                        .append('=').append(oldOptionId);
                }
            }
            from.append(')');

            // add dates, if given
            if (minUtilDate != null || maxUtilDate != null) 
            {
                joinCounter++;
                from.append(INNER_JOIN + ActivitySetPeer.TABLE_NAME + ON +
                             ActivitySetPeer.TRANSACTION_ID + '=' +
                             ActivityPeer.TRANSACTION_ID);
                from.append(AND);

                addDateRange(ActivitySetPeer.CREATED_DATE, 
                             minUtilDate, maxUtilDate, from);
                
                from.append(')');
            }
        }
    }

    private Long[] addCoreSearchCriteria(StringBuffer fromClause, 
                                              StringBuffer whereClause,
                                              Set tableAliases)
        throws Exception
    {
        if (isXMITSearch()) 
        {
            Criteria crit = new Criteria();
            mitList.addToCriteria(crit);
            String sql = crit.toString();
            int wherePos = sql.indexOf(" WHERE ");
            whereClause.append(sql.substring(wherePos + 7));
        }
        else 
        {
            whereClause.append(IssuePeer.MODULE_ID).append('=')
                .append(getModule().getModuleId());
            whereClause.append(AND).append(IssuePeer.TYPE_ID).append('=')
                .append(getIssueType().getIssueTypeId());
        }
        whereClause.append(AND).append(IssuePeer.DELETED).append("=0");

        // add option values
        lastUsedAVList = new ArrayList(getAttributeValues());

        // remove unset AttributeValues before searching
        List setAttValues = removeUnsetValues(lastUsedAVList);        
        addSelectedAttributes(fromClause, setAttValues, tableAliases);

        // search for issues based on text
        Long[] matchingIssueIds = getTextMatches(setAttValues);

        if (matchingIssueIds == null || matchingIssueIds.length > 0)
        {
            addIssueIdRange(whereClause);
            //addMinimumVotes(whereClause);

            // add user values
            addUserAndCreatedDateCriteria(fromClause, whereClause);

            // add text search matches
            addIssuePKsCriteria(whereClause, matchingIssueIds);

            // state change query
            addStateChangeQuery(fromClause);
        }
        return matchingIssueIds;
    }

    private void addIssuePKsCriteria(StringBuffer sb, Long[] ids)
    {
       if (ids != null && ids.length > 0)
       {
           sb.append(AND).append(IssuePeer.ISSUE_ID).append(IN)
               .append(StringUtils.join(ids, ",")).append(')');
       }     
    }

    /**
     * Get a List of Issues that match the criteria given by this
     * SearchIssue's searchWords and the quick search attribute values.
     *
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public IteratorWithSize getQueryResults()
        throws ComplexQueryException, Exception
    {
        checkModified();
        if (!isSearchAllowed) 
        {
            lastQueryResults = IteratorWithSize.EMPTY;            
        }
        else if (lastQueryResults == null) 
        {
            Set tableAliases = new HashSet();
            StringBuffer from = new StringBuffer();
            StringBuffer where = new StringBuffer();
            joinCounter = 0;
            Long[] matchingIssueIds = addCoreSearchCriteria(from, where,
                                                                 tableAliases);
            if (joinCounter > MAX_INNER_JOIN) 
            {
                throw new ComplexQueryException(L10NKeySet.ExceptionQueryTooComplex);
            }
            // the matchingIssueIds are text search matches.  if length == 0,
            // then no need to search further.  if null then there was no
            // text to search, so continue the search process.
            if (matchingIssueIds == null || matchingIssueIds.length > 0) 
            {            
                lastQueryResults = getQueryResults(from, where, tableAliases);
            }
            else 
            {
                lastQueryResults = IteratorWithSize.EMPTY;
            }            
        }
        
        return lastQueryResults;
    }


    public int getIssueCount()
        throws ComplexQueryException, Exception
    {
        checkModified();
        int count = 0;
        if (isSearchAllowed) 
        {
            if (lastTotalIssueCount >= 0) 
            {
                count = lastTotalIssueCount;
            }
            else 
            {
                count = countFromDB();
            }
            lastTotalIssueCount = count;
        }

        return count;
    }

    private int countFromDB()
        throws ComplexQueryException, Exception
    {
        int count = 0;
        StringBuffer from = new StringBuffer();
        StringBuffer where = new StringBuffer();
        joinCounter = 0;
        Long[] matchingIssueIds = addCoreSearchCriteria(from, where,
                                                        new HashSet());
        if (joinCounter > MAX_INNER_JOIN) 
        {
            throw new ComplexQueryException(L10NKeySet.ExceptionQueryTooComplex);
        }
        
        if (matchingIssueIds == null || matchingIssueIds.length > 0) 
        {
            StringBuffer sql = new StringBuffer("SELECT count(DISTINCT ");
            sql.append(IssuePeer.ISSUE_ID).append(')').append(" FROM ")
                .append(IssuePeer.TABLE_NAME);
            if (from.length() > 0) 
            {
                sql.append(' ').append(from);
            }
            if (where.length() > 0) 
            {
                sql.append(WHERE).append(where);
            }
            String countSql = sql.toString();
            
            Connection localCon = conn;
            Statement stmt = null;
            try
            {
                if (localCon == null) 
                {
                    localCon = Torque.getConnection();
                }
                long startTime = System.currentTimeMillis();
                stmt = localCon.createStatement();
                ResultSet resultSet = stmt.executeQuery(countSql);
                if (resultSet.next()) 
                {
                    count = resultSet.getInt(1);
                }
                logTime(countSql, System.currentTimeMillis() - startTime, 
                        50L, 500L);
            }
            finally
            {
                if (stmt != null) 
                {
                    stmt.close();
                }                
                if (conn == null && localCon != null) 
                {
                    localCon.close();
                }
            }
        }
        return count;
    }

    private static final String LOGGER = "org.apache.torque";
    private void logTime(String message, long time, 
                         long infoLimit, long warnLimit)
    {
        if (time > warnLimit) 
        {
            Log.get(LOGGER).warn(message + "\nTime = " + time + " ms");
        }
        else if (time > infoLimit) 
        {
            Logger log = Log.get(LOGGER);
            if (log.isInfoEnabled()) 
            {
                log.info(message + "\nTime = " + time + " ms");                
            }
        }
        else
        {
            Logger log = Log.get(LOGGER);
            if (log.isDebugEnabled()) 
            {
                log.info(message + "\nTime = " + time + " ms");
            }
        }        
    }

    private String setupSortColumn(Integer sortAttrId, 
                                   StringBuffer sortOuterJoin,
                                   Set tableAliases)
        throws TorqueException
    {
        String alias = AV + sortAttrId;
        if (!tableAliases.contains(alias)) 
        {
            sortOuterJoin.append(LEFT_OUTER_JOIN)
                .append(AttributeValuePeer.TABLE_NAME).append(' ')
                .append(alias).append(ON)
                .append(IssuePeer.ISSUE_ID).append('=')
                .append(alias).append(".ISSUE_ID AND ").append(alias)
                .append(".DELETED=0 AND ").append(alias)
                .append(".ATTRIBUTE_ID=").append(sortAttrId).append(')');
        }        
            String sortColumn;
            Attribute att = AttributeManager.getInstance(sortAttrId); 
            if (att.isOptionAttribute())
            {
                // add the sort column
                sortColumn = SORTRMO_PREFERRED_ORDER;
                // join the RMO table to the alias we are sorting
                sortOuterJoin.append(BASE_OPTION_SORT_LEFT_JOIN).append(alias)
                    .append(DOT_OPTION_ID_PAREN);
            }
            else 
            {
                sortColumn = alias + DOT_VALUE;
            }
            return sortColumn;
    }

    private List getSearchSqlPieces(StringBuffer from, StringBuffer where,
                                    Set tableAliases)
        throws TorqueException
    {
        List searchStuff = new ArrayList(3);
        Integer sortAttrId = getSortAttributeId();

        // Get matching issues, with sort criteria
        StringBuffer sql = getSelectStart();
        sql.append(',').append(IssuePeer.MODULE_ID)
            .append(',').append(IssuePeer.TYPE_ID);
        String sortColumn = null;
        StringBuffer sortOuterJoin = null;
        if (sortAttrId != null) 
        {
            sortOuterJoin = new StringBuffer(128);
            sortColumn = setupSortColumn(sortAttrId, sortOuterJoin,
                                         tableAliases);
            sql.append(',').append(sortColumn);
        }

        sql.append(FROM).append(IssuePeer.TABLE_NAME);
        if (from.length() > 0) 
        {
            sql.append(' ').append(from.toString());
        }
        if (sortOuterJoin != null) 
        {
            sql.append(sortOuterJoin);    
        }
        if (where.length() > 0) 
        {
            sql.append(WHERE).append(where);
        }
        addOrderByClause(sql, sortColumn);
        searchStuff.add(sql.toString());

        // add the attribute value columns that will be shown in the list.
        // these are joined using a left outer join, so the additional
        // columns do not affect the results of the search (no additional
        // criteria are added to the where clause.)          
        List rmuas = getIssueListAttributeColumns();
        if (rmuas != null)
        {
            int valueListSize = rmuas.size();
            StringBuffer outerJoin = new StringBuffer(10 * valueListSize + 20);

            int count = 0;
            int maxJoin = MAX_JOIN - 2;
            //List columnSqlList = new ArrayList(valueListSize/maxJoin + 1);
            StringBuffer partialSql = getSelectStart();
            tableAliases = new HashSet(MAX_JOIN);
            for (Iterator i = rmuas.iterator(); i.hasNext();) 
            {
                RModuleUserAttribute rmua = (RModuleUserAttribute)i.next();
                Integer attrPK = rmua.getAttributeId();
          
                
                String id = attrPK.toString();
                String alias = AV + id;
                // add column to SELECT column clause
                partialSql.append(',').append(alias).append(DOT_VALUE);
                // if no criteria was specified for a displayed attribute
                // add it as an outer join
                if (!tableAliases.contains(alias) 
                    && !attrPK.equals(sortAttrId))
                {
                    outerJoin.append(LEFT_OUTER_JOIN)
                        .append(AttributeValuePeer.TABLE_NAME).append(' ')
                        .append(alias).append(ON)
                        .append(IssuePeer.ISSUE_ID).append('=')
                        .append(alias).append(".ISSUE_ID AND ").append(alias)
                        .append(".DELETED=0 AND ").append(alias)
                        .append(".ATTRIBUTE_ID=").append(id).append(')');
                    tableAliases.add(alias);
                }

                count++;
                if (count == maxJoin || !i.hasNext()) 
                {
                    ColumnBundle cb = new ColumnBundle();
                    cb.size = count;
                    if (sortAttrId != null) 
                    {
                        cb.sortColumn = setupSortColumn(sortAttrId, outerJoin,
                                                        tableAliases);
                        partialSql.append(',').append(
                            cb.sortColumn);
                    }
                    cb.select = partialSql;
                    cb.outerJoins = outerJoin;
                    searchStuff.add(cb);

                    partialSql = getSelectStart();
                    outerJoin = new StringBuffer(512);
                    tableAliases.clear();
                    count = 0;
                }
            }
        }
        return searchStuff;
    }

    private IteratorWithSize getQueryResults(StringBuffer from, 
                                             StringBuffer where,
                                             Set tableAliases)
        throws TorqueException, ComplexQueryException, Exception
    {
        // return a List of QueryResult objects
        IteratorWithSize result = null;
        try
        {
            if (conn == null)
            {
                conn = Torque.getConnection();
                connectionStartTime = System.currentTimeMillis();
            }

            // The code currently always calls getIssueCount() after we run this
            // query.  We can avoid having to use 2 connections by calling it
            // now using 'conn'.  It leaves the possibility of running the two
            // queries within a transaction as well.  We also use the result 
            // here to avoid the more complex query if there are no results.
            int count = getIssueCount();
            if (count > 0) 
            {
                result = new QueryResultIterator(this, count, from, where,
                                                 tableAliases);
            }
            else 
            {
                result = IteratorWithSize.EMPTY;
            }
        }
        catch (SQLException e)
        {
            close();
            throw e; //EXCEPTION
        }
        /*
        catch (TorqueException e)
        {
            close();
            throw e;
        }
        */
        return result;
    }

    private void addOrderByClause(StringBuffer sql, String sortColumn)
    {
        if (sortColumn == null) 
        {
            sql.append(ORDER_BY).append(IssuePeer.ID_PREFIX);
            sql.append(' ').append(getSortPolarity());
            sql.append(',').append(IssuePeer.ID_COUNT);
            sql.append(' ').append(getSortPolarity());
        }
        else 
        {
            sql.append(ORDER_BY).append(sortColumn);
            sql.append(' ').append(getSortPolarity());
            // add pk sort so that rows can be combined easily
            sql.append(',').append(IssuePeer.ISSUE_ID).append(" ASC");
        }
    }

    private StringBuffer getSelectStart()
    {
        StringBuffer sql = new StringBuffer(512);
        sql.append(SELECT_DISTINCT)
            .append(IssuePeer.ISSUE_ID).append(',')
            .append(IssuePeer.ID_PREFIX).append(',')
            .append(IssuePeer.ID_COUNT);
        return sql;
    }


    /**
     * Used by QueryResult to avoid multiple db hits in the event caching
     * is not being used application-wide.  It is used if the IssueList.vm
     * template is printing the module names next to each issue id.
     * As this IssueSearch object is short-lived, use of a simple Map based
     * cache is ok, need to re-examine if the lifespan is increased.
     *
     * @param id an <code>Integer</code> value
     * @return a <code>Module</code> value
     * @exception TorqueException if an error occurs
     */
    Module getModule(Integer id)
        throws TorqueException
    {
        Module module = (Module)moduleMap.get(id);
        if (module == null)
        {
            module = ModuleManager.getInstance(id);
            moduleMap.put(id, module);
        }
        return module;
    }
    
    /**
     * Used by QueryResult to avoid multiple db hits in the event caching
     * is not being used application-wide.  It is used if the IssueList.vm
     * template is printing the issue type names next to each issue id.
     * As this IssueSearch object is short-lived, use of a simple Map based
     * cache is ok, need to re-examine if the lifespan is increased.
     *
     * @param moduleId an <code>Integer</code> value
     * @param issueTypeId an <code>Integer</code> value
     * @return a <code>RModuleIssueType</code> value
     * @exception TorqueException if an error occurs
     */
    RModuleIssueType getRModuleIssueType(Integer moduleId, Integer issueTypeId)
        throws TorqueException
    {
        SimpleKey[] nks = {SimpleKey.keyFor(moduleId.intValue()), 
                           SimpleKey.keyFor(issueTypeId.intValue())};
        ObjectKey key = new ComboKey(nks);
        RModuleIssueType rmit = (RModuleIssueType)rmitMap.get(key);
        if (rmit == null)
        {
            rmit = RModuleIssueTypeManager.getInstance(key);
            rmitMap.put(key, rmit);
        }
        return rmit;
    }

    /**
     * Called by the garbage collector to release any database
     * resources associated with this query.
     *
     * @see #close()
     */
    protected void finalize()
        throws Throwable
    {
        try
        {
            if (conn != null) 
            {
                Log.get(LOGGER)
                    .warn("Closing connection in " + this + " finalizer");
                // if this object was left this state it is very likely that
                // the IssueSearchFactory was not notified either.
                // We error on the side of possibly increasing the available
                // IssueSearch objects, over potentially freezing users out
                IssueSearchFactory.INSTANCE.notifyDone();
            }
        }
        finally
        {
            close();
            super.finalize();
        }
    }

    /**
     * Releases any managed resources associated with this search
     * (e.g. database connections, etc.).
     */
    public void close()
    {
        if (conn != null)
        {
            // Be extremely paranoid about assuring that the database
            // connection is released to avoid leaks.
            try
            {
                Logger log = Log.get(LOGGER);
                if (log.isDebugEnabled())
                {
                    log.debug("Releasing issue search database connection");
                }
            }
            finally
            {
                try 
                {
                    if (searchRS != null) 
                    {
                        searchRS.close();
                        searchRS = null;
                    }
                }
                catch (Exception e)
                {
                    try
                    {
                        Log.get(LOGGER).warn(
                            "Unable to close jdbc Statement", e);
                    }
                    catch (Exception ignore)
                    {
                    }
                }
                try 
                {
                    if (searchStmt != null) 
                    {
                        searchStmt.close();
                        searchStmt = null;
                    }
                }
                catch (Exception e)
                {
                    try
                    {
                        Log.get(LOGGER).warn(
                            "Unable to close jdbc Statement", e);
                    }
                    catch (Exception ignore)
                    {
                    }
                }

                try 
                {
                    closeStatementsAndResultSets();
                    stmtList = null;
                    rsList = null;
                }
                catch (Exception e)
                {
                    try
                    {
                        Log.get(LOGGER).warn(
                            "Unable to close jdbc Statement", e);
                    }
                    catch (Exception ignore)
                    {
                    }
                }
                
                Torque.closeConnection(this.conn);
                this.conn = null;
                logTime(this + 
                        " released database connection which was held for:",
                        System.currentTimeMillis() - connectionStartTime, 
                        5000L, 30000L);
            }
        }
    }

    
    private void closeStatementsAndResultSets()
        throws SQLException
    {
        if (rsList != null) 
        {
            for (Iterator iter = rsList.iterator(); iter.hasNext();) 
            {
                ((ResultSetAndSize)iter.next()).resultSet.close();
            }
            rsList.clear();            
        }
        
        if (stmtList != null) 
        {
            for (Iterator iter = stmtList.iterator(); iter.hasNext();) 
            {
                ((Statement)iter.next()).close();
            }
            stmtList.clear();            
        }
    }        

    private class QueryResultIterator implements IteratorWithSize
    {
        final IssueSearch search;
        final List searchStuff;
        final int size;

        QueryResult[]  cachedQRs;

        /**
         * @param issues The issue query results.
         */
        private QueryResultIterator(IssueSearch search, int size, 
                                    StringBuffer from, StringBuffer where,
                                    Set tableAliases)
            throws SQLException, TorqueException
        {
            this.search = search;
            this.size = size;
            searchStuff = getSearchSqlPieces(from, where, tableAliases);

            int numQueries = searchStuff.size();
            stmtList = new ArrayList(numQueries);
            rsList = new ArrayList(numQueries);
            
            long queryStartTime = System.currentTimeMillis();
            searchStmt = conn.createStatement();
            String searchSql = (String)searchStuff.get(0);
            try 
            {
                searchRS = searchStmt.executeQuery(searchSql);                
                logTime(searchSql + 
                    "\nTime to only execute the query, not return results.",
                    System.currentTimeMillis() - queryStartTime, 
                    50L, 500L);
            }
            catch (SQLException e)
            {
                Log.get(LOGGER).warn("Search sql:\n" + searchSql + 
                    "\nresulted in an exception: " + e.getMessage());
                throw e; //EXCEPTION
                
            }
        }

        public int size()
        {
            return size;
        }

        // ----------------------------------------------------------------
        // Iterator implementation

        private QueryResult nextQueryResult;
        public Object next()
        {
            if (hasNext()) 
            {
                hasNext = null;
                return nextQueryResult;
            }
            else 
            {
                throw new NoSuchElementException("Iterator is exhausted"); //EXCEPTION
            }
        }


        private Boolean hasNext;
        public boolean hasNext()
        {
            if (hasNext == null) 
            {
                hasNext = (prepareNextQueryResult()) 
                    ? Boolean.TRUE : Boolean.FALSE;
            }
            return hasNext.booleanValue();
        }


        public void remove()
        {
            throw new UnsupportedOperationException(
                "'remove' is not implemented"); //EXCEPTION
        }


        int index = -1;
        /**
         * nextQueryResult should be non-null at the end of this method
         * if it returns true, otherwise false should be returned.
         *
         * @return a <code>boolean</code> value
         */
        private boolean prepareNextQueryResult()
        {
            boolean anyMoreResults;
            try 
            {
                anyMoreResults = doPrepareNextQueryResult();
            }
            catch (Exception e)
            {
                anyMoreResults = false;
                Log.get(LOGGER).warn(
                    "An exception prevented getting the next result.", e);
            }
            return anyMoreResults;
        }            

        private boolean doPrepareNextQueryResult()
            throws SQLException
        {
            boolean anyMoreResults = true;
            
            if (index < 0 || index >= 1000)
            {
                if (cachedQRs == null) 
                {
                    cachedQRs = new QueryResult[1000];
                }
                else 
                {
                    // remove the old
                    for (int i = cachedQRs.length - 1; i >= 0; i--) 
                    {
                        cachedQRs[i] = null;
                    }
                    closeStatementsAndResultSets();
                }
                
                int count = 0;
                QueryResult qr;
                String previousPK = null;
                StringBuffer pks = new StringBuffer(512);
                while (count < 1000 && searchRS.next()) 
                {
                    String pk = searchRS.getString(1);
                    if (!pk.equals(previousPK)) 
                    {
                        previousPK = pk;
                        pks.append(pk).append(',');
                        qr = new QueryResult(search);
                        qr.setIssueId(pk);
                        qr.setIdPrefix(searchRS.getString(2));
                        qr.setIdCount(searchRS.getString(3));
                        qr.setModuleId(new Integer(searchRS.getInt(4)));
                        qr.setIssueTypeId(new Integer(searchRS.getInt(5)));
                        cachedQRs[count++] = qr;
                    }                    
                }                

                anyMoreResults = count > 0;
                if (anyMoreResults) 
                {
                    index = 0;
                    pks.setLength(pks.length() - 1);

                    // execute column result queries
                    if (searchStuff.size() > 1) 
                    {
                        Iterator i = searchStuff.iterator();
                        i.next();
                        while (i.hasNext()) 
                        {
                            ColumnBundle cb = (ColumnBundle)i.next();
                            StringBuffer sql = new StringBuffer(512);
                            sql.append(cb.select);

                            sql.append(FROM).append(IssuePeer.TABLE_NAME);
                            if (cb.outerJoins != null) 
                            {
                                sql.append(cb.outerJoins);    
                            }
                            sql.append(WHERE).append(IssuePeer.ISSUE_ID)
                                .append(IN).append(pks).append(')');
                            addOrderByClause(sql, cb.sortColumn);
                            Statement stmt = conn.createStatement();
                            ResultSet rs = stmt.executeQuery(sql.toString());
                            rs.next();
                            stmtList.add(stmt);
                            rsList.add(new ResultSetAndSize(rs, cb.size));
                        }
                    }
                }
            }
            else if (cachedQRs[index] == null)
            {
                anyMoreResults = false;                
            }

            if (anyMoreResults) 
            {
                // remove old results to allow gc, if needed
                if (index > 0) 
                {
                    cachedQRs[index-1] = null;
                }
                nextQueryResult = cachedQRs[index++];
                buildQueryResult(nextQueryResult);            
            }

            return anyMoreResults;
        }


        /**
         * Assembles one or more rows from a <code>ResultSet</code> into a
         * single {@link QueryResult} object.  Assumes that rows in the
         * <code>ResultSet</code> are grouped by issue.
         *
         * @return A single {@link QueryResult} object.
         * @exception SQLException If a database error occurs.
         */
        private void buildQueryResult(QueryResult qr)
            throws SQLException
        {
            String queryResultPK = qr.getIssueId();
            Logger scarabLog = Log.get("org.tigris.scarab");

            // add column values
            int index = 0;
            for (Iterator iter = rsList.iterator(); iter.hasNext();) 
            {
                ResultSetAndSize rss = (ResultSetAndSize)iter.next();
                ResultSet resultSet = rss.resultSet;
                int size = rss.size;
                String pk = resultSet.getString(1);
                while (queryResultPK.equals(pk))
                {
                    List values = qr.getAttributeValues();
                    // Each attribute can result in a separate record.  As we
                    // have sorted on the primary key column in addition to
                    // any other sort, all attributes for a given issue will
                    // be grouped.  Map these multiple records into a single
                    // QueryResult per issue.
                    if (values == null || index >= values.size()) 
                    {
                        queryResultStarted(resultSet, qr, size);
                        if (scarabLog.isDebugEnabled())
                        {
                            scarabLog.debug("Fetching query result at index "
                                            + index + " with ID of "
                                            + queryResultPK);
                        }
                    }
                    else 
                    {
                        queryResultContinued(resultSet, qr, index, size);
                    }
                    
                    pk = (resultSet.next()) ? resultSet.getString(1): null;
                }
                index += size;
            }
        }

        private void queryResultStarted(ResultSet rs, QueryResult qr, 
                                        int valueListSize)
            throws SQLException
        {
            if (valueListSize > 0) 
            {
                // Some attributes can be multivalued.
                List values = new ArrayList(valueListSize);
                for (int j = 0; j < valueListSize; j++) 
                {
                    ArrayList multiVal = new ArrayList(2);
                    multiVal.add(rs.getString(j + 4));
                    values.add(multiVal);
                }
                List lastValues = qr.getAttributeValues();
                if (lastValues == null) 
                {
                    qr.setAttributeValues(values);                    
                }
                else 
                {
                    lastValues.addAll(values);
                }
            }
        }

        private void queryResultContinued(ResultSet rs, QueryResult qr, 
                                          int base, int valueListSize)
            throws SQLException
        {
            if (valueListSize > 0)
            {
                List values = qr.getAttributeValues();
                for (int j = 0; j < valueListSize; j++)
                {
                    String s = rs.getString(j + 4);

                    // As it's possible that multiple rows
                    // could have the same value for a given
                    // attribute, and we don't want to add the
                    // same value many times, check for this
                    // below.  See the code in the "else if"
                    // block about 10 lines down to see how
                    // the values lists are arranged to allow
                    // for multiple values.
                    List prevValues = (List) values.get(j + base);
                    boolean newValue = true;
                    for (int k = 0; k < prevValues.size(); k++)
                    {
                        if (ObjectUtils.equals(prevValues.get(k), s))
                        {
                            newValue = false;
                            break;
                        }
                    }
                    if (newValue) 
                    {
                        prevValues.add(s);
                    }
                }
            }
        }
    }

    private static class ColumnBundle
    {
        int size;
        StringBuffer select;
        StringBuffer outerJoins;
        String sortColumn;
    }

    private static class ResultSetAndSize
    {
        private ResultSet resultSet;
        private int size;
        ResultSetAndSize(ResultSet rs, int s)
        {
            resultSet = rs;
            size = s;
        }
    }
}

