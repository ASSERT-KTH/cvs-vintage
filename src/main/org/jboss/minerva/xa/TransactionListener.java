/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.xa;

/**
 * Callback for notification when a transaction is finished.
 * @version $Revision: 1.1 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public interface TransactionListener {
    /**
     * Indicates that the transaction this instance was part of has finished.
     */
    public void transactionFinished(XAConnectionImpl con);
}