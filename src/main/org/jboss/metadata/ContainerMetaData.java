/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.Set;

/**
 * The metadata for a bean's Container.  In general, each bean may have
 * its own ContainerMetaData, or multiple beans may share a single
 * ContainerMetaData.  It depends on the configuration and container
 * implementation.
 */
public interface ContainerMetaData extends MetaData {
}