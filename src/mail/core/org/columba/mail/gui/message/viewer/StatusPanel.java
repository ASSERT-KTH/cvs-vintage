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
package org.columba.mail.gui.message.viewer;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.message.filter.SecurityStatusEvent;
import org.columba.mail.gui.message.filter.SecurityStatusListener;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author fdietz
 *  
 */
public class StatusPanel extends JPanel implements SecurityStatusListener {

    private ImageIcon attachment = ImageLoader.getImageIcon("stock_attach.png");

    private JLabel attachmentLabel;

    private JLabel decryptionLabel;

    public StatusPanel() {
        setLayout(new FlowLayout());

        attachmentLabel = new JLabel();

        decryptionLabel = new JLabel();

        add(attachmentLabel);
        add(decryptionLabel);
    }

    public void updateUI() {
        super.updateUI();

        //      lightgray background
        setBackground(new Color(235, 235, 235));
    }

    public void setStatus(boolean hasAttachment) {
        if (hasAttachment) {
            attachmentLabel.setIcon(attachment);

        } else {
            attachmentLabel.setIcon(null);

        }
    }

    /**
     * @see org.columba.mail.gui.message.filter.SecurityStatusListener#statusUpdate(org.columba.mail.gui.message.filter.SecurityStatusEvent)
     */
    public void statusUpdate(SecurityStatusEvent event) {

        int status = event.getStatus();

        switch (status) {
        case SecurityInformationController.DECRYPTION_SUCCESS:
            {
                decryptionLabel.setIcon(ImageLoader
                        .getImageIcon("pgp-signature-ok-24.png"));
                decryptionLabel.setToolTipText(MailResourceLoader.getString(
                        "menu", "mainframe", "security_decrypt_success"));

                break;
            }

        case SecurityInformationController.DECRYPTION_FAILURE:
            {
                decryptionLabel.setIcon(ImageLoader
                        .getImageIcon("pgp-signature-bad-24.png"));
                decryptionLabel.setToolTipText(MailResourceLoader.getString(
                        "menu", "mainframe", "security_encrypt_fail"));

                break;
            }

        case SecurityInformationController.VERIFICATION_SUCCESS:
            {
                decryptionLabel.setIcon(ImageLoader
                        .getImageIcon("pgp-signature-ok-24.png"));
                decryptionLabel.setToolTipText(MailResourceLoader.getString(
                        "menu", "mainframe", "security_verify_success"));

                break;
            }

        case SecurityInformationController.VERIFICATION_FAILURE:
            {
                decryptionLabel.setIcon(ImageLoader
                        .getImageIcon("pgp-signature-bad-24.png"));
                decryptionLabel.setToolTipText(MailResourceLoader.getString(
                        "menu", "mainframe", "security_verify_fail"));

                break;
            }

        case SecurityInformationController.NO_KEY:
            {
                decryptionLabel.setIcon(ImageLoader
                        .getImageIcon("pgp-signature-nokey-24.png"));
                decryptionLabel.setToolTipText(MailResourceLoader.getString(
                        "menu", "mainframe", "security_verify_nokey"));

                break;
            }
        case SecurityInformationController.NOOP:
            {
                decryptionLabel.setIcon(null);
                decryptionLabel.setToolTipText("");
                break;
            }
        }
    }
}
