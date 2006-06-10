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
package org.columba.mail.folder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.columba.core.io.DiskIO;

/**
 * Convenience methods for folder testcases.
 * 
 * @author fdietz
 */
public final class FolderTstHelper {

    /**
     * This directory is used to create mail folders
     */
    public static String homeDirectory = System.getProperties().getProperty(
            "user.dir");

    /**
     * Read message " <number>.eml" into String.
     * 
     * @param number
     *            number of message
     * @return string containing message source
     * @throws Exception
     */
    public static String getString(int number) throws Exception {
        return DiskIO.readFileInString(new File(new String(
                "src/mail/test/org/columba/mail/folder/" + number + ".eml"))).replaceAll("\n", "\r\n");
    }
    
    /**
     * Read string from file.
     * 
     * @param filename		name of file
     * @return				string containing file contents
     * @throws Exception
     */
    public static String getString(String filename) throws Exception {
        return DiskIO.readFileInString(new File(new String(
                "src/mail/test/org/columba/mail/folder/" + filename))).replaceAll("\n", "\r\n");
    }

    /**
     * Create ByteArrayInputStream from String.
     * 
     * @param s
     *            String
     * @return ByteArrayInputStream
     */
    public static ByteArrayInputStream getByteArrayInputStream(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    /**
     * Create String from InputStream.
     * 
     * @param is
     *            inputstream
     * @return string
     * @throws Exception
     */
    public static String getStringFromInputStream(InputStream is)
            throws Exception {
        StringBuffer result = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String nextLine = reader.readLine();

        while (nextLine != null) {
            result.append(nextLine);
            result.append("\r\n");
            nextLine = reader.readLine();
        }

        return result.toString();
    }
}
