
package org.tigris.scarab.om;


import org.apache.commons.lang.Objects;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;
import org.tigris.scarab.util.word.IssueSearch;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class MITListItem 
    extends org.tigris.scarab.om.BaseMITListItem
    implements Persistent
{
    private static final NumberKey MULTIPLE_KEY = new NumberKey(0);

    public int getIssueCount()
        throws Exception
    {
        IssueSearch is = new IssueSearch(getModule(), getIssueType());
        return is.getIssueCount();
    }

    public boolean isSingleModuleIssueType()
    {
        boolean single = true;
        if (MULTIPLE_KEY.equals(getModuleId()) 
            || MULTIPLE_KEY.equals(getIssueTypeId())) 
        {
            single = false;
        }
        return single;
    }

    public boolean isSingleModule()
    {
        return !MULTIPLE_KEY.equals(getModuleId());
    }

    public boolean isSingleIssueType()
    {
        return !MULTIPLE_KEY.equals(getIssueTypeId());
    }

    public boolean isUseCurrentModule()
    {
        return getModuleId() == null;
    }

    public boolean isUseCurrentIssueType()
    {
        return getIssueTypeId() == null;
    }

    public String getQueryKey()
    {
        String key = super.getQueryKey();
        if (key == null || key.length() == 0) 
        {
            StringBuffer sb = new StringBuffer();
            if (getModuleId() != null) 
            {
                sb.append(getModuleId());
            }
            sb.append(':');
            if (getIssueTypeId() != null) 
            {
                sb.append(getIssueTypeId());
            }
            key = sb.toString();
        }
        return key;
    }

    public int hashCode()
    {
        int hashCode = 10;
        if (getModuleId() != null) 
        {
            hashCode += getModuleId().hashCode();
        }
        if (getIssueTypeId() != null) 
        {
            hashCode += getIssueTypeId().hashCode();
        }
        return hashCode;
    }

    public boolean equals(Object obj)
    {
        boolean isEqual = false;
        if (obj instanceof MITListItem) 
        {
            MITListItem item = (MITListItem)obj;
            isEqual = Objects.equals(this.getModuleId(), item.getModuleId());
            isEqual &= Objects.equals(this.getIssueTypeId(), 
                                      item.getIssueTypeId());
        }
        return isEqual;
    }
}
