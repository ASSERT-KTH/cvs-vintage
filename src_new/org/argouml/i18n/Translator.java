// Copyright (c) 1996-2002 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

/**
 * A tool class to help localization.
 *
 * @author Jean-Hugues de Raigniac
 *
 */
public class Translator {

    /** Binding between new key names and old ones needed by gef. */
    private static Properties images = null;

    /** Property file containing the bindings. */
    private static File properties =
        new File("org/argouml/i18n/images.properties");

    /**   
     * Loads image bindings from a File.
     * @param file the properties file
     * @return the properties in file
     */
    private static Properties loadImageBindings (File file) {

        FileInputStream fileStream = null;
        Properties properties = new Properties();

        try {
            fileStream = new FileInputStream(file);
            properties.load(fileStream);
            fileStream.close();
        } catch (IOException ex) {
            System.out.println("Unable to load properties from file: "
                               + file.getAbsolutePath());
            ex.printStackTrace(System.out);
            System.exit(1);
        }

        return properties;
    }

    /**   
     * Provide a "gef compliant" image file name.
     * @param name the new i18n key
     * @return the old i18n key
     */
    public static String getImageBinding (String name) {

        String binding = null;

        if (images == null) {
            images = loadImageBindings(properties);
        }

        binding = images.getProperty(name);

        if (binding == null) {
            return name;
        } else {
            return binding;
        }
    }

    /** Helper for localization to eliminate the need to import
     *  the gef util library. 
     * @param bundle a binding to a bundle of i18n resources
     * @param key the key to loacalize
     * @return the translation
     */
    public static String localize(String bundle, String key) {
        return org.tigris.gef.util.Localizer.localize(bundle, key);
    }
}
