// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.command;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.text.MessageFormat;

import org.columba.core.gui.util.ErrorDialog;
import org.columba.core.gui.util.ExceptionDialog;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.imap.IMAPDisconnectedException;
import org.columba.ristretto.imap.IMAPException;

/**
 * Handles all exceptions catched by Worker.construct(). Opens error dialogs.
 * 
 * @see Worker
 * @author fdietz
 */
public class ExceptionHandler {

    /**
     * Handle all kinds of exceptions.
     * 
     * @param e
     *            exception to process
     */
    public void processException(Exception e) {
        if (e instanceof SocketException) {
            processSocketException((SocketException) e);
        } else if (e instanceof IOException) {
            processIOException((IOException) e);
        } else if ( e instanceof IMAPException ) {
        	processIMAPExcpetion((IMAPException) e);
        }
        else {
            // unknown exception - this is most likely a Columba-specific bug
            e.printStackTrace();

            // show error dialog, with exception message and stack-trace
            // -> dialog also provides a button for the user to easily
            // -> report a bug
            new ExceptionDialog(e);
        }
    }

    /**
	 * @param exception
	 */
	private void processIMAPExcpetion(IMAPException exception) {
		String errorMessage = "";
		String serverResponse = "";
		
		if( exception.getResponse() != null ) {
			serverResponse = ": " + exception.getResponse().getResponseMessage();
		}
		
		if( exception instanceof IMAPDisconnectedException ) {
			errorMessage = MailResourceLoader.getString("dialog", "error",
					"imap_disconnected_error") + serverResponse;
		} else {
			errorMessage = MailResourceLoader.getString("dialog", "error",
			"imap_error") + serverResponse;
		}
        
		showErrorDialog(errorMessage, exception);
	}

	/**
     * Handle all java.net.SocketException
     * 
     * @param e
     *            a socket exception
     */
    private void processSocketException(SocketException e) {
        String errorMessage = "";

        if (e instanceof BindException) {
            errorMessage = MailResourceLoader.getString("dialog", "error",
                    "bind_error");
        } else if (e instanceof ConnectException) {
            errorMessage = MailResourceLoader.getString("dialog", "error",
                    "connect_error");
        } else if (e instanceof NoRouteToHostException) {
            errorMessage = MailResourceLoader.getString("dialog", "error",
                    "no_route_to_host_error");
        } else if (e instanceof PortUnreachableException) {
            errorMessage = MailResourceLoader.getString("dialog", "error",
                    "port_unreachable_error");
        }

        showErrorDialog(errorMessage, e);
    }

    /**
     * Handle all java.io.IOExceptions
     * 
     * @param e
     *            io exception to process
     */
    private void processIOException(IOException e) {
        String errorMessage = "";

        if (e instanceof ProtocolException) {
            errorMessage = MailResourceLoader.getString("dialog", "error",
                    "protocol_error");
        } else if (e instanceof SocketException) {
            errorMessage = MailResourceLoader.getString("dialog", "error",
                    "socket_error");
        } else if (e instanceof SocketTimeoutException) {
            errorMessage = MailResourceLoader.getString("dialog", "error",
                    "socket_timeout_error");
        } else if (e instanceof UnknownHostException) {
            errorMessage = MessageFormat.format(
            		MailResourceLoader.getString("dialog", "error",
                    "unknown_host_error"), new Object[] { e.getMessage()});
        } else if (e instanceof UnknownServiceException) {
            errorMessage = MailResourceLoader.getString("dialog", "error",
                    "unknown_service_error");
        }
        
        showErrorDialog(errorMessage, e);
    }

    /**
     * Show error dialog.
     * 
     * @param errorMessage
     *            human-readable error message
     * @param e
     *            exception to process
     */
    private void showErrorDialog(String details, Exception e) {
    	new ErrorDialog(details, e);
    }
}