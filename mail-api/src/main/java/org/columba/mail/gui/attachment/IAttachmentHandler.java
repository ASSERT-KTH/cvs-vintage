package org.columba.mail.gui.attachment;

import org.columba.api.plugin.IExtensionInterface;


/**
 * Attachment handler is called when opening attachment.
 * <p>
 * Extensions should be registered as <code>IExtensionHandlerKeys.ORG_COLUMBA_ATTACHMENT_HANDLER</code>. 
 * 
 * @author frd
 */
public interface IAttachmentHandler extends IExtensionInterface {

	/**
	 * Method is called when user triggered the "Open Attachment" action. Columba
	 * automatically places the attachment in a temporary file, which is removed
	 * later on automatically. 
	 * <p>
	 * @param context						attachment context parameters
	 * @throws IllegalArgumentException		in case of invalid arguments
	 */
	public void execute(IAttachmentContext context) throws IllegalArgumentException;
}
