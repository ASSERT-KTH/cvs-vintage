// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.util.EventListener;

/**
 * An interface to the asynchronous resolver.
 * @see Resolver
 *
 * @author Brian Wellington
 */

public interface ResolverListener extends EventListener {

/**
 * The callback used by an asynchronous resolver
 * @param id The identifier returned by Resolver.sendAsync()
 * @param m The response message as returned by the Resolver
 */
public void receiveMessage(Object id, Message m);

/**
 * The callback used by an asynchronous resolver when an exception is thrown
 * @param id The identifier returned by Resolver.sendAsync()
 * @param e The thrown exception
 */
public void handleException(Object id, Exception e);

}
