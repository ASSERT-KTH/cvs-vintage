/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.schema;

/**
 * Interface provding access to properties of core data types.
 */
public interface AbstractType
{
   public static final int UNKNOWN = 0;
   public static final int OBJECT = 1;
   public static final int BOOLEAN = 2;
   public static final int STRING = 3;
   public static final int INTEGER = 4;
   public static final int DOUBLE = 5;
   public static final int DECIMAL = 6;
   public static final int DATETIME = 7;
   public static final int BINARY = 8;

   /**
    * Return the name of this type.
    * @return the name of this type
    */
   public String getName();

   /**
    * Return the family for this type
    * @return the famility for this type
    */
   public int getFamily();
}
