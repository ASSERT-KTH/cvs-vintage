package org.columba.mail.gui.composer;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextArea;

import org.columba.core.config.Config;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.AccountItem;
import org.columba.mail.gui.config.account.EditSignatureAction;

public class SignatureView extends JTextArea implements MouseListener,
		ItemListener, Observer {

	private ComposerController controller;

	private AccountItem item;

	public SignatureView(ComposerController controller) {
		super();

		this.controller = controller;

		controller.getAccountController().getView().addItemListener(this);

		setEditable(false);

		addMouseListener(this);
		
        Font font = FontProperties.getTextFont();
        setFont(font);

        XmlElement options = Config.getInstance().get("options").getElement("/options");
        XmlElement gui = options.getElement("gui");
        XmlElement fonts = gui.getElement("fonts");

        if (fonts == null) {
            fonts = gui.addSubElement("fonts");
        }

        // register interest on configuratin changes
        fonts.addObserver(this);
		
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {

			item = (AccountItem) controller.getAccountController().getView()
					.getSelectedItem();
			if (item.getIdentity().getSignature() != null) {
				StringBuffer strbuf = new StringBuffer();

				try {
					BufferedReader in = new BufferedReader(new FileReader(item
							.getIdentity().getSignature()));

					/*
					 * BufferedReader in = new BufferedReader( new
					 * InputStreamReader( new FileInputStream(file),
					 * model.getCharsetName()));
					 */
					String str;

					while ((str = in.readLine()) != null) {
						strbuf.append(str + "\n");
					}

					in.close();

					setText(strbuf.toString());
				} catch (IOException ex) {
					setText("");
				}
			} else {
				setText("");
			}
		}

	}

	public void mouseClicked(MouseEvent event) {
		if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0
				&& item != null) {
			if (event.getClickCount() >= 2) {
				new EditSignatureAction(controller, item).actionPerformed(null);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}
	
    /**
    *
    * @see org.columba.mail.gui.config.general.MailOptionsDialog
    *
    * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
    */
   public void update(Observable arg0, Object arg1) {
       Font font = FontProperties.getTextFont();
       setFont(font);
   }
	

}
