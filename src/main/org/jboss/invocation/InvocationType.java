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
   private static final ArrayList values = new ArrayList(4);

   public static final InvocationType REMOTE = 
         new InvocationType("REMOTE", false, false);
   public static final InvocationType LOCAL = 
         new InvocationType("LOCAL", false, true);
   public static final InvocationType HOME = 
         new InvocationType("HOME", true, false);
   public static final InvocationType LOCALHOME = 
         new InvocationType("LOCALHOME", true, true);

   private final transient String name;
   private final transient boolean isLocal;
   private final transient boolean isHome;

   // this is the only value serialized
   private final int ordinal;
 
   private InvocationType(String name, boolean isHome, boolean isLocal) {
      this.name = name;
      this.isLocal = isLocal;
      this.isHome = isHome;
      this.ordinal = nextOrdinal++;
      values.add(this);
   }

   public boolean isLocal()
   {
      return isLocal;
   }

   public boolean isHome()
   {
      return isHome;
   }

   public String toString() {
      return name;
   }

   Object readResolve() throws ObjectStreamException {
      return values.get(ordinal);
   }
}


