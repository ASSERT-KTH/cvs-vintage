package org.columba.mail.filter.plugins.colormessage;

import java.awt.Color;

import org.columba.core.command.Command;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.filter.plugins.AbstractFilterAction;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.ColumbaHeader;

/**
 * This Filter Action colors a message with a specified color.
 * The action retrieves the integer attribute "<b>rgb</b>" from
 * the filter action as the color to color the message with.
 * In order to keep the memory consumtion up, it creates the <code>Color</code>
 * object by using the <code>ColorFactory</code>.
 * The message is colored by setting the "<b>columba.color</b>" header.
 * 
 * @author redsolo
 */
public class ColorMessageFilterAction extends AbstractFilterAction {
	
	/** {@inheritDoc} */
	public Command getCommand(FilterAction filterAction, Folder srcFolder, Object[] uids) throws Exception {
		for (int i = 0; i < uids.length; i++) {
			ColumbaHeader headers = srcFolder.getMessageHeader(uids[i]);
			int rgb = filterAction.getInteger("rgb", Color.black.getRGB());
			headers.set("columba.color", ColorFactory.getColor(rgb));
		}
		return null;
	}
}