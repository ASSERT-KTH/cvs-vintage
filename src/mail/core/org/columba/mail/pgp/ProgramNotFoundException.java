/*
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is "Java Security Component Framework"
 * 
 * The Initial Developer of the Original Code are Thomas Wabner, alias waffel.
 * Portions created by Thomas Wabner are Copyright (C) 2004.
 * 
 * All Rights reserved. Created on 29.07.2004
 *  
 */
package org.columba.mail.pgp;

import org.waffel.jscf.JSCFException;

/**
 * This Exception should be used, if the given Programm like /usr/bin/gpg cannot
 * found or is null. If this Exception is thrown, then we should popup an error
 * window with a link to the config, where the user can set the path to gpg or
 * disable the feature.
 * 
 * @author waffel
 *  
 */
public class ProgramNotFoundException extends JSCFException
{
  /**
   * Creates a new ProgramNotFoundException and give it the reason in the arg0
   * variable.
   * 
   * @param arg0
   *          The reason as a string.
   */
  public ProgramNotFoundException (String arg0)
  {
    super(arg0);
  }

}