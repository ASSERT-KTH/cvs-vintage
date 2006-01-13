/******************************************************************************* * Copyright (c) 2006 IBM Corporation and others. * All rights reserved. This program and the accompanying materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: *     IBM Corporation - initial API and implementation ******************************************************************************/package org.eclipse.ui.internal.handlers;import java.util.Collection;import java.util.HashMap;import java.util.HashSet;import java.util.Iterator;import java.util.Map;import java.util.Set;import org.eclipse.core.commands.IHandler;import org.eclipse.core.expressions.Expression;import org.eclipse.core.expressions.IEvaluationContext;import org.eclipse.ui.ISourceProvider;import org.eclipse.ui.handlers.IHandlerActivation;import org.eclipse.ui.handlers.IHandlerService;import org.eclipse.ui.internal.expressions.AndExpression;/** * A handler service which delegates almost all responsibility to the parent * service. It is only responsible for disposing of locally activated handlers * when it is disposed. * <p> * This class is not intended for use outside of the * <code>org.eclipse.ui.workbench</code> plug-in. * </p> * <p> * <strong>EXPERIMENTAL</strong>. This class or interface has been added as * part of a work in progress. There is a guarantee neither that this API will * work nor that it will remain the same. Please do not use this API without * consulting with the Platform/UI team. * </p> *  * @since 3.2 */public class SlaveHandlerService implements IHandlerService {	/**	 * The default expression to use when	 * {@link #activateHandler(String, IHandler)} is called. Handlers	 * contributed using that method will only be active when this service is	 * active. However, this expression will be used for conflict resolution.	 */	protected final Expression defaultExpression;	/**	 * A map of the local activation to the parent activations. If this service	 * is inactive, then all parent activations are <code>null</code>.	 * Otherwise, they point to the corresponding activation in the parent	 * service.	 */	protected final Map localActivationsToParentActivations = new HashMap();	/**	 * The parent handler service to which all requests are ultimately routed.	 * This value is never <code>null</code>.	 */	protected final IHandlerService parent;	/**	 * The activations registered with the parent handler service. This value is	 * never <code>null</code>.	 */	protected final Set parentActivations = new HashSet();	/**	 * Constructs a new instance.	 * 	 * @param parentHandlerService	 *            The parent handler service for this slave; must not be	 *            <code>null</code>.	 * @param defaultExpression	 *            The default expression to use if none is provided. This is	 *            primarily used for conflict resolution. This value may be	 *            <code>null</code>.	 */	public SlaveHandlerService(final IHandlerService parentHandlerService,			final Expression defaultExpression) {		if (parentHandlerService == null) {			throw new NullPointerException(					"The parent handler service cannot be null"); //$NON-NLS-1$		}		this.defaultExpression = defaultExpression;		this.parent = parentHandlerService;	}	public final IHandlerActivation activateHandler(			final IHandlerActivation childActivation) {		final String commandId = childActivation.getCommandId();		final IHandler handler = childActivation.getHandler();		final Expression childExpression = childActivation.getExpression();		final AndExpression expression;		if (childExpression instanceof AndExpression) {			expression = (AndExpression) childExpression;		} else {			expression = new AndExpression();			if (childExpression != null) {				expression.add(childExpression);			}		}		if (defaultExpression != null) {			expression.add(defaultExpression);		}		final int depth = childActivation.getDepth() + 1;		final IHandlerActivation localActivation = new HandlerActivation(				commandId, handler, expression, depth, this);		return doActivation(localActivation);	}	public final IHandlerActivation activateHandler(final String commandId,			final IHandler handler) {		final IHandlerActivation localActivation = new HandlerActivation(				commandId, handler, defaultExpression,				IHandlerActivation.ROOT_DEPTH, this);		return doActivation(localActivation);	}	public final IHandlerActivation activateHandler(final String commandId,			final IHandler handler, final Expression expression) {		final IHandlerActivation activation = parent.activateHandler(commandId,				handler, expression);		parentActivations.add(activation);		return activation;	}	public final IHandlerActivation activateHandler(final String commandId,			final IHandler handler, final Expression expression,			final int sourcePriorities) {		return activateHandler(commandId, handler, expression);	}	public final void addSourceProvider(final ISourceProvider provider) {		parent.addSourceProvider(provider);	}	public final void deactivateHandler(final IHandlerActivation activation) {		final IHandlerActivation parentActivation;		if (localActivationsToParentActivations.containsKey(activation)) {			parentActivation = (IHandlerActivation) localActivationsToParentActivations					.remove(activation);		} else {			parentActivation = activation;		}		if (parentActivation != null) {			parent.deactivateHandler(parentActivation);			parentActivations.remove(parentActivation);		}	}	public final void deactivateHandlers(final Collection activations) {		final Iterator activationItr = activations.iterator();		while (activationItr.hasNext()) {			final IHandlerActivation activation = (IHandlerActivation) activationItr					.next();			deactivateHandler(activation);		}	}	public final void dispose() {		parent.deactivateHandlers(parentActivations);		parentActivations.clear();		localActivationsToParentActivations.clear();	}	protected IHandlerActivation doActivation(			final IHandlerActivation localActivation) {		final IHandlerActivation parentActivation;		parentActivation = parent.activateHandler(localActivation);		parentActivations.add(parentActivation);		localActivationsToParentActivations.put(localActivation,				parentActivation);		return localActivation;	}	public final IEvaluationContext getCurrentState() {		return parent.getCurrentState();	}	public final void readRegistry() {		parent.readRegistry();	}	public final void removeSourceProvider(final ISourceProvider provider) {		parent.removeSourceProvider(provider);	}}