package org.columba.mail.gui.composer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.AccountList;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.PGPItem;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AccountController implements ItemListener {
	AccountView view;
	ComposerModel model;
	ComposerInterface composerInterface;

	JCheckBoxMenuItem signMenuItem;
	JCheckBoxMenuItem encryptMenuItem;

	public AccountController(ComposerInterface ci, ComposerModel model) {
		this.model = model;
		this.composerInterface = ci;

		view = new AccountView(model);
		
		AccountList config = MailConfig.getAccountList();

		for (int i = 0; i < config.count(); i++) {
			view.addItem(config.get(i));
			if (i == 0) {
				view.setSelectedItem(config.get(i));
				composerInterface.identityInfoPanel.set(config.get(i));
			}
		}

		view.addItemListener(this);
	}

	public void setSecurityMenuItems(
		JCheckBoxMenuItem signItem,
		JCheckBoxMenuItem encryptItem) {
		signMenuItem = signItem;
		encryptMenuItem = encryptItem;

		AccountItem item = (AccountItem) view.getSelectedItem();

		PGPItem pgpItem = item.getPGPItem();
		if( pgpItem.getEnabled() ) {
			signMenuItem.setEnabled(true);
			encryptMenuItem.setEnabled(true);
			
			model.setSignMessage(pgpItem.getAlwaysSign());
			model.setEncryptMessage(pgpItem.getAlwaysEncrypt());
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			AccountItem item = (AccountItem) view.getSelectedItem();
			composerInterface.identityInfoPanel.set(item);

			PGPItem pgpItem = item.getPGPItem();
			signMenuItem.setEnabled(pgpItem.getEnabled());
			signMenuItem.setSelected(pgpItem.getAlwaysSign());

			encryptMenuItem.setEnabled(pgpItem.getEnabled());
			encryptMenuItem.setSelected(pgpItem.getAlwaysEncrypt());
		}
	}

	public void updateComponents(boolean b) {
		if (b == true) {
			view.setSelectedItem(model.getAccountItem());
			encryptMenuItem.setSelected(model.isEncryptMessage());
			signMenuItem.setSelected(model.isSignMessage());
		} else {
			model.setAccountItem((AccountItem) view.getSelectedItem());
			model.setSignMessage(signMenuItem.isSelected());
			model.setEncryptMessage(encryptMenuItem.isSelected());
		}
	}

}
