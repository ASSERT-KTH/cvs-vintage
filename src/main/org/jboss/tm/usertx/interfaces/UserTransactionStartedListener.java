
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.tm.usertx.interfaces;



import java.util.EventListener;
import javax.transaction.SystemException;


/**
 * UserTransactionStartedListener.java
 *
 *
 * Created: Sun Nov 24 14:10:00 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public interface UserTransactionStartedListener extends EventListener 
{
   void userTransactionStarted() throws SystemException;
}
                                                       
