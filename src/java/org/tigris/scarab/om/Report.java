package org.tigris.scarab.om;


import org.apache.torque.om.UnsecurePersistent;

// JDK classes
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Arrays;

import org.apache.commons.util.ObjectUtils;
import com.workingdogs.village.Record;

// Turbine classes
import org.apache.turbine.Log;
import org.apache.torque.om.Retrievable;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.pool.DBConnection;

import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;

import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.util.parser.StringValueParser;
import org.apache.fulcrum.util.parser.ValueParser;
import org.apache.fulcrum.intake.Intake;
import org.apache.fulcrum.intake.model.Group;

import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.user.UserManager;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.module.ModuleManager;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ActivityPeer;
import org.tigris.scarab.om.TransactionPeer;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.util.OptionModel;
import org.tigris.scarab.util.TableModel;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class Report 
    extends org.tigris.scarab.om.BaseReport
    implements UnsecurePersistent
{
    private static final String[] REPORT_TYPES = 
        {"comparative analysis (single date/time)", 
         "rate of change (multiple date/time)"};

    private static final String[] AXIS_CATEGORIES = 
        {"attributes/options", "attributes/users", "committed by/author"};

    private static List reportTypes;
    private static List axisCategories;

    private static final String ACT_ATTRIBUTE_ID;
    private static final String ACT_NEW_USER_ID;
    private static final String ACT_NEW_OPTION_ID;
    private static final String ACT_ISSUE_ID;
    private static final String ACT_TRANSACTION_ID;
    private static final String ACT_END_DATE;
    private static final String TRAN_TRANSACTION_ID;
    private static final String TRAN_CREATED_DATE;
    private static final String TRAN_CREATED_BY;
    private static final String TRAN_TYPE_ID;

    //private ModuleEntity module;
    //private String name;    
    //private String description;
    private int type;
    private ScarabUser generatedBy;
    private Date generatedDate;
    private String[] toBeGrouped;
    private List dates;
    private int axis1Category;
    private int axis2Category;
    private String[] axis1Keys;
    private String[] axis2Keys;
    private List optionGroups;

    /** used to store query key as part of Retrievable interface */ 
    private String queryKey;

    static
    {
        reportTypes = new ArrayList();
        for ( int i=0; i<REPORT_TYPES.length; i++ ) 
        {
            reportTypes.add( new OptionModel(i, REPORT_TYPES[i]) );
        }

        axisCategories = new ArrayList();
        for ( int i=0; i<AXIS_CATEGORIES.length; i++ ) 
        {
            axisCategories.add(new OptionModel(i, AXIS_CATEGORIES[i]));
        }

        // column names only
        ACT_ATTRIBUTE_ID = ActivityPeer.ATTRIBUTE_ID.substring(
            ActivityPeer.ATTRIBUTE_ID.indexOf('.')+1);
        ACT_NEW_USER_ID = ActivityPeer.NEW_USER_ID.substring(
            ActivityPeer.NEW_USER_ID.indexOf('.')+1);
        ACT_NEW_OPTION_ID = ActivityPeer.NEW_OPTION_ID.substring(
            ActivityPeer.NEW_OPTION_ID.indexOf('.')+1);
        ACT_ISSUE_ID = ActivityPeer.ISSUE_ID.substring(
            ActivityPeer.ISSUE_ID.indexOf('.')+1);
        ACT_TRANSACTION_ID = ActivityPeer.TRANSACTION_ID.substring(
            ActivityPeer.TRANSACTION_ID.indexOf('.')+1);
        ACT_END_DATE = ActivityPeer.END_DATE.substring(
            ActivityPeer.END_DATE.indexOf('.')+1);
        TRAN_TRANSACTION_ID = TransactionPeer.TRANSACTION_ID.substring(
            TransactionPeer.TRANSACTION_ID.indexOf('.')+1);
        TRAN_CREATED_DATE = TransactionPeer.CREATED_DATE.substring(
            TransactionPeer.CREATED_DATE.indexOf('.')+1);
        TRAN_CREATED_BY = TransactionPeer.CREATED_BY.substring(
            TransactionPeer.CREATED_BY.indexOf('.')+1);
        TRAN_TYPE_ID = TransactionPeer.TYPE_ID.substring(
            TransactionPeer.TYPE_ID.indexOf('.')+1);
    }

    public List getReportTypes()
    {
        return reportTypes;
    }

    public List getAxisCategories()
    {
        return axisCategories;
    }
    
    /**
     * Get the value of module.
     * @return value of module.
     */
    public ModuleEntity getModule() 
        throws Exception
    {
        return ModuleManager.getInstance(getModuleId());
    }
    
    /**
     * Set the value of module.
     * @param v  Value to assign to module.
     */
    public void setModule(ModuleEntity  v) 
        throws Exception
    {
        setModuleId(v.getModuleId());
    }
    
    /* *
     * Get the value of name.
     * @return value of name.
     * /
    public String getName() 
    {
        return name;
    }
    
    /* *
     * Set the value of name.
     * @param v  Value to assign to name.
     * /
    public void setName(String  v) 
    {
        this.name = v;
    }
    
    /* *
     * Get the value of description.
     * @return value of description.
     * /
    public String getDescription() 
    {
        return description;
    }
    
    /* *
     * Set the value of description.
     * @param v  Value to assign to description.
     * / 
    public void setDescription(String  v) 
    {
        this.description = v;
    }
    */

    
    /**
     * Get the value of type.
     * @return value of type.
     */
    public int getType() 
    {
        return type;
    }
    
    /**
     * Set the value of type.
     * @param v  Value to assign to type.
     */
    public void setType(int  v) 
    {
        this.type = v;
    }

    
    /**
     * Get the value of generatedBy.
     * @return value of generatedBy.
     */
    public ScarabUser getGeneratedBy() 
        throws Exception
    {
        if ( generatedBy == null ) 
        {
            if ( getUserId() != null ) 
            {
                generatedBy = UserManager.getInstance(getUserId());
            }
        }
        
        return generatedBy;
    }
    
    /**
     * Set the value of generatedBy.
     * @param v  Value to assign to generatedBy.
     */
    public void setGeneratedBy(ScarabUser  v)
        throws Exception
    {
        this.generatedBy = v;
        super.setUserId(v.getUserId());
    }
    
    
    /**
     * This is the date that was used in the queries for reports on a 
     * single date.  It is not necessarily the same as the date on which the
     * queries were run.
     * @return value of generatedDate.
     */
    public Date getGeneratedDate() 
    {
        if ( generatedDate == null ) 
        {
            // if we have multiple dates or if no date was set just set this 
            // date to the current time
            if ( getType() == 1 || getNewDate() == null ) 
            {
                generatedDate = new Date();
            }
            else 
            {
                generatedDate = getNewDate();
            }
        }
        
        return generatedDate;
    }
    
    
    /**
     */
    public String[] getAttributesAndOptionsForGrouping() 
    {
        return this.toBeGrouped;
    }
    
    /**
     */
    public void setAttributesAndOptionsForGrouping(String[] v) 
    {
        if ( v != null && (v.length == 0 || v[0].length() == 0) ) 
        {
            this.toBeGrouped = null;
        }
        else 
        {
            this.toBeGrouped = v;            
        }
    }

    public List getOptionGroups()
    {
        return optionGroups;
    }

    public OptionGroup getNewOptionGroup()
    {
        return new OptionGroup();
    }

    public void setOptionGroups(List groups)
    {
        this.optionGroups = groups;
    }

    public String[] getGroupNames()
    {
        String[] names = null;
        if ( optionGroups != null ) 
        {
            names = new String[optionGroups.size()];
            for ( int i=0; i<names.length; i++ ) 
            {
                names[i] = ((OptionGroup)optionGroups.get(i)).getDisplayValue();
            }
        }
        return names;
    }

    public void setGroupNames(String[] names)
    {
        if ( names == null ) 
        {
            optionGroups = null;
        }
        else 
        {
            optionGroups = new ArrayList(names.length);
            for ( int i=0; i<names.length; i++ ) 
            {
                optionGroups.add(new OptionGroup(names[i]));
            }
        }
    }

    public List getSelectedOptionsForGrouping()
        throws Exception
    {
        List options = null;
        String[] toBeGrouped = getAttributesAndOptionsForGrouping();
        if ( toBeGrouped == null ) 
        {
            options = new ArrayList(0);
        }
        else 
        {
            options = getSelectedOptions(toBeGrouped);
        }
        return options;
    }

    private String[] remove(String[] array, int index)
    {
        String[] newArray = new String[array.length-1];
        for ( int i=0; i<array.length; i++ ) 
        {
            if ( i != index ) 
            {
                newArray[i] = array[i];
            }
        }
        return newArray;
    }

    private void moveTo(String[] array, int currentPosition, int endPosition)
        throws ScarabException
    {
        if ( endPosition > currentPosition ) 
        {
            throw new ScarabException(
                "Cannot move towards a higher indexed position");
        }
        String tmp = array[currentPosition];
        for ( int i=currentPosition-1; i>=endPosition; i-- ) 
        {
            array[i+1] = array[i];
        }
        array[endPosition] = tmp;
    }

    /**
     * fill out list of RModuleOptions based on selected attributes
     * and options
     */
    private List getSelectedOptions(String[] keys)
        throws Exception
    {
        ModuleEntity module = getModule();
        List rmas = module.getRModuleAttributes(true);
        List options = new ArrayList(7*rmas.size());
        int start = 0;
        for ( int i=0; i<rmas.size() && keys.length != start; i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
            if ( rma.getAttribute().isOptionAttribute()) 
            {            
                String rmaId = getKey(rma);
                boolean isRMASelected = false;
                for ( int j=start; j<keys.length; j++ ) 
                {
                    if ( rmaId.equals(keys[j]) ) 
                    {
                        isRMASelected = true;
                        //removing the key, as it is already matched
                        moveTo(keys, j, start);
                        start++;
                        break;
                    }                    
                }
                // if selected add all the attributes otherwise we still need
                // to check for a partial list
                List rmos = module
                    .getLeafRModuleOptions(rma.getAttribute());
                if ( isRMASelected ) 
                {
                    for ( int j=0; j<rmos.size(); j++ ) 
                    {
                        options.add( rmos.get(j) );
                    }               
                }
                else 
                {
                    for ( int j=0; j<rmos.size(); j++ ) 
                    {
                        String rmoId = getKey((RModuleOption)rmos.get(j));
                        boolean isRMOSelected = false;
                        for ( int k=start; k<keys.length; k++ ) 
                        {
                            if ( rmoId.equals(keys[k]) ) 
                            {
                                isRMOSelected = true;
                                //removing the key, as it is already matched
                                moveTo(keys, k, start);
                                start++;
                                break;
                            }                    
                        }
                        if ( isRMOSelected ) 
                        {
                            options.add( rmos.get(j) );
                        }                                   
                    }
                }
            }
        }
        return options;
    }

    /**
     * fill out list of AttributeAndUser's based on selected attributes
     * and users
     */
    private List getSelectedAttributeAndUsers(String[] keys)
        throws Exception
    {
        ModuleEntity module = getModule();
        List rmas = module.getRModuleAttributes(true);
        List ausers = new ArrayList(7*rmas.size());
        int start = 0;
        for ( int i=0; i<rmas.size() && keys.length != start; i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
            Attribute attribute = rma.getAttribute(); 
            if ( attribute.isUserAttribute()) 
            {            
                String rmaId = getKey(rma);
                boolean isRMASelected = false;
                for ( int j=start; j<keys.length; j++ ) 
                {
                    if ( rmaId.equals(keys[j]) ) 
                    {
                        isRMASelected = true;
                        //removing the key, as it is already matched
                        moveTo(keys, j, start);
                        start++;
                        break;
                    }                    
                }
                // if selected add all the attributes otherwise we still need
                // to check for a partial list
                List users = Arrays.asList(module.getEligibleUsers(attribute));
                if ( isRMASelected ) 
                {
                    for ( int j=0; j<users.size(); j++ ) 
                    {
                        ausers.add( new AttributeAndUser( 
                            rma, (ScarabUser)users.get(j) ));
                    }               
                }
                else 
                {
                    for ( int j=0; j<users.size(); j++ ) 
                    {
                        String userId = 
                            getKey(attribute, (ScarabUser)users.get(j));
                        boolean isRMOSelected = false;
                        for ( int k=start; k<keys.length; k++ ) 
                        {
                            if ( userId.equals(keys[k]) ) 
                            {
                                isRMOSelected = true;
                                //removing the key, as it is already matched
                                moveTo(keys, k, start);
                                start++;
                                break;
                            }                    
                        }
                        if ( isRMOSelected ) 
                        {
                            ausers.add( new AttributeAndUser(
                                rma, (ScarabUser)users.get(j)) );
                        }                                   
                    }
                }
            }
        }
        return ausers;
    }

    /**
     * fill out list of ScarabUser's based on selected committers
     */
    private List getSelectedCommitters(String[] keys)
        throws Exception
    {
        ModuleEntity module = getModule();
        List users = Arrays.asList(module.getEligibleIssueReporters());
        List committers = new ArrayList(users.size());
        int start = 0;
        for ( int j=0; j<users.size(); j++ ) 
        {
            ScarabUser user = (ScarabUser)users.get(j);

            String userId = getKey(user);
            for ( int k=start; k<keys.length; k++ ) 
            {
                if ( userId.equals(keys[k]) ) 
                {
                    //removing the key, as it is already matched
                    moveTo(keys, k, start);
                    start++;
                    committers.add(user);
                    break;
                }                    
            }
        }

        return committers;
    }

    public List getSelectedAxis1()
        throws Exception
    {
        List list = null;
        String[] keys = getAxis1Keys();
        if ( keys == null ) 
        {
            list = new ArrayList(0);
        }
        else 
        {
            list = pickSelectedList(getAxis1Category(), keys);
        }
        return list;
    }

    public List getSelectedAxis2()
        throws Exception
    {
        List list = null;
        String[] keys = getAxis2Keys();
        if ( keys == null ) 
        {
            list = new ArrayList(0);
        }
        else 
        {
            list = pickSelectedList(getAxis2Category(), keys);
        }
        return list;
    }

    private List pickSelectedList(int category, String[] keys)
        throws Exception
    {
        List list = null;
        switch ( category ) 
        {
        case 0: // option attributes
            list = getSelectedOptions(keys);
            break;
        case 1: // user attributes
            list = getSelectedAttributeAndUsers(keys);
            break;
        case 2: // committed by
            list = getSelectedCommitters(keys);           
            break;
        }
        return list;
    }

    public List getAllOptionsForGrouping()
        throws Exception
    {
        ModuleEntity module = getModule();
        List rmas = module.getRModuleAttributes(true);
        List allOptions = new ArrayList(7*rmas.size());
        for ( int i=0; i<rmas.size(); i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
            if ( rma.getAttribute().isOptionAttribute()) 
            {            
                allOptions.add( new ReportOptionModel(rma) );
                List rmos = module.getLeafRModuleOptions(rma.getAttribute());

                for ( int j=0; j<rmos.size(); j++ ) 
                {
                    allOptions.add( new ReportOptionModel(
                        (RModuleOption)rmos.get(j) ));
                }               
            }
        }
        return allOptions;
    }

    public Date[] getDates()
    {
        Date[] d = null;
        if ( dates != null ) 
        {
            int max = dates.size();
            d = new Date[max];
            for ( int i=0; i<max; i++ ) 
            {
                d[i] = (Date)((ReportDate)dates.get(i)).getDate();
            }
        }
        
        return d;
    }


    public void setDates(Date[] v)
    {
        if ( v == null ) 
        {
            dates = null;
        }
        else
        {
            int max = v.length;
            dates = new ArrayList(max);
            for ( int i=0; i<max; i++ ) 
            {
                dates.add(new ReportDate(v[i]));
            }
        }
    }

    /**
     * Returns the last setNewDate or null, if no dates have been set.
     * @return value of newDate.
     */
    public Date getNewDate() 
    {
        Date date = null;
        if ( dates != null && dates.size() != 0 ) 
        {
            date = (Date)((ReportDate)dates.get(dates.size()-1)).getDate();
        }
        return date;
    }


    /**
     * Set the value of newDate.
     * @param v  Value to assign to newDate.
     */
    public void setNewDate(Date  date) 
    {
        if ( date != null ) 
        {
            if ( dates == null ) 
            {
                dates = new ArrayList();
            }
            ReportDate reportDate = new ReportDate(date);
            reportDate.setQueryKey(String.valueOf(dates.size()));
            dates.add(reportDate);
        }
    }


    public void setReportDates(List v)
    {
        this.dates = v;
    }

    public List getReportDates()
    {
        return dates;
    }

    public ReportDate getNewReportDate()
    {
        return new ReportDate();
    }

    public List getAxis1OptionList()
        throws Exception
    {
        return pickOptionList(getAxis1Category());
    } 

    public List getAxis2OptionList()
        throws Exception
    {
        return pickOptionList(getAxis2Category());
    }

    private List pickOptionList(int category)
        throws Exception
    {
        List options = null;
        switch ( category ) 
        {
        case 0: // option attributes
            options = getOptionsMinusGroupedOptions();
            break;
        case 1: // user attributes
            options = getUserOptions();
            break;
        case 2: // committers
            options = getPossibleCommitters();
        }
        return options;
    }


    public List getOptionsMinusGroupedOptions()
        throws Exception
    {
        ModuleEntity module = getModule();
        List rmas = module.getRModuleAttributes(true);
        List options = new ArrayList(7*rmas.size());
        for ( int i=0; i<rmas.size(); i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);

            if ( !isGroupedAttribute(rma) && 
                 rma.getAttribute().isOptionAttribute()) 
            {            
                options.add( new ReportOptionModel(rma) );
                List rmos = module.getLeafRModuleOptions(rma.getAttribute());

                for ( int j=0; j<rmos.size(); j++ ) 
                {
                    RModuleOption rmo = (RModuleOption)rmos.get(j);
                    if ( !isGroupedOption(rmo)) 
                    {
                        options.add( new ReportOptionModel(rmo));
                    }   
                }               
            }
        }
        return options;
    }

    public List getUserOptions()
        throws Exception
    {
        ModuleEntity module = getModule();
        List rmas = module.getRModuleAttributes(true);
        List options = new ArrayList(7*rmas.size());
        for ( int i=0; i<rmas.size(); i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
            Attribute attribute = rma.getAttribute();
            if ( attribute.isUserAttribute()) 
            {            
                options.add( new ReportOptionModel(rma) );
                List users = Arrays.asList(module.getEligibleUsers(attribute));

                for ( int j=0; j<users.size(); j++ ) 
                {
                    ScarabUser user = (ScarabUser)users.get(j);
                    options.add( 
                        new ReportOptionModel(attribute, user) );
                }               
            }
        }
        return options;
    }

    public List getPossibleCommitters()
        throws Exception
    {
        List users = Arrays.asList(getModule().getEligibleIssueReporters());
        List options = new ArrayList(users.size());
        for ( int j=0; j<users.size(); j++ ) 
        {
            ScarabUser user = (ScarabUser)users.get(j);
            options.add( new ReportOptionModel(user) );
        }               

        return options;
    }

    private boolean isGroupedAttribute(RModuleAttribute rma)
        throws Exception
    {
        String test = getKey(rma);
        return isGroupedAttributeOrOption(test);
    }

    private boolean isGroupedOption(RModuleOption rmo)
        throws Exception
    {
        String test = getKey(rmo);
        boolean isGroupedOption = isGroupedAttributeOrOption(test);
        if ( !isGroupedOption ) 
        {
            // check that the whole attribute is not picked
            test = getKey(rmo.getAttributeOption().getAttribute());
            isGroupedOption = isGroupedAttributeOrOption(test);
        }
        
        return isGroupedOption;
    }

    private boolean isGroupedAttributeOrOption(String test)
    {
        boolean isGrouped = false;
        String[] attributeAndOptions = getAttributesAndOptionsForGrouping();
        if ( attributeAndOptions != null ) 
        {
            for (int i=0; i<attributeAndOptions.length; i++)
            {
                if ( test.equals(attributeAndOptions[i]) )
                {
                    isGrouped = true;
                    break;
                }
            }
        }
        return isGrouped;
    }

    /**
     * Get the value of axis1Category.
     * @return value of axis1Category.
     */
    public int getAxis1Category() 
    {
        return axis1Category;
    }
    
    /**
     * Set the value of axis1Category.
     * @param v  Value to assign to axis1Category.
     */
    public void setAxis1Category(int  v) 
    {
        this.axis1Category = v;
    }
    
    
    /**
     * Get the value of axis2Category.
     * @return value of axis2Category.
     */
    public int getAxis2Category() 
    {
        return axis2Category;
    }
    
    /**
     * Set the value of axis2Category.
     * @param v  Value to assign to axis2Category.
     */
    public void setAxis2Category(int  v) 
    {
        this.axis2Category = v;
    }
    
    /**
     */
    public String[] getAxis1Keys() 
    {
        return this.axis1Keys;
    }
    
    /**
     */
    public void setAxis1Keys(String[] v) 
    {
        if ( v != null && (v.length == 0 || v[0].length() == 0) ) 
        {
            this.axis1Keys = null;
        }
        else 
        {
            this.axis1Keys = v;
        }
    }

    /**
     */
    public String[] getAxis2Keys() 
    {
        return this.axis2Keys;
    }
    
    /**
     */
    public void setAxis2Keys(String[] v) 
    {
        if ( v != null && (v.length == 0 || v[0].length() == 0) ) 
        {
            this.axis2Keys = null;
        }
        else 
        {
            this.axis2Keys = v;
        }
    }

    private int getIssueCount(Object o1, Object o2, Object ogOrRmo, Date date)
        throws Exception
    {
        Criteria crit = new Criteria();
        // select count(issue_id) from activity a1 a2 a3, transaction t1 t2 t3
        crit.addSelectColumn("count(DISTINCT a1." + ACT_ISSUE_ID + ')');
        addOptionOrGroup(1, o1, date, crit);
        addOptionOrGroup(2, o2, date, crit);
        addOptionOrGroup(3, ogOrRmo, date, crit);
        // need to add in module criteria !FIXME!
        return getCountAndCleanUp(crit);
    }

    public int getIssueCount(Object o1, Date date)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.addSelectColumn("count(DISTINCT a1." + ACT_ISSUE_ID + ')');
        addOptionOrGroup(1, o1, date, crit);
        // need to add in module criteria !FIXME!
        return getCountAndCleanUp(crit);
    }

    private int[] aliases = new int[5];

    private void registerAlias(int alias, Criteria crit)
    {
        for ( int i=0; i<aliases.length; i++) 
        {
            if ( aliases[i] == alias ) 
            {
                break;
            }
            else if ( aliases[i] <= 0 ) 
            {
                aliases[i] = alias;
                if ( i != 0 ) 
                {
                    crit.addJoin("a"+aliases[0]+'.'+ACT_ISSUE_ID, 
                                 "a"+alias+'.'+ACT_ISSUE_ID);
                }
                break;
            }
        }
        crit.addAlias("a"+alias, ActivityPeer.TABLE_NAME);
        crit.addAlias("t"+alias, TransactionPeer.TABLE_NAME);
    }

    private void addOptionOrGroup(int alias, Object optionOrGroup, 
                                  Date date, Criteria crit)
    {
        String a = "a"+alias;
        String t = "t"+alias;
        if ( optionOrGroup != null ) 
        {
            registerAlias(alias, crit);
            crit.addJoin(a+"."+ACT_TRANSACTION_ID, t+'.'+TRAN_TRANSACTION_ID);
            crit.add(t, TRAN_CREATED_DATE, date, Criteria.LESS_THAN);   
            // end date criteria
            Criteria.Criterion c1 = crit
                .getNewCriterion(a, ACT_END_DATE, date, Criteria.GREATER_THAN);
            c1.or(crit.getNewCriterion(a, ACT_END_DATE, null, Criteria.EQUAL));
            crit.add(c1);
        }

        if ( optionOrGroup instanceof OptionGroup ) 
        {
            List options = ((OptionGroup)optionOrGroup).getOptions();
            if ( options != null && options.size() > 0 ) 
            {            
                NumberKey[] nks = new NumberKey[options.size()];
                for ( int i=0; i<nks.length; i++) 
                {
                    nks[i] = ((RModuleOption)options.get(i)).getOptionId();
                }
                
                crit.addIn(a+'.'+ACT_NEW_OPTION_ID, nks);
            }
            else 
            {
                // group is empty make sure there are no results
                crit.add(a+'.'+ACT_NEW_OPTION_ID, -1);
            }
        }
        else if (optionOrGroup instanceof RModuleOption)
        {
            crit.add(a, ACT_NEW_OPTION_ID, 
                     ((RModuleOption)optionOrGroup).getOptionId());
        }
        else if (optionOrGroup instanceof AttributeOption)
        {
            crit.add(a, ACT_NEW_OPTION_ID, 
                     ((AttributeOption)optionOrGroup).getOptionId());
        }
        else if (optionOrGroup instanceof AttributeAndUser)
        {
            RModuleAttribute rma = ((AttributeAndUser)optionOrGroup)
                .getRModuleAttribute();
            ScarabUser user = ((AttributeAndUser)optionOrGroup).getUser();
            crit.add(a, ACT_ATTRIBUTE_ID, rma.getAttributeId());
            crit.add(a, ACT_NEW_USER_ID, user.getUserId());
        }
        else if (optionOrGroup instanceof ScarabUser)
        {
            crit.add(t, TRAN_TYPE_ID, 
                     TransactionTypePeer.CREATE_ISSUE__PK);
            crit.add(t, TRAN_CREATED_BY, 
                     ((ScarabUser)optionOrGroup).getUserId());
        }
    }

    private int getCountAndCleanUp(Criteria crit)
        throws Exception
    {
        List records = ActivityPeer.doSelectVillageRecords(crit);
        // clean up
        for ( int i=0; i<aliases.length; i++ ) 
        {
            aliases[i] = 0;
        }
        return ((Record)records.get(0)).getValue(1).asInt();
    }

    public void setQueryString(String v)
    {
        super.setQueryString(v);
        StringValueParser parser = new StringValueParser();
        // method is not allowed to pass exception, so Log it and convert.
        try
        {
            parser.parse(v, '&', '=', true);
            populate(parser);
        }
        catch (Exception e)
        {
            String mesg = "Could not populate the report using parameters: " + 
                parser;
            Log.error(mesg, e);
            throw new RuntimeException("Check logs for error message. "+mesg);
        }
    }

    private void populate(ValueParser parameters)
        throws Exception
    {
        Intake intake = new Intake();
        intake.init(parameters);

        Group intakeReport = intake.get("Report", this.getQueryKey(), false);
        if ( intakeReport == null ) 
        {   
            intakeReport = intake.get("Report", "", false);
        }  
        if ( intakeReport != null ) 
        {   
            intakeReport.setValidProperties(this);

        // set up option groups
        int i = 0;
        List groups = new ArrayList();
        Report.OptionGroup group = this.getNewOptionGroup();
        group.setQueryKey(String.valueOf(i++));
        Group intakeGroup = intake.get("OptionGroup", 
                                       group.getQueryKey(), false);
        while ( intakeGroup != null ) 
        {
            intakeGroup.setValidProperties(group);
            groups.add(group);

            group = this.getNewOptionGroup();
            group.setQueryKey(String.valueOf(i++));
            intakeGroup = intake.get("OptionGroup", 
                                     group.getQueryKey(), false);
        }
        this.setOptionGroups(groups);

        List options = this.getSelectedOptionsForGrouping();
        for ( i=0; i<options.size(); i++ ) 
        {
            RModuleOption rmo = (RModuleOption)options.get(i);
            String key = "ofg" + rmo.getQueryKey();
            int groupIndex = parameters.getInt(key);
            if ( groupIndex >= 0 && groupIndex < groups.size() ) 
            {
                ((Report.OptionGroup)groups.get(groupIndex))
                    .addOption(rmo);
            }
        }

        // set up dates
        i = 0;
        List dates = new ArrayList();
        Report.ReportDate date = this.getNewReportDate();
        date.setQueryKey(String.valueOf(i++));
        Group intakeDate = intake.get("ReportDate", 
                                       date.getQueryKey(), false);
        while ( intakeDate != null ) 
        {
            if ( intakeDate.get("Date").isSet()) 
            {
                intakeDate.setValidProperties(date);
                dates.add(date);                
            }
            
            date = this.getNewReportDate();
            date.setQueryKey(String.valueOf(i++));
            intakeDate = intake.get("ReportDate", 
                                     date.getQueryKey(), false);
        }
        if ( dates.size() > 0 ) 
        {
            // the intakeReport.setProperties call above may have added a date
            // so we do not want to lose it. 
            List reportDates = this.getReportDates();
            if ( reportDates != null ) 
            {
                for ( int j=0; j<reportDates.size(); j++ ) 
                {
                    Report.ReportDate reportDate = 
                        (Report.ReportDate)reportDates.get(j);
                    date.setQueryKey(String.valueOf(i++));
                    dates.add(reportDate);
                }
            }
            this.setReportDates(dates);            
        }
        }
    }



    public TableModel getModel()
        throws Exception
    {
        return new Report1TableModel();
    }
        
    public class Report1TableModel extends TableModel
    {           
        List secondCriteria;
        List columnData;
        List rowData;
        boolean isGroups;

            public Report1TableModel()
                throws Exception
            {
                rowData = getSelectedAxis1();
                if ( getType() == 0 ) 
                {
                    columnData = getSelectedAxis2();                    
                    secondCriteria = getOptionGroups();
                    isGroups = true;
                    if ( secondCriteria == null ) 
                    {
                        isGroups = false;
                        secondCriteria = getSelectedOptionsForGrouping();
                    }
                }
                else 
                {
                    columnData = Arrays.asList(getDates());
                }
            }         

            public int getColumnCount()
            {
                int size = columnData.size();
                if ( secondCriteria != null && secondCriteria.size() > 1 ) 
                {
                    size *= secondCriteria.size();
                }
                
                return size;
            }

            public int getRowCount()
            {
                return rowData.size();
            }


        public Object getValueAt(int row, int column)
            throws Exception
        {
            Object contents = null;
            int r = row - 1;
            int c = column - 1;
            
            if ( c >= 0 ) 
            {
                Object secCrit = null;
                int size = 1;
                if ( secondCriteria != null && secondCriteria.size() > 0 ) 
                {
                    size = secondCriteria.size();
                    secCrit = secondCriteria.get(c%size);
                }

                Object cData = columnData.get(c/size);
                if ( r >= 0) 
                {
                    Object rData = rowData.get(r);
                    if ( cData instanceof Date ) 
                    {
                        contents = new Integer( 
                            getIssueCount(rData, (Date)cData) ); 
                    }
                    else 
                    {
                        contents = new Integer( getIssueCount(
                            rData, cData, secCrit, getGeneratedDate())); 
                    }
                }
                else   
                {                      
                    ColumnHeading heading = new ColumnHeading();
                    if ( cData instanceof Date ) 
                    {
                        heading.setLabel(cData);
                    }
                    else 
                    {
                        heading.setLabel(new Label(cData, secCrit));
                    }
                    contents = heading;
                }                    
            }
            else if ( r >= 0 && c == -1 )
            {                     
                Object rData = rowData.get(r);
                RowHeading heading = new RowHeading();
                heading.setLabel(new Label(rData));
                contents = heading;
            }
            else 
            {
                contents  = new ColumnHeading();
            }
                            
            return contents;
        }

        public class Label 
        {
            List objs;
            
            public Label(Object obj)
            {
                objs = new ArrayList(1);
                objs.add(obj);
            }

            public Label(Object obj1, Object obj2)
            {
                if ( obj2 == null ) 
                {
                    objs = new ArrayList(1);
                    objs.add(obj1);
                }
                else 
                {
                    objs = new ArrayList(2);
                    objs.add(obj1);
                    objs.add(obj2);
                }
            }

            public boolean isOption(Object obj)
            {
                return obj instanceof RModuleOption;
            }
            public boolean isOptionGroup(Object obj)
            {
                return obj instanceof OptionGroup;
            }
            public boolean isAttributeAndUser(Object obj)
            {
                return obj instanceof AttributeAndUser;
            }
            public boolean isUser(Object obj)
            {
                return obj instanceof ScarabUser;
            }
            public List getSubLabels()
            {
                return objs;
            }
            public Object getSubLabel1()
            {
                return objs.get(0);
            }
            public Object getSubLabel2()
            {
                return objs.get(1);
            }
        }

    }



    // *********************************************************
    // Retrievable implementation
    // *********************************************************
    
    /**
     * Get the value of queryKey.
     * @return value of queryKey.
     */    
    public String getQueryKey() 
    {
        if ( queryKey == null ) 
        {
            return "";
        }
        return queryKey;
    }
    
    /**
     * Set the value of queryKey.
     * @param v  Value to assign to queryKey.
     */
    public void setQueryKey(String  v) 
    {
        this.queryKey = v;
    }



    // *********************************************************

    public static class OptionGroup
        implements Retrievable
    {
        private String displayValue;
        private boolean selected;
        private String queryKey;
        private List options;
        
        public OptionGroup()
        {
        }

        public OptionGroup(String name)
        {
            displayValue = name;
        }

        /**
         * Get the value of displayValue.
         * @return value of displayValue.
         */
        public String getDisplayValue() 
        {
            return displayValue;
        }
        
        /**
         * Set the value of displayValue.
         * @param v  Value to assign to displayValue.
         */
        public void setDisplayValue(String  v) 
        {
            this.displayValue = v;
        }
        
        
        /**
         * Get the value of selected.
         * @return value of selected.
         */
        public boolean isSelected() 
        {
            return selected;
        }
        
        /**
         * Set the value of selected.
         * @param v  Value to assign to selected.
         */
        public void setSelected(boolean  v) 
        {
            this.selected = v;
        }
        

        public void addOption(RModuleOption rmo)
        {
            if ( options == null ) 
            {
                options = new ArrayList();
            }
            options.add(rmo);
        }

        public List getOptions()
        {
            if ( options == null ) 
            {
                options = new ArrayList();
            }
            return options;
        }

        // *********************************************************
        // Retrievable implementation
        // *********************************************************
        
        /**
         * Get the value of queryKey.
         * @return value of queryKey.
         */    
        public String getQueryKey() 
        {
            /*
            List groups = getOptionGroups();
            int index = -1;
            for ( int i=0; i<groups.size(); i++ ) 
            {
                if ( ((OptionGroup)groups.get(i)).getDisplayValue()
                     .equals(displayValue)) 
                {
                    index = i;
                    break;
                }
            }
            
            return String.valueOf(index);
            */
            if ( queryKey == null ) 
            {
                return "";
            }
            return queryKey;
        }

        
        /**
         * Set the value of queryKey.
         * @param v  Value to assign to queryKey.
         */
        public void setQueryKey(String  v) 
        {
            this.queryKey = v;
        }
    }


    // *********************************************************

    public static class ReportDate
        implements Retrievable
    {
        private Date date;
        private boolean selected;
        private String queryKey;
        
        public ReportDate()
        {
        }

        public ReportDate(Date date)
        {
            this.date = date;
        }

        
        /**
         * Get the value of date.
         * @return value of date.
         */
        public Date getDate() 
        {
            return date;
        }
        
        /**
         * Set the value of date.
         * @param v  Value to assign to date.
         */
        public void setDate(Date  v) 
        {
            this.date = v;
        }
        
        
        /**
         * Get the value of selected.
         * @return value of selected.
         */
        public boolean isSelected() 
        {
            return selected;
        }
        
        /**
         * Set the value of selected.
         * @param v  Value to assign to selected.
         */
        public void setSelected(boolean  v) 
        {
            this.selected = v;
        }
        

        // *********************************************************
        // Retrievable implementation
        // *********************************************************
        
        /**
         * Get the value of queryKey.
         * @return value of queryKey.
         */    
        public String getQueryKey() 
        {
            /*
            List groups = getOptionGroups();
            int index = -1;
            for ( int i=0; i<groups.size(); i++ ) 
            {
                if ( ((OptionGroup)groups.get(i)).getDisplayValue()
                     .equals(displayValue)) 
                {
                    index = i;
                    break;
                }
            }
            
            return String.valueOf(index);
            */
            if ( queryKey == null ) 
            {
                return "";
            }
            return queryKey;
        }

        
        /**
         * Set the value of queryKey.
         * @param v  Value to assign to queryKey.
         */
        public void setQueryKey(String  v) 
        {
            this.queryKey = v;
        }
    }

    
    // *********************************************************

    // *********************************************************

    public static class ReportOptionModel
        extends OptionModel
    {
        private boolean isAttribute;

        ReportOptionModel(RModuleAttribute rma)
            throws Exception
        {
            setIsAttribute(true);
            super.setName(rma.getDisplayValue());
            setValue( getKey(rma) );
        }

        ReportOptionModel(RModuleOption rmo)
            throws Exception
        {
            setIsAttribute(false);
            super.setName(rmo.getDisplayValue());
            setValue( getKey(rmo) );
        }

        ReportOptionModel(Attribute a, ScarabUser user)
            throws Exception
        {
            setIsAttribute(false);
            super.setName(user.getUserName());
            setValue( getKey(a, user) );
        }

        ReportOptionModel(ScarabUser user)
            throws Exception
        {
            setIsAttribute(false);
            super.setName(user.getUserName());
            setValue( getKey(user) );
        }

        /**
         * Get the value of isAttribute.
         * @return value of isAttribute.
         */
        public boolean isAttribute() 
        {
            return isAttribute;
        }
        
        /**
         * Set the value of isAttribute.
         * @param v  Value to assign to isAttribute.
         */
        public void setIsAttribute(boolean  v) 
        {
            this.isAttribute = v;
        }   
    }    

    private static String getKey(RModuleAttribute rma)
        throws Exception
    {
        return getKey(rma.getAttribute());
    }
    
    private static String getKey(Attribute a)
        throws Exception
    {
        String key = null;
        if ( a.isUserAttribute() ) 
        {
            key = "ua" + a.getQueryKey();
        }
        else 
        {
            key = "a" + a.getQueryKey();
        }
        
        return key;
    }
    
    private static String getKey(RModuleOption rmo)
        throws Exception
    {
        return getKey(rmo.getAttributeOption());
    }
    
    private static String getKey(AttributeOption o)
    {
        return o.getQueryKey();
    }
    
    private static String getKey(Attribute a, ScarabUser user)
    {
        return "au" + a.getQueryKey() + ':' + user.getQueryKey();
    }

    private static String getKey(ScarabUser user)
    {
        return 'u' + user.getQueryKey();
    }

    public static class AttributeAndUser
    {
        private RModuleAttribute attribute;
        private ScarabUser user;

        public AttributeAndUser(RModuleAttribute attribute, ScarabUser user)
        {
            this.attribute = attribute;
            this.user = user;
        }

        /**
         * Get the value of attribute.
         * @return value of attribute.
         */
        public RModuleAttribute getRModuleAttribute() 
        {
            return attribute;
        }
        
        /**
         * Set the value of attribute.
         * @param v  Value to assign to attribute.
         */
        public void setRModuleAttribute(RModuleAttribute  v) 
        {
            this.attribute = v;
        }
                
        /**
         * Get the value of user.
         * @return value of user.
         */
        public ScarabUser getUser() 
        {
            return user;
        }
        
        /**
         * Set the value of user.
         * @param v  Value to assign to user.
         */
        public void setUser(ScarabUser  v) 
        {
            this.user = v;
        }
        
    }

}
