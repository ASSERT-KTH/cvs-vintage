/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/


package org.jboss.cmp.query;

public class UnmappedEntryException extends Exception
{
   private Object entry;

   public UnmappedEntryException(String s, Object entry)
   {
      super(s);
      this.entry = entry;
   }

   public Object getEntry()
   {
      return entry;
   }
}
