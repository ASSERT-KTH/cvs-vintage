/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

// $Id: AllowedOperationsFlags.java,v 1.1 2004/04/15 14:30:27 tdiesler Exp $

import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Constants used by the AllowedOperationsAssociation
 *
 * According to the EJB2.1 spec not all context methods can be accessed at all times
 * For example ctx.getPrimaryKey() should throw an IllegalStateException when called from within ejbCreate()
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public interface AllowedOperationsFlags
{
   // Constants -----------------------------------------------------

   /**
    * These constants are used to validate method access
    */
   public static final int NOT_ALLOWED = 0;
   public static final int IN_INTERCEPTOR_METHOD = (int) Math.pow(2, 0);
   public static final int IN_EJB_ACTIVATE = (int) Math.pow(2, 1);
   public static final int IN_EJB_PASSIVATE = (int) Math.pow(2, 2);
   public static final int IN_EJB_REMOVE = (int) Math.pow(2, 3);
   public static final int IN_EJB_CREATE = (int) Math.pow(2, 4);
   public static final int IN_EJB_POST_CREATE = (int) Math.pow(2, 5);
   public static final int IN_EJB_FIND = (int) Math.pow(2, 6);
   public static final int IN_EJB_HOME = (int) Math.pow(2, 7);
   public static final int IN_EJB_TIMEOUT = (int) Math.pow(2, 8);
   public static final int IN_EJB_LOAD = (int) Math.pow(2, 9);
   public static final int IN_EJB_STORE = (int) Math.pow(2, 10);
   public static final int IN_SET_ENTITY_CONTEXT = (int) Math.pow(2, 11);
   public static final int IN_UNSET_ENTITY_CONTEXT = (int) Math.pow(2, 12);
   public static final int IN_SET_SESSION_CONTEXT = (int) Math.pow(2, 13);
   public static final int IN_SET_MESSAGE_DRIVEN_CONTEXT = (int) Math.pow(2, 14);
   public static final int IN_AFTER_BEGIN = (int) Math.pow(2, 15);
   public static final int IN_BEFORE_COMPLETION = (int) Math.pow(2, 16);
   public static final int IN_AFTER_COMPLETION = (int) Math.pow(2, 17);
   public static final int IN_BUSINESS_METHOD = (int) Math.pow(2, 18);
   public static final int IN_SERVICE_ENDPOINT_METHOD = (int) Math.pow(2, 19);
}
