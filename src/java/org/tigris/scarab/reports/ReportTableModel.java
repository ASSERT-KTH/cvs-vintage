package org.tigris.scarab.reports;

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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.sql.Connection;
import com.workingdogs.village.Record;

// Turbine classes
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;

import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributeManager;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleOptionManager;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ActivityPeer;
import org.tigris.scarab.om.ActivitySetPeer;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.util.OptionModel;
import org.tigris.scarab.util.TableModel;
import org.tigris.scarab.services.security.ScarabSecurity;

public class ReportTableModel 
    extends TableModel
{ 
    private static final String ACT_ATTRIBUTE_ID = 
        ActivityPeer.ATTRIBUTE_ID.substring(
            ActivityPeer.ATTRIBUTE_ID.indexOf('.')+1);
    private static final String ACT_NEW_USER_ID = 
        ActivityPeer.NEW_USER_ID.substring(
            ActivityPeer.NEW_USER_ID.indexOf('.')+1);
    private static final String ACT_NEW_OPTION_ID = 
        ActivityPeer.NEW_OPTION_ID.substring(
            ActivityPeer.NEW_OPTION_ID.indexOf('.')+1);
    private static final String ACT_ISSUE_ID = 
        ActivityPeer.ISSUE_ID.substring(
            ActivityPeer.ISSUE_ID.indexOf('.')+1);
    private static final String ACT_TRANSACTION_ID = 
        ActivityPeer.TRANSACTION_ID.substring(
            ActivityPeer.TRANSACTION_ID.indexOf('.')+1);
    private static final String ACT_END_DATE = 
        ActivityPeer.END_DATE.substring(
            ActivityPeer.END_DATE.indexOf('.')+1);
    private static final String TRAN_TRANSACTION_ID = 
        ActivitySetPeer.TRANSACTION_ID.substring(
            ActivitySetPeer.TRANSACTION_ID.indexOf('.')+1);
    private static final String TRAN_CREATED_DATE = 
        ActivitySetPeer.CREATED_DATE.substring(
            ActivitySetPeer.CREATED_DATE.indexOf('.')+1);
    private static final String TRAN_CREATED_BY = 
        ActivitySetPeer.CREATED_BY.substring(
            ActivitySetPeer.CREATED_BY.indexOf('.')+1);
    private static final String TRAN_TYPE_ID = 
        ActivitySetPeer.TYPE_ID.substring(
            ActivitySetPeer.TYPE_ID.indexOf('.')+1);

    private ReportDefinition reportDefn;
    private List rowHeadings;
    private List columnHeadings;
    private Date date;
    private NumberKey moduleId;
    private NumberKey issueTypeId;
    private MITList mitList;

    private int[] colspan;
    private int[] rowspan;

    ReportTableModel(ReportBridge report, Date date)
        throws TorqueException
    {
        this.reportDefn = report.getReportDefinition();
        ReportAxis axis = null;
        List axes = reportDefn.getReportAxisList();
        if (axes != null && axes.size() >= 2) 
        {
            axis = (ReportAxis)axes.get(1);
        }
        if (axis != null) 
        {
            columnHeadings = axis.getReportHeadings();
        }
        
        if (axes != null && axes.size() >= 1) 
        {
            axis = (ReportAxis)axes.get(0);
        }
        if (axis != null) 
        {
            rowHeadings = axis.getReportHeadings();
        }

        this.date = date;

        List xmits = reportDefn.getModuleIssueTypes();
        if (xmits.size() == 1) 
        {
            ModuleIssueType mit = (ModuleIssueType)xmits.get(0);
            this.moduleId = new NumberKey(mit.getModuleId().toString());
            this.issueTypeId = new NumberKey(mit.getIssueTypeId().toString());
        }
        else 
        {
            this.mitList = report.getMITList();
        }
        
    }

    /**
     * Get the RowHeadings value.
     * @return the RowHeadings value.
     */
    public List getRowHeadings()
    {
        return rowHeadings;
    }

    /**
     * Get the ColumnHeadings value.
     * @return the ColumnHeadings value.
     */
    public List getColumnHeadings()
    {
        return columnHeadings;
    }

    public int getColspan(int index)
    {
        int result = 1;
        if (columnHeadings != null) 
        {
            if (colspan == null) 
            {
                int numLevels = columnHeadings.size();
                colspan = new int[numLevels - 1];
                for (int j=0; j<numLevels-1; j++) 
                {
                    colspan[j] = 1;
                    for (int i=numLevels-1; i>j; i--) 
                    {
                        colspan[j] *= 
                            ((ReportHeading)columnHeadings.get(i)).size();
                    }
                }
            }

            if (index < colspan.length) 
            {
                result = colspan[index];
            }
        }
        
        return result;
    }

    public int getRowspan(int index)
    {
        int result = 1;
        if (rowHeadings != null) 
        {
            if (rowspan == null) 
            {
                int numLevels = rowHeadings.size();
                rowspan = new int[numLevels - 1];
                for (int j=0; j<numLevels-1; j++) 
                {
                    rowspan[j] = 1;
                    for (int i=numLevels-1; i>j; i--) 
                    {
                        rowspan[j] *= 
                            ((ReportHeading)rowHeadings.get(i)).size();
                    }
                }
            }

            if (index < rowspan.length) 
            {
                result = rowspan[index];
            }
        }
        
        return result;
    }

    public int getColumnCount()
    {
        return ((ReportHeading)columnHeadings.get(0)).size() * getColspan(0);
    }
    
    public int getRowCount()
    {
        return ((ReportHeading)rowHeadings.get(0)).size() * getRowspan(0);
    }


    private Object[] getColumnDataArray(int column)
    {
        Object[] dataArray = null;
        if (columnHeadings == null) 
        {
            dataArray = new Object[0];
        }
        else 
        {
            int numLevels = columnHeadings.size();
            dataArray = new Object[numLevels];
            int index = 0;
            for (Iterator i=columnHeadings.iterator(); i.hasNext(); index++)
            {
                ReportHeading heading = (ReportHeading)i.next();
                dataArray[index] = heading.get((column/getColspan(index)) % heading.size());
            }
        }
        
        return dataArray;
    }

    private Object[] getRowDataArray(int row)
    {
        Object[] dataArray = null;
        if (rowHeadings == null) 
        {
            dataArray = new Object[0];
        }
        else 
        {
            int numLevels = rowHeadings.size();
            dataArray = new Object[numLevels];
            int index = 0;
            for (Iterator i=rowHeadings.iterator(); i.hasNext(); index++)
            {
                ReportHeading heading = (ReportHeading)i.next();
                dataArray[index] = heading.get((row/getRowspan(index)) % heading.size());
            }
        }
        return dataArray;
    }

    public Object getValueAt(int row, int column)
        throws Exception
    {
        Object contents = null;
        if (row < 0 || row >= getRowCount()) 
        {
            throw new IndexOutOfBoundsException("Row index was " + row);
        }
        
        if (column < 0 || column >= getColumnCount()) 
        {
            throw new IndexOutOfBoundsException("Column index was " + column);
        }

        // could use a categories list to make this simpler
        if (columnHeadings != null && columnHeadings.size() == 1 && 
            ((ReportHeading)columnHeadings.get(0)).get(0) instanceof ReportDate) 
        {
            contents = new Integer(getIssueCount(getRowDataArray(row), 
                ((ReportDate)((ReportHeading)columnHeadings.get(0))
                 .get(column)).dateValue())); 
        }
        else if (rowHeadings != null && rowHeadings.size() == 1 && 
                 ((ReportHeading)rowHeadings.get(0)).get(0) instanceof ReportDate)
        {
            contents = new Integer(getIssueCount(getColumnDataArray(column),
                ((ReportDate)((ReportHeading)rowHeadings.get(0))
                 .get(row)).dateValue()));  
        }
        else 
        {
            contents = new Integer(getIssueCount(
                getRowDataArray(row), getColumnDataArray(column), date)); 
        }

        return contents;
    }

    private int getIssueCount(Object[] rowData, Object[] columnData, Date date)
        throws Exception
    {
        Criteria crit = new Criteria();
        // select count(issue_id) from activity a1 a2 a3, activitySet t1 t2 t3
        crit.addSelectColumn("count(DISTINCT a0." + ACT_ISSUE_ID + ')');
        int rowLength = rowData.length;
        for (int i=0; i<rowLength; i++) 
        {
            addOptionOrGroup(i, rowData[i], date, crit);
            
        }
        for (int i=0; i<columnData.length; i++) 
        {
            addOptionOrGroup(i+rowLength, columnData[i], date, crit);
        }
        return getCountAndCleanUp(crit);
    }

    public int getIssueCount(Object[] dataArray, Date date)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.addSelectColumn("count(DISTINCT a0." + ACT_ISSUE_ID + ')');
        for (int i=0; i<dataArray.length; i++) 
        {
            addOptionOrGroup(i, dataArray[i], date, crit);
        }
        return getCountAndCleanUp(crit);
    }

    private void addOptionOrGroup(int alias, Object optionOrGroup, 
                                  Date date, Criteria crit)
    {
        if (optionOrGroup == null) 
        {
            throw new NullPointerException("cell definition cannot contain nulls");
        }

        String a = "a"+alias;
        String t = "t"+alias;
        // there is some redundancy here, but over specifying the joins usually
        // allows the query optimizer to obtain a better optimization
        crit.addJoin(a + '.' + ACT_ISSUE_ID, IssuePeer.ISSUE_ID);
        for (int i=alias-1; i>=0; i--) 
        {
            crit.addJoin("a"+i+'.'+ACT_ISSUE_ID, a+'.'+ACT_ISSUE_ID);
        }
        crit.addAlias("a"+alias, ActivityPeer.TABLE_NAME);
        crit.addAlias("t"+alias, ActivitySetPeer.TABLE_NAME);
        
        crit.addJoin(a+"."+ACT_TRANSACTION_ID, t+'.'+TRAN_TRANSACTION_ID);
        crit.add(t, TRAN_CREATED_DATE, date, Criteria.LESS_THAN);   
        // end date criteria
        Criteria.Criterion c1 = crit
            .getNewCriterion(a, ACT_END_DATE, date, Criteria.GREATER_THAN);
        c1.or(crit.getNewCriterion(a, ACT_END_DATE, null, Criteria.EQUAL));
        crit.add(c1);

        if (optionOrGroup instanceof ReportGroup) 
        {
            List options = ((ReportGroup)optionOrGroup)
                .getReportOptionAttributes();
            if (options != null && options.size() > 0) 
            {            
                Integer[] nks = new Integer[options.size()];
                for (int i=0; i<nks.length; i++) 
                {
                    nks[i] = ((ReportOptionAttribute)options.get(i))
                        .getOptionId();
                }
                
                crit.addIn(a+'.'+ACT_NEW_OPTION_ID, nks);
            }
            else 
            {
                List users = ((ReportGroup)optionOrGroup)
                    .getReportUserAttributes();
                if (users != null && users.size() > 0) 
                {            
                    Integer[] nks = new Integer[users.size()];
                    for (int i=0; i<nks.length; i++) 
                    {
                        nks[i] = ((ReportUserAttribute)users.get(i))
                            .getUserId();
                    }
                    
                    crit.addIn(a+'.'+ACT_NEW_USER_ID, nks);
                }
                else 
                {
                    // group is empty make sure there are no results
                    crit.add(a+'.'+ACT_NEW_OPTION_ID, -1);
                }
            }
        }
        else if (optionOrGroup instanceof ReportOptionAttribute)
        {
            crit.add(a, ACT_NEW_OPTION_ID, 
                     ((ReportOptionAttribute)optionOrGroup).getOptionId());
        }
        else if (optionOrGroup instanceof ReportUserAttribute)
        {
            ReportUserAttribute rua = (ReportUserAttribute)optionOrGroup;
            Integer attributeId = rua.getAttributeId();
            if (attributeId.intValue() == 0)
            {
                // committed by
                crit.add(t, TRAN_TYPE_ID, 
                         ActivitySetTypePeer.CREATE_ISSUE__PK);
                crit.add(t, TRAN_CREATED_BY, rua.getUserId());
            }
            else 
            {
                crit.add(a, ACT_ATTRIBUTE_ID, rua.getAttributeId());
                crit.add(a, ACT_NEW_USER_ID, rua.getUserId());
            }
        }
    }

    private boolean isXMITSearch()
    {
        return mitList != null && !mitList.isSingleModuleIssueType();
    }

    private int getCountAndCleanUp(Criteria crit)
        throws Exception
    {
        if (isXMITSearch())
        {
            mitList.addToCriteria(crit);
        }
        else 
        {
            crit.add(IssuePeer.MODULE_ID, moduleId);
            crit.add(IssuePeer.TYPE_ID, issueTypeId);
        }

        crit.add(IssuePeer.DELETED, false);
        return ((Record)ActivityPeer.doSelectVillageRecords(crit).get(0))
            .getValue(1).asInt();
    }

    public boolean isOption(Object obj)
    {
        return obj instanceof ReportOptionAttribute;
    }
    public boolean isOptionGroup(Object obj)
    {
        return obj instanceof ReportGroup;
    }
    public boolean isAttributeAndUser(Object obj)
    {
        return obj instanceof ReportUserAttribute;
    }
    public boolean isUser(Object obj)
    {
        return obj instanceof ScarabUser;
    }
    public boolean isReportDate(Object obj)
    {
        return obj instanceof ReportDate;
    }

    public String displayAttribute(Object cell)
    {
        return reportDefn.displayAttribute(cell);
    }

    public String displayOption(ReportOptionAttribute cell)
    {
        return reportDefn.displayOption(cell);
    }

    public String displayUser(ReportUserAttribute cell)
    {
        return reportDefn.displayUser(cell);
    }
}
