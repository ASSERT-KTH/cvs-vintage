/******************************************************************************* * Copyright (c) 2006 IBM Corporation and others. * All rights reserved. This program and the accompanying materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: *     IBM Corporation - initial API and implementation ******************************************************************************/package org.eclipse.ui.services;/** * <p> * A component with which one or more services are registered. The services can * be retrieved from this locator using some key -- typically the class * representing the interface the service must implement. For example: * </p> *  * <pre> * IHandlerService service = (IHandlerService) workbenchWindow * 		.getService(IHandlerService.class); * </pre> *  * <p> * This interface is not to be implemented or extended by clients. * </p> * <p> * <strong>EXPERIMENTAL</strong>. This class or interface has been added as * part of a work in progress. There is a guarantee neither that this API will * work nor that it will remain the same. Please do not use this API without * consulting with the Platform/UI team. * </p> *  * @since 3.2 */public interface IServiceLocator {	/**	 * Retrieves the service corresponding to the given key.	 * 	 * @param key	 *            The key for the service to retrieve; should not be	 *            <code>null</code>.	 * @return The service, or <code>null</code> if no such service could be	 *         found.	 */	public Object getService(Object key);	/**	 * Whether this service exists within the scope of this service locator or	 * one of its parents.	 * 	 * @param key	 *            The key to look up; must not be <code>null</code>.	 * @return <code>true</code> iff the service locator can find a service	 *         for the given key; <code>false</code> otherwise.	 */	public boolean hasService(Object key);}