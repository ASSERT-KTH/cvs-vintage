/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import org.w3c.dom.Element;
import org.jboss.metadata.MetaData;
import org.jboss.deployment.DeploymentException;

import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Represents
 *    <left-join cmr-field="lineItems">
 *       <left-join cmr-field="product" eager-load-group="product"/>
 *    </left-join>
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public final class JDBCLeftJoinMetaData
{
   private final String cmrField;
   private final String eagerLoadGroup;
   private final List leftJoinList;

   public static List readLeftJoinList(Iterator leftJoinIterator)
      throws DeploymentException
   {
      List leftJoinList;
      if(leftJoinIterator.hasNext())
      {
         leftJoinList = new ArrayList();
         while(leftJoinIterator.hasNext())
         {
            Element leftJoinElement = (Element)leftJoinIterator.next();
            JDBCLeftJoinMetaData leftJoin = new JDBCLeftJoinMetaData(leftJoinElement);
            leftJoinList.add(leftJoin);
         }
      }
      else
      {
         leftJoinList = Collections.EMPTY_LIST;
      }
      return leftJoinList;
   }

   /**
    * Used only from the testsuite.
    */ 
   public JDBCLeftJoinMetaData(String cmrField, String eagerLoadGroup, List leftJoinList)
   {
      this.cmrField = cmrField;
      this.eagerLoadGroup = eagerLoadGroup;
      this.leftJoinList = leftJoinList;
   }

   public JDBCLeftJoinMetaData(Element element) throws DeploymentException
   {
      cmrField = element.getAttribute("cmr-field");
      if(cmrField == null || cmrField.trim().length() == 0)
      {
         throw new DeploymentException("left-join MUST have non-empty cmr-field attribute.");
      }

      String eagerLoadGroup = element.getAttribute("eager-load-group");
      if(eagerLoadGroup == null || eagerLoadGroup.trim().length() == 0)
      {
         this.eagerLoadGroup = "*";
      }
      else
      {
         this.eagerLoadGroup = eagerLoadGroup;
      }

      Iterator leftJoinIterator = MetaData.getChildrenByTagName(element, "left-join");
      leftJoinList = readLeftJoinList(leftJoinIterator);
   }

   public String getCmrField()
   {
      return cmrField;
   }

   public String getEagerLoadGroup()
   {
      return eagerLoadGroup;
   }

   public Iterator getLeftJoins()
   {
      return leftJoinList.iterator();
   }

   public boolean equals(Object o)
   {
      boolean result;
      if(o == this)
      {
         result = true;
      }
      else if(o instanceof JDBCLeftJoinMetaData)
      {
         JDBCLeftJoinMetaData other = (JDBCLeftJoinMetaData)o;
         result =
            (cmrField == null ? other.cmrField == null : cmrField.equals(other.cmrField)) &&
            (eagerLoadGroup == null ? other.eagerLoadGroup == null : eagerLoadGroup.equals(other.eagerLoadGroup)) &&
            (leftJoinList == null ? other.leftJoinList == null : leftJoinList.equals(other.leftJoinList));
      }
      else
      {
         result = false;
      }
      return result;
   }

   public int hashCode()
   {
      int result = Integer.MIN_VALUE;
      result += (cmrField == null ? 0 : cmrField.hashCode());
      result += (eagerLoadGroup == null ? 0 : eagerLoadGroup.hashCode());
      result += (leftJoinList == null ? 0 : leftJoinList.hashCode());
      return result;
   }

   public String toString()
   {
      return "[cmr-field=" + cmrField + ", eager-load-group=" + eagerLoadGroup + ", left-join=" + leftJoinList + ']';
   }
}
