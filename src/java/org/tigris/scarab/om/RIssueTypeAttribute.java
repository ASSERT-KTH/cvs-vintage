
package org.tigris.scarab.om;

import java.util.List;
import java.util.ArrayList;

import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class RIssueTypeAttribute 
    extends org.tigris.scarab.om.BaseRIssueTypeAttribute
    implements Persistent
{
    /**
     * This method sets the defaultTextFlag property and also makes sure 
     * that no other related RIA is defined as the default.  It should be
     * used instead of setDefaultTextFlag in application code.
     *
     * @param b a <code>boolean</code> value
     */
    public void setIsDefaultText(boolean b)
        throws Exception
    {
        if (b && !getDefaultTextFlag()) 
        {
            // get related RIAs
            List rias = getIssueType().getRIssueTypeAttributes(false);
            
            // make sure no other rma is selected
            for ( int i=0; i<rias.size(); i++ ) 
            {
                RIssueTypeAttribute ria = (RIssueTypeAttribute)rias.get(i);
                if ( ria.getDefaultTextFlag() ) 
                {
                    ria.setDefaultTextFlag(false);
                    ria.save();
                    break;
                }
            }
        }
        setDefaultTextFlag(b);
    }

    public void delete( ScarabUser user )
         throws Exception
    {                
            Criteria c = new Criteria()
                .add(RIssueTypeAttributePeer.ISSUE_TYPE_ID, getIssueTypeId())
                .add(RIssueTypeAttributePeer.ATTRIBUTE_ID, getAttributeId());
            RIssueTypeAttributePeer.doDelete(c);
            Attribute attr = getAttribute();
            String attributeType = null;
            attributeType = (attr.isUserAttribute() ? IssueType.USER : IssueType.NON_USER);
            getIssueType().getRIssueTypeAttributes(false, attributeType).remove(this);

            // delete module-option mappings
            if (attr.isOptionAttribute())
            {
                List optionList = getIssueType().getRIssueTypeOptions(attr, false);
                ArrayList optionIdList = new ArrayList(optionList.size());
                for (int i =0; i<optionList.size(); i++)
                { 
                    optionIdList.add(((RIssueTypeOption)optionList.get(i)).getOptionId());
                }
                Criteria c2 = new Criteria()
                    .add(RIssueTypeOptionPeer.ISSUE_TYPE_ID, getIssueTypeId())
                    .addIn(RIssueTypeOptionPeer.OPTION_ID, optionIdList);
                RIssueTypeOptionPeer.doDelete(c2);
            }
    }

}
