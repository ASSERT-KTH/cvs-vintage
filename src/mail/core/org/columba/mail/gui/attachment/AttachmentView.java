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
package org.columba.mail.gui.attachment;

import org.columba.mail.gui.attachment.util.AttachmentImageIconLoader;
import org.columba.mail.gui.attachment.util.IconPanel;

import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.message.StreamableMimePart;

import java.util.List;

import javax.swing.ImageIcon;


/**
 * @author freddy
 */
public class AttachmentView extends IconPanel {
    private AttachmentModel model;

    public AttachmentView(AttachmentModel model) {
        super();
        this.model = model;
    }

    public AttachmentModel getModel() {
        return model;
    }

    public StreamableMimePart getSelectedMimePart() {
        return (StreamableMimePart) model.getDisplayedMimeParts().get(getSelected());
    }

    public boolean setMimePartTree(MimeTree collection) {
        String contentType;
        String contentSubtype;
        String text = null;
        boolean output = false;

        removeAll();

        model.setCollection(collection);

        List displayedMimeParts = model.getDisplayedMimeParts();

        // Display resulting MimeParts
        for (int i = 0; i < displayedMimeParts.size(); i++) {
            StreamableMimePart mp = (StreamableMimePart) displayedMimeParts.get(i);

            MimeHeader header = mp.getHeader();
            MimeType type = header.getMimeType();

            contentType = type.getType();
            contentSubtype = type.getSubtype();

            //Get Text for Icon
            if (header.getFileName() != null) {
                text = header.getFileName();
            } else {
                text = contentType + "/" + contentSubtype;
            }

            //Get Tooltip for Icon
            StringBuffer tooltip = new StringBuffer();
            tooltip.append("<html><body>");

            if (header.getFileName() != null) {
                tooltip.append(header.getFileName());
                tooltip.append(" - ");
            }

            tooltip.append("<i>");

            if (header.getContentDescription() != null) {
                tooltip.append(header.getContentDescription());
            } else {
                tooltip.append(contentType);
                tooltip.append("/");
                tooltip.append(contentSubtype);
            }

            tooltip.append("</i></body></html>");

            ImageIcon icon = null;

            icon = AttachmentImageIconLoader.getImageIcon(type.getType(),
                    type.getSubtype());

            add(icon, text, tooltip.toString());
            output = true;
        }

        return output;
    }
}
