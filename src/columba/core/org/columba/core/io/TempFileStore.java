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
package org.columba.core.io;

import org.columba.core.main.MainInterface;

import java.io.File;

/**
 * Factory to create temporary Files on the file storage.
 */
public final class TempFileStore {
    private static File tempDir;

    static {
        File configDir = MainInterface.config.getConfigDirectory();

        tempDir = new File(configDir, "tmp");
        DiskIO.emptyDirectory(tempDir);
        DiskIO.ensureDirectory(tempDir);
    }

    /**
     * Utility class should not have a public constructor.
     */
    private TempFileStore() {
    }

    /**
     * Returns a String with spaces replaced by underscore.
     * @param s in string
     * @return incoming string without spaces.
     */
    private static String replaceWhiteSpaces(String s) {
        return s.replace(' ', '_');
    }

    /**
     * Create a temporary file on the temporary folder storage.
     * @return a File. File suffix is tmp.
     */
    public static File createTempFile() {
        return createTempFileWithSuffix("tmp");
    }

    /**
     * Create a temporary file with the specified filename.
     * @param name the name of the file.
     * @return a File.
     */
    public static File createTempFile(String name) {
        return createTemporaryFile(replaceWhiteSpaces(name));
    }

    /**
     * Create a temporary file with the specified file name suffix.
     * @param suffix the suffix for the file.
     * @return a File.
     */
    public static File createTempFileWithSuffix(String suffix) {
        return createTemporaryFile("columba" + System.currentTimeMillis() + "." + suffix);
    }

    /**
     * Creates a temporary file that is removed when the program exits.
     * @param name the name of the file.
     * @return a File.
     */
    private static File createTemporaryFile(String name) {
        File newFile = new File(tempDir, name);
        newFile.deleteOnExit();
        return newFile;
    }
}
