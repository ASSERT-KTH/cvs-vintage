//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.config.account;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.columba.core.config.Config;
import org.columba.mail.config.AccountList;
import org.columba.mail.gui.config.account.util.AccountHeaderRenderer;
import org.columba.mail.gui.config.account.util.NameRenderer;
import org.columba.mail.gui.config.account.util.StringAccountRenderer;
import org.columba.mail.util.MailResourceLoader;
import org.columba.core.main.MainInterface;

class AccountListTable extends JTable
{
    private AccountListDataModel model;
    private Config config;
    private AccountList AccountList;

    public AccountListTable(AccountList accountList, ConfigFrame frame)
    {
        super();

	this.AccountList = accountList;
        config = MainInterface.config;

        setSelectionMode( ListSelectionModel.SINGLE_SELECTION );


        model = new AccountListDataModel( AccountList );
        //update();

        setModel( model );

        setShowGrid(false);
        setIntercellSpacing(new java.awt.Dimension(0, 0));
		

        TableColumn tc = getColumn( MailResourceLoader.getString("dialog","account", "accountname") ); //$NON-NLS-1$
        tc.setCellRenderer( new NameRenderer() );
        tc.setHeaderRenderer( new AccountHeaderRenderer( MailResourceLoader.getString("dialog","account", "list_accountname") ) ); //$NON-NLS-1$
	

        tc = getColumn( MailResourceLoader.getString("dialog","account", "type") ); //$NON-NLS-1$
        tc.setMaxWidth(100);
        tc.setMinWidth(100);
        tc.setCellRenderer( new StringAccountRenderer(true) );
        tc.setHeaderRenderer( new AccountHeaderRenderer( MailResourceLoader.getString("dialog","account", "type") ) ); //$NON-NLS-1$


        sizeColumnsToFit( AUTO_RESIZE_NEXT_COLUMN );
    }

    public void update()
    {
        model.fireTableDataChanged();

    }



}


