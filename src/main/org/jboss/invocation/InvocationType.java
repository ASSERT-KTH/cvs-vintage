/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation;

import java.io.Serializable;
import java.io.ObjectStreamException;

import java.util.ArrayList;

/**
 * Type safe enumeration used for to identify the invocation types.
 */
public final class InvocationType implements Serializable {
   // these fields are used for serialization
   private static int nextOrdinal = 0;
   private static final ArrayList values = new ArrayList(8);

   public static final InvocationType REMOTE = 
         new InvocationType("REMOTE");
   public static final InvocationType LOCAL = 
         new InvocationType("LOCAL");
   public static final InvocationType HOME = 
         new InvocationType("HOME");
   public static final InvocationType LOCALHOME = 
         new InvocationType("LOCALHOME");
   public static final InvocationType GETHOME = 
         new InvocationType("GETHOME");
   public static final InvocationType GETREMOTE = 
         new InvocationType("GETREMOTE");
   public static final InvocationType GETLOCALHOME = 
         new InvocationType("GETLOCALHOME");
   public static final InvocationType GETLOCAL = 
         new InvocationType("GETLOCAL");

   private final transient String name;

   // this is the only value serialized
   private final int ordinal;
 
   private InvocationType(String name) {
      this.name = name;
      this.ordinal = nextOrdinal++;
      values.add(this);
   }

   public String toString() {
      return name;
   }

   Object readResolve() throws ObjectStreamException {
      return values.get(ordinal);
   }
}


