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

package org.columba.mail.config;

import java.io.File;

import org.columba.core.xml.XmlElement;

import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;

/**
 * Encapsulates an identity used with an account.
 */
public class Identity {
    protected XmlElement e;
    protected Address address;
    protected Address replyToAddress;
    
    public Identity(XmlElement e) throws ParserException {
        this.e = e;
        address = Address.parse(e.getAttribute("address"));
        address.setDisplayName(e.getAttribute("name"));
        replyToAddress = Address.parse(e.getAttribute("reply_address"));
    }
    
    public Address getAddress() {
        return address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
        e.addAttribute("name", address.getDisplayName());
        e.addAttribute("address", address.getMailAddress());
    }
    
    public Address getReplyToAddress() {
        return replyToAddress;
    }
    
    public void setReplyToAddress(Address address) {
        replyToAddress = address;
        e.addAttribute("reply_address", address.getMailAddress());
    }
    
    public String getOrganisation() {
        return e.getAttribute("organisation");
    }
    
    public void setOrganisation(String organisation) {
        if (organisation != null) {
            e.addAttribute("organisation", organisation);
        } else {
            e.getAttributes().remove("organisation");
        }
    }
    
    /**
     * Returns the signature that should be attached to outgoing mails.
     */
    public File getSignature() {
        String path = e.getAttribute("signature_file");
        if (path != null) {
            File signature = new File(path);
            if (signature.exists() && signature.isFile()) {
                return signature;
            }
        }
        return null;
    }
    
    /**
     * Sets the signature to be attached to outgoing mails.
     */
    public void setSignature(File signature) {
        if (signature != null && signature.exists() && signature.isFile()) {
            e.addAttribute("signature_file", signature.getPath());
        } else {
            e.getAttributes().remove("signature_file");
        }
    }
}
