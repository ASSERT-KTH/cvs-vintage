package org.tigris.scarab.om;

import org.apache.torque.om.NumberKey;

import org.apache.torque.om.UnsecurePersistent;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class IssueType 
    extends org.tigris.scarab.om.BaseIssueType
    implements UnsecurePersistent
{

    public static final NumberKey ISSUE__PK = new NumberKey("1");
    public static final NumberKey USER_TEMPLATE__PK = new NumberKey("2");
    public static final NumberKey GLOBAL_TEMPLATE__PK = new NumberKey("3");
}
