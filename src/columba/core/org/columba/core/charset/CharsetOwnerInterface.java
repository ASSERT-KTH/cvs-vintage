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
//All Rights Reserved.undation, Inc., 59 Temple Place - Suite 330, Boston, MA
// 02111-1307, USA.
package org.columba.core.charset;

import java.nio.charset.Charset;


/**
 * Should be implemented by objects encapsulating a charset. Enables
 * interested objects to register listeners.
 */
public interface CharsetOwnerInterface {
    /**
 * Returns the currently chosen charset. This method may return null
 * if Columba should try to autodetect the charset.
 */
    public abstract Charset getCharset();

    /**
 * Sets the currently active charset. This method must notify all
 * registered CharacterListener instances. If null is passed, the
 * charset will be determined automatically.
 */
    public abstract void setCharset(Charset charset);

    /**
 * Registers a listener to get notified whenever the charset changes.
 */
    public abstract void addCharsetListener(CharsetListener l);

    /**
 * Unregisters a previously registered CharacterListener instance.
 */
    public abstract void removeCharsetListener(CharsetListener l);
}
