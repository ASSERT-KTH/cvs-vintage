/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.bridge;

public interface CMRFieldBridge extends FieldBridge
{
   public boolean isSingleValued();

   public EntityBridge getRelatedEntity();
}
