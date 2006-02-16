/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.fieldassist;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * ContentAssistCommandAdapter extends {@link ContentProposalAdapter} to invoke
 * content proposals using a specified {@link org.eclipse.ui.commands.ICommand}. The ability to specify
 * a {@link org.eclipse.jface.bindings.keys.KeyStroke} that explicitly invokes content proposals is hidden by
 * this class, and instead the String id of a command is used. If no command id
 * is specified by the client, then the default workbench content assist command
 * is used.
 * <p>
 * This class is not intended to be subclassed.
 * 
 * @since 3.2
 */
public class ContentAssistCommandAdapter extends ContentProposalAdapter {

	private String commandId;

	/**
	 * The command id used for content assist. (value
	 * <code>"org.eclipse.ui.edit.text.contentAssist.proposals"</code>)
	 */
	public static final String CONTENT_PROPOSAL_COMMAND = "org.eclipse.ui.edit.text.contentAssist.proposals"; //$NON-NLS-1$

	private IHandlerService handlerService;

	private IHandlerActivation activeHandler;

	private IHandler proposalHandler = new AbstractHandler() {
		public Object execute(ExecutionEvent event) {
			openProposalPopup();
			return null;
		}

	};

	/**
	 * Construct a content proposal adapter that can assist the user with
	 * choosing content for the field.
	 * 
	 * @param control
	 *            the control for which the adapter is providing content assist.
	 *            May not be <code>null</code>.
	 * @param controlContentAdapter
	 *            the <code>IControlContentAdapter</code> used to obtain and
	 *            update the control's contents as proposals are accepted. May
	 *            not be <code>null</code>.
	 * @param proposalProvider
	 *            the <code>IContentProposalProvider</code> used to obtain
	 *            content proposals for this control, or <code>null</code> if
	 *            no content proposal is available.
	 * @param commandId
	 *            the String id of the command that will invoke the content
	 *            assistant. If not supplied, the default value will be
	 *            "org.eclipse.ui.edit.text.contentAssist.proposals".
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. If specified, these characters will trigger
	 *            auto-activation of the proposal popup, regardless of the
	 *            specified command id.
	 */
	public ContentAssistCommandAdapter(Control control,
			IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider,
			String commandId,
			char[] autoActivationCharacters) {
		super(control, controlContentAdapter, proposalProvider,
				null, autoActivationCharacters);
		this.commandId = commandId;
		if (commandId == null)
			this.commandId = CONTENT_PROPOSAL_COMMAND;

		// If no autoactivation characters were specified, set them to the empty
		// array
		// so that we don't get the alphanumeric auto-trigger of our superclass.
		if (autoActivationCharacters == null) {
			this.setAutoActivationCharacters(new char[] {});
		}

		// Add listeners to the control to manage activation of the handler
		addListeners(control);

		// Cache the handler service so we don't have to retrieve it each time
		this.handlerService = (IHandlerService) PlatformUI.getWorkbench()
				.getAdapter(IHandlerService.class);
	}

	/*
	 * Add the listeners needed in order to activate the content assist command
	 * on the control.
	 */
	private void addListeners(Control control) {
		control.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				if (activeHandler != null) {
					handlerService.deactivateHandler(activeHandler);
					activeHandler = null;
				}
			}

			public void focusGained(FocusEvent e) {
				if (isEnabled()) {
					if (activeHandler == null) {
						activeHandler = handlerService.activateHandler(
								commandId, proposalHandler);
					}
				} else {
					if (activeHandler != null) {
						handlerService.deactivateHandler(activeHandler);
					}
					activeHandler = null;
				}
			}
		});
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (activeHandler != null) {
					handlerService.deactivateHandler(activeHandler);
					activeHandler = null;
				}

			}
		});
	}

	/**
	 * Return the string command ID of the command used to invoke content
	 * assist.
	 * 
	 * @return the command ID of the command that invokes content assist.
	 */
	public String getCommandId() {
		return commandId;
	}
}
