package org.tigris.scarab.om;

import org.apache.torque.om.NumberKey;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class TransactionTypePeer 
    extends org.tigris.scarab.om.BaseTransactionTypePeer
{
    public static final NumberKey CREATE_ISSUE__PK = new NumberKey("1");
    public static final NumberKey EDIT_ISSUE__PK = new NumberKey("2");
    public static final NumberKey MOVE_ISSUE__PK = new NumberKey("3");
    public static final NumberKey RETOTAL_ISSUE_VOTE__PK = new NumberKey("4");
}
