/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.lock;

/**
 * This interface is used to mark the methods of a subclass as a
 * non-entrant ejb invocation. The bean lock will let a call through
 * if the method is defined on a sub-interface of inplementatior of
 * this interface.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $$
 */
public interface NonentrantMessage {
}
