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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.mail.spam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.columba.core.io.CloneStreamMaster;
import org.columba.core.main.MainInterface;
import org.macchiato.DBWrapper;
import org.macchiato.Message;
import org.macchiato.SpamFilter;
import org.macchiato.SpamFilterImpl;
import org.macchiato.db.FrequencyDB;
import org.macchiato.db.FrequencyDBImpl;
import org.macchiato.db.FrequencyIO;
import org.macchiato.db.MD5SumHelper;
import org.macchiato.log.MacchiatoLogger;
import org.macchiato.maps.ProbabilityMap;

/**
 * High-level wrapper for the spam filter.
 * <p>
 * Class should be used by Columba, to add ham or spam messages to the training
 * database. And to score messages using this training set.
 * <p>
 * Note, that its necessary for this filter to train a few hundred messages,
 * before its starting to work. I'm usually starting with around 1000 messages
 * while keeping it up-to-date with messages which are scored wrong.
 * <p>
 * If training mode is enabled, the spam filter automatically adds messages to
 * its frequency database.
 *
 * @author fdietz
 */
public class SpamController {
    
    /**
     * Delete messages from DB, if DB size > THRESHOLD
     */
    public final static int THRESHOLD = 200000;
    
    /**
     * Delete messages from DB after 7 days, if they don't
     * affect the scoring process because of low occurences.
     */
    public final static int AGE = 7;
    /**
     * singleton pattern instance of this class
     */
    private static SpamController instance;
    
    /**
     * spam filter in macchiator library doing the actual work
     */
    private SpamFilter filter;
    
    /**
     * database of tokens, storing occurences of tokens, etc.
     */
    private FrequencyDB db;
    
    /**
     * file to store the token database
     */
    private File file;
    
    /**
     * dirty flag for database changes
     */
    private boolean hasChanged = false;
    
    /**
     * is cache already loaded?
     */
    private boolean alreadyLoaded = false;
    
    /**
     * private constructor
     */
    private SpamController() {
        db = new DBWrapper(new FrequencyDBImpl());
        
        filter = new SpamFilterImpl(db);
        
        // make Columba logger parent of macchiato logger
        MacchiatoLogger.setParentLogger(Logger.getLogger("org.columba.mail.spam"));
    }
    
    /**
     * Get instance of class.
     *
     * @return spam controller
     */
    public static SpamController getInstance() {
        if (instance == null) {
            instance = new SpamController();
            File configDirectory = MainInterface.config.getConfigDirectory();
            File mailDirectory = new File(configDirectory, "mail");
            instance.file = new File(mailDirectory, "spam.db");
        }
        
        return instance;
    }
    
    /**
     * Add this message to the token database as spam.
     *
     * @param istream
     */
    public void trainMessageAsSpam(InputStream istream, List list) {
        // load database from file
        load();
        
        try {
            CloneStreamMaster master = new CloneStreamMaster(istream);
            InputStream inputStream = master.getClone();
            
            byte[] md5sum = MD5SumHelper.createMD5(inputStream);
            // close stream
            inputStream.close();
            
            // get new inputstream
            inputStream = master.getClone();
            
            Message message = new Message(inputStream, list, md5sum);
            // check if this message was already learned
            // -> only add if this is not the case
            if (db.MD5SumExists(md5sum)) {
                // message already exists
                // --> correct token data
                filter.correctMessageAsSpam(message);
            } else {
                // new message
                filter.trainMessageAsSpam(message);
            }
            
            // close stream
            inputStream.close();
            
            // set dirty flag
            hasChanged = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException nsae) {} //does not occur
    }
    
    /**
     * Add this message to the token database as ham.
     *
     * @param istream
     * @param list
     */
    public void trainMessageAsHam(InputStream istream, List list) {
        // load database from file
        load();
        
        try {
            CloneStreamMaster master = new CloneStreamMaster(istream);
            InputStream inputStream = master.getClone();
            
            byte[] md5sum = MD5SumHelper.createMD5(inputStream);
            // close stream
            inputStream.close();
            
            // get new inputstream
            inputStream = master.getClone();
            Message message = new Message(inputStream, list, md5sum);
            
            // check if this message was already learned
            if (db.MD5SumExists(md5sum)) {
                // message already exists
                
                // --> correct token data
                filter.correctMessageAsHam(message);
            } else {
                // new message
                
                filter.trainMessageAsHam(message);
            }
            
            // close stream
            inputStream.close();
            
            // set dirty flag
            hasChanged = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException nsae) {} //does not occur
    }
    
    /**
     * Score message. Using a threshold of 90% here. Every message with at least
     * 90% is spam. This value should be increased in the future.
     *
     * @param istream
     *
     * @return true, if message is spam. False, otherwise.
     */
    public boolean scoreMessage(InputStream istream, ProbabilityMap map) {
        // load database from file
        load();
        
        float score = filter.scoreMessage(new Message(istream), map);
        return score >= 0.9;
    }
    
    public void printDebug() {
        ((FrequencyDBImpl) db).printDebug();
    }
    
    /**
     * Load frequency DB from file.
     */
    private void load() {
        try {
            // only load if necessary
            if (!alreadyLoaded && file.exists()) {
                FrequencyIO.load(db, file);
            }
            
            alreadyLoaded = true;
        } catch (IOException e) {
            //TODO: i18n
            JOptionPane.showMessageDialog(
                MainInterface.frameModel.getActiveFrame(),
                "An error occured while loading the spam database.\n" +
                "I will use an empty one.", 
                "Error loading database",
                JOptionPane.ERROR_MESSAGE);
            if (MainInterface.DEBUG) {
                e.printStackTrace();
            }
            
            // fail-case
            db = new FrequencyDBImpl();
            
            alreadyLoaded = true;
        }
    }
    
    /**
     * Save frequency DB to file.
     */
    public void save() {
        try {
            // only save if changes exist
            if (alreadyLoaded && hasChanged) {
                // cleanup DB -> remove old tokens
                db.cleanupDB(THRESHOLD);
                
                // save DB to disk
                FrequencyIO.save(db, file);
            }
        } catch (IOException e) {
            if (MainInterface.DEBUG) {
                e.printStackTrace();
            }
            //TODO: i18n
            int value = JOptionPane.showConfirmDialog(
                MainInterface.frameModel.getActiveFrame(),
                "An error occured while saving the spam database.\n" +
                "Try again?",
                "Error saving database",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (value == JOptionPane.YES_OPTION) {
                save();
            }
        }
    }
}
