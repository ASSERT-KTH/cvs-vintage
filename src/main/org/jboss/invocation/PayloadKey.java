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
 * Type safe enumeration used for to identify the payloads.
 */
public final class PayloadKey implements Serializable {
   // these fields are used for serialization
   private static int nextOrdinal = 0;
   private static final ArrayList values = new ArrayList(3);

   /** Put me in the transient map, not part of payload. */
   public final static PayloadKey TRANSIENT = new PayloadKey("TRANSIENT");
   
   /** Do not serialize me, part of payload as is. */
   public final static PayloadKey AS_IS = new PayloadKey("AS_IS");

   /** Put me in the payload map. */
   public final static PayloadKey PAYLOAD = new PayloadKey("PAYLOAD");

   private final transient String name;

   // this is the only value serialized
   private final int ordinal;
 
   private PayloadKey(String name) {
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


