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
package org.columba.mail.pgp;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.config.PGPItem;
import org.columba.mail.main.MailInterface;
import org.waffel.jscf.JSCFConnection;
import org.waffel.jscf.JSCFDriverManager;
import org.waffel.jscf.JSCFException;
import org.waffel.jscf.gpg.GPGDriver;


/**
 * @author waffel
 * The <code>JSCFController</code> controls JSCFDrivers and Connections. It chaches for each account the connection to 
 * JSCFDrivers. The <code>JSCFController</code> uses the "singleton pattern", which mean, that you should access it, using
 * the <code>getInstcane</code> method.
 */
public class JSCFController {

    private static JSCFController myInstance = null;
    private static Map connectionMap;
    
    /**
     * Gives a instance of the <code>JSCFController</code> back. If no instance was created before, a new instance will be created.
     * @return A Instance of <code>JSCFController</code>
     */
    public static JSCFController getInstance() {
        if (myInstance == null) {
            myInstance = new JSCFController();
            registerDrivers();
            connectionMap = new Hashtable();
        }
        return myInstance;
    }
    
    private static void registerDrivers() {
        try {
            // at the moment we are only supporting gpg. So let us code hard here the gpg driver
            JSCFDriverManager.registerJSCFDriver(new GPGDriver());
        } catch (JSCFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a new Connection to the gpg driver, if the connection for the given <code>userID</code> are not exists. Properties
     * for the connection are created by using the PGPItem from the <code>AccountItem</code>. Properties like PATH and the
     * GPG USERID are stored for the connection, if the connection are not exists. 
     * @param userID UserID from which the connection should give back
     * @return a alrady etablished connection for this user or a newly created connection for this userID, if no connection exists for
     * the userID
     * @throws JSCFException If there are several Driver problems
     */
    public JSCFConnection getConnection(String userID) throws JSCFException {
        PGPItem pgpItem = MailInterface.config.getAccountList().getDefaultAccount().getPGPItem();
        JSCFConnection con = (JSCFConnection) connectionMap.get(userID);
        if (con == null) {
            // let us hard coding the gpg for each connection. Later we should support also other variants (like smime)
            con = JSCFDriverManager.getConnection("jscf:gpg:");
            Properties props = con.getProperties();
            props.put("PATH", pgpItem.get("path"));
            ColumbaLogger.log.info("gpg path: "+props.get("PATH"));
            props.put("USERID", pgpItem.get("id"));
            ColumbaLogger.log.info("current gpg userID: "+props.get("USERID"));
            con.setProperties(props);
            connectionMap.put(userID, con);
        }
        return con;
    }
    
    /**
     * Creates a new JSCFConnection for the current used Account. The current used Account is determind from the AccountItem.
     * This method calls only {@link #getConnection(String)} with the <code>id</code> from the PGPItem.
     * @return  a alrady etablished connection for the current account or a newly created connection for the current account, if no 
     * connection exists for the current account
     * @throws JSCFException If there are several Driver problems
     */
    public JSCFConnection getConnection() throws JSCFException {
        PGPItem pgpItem = MailInterface.config.getAccountList().getDefaultAccount().getPGPItem();
        return getConnection(pgpItem.get("id"));
    }
    
}
