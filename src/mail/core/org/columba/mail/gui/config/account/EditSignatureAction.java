package org.columba.mail.gui.config.account;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.Identity;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;


public class EditSignatureAction extends AbstractColumbaAction implements Observer, ItemListener {

	private Identity identity;
	private ComposerController composerController;
	
	public EditSignatureAction(IFrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString(
                "dialog", "account", "editsignature"));
		
		composerController = (ComposerController) getFrameMediator();
		

		composerController.getAccountController().getView().addItemListener(this);

		identity = composerController.getModel().getAccountItem().getIdentity();
		
		setEnabled(identity.getSignature() != null && ColumbaDesktop.getInstance().supportsOpen());		
		identity.addObserver(this);
	}

	public EditSignatureAction(IFrameMediator frameMediator, AccountItem item) {
		super(frameMediator, MailResourceLoader.getString(
                "dialog", "account", "editsignature"));
		
		identity = item.getIdentity();
	}
	
	public void actionPerformed(ActionEvent e) {		
		File signature = identity.getSignature();
		if( signature == null) {
			//Create the file
			signature = new File(System.getProperty("user.home"),".signature");
		}
		
		ColumbaDesktop.getInstance().openAndWait(signature);
		
		// update signature -> will notify all listeners that signature changed
		identity.setSignature(signature);
	}

	public void update(Observable o, Object arg) {
		setEnabled(arg != null && ColumbaDesktop.getInstance().supportsOpen() );
	}

	public void itemStateChanged(ItemEvent e) {
		identity.removeObserver(this);

        if (e.getStateChange() == ItemEvent.SELECTED) {
            AccountItem item = (AccountItem) composerController.getAccountController().getView().getSelectedItem();
    		identity = item.getIdentity();
    		
    		setEnabled(identity.getSignature() != null && ColumbaDesktop.getInstance().supportsOpen());		
    		identity.addObserver(this);
        }
		
	}

}
