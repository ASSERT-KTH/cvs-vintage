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

import org.columba.core.io.StreamUtils;
import org.columba.core.logging.ColumbaLogger;

import org.columba.mail.config.PGPItem;
import org.columba.mail.gui.util.PGPPassphraseDialog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;


/**
 * This class is used to handle all PGP-actions in a way that the underlaying implementation is abstracted from the whole
 * pgp process to run the action. There are severals PGP-Tools declared, but only GPG currently used. The PGPController
 * offers function like verify, sign, decrypt and ancrypt of given Streams or Strings.
 * <p>
 * The PGPController use the {@link org.columba.mail.pgp.DefaultUtil DefaultUtil} to run the called method. It provides
 * function for reading results or error given from the DefaultUtil. It can be see as an abstract layer between high pgp
 * functions and the whole implementation which self is encapsulate by the DefaultUtil.
 * <p>
 * To handle with pgp and all pgp-functions only use methods of this class and not from the DefaultUtil or the whole
 * implementation.
 * @TODO  offering a function to say what pgp-tool will be used or give all methods the choise of the to use pgp-tool.
 * @author waffel, fdietz, tstisch
 *
 */
public class PGPController {
    /**
     * The GPG PGP-Tool. It is implemented in {@link GnuPGUtil}.
     */
    public final static int GPG = 0;

    /**
     * currently not used
     */
    public final static int PGP2 = 1;

    /**
     * currently not used
     */
    public final static int PGP5 = 2;

    /**
     * currently not used
     */
    public final static int PGP6 = 3;

    /**
     * The Decrypt action used for decrypting messages.
     */
    public final static int DECRYPT_ACTION = 0;

    /**
     * The Encrypt action used for encrypting messages.
     */
    public final static int ENCRYPT_ACTION = 1;

    /**
     * The sign action used for signing messages.
     */
    public final static int SIGN_ACTION = 2;

    /**
     * The verify action used to verify a message.
     */
    public final static int VERIFY_ACTION = 3;

    /**
     * The encrypt and sign action used to encrypt and sign a message.
     */
    public final static int ENCRYPTSIGN_ACTION = 4;
    private static PGPController myInstance;
    private int type;
    private String path;
    private String id;
    private int exitVal;
    private byte[] byteArray = null;
    private boolean save = false;
    private String pgpMessage;
    private PGPPassphraseDialog dialog;
    private File tempFile;
    private Map passwordMap;

    /**
     * here are the utils, from which you can sign, verify, encrypt and decrypt messages
     * at the moment there are only one tool - gpg, the gnu pgp programm which
     * comes with an commandline tool to do things with your pgp key.
     * @see DefaultUtil
     */
    private DefaultUtil[] utils = { new GnuPGUtil(), };

    /**
     * The default constructor for this class. The exit value is default 0.
     */
    protected PGPController() {
        exitVal = 0;

        passwordMap = new HashMap();
    }

    /**
     * Gives back an Instance of PGPController. This function controls, that only
     * one Instance is created in one columba session. If never before an Instance
     * is created, an Instance of Type PGPController is created and returned. Is
     * there is alrady an Instance, this instance is returned.
     * @return PGPController a new PGPController if there is no one created in one
     * columba session, or an alrady existing PGPConstroller, so only one Controller
     * is used in one columba session.
     */
    public static PGPController getInstance() {
        if (myInstance == null) {
            myInstance = new PGPController();
        }

        ColumbaLogger.log.info(myInstance.toString());

        return myInstance;
    }

    /**
     * gives the return value from the pgp-program back. This can used for
     * controlling errors when signing ... messages
     * @return int exitValue, 0 means all ok, all other exit vlaues identifying errors. Errors can be obtained by the
    * @see #getPGPErrorStream()
     */
    public int getReturnValue() {
        return exitVal;
    }

    /**
    * Decrypts a given InputStream. The methods asks the user for the passphrase. If the users cancels the dialog a
    * {@link CancelledException} is thrown. The method asks recursive the user for the correct passphrase until
    * the user cancels the dialog or gives the correct passphrase. If the are other problems like running the whole
    * pgp-tool a {@link PGPException} is thrown.
    * <p>
    * The decrypted InputStream is returned as a InputStream from which the decrypted message can be read. The Stream
    * is given bacl from the DefaultUtil {@link DefaultUtil#getStreamResult()}.
     * @param cryptMessage encrypted InputStream wich should be decrypted.
     * @param item The item which holds all necessary information like stored passphrase or the path to the pgp-tool.
     * @return a decrypted InputStream
     * @throws PGPException if the underlaying implementation throws an exception.
     */
    public InputStream decrypt(InputStream cryptMessage, PGPItem item)
        throws PGPException {
        exitVal = -1;

        String error = null;

        this.checkPassphrase(item);

        try {
            exitVal = utils[GPG].decrypt(item, cryptMessage);
            ColumbaLogger.log.info("exitVal=" + exitVal);

            error = utils[GPG].parse(utils[GPG].getErrorString());
        } catch (Exception e) {
            throw new PGPException(error);
        }

        pgpMessage = new String(error);

        if (exitVal != 0) {
            throw new PGPException(error);
        }

        return utils[GPG].getStreamResult();
    }

    /**
     * Verify a given message with a given signature. Can the signature for the given message be verified the method
     * returns true, else false. The given item should holding the path to the pgp-tool. While gpg dosn't yet supporting
     * a real stream based process to verify a given detached signature with a message the method creates a temporary
     * file which holds the signature. After the verify process the temporary file is deleted.
     * @param item PGPItem wich should holding the path to the pgp-tool
     * @param message The message for wich the given signature should be verify.
     * @param signature Signature wich should be verify for the given message.
     * @return true if the signature can be verify for the given message, else false.
    * @see DefaultUtil#verify(PGPItem, InputStream, InputStream)
     */
    public void verifySignature(InputStream message, InputStream signature,
        PGPItem item) throws PGPException {
        int exitVal = -1;
        String error = null;
        String output = null;

        try {
            exitVal = utils[GPG].verify(item, message, signature);

            error = utils[GPG].parse(utils[GPG].getErrorString());
        } catch (Exception e) {
            e.printStackTrace();

            throw new PGPException(error);
        }

        pgpMessage = error;

        if (exitVal == 1) {
            throw new VerificationException(error);
        }

        if (exitVal == 2) {
            throw new MissingPublicKeyException(error);
        }
    }

    /**
     * Encryptes a given message  and returnes the encrypted message as an InputStream. The given pgp-item should have
    * a entry with all recipients seperated via space. The entry is called recipients. If an error occurse the error result is
     * shown to the user via a dialog.
     * @param message The message to be encrypt
     * @param item the item which holds information like path to pgp-tool and recipients for which the message should be
     * encrypted.
    * @exception PGPException The exception is thrown, if the exit-value from the whole gpg-program is != 0.
    * @see DefaultUtil#encrypt(PGPItem, InputStream)
     * @return the encrypted message if all is ok, else an empty input-stream. {@link DefaultUtil#getStreamResult()}
     */
    public InputStream encrypt(InputStream pgpStream, PGPItem item)
        throws PGPException {
        int exitVal = -1;
        String error = "";

        try {
            this.createTempFileFromStream(pgpStream);

            exitVal = utils[GPG].encrypt(item, this.getTempInputStream());
            ColumbaLogger.log.info("exitVal=" + exitVal);

            error = utils[GPG].parse(utils[GPG].getErrorString());
        } catch (Exception e) {
            byteArray = null;
            throw new PGPException(error);
        }

        pgpMessage = new String(error);

        byteArray = null;

        if (exitVal != 0) {
            throw new PGPException(error);
        }

        return utils[GPG].getStreamResult();
    }

    /**
     * signs an message and gives the signed message as an InputStream back to the application. This method call the
     * GPG-Util to sign the message. if the passphrase is currently not stored in the PGPItem the user is called for
     * a new passphrase. If the user dosn't give a passphrase (he cancel the dialog) the method returns null.
     * The Util (in this case GPG is called to sign the message with the user-id. The exit-value from the sign-process is
     * stored in the global exit value. In the case, that the exit-value is 2 then the error-message from the gpg-program is
     * printed to the user in an dialog. The value null is then returned.
     * If the value is equal to 1 null is returned. If an exception occurrs, the exception-message is shown to the
     * user in a dialog and null is returned.
     * @param pgpMessage the message that is to signed
     * @param item the item wich holds the userid for the pgp key. Eventual the passphrase is also stored in the item. Then
     * stored passphrase is used.
     * @return The signed message as an InputStream. Null, when
     * an error occurse or the exit-value is not equal to 0 from the whole gpg-util is returned.
     */
    public InputStream sign(InputStream pgpStream, PGPItem item)
        throws PGPException {
        exitVal = -1;

        String error = null;
        ColumbaLogger.log.info("signing called");
        this.checkPassphrase(item);

        try {
            exitVal = utils[GPG].sign(item, pgpStream);
            ColumbaLogger.log.info("exitVal=" + exitVal);

            error = utils[GPG].parse(utils[GPG].getErrorString());
        } catch (Exception e) {
            ColumbaLogger.log.severe(e.getMessage());
            throw new PGPException(error);
        }

        pgpMessage = new String(error);

        if (exitVal != 0) {
            ColumbaLogger.log.severe(error);
            throw new PGPException(error);
        }

        return utils[GPG].getStreamResult();
    }

    /**
     * Checks with a test string if the test String can be signed. The user is ask for his passphrase until the passphrase is ok or
     * the user cancels the dialog. If the user cancels the dialog a PGPException with the error string from the pgp tool is thrown.
     * This method returned normal only if the user give the right passphrase-
     * @param item PGPItem used for signing the test string
     * @exception PGPException if the user cancels the passphrase dialog or the pgp tool has errors and returns with exit code != 0.
     */
    private void checkPassphrase(PGPItem item) throws PGPException {
        String testStr = "test";
        int exitVal = -1;

        // loop until signing was sucessful or the user cancels the passphrase dialog
        while ((exitVal != 0) && (this.getPassphrase(item) == true)) {
            try {
                exitVal = utils[GPG].sign(item,
                        new ByteArrayInputStream(testStr.getBytes()));
            } catch (Exception e) {
                throw new PGPException(utils[GPG].parse(
                        utils[GPG].getErrorString()));
            }
        }

        if (exitVal != 0) {
            throw new PGPException(utils[GPG].parse(utils[GPG].getErrorString()));
        }
    }

    /**
     * signs an message and gives the signed message string back to the application. This method call the GPG-Util to sign
     * the message. If no passphrase is given, an empty String is returned. If the passphrase String
     * has an length > 0, a new Passphrase-Dialog is opend and asked the user for
     * input the password for his key. The Util (in this case GPG is called to
     * sign the message with the user-id. The exit-value from the sign-process is
     * stored in the global exit value. In the case, that the exit-value is 2 then
     * the error-message from the gpg-program is printed to the user in an dialog.
     * The value null is then returned. If the value is equal to 1 null is
     * returned. If an exception occurrs, the exception-message is shown to the
     * user in dialog and null is returned
     * @param pgpMessage the message that is to signed
     * @param item the item wich holds the passphrase (the userid for the pgp key)
     * @return String the signed message with the sign string inside. Null, when
     * an error or an exit-value not equal 0 from the whole gpg-util is returned
     * @deprecated After ristretto is used in columba only Streams instead of Strings supported. Use
     * {@link #sign(InputStream, PGPItem)}.
     */
    public String sign(String pgpMessage, PGPItem item) {
        exitVal = -1;

        // this is, if we have more then one pgp-type
        //type = item.getInteger("type");
        path = item.get("path");
        id = item.get("id");

        if (!this.getPassphrase(item)) {
            return null;
        }

        try {
            ColumbaLogger.log.info("pgpmessage: !!" + pgpMessage + "!!");
            exitVal = utils[GPG].sign(item, pgpMessage);

            if (!checkError(exitVal, item)) {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, utils[GPG].getErrorString());

            if (save == false) {
                item.clearPassphrase();
            }

            return null;
        }

        if (save == false) {
            item.clearPassphrase();
        }

        ColumbaLogger.log.info(utils[GPG].getResult());

        return utils[GPG].getResult();
    }

    private boolean getPassphrase(PGPItem item) {
        String passphrase = "";

        if (passwordMap.containsKey(item.get("id"))) {
            passphrase = (String) passwordMap.get(item.get("id"));
        }

        item.setPassphrase(passphrase);

        boolean ret = true;

        dialog = new PGPPassphraseDialog();

        if (passphrase.length() == 0) {
            dialog.showDialog(item.get("id"), passphrase, false);

            if (dialog.success()) {
                passphrase = new String(dialog.getPassword(), 0,
                        dialog.getPassword().length);
                item.setPassphrase(passphrase);

                save = dialog.getSave();

                // save passphrase in hash map
                if (save) {
                    passwordMap.put(item.get("id"), passphrase);
                }

                ret = true;
            } else {
                ret = false;
            }
        }

        return ret;
    }

    private boolean checkError(int exitVal, PGPItem item) {
        boolean ret = true;

        if (exitVal == 2) {
            JOptionPane.showMessageDialog(null, utils[GPG].getErrorString());
            ret = false;
        }

        return ret;
    }

    /**
     * Returnes the Result from the GPG Util as an InputStream. On each operation with the GPG Util this Stream is
     *  overidden. The ResultStream can be empty if it has earliyer readed out.
     * @return The Result from the GPG Util as an InputStream.
     */
    public InputStream getPGPResultStream() {
        return utils[GPG].getStreamResult();
    }

    /**
     * Returns the ErrorResult from the GPG Util as an InputStream. On each operation with the GPG Util this Stream is
     *  overidden. The ResultStream can be empty if it has earliyer readed out.
     * @return The ErrorResult from the GPG Util as an InputStream.
     */
    public InputStream getPGPErrorStream() {
        return utils[GPG].getErrorStream();
    }

    /**
     * Gets the pgp commandline output.
     *
     * @return        output of the commandline
     */
    public String getPgpMessage() {
        return pgpMessage;
    }

    /**
    * Creates a temporary file with the context of the given Stream. The given Stream is saved in the temporary file. The
    * temporary file is used in recursive methods like  {@link #sign(InputStream, PGPItem)} . The file itselfs is saved in a
    * internal variable and the internal variable is overidden when you call this method. The file should be deleted after a
    * correct exit of the running VM {@link File#deleteOnExit()}.
     * @param in Stream wich should be written to the temporary file. The file self is created via  {@link File#createTempFile(java.lang.String, java.lang.String)}.
     * @throws IOException If the file cannot be created or written.
     */
    protected void createTempFileFromStream(InputStream in)
        throws IOException {
        tempFile = File.createTempFile("columba-pgp", ".tmp");

        //		make sure file is deleted automatically when closing VM
        tempFile.deleteOnExit();

        FileOutputStream out = new FileOutputStream(tempFile);
        StreamUtils.streamCopy(in, out);
        in.close();
        out.close();
    }

    /**
    * Gives the content of a temporary file as an InputStream back. The temporary file is created with
    * {@link #createTempFileFromStream(InputStream)}.
     * @return The contents of the file as a InputStream.
     * @throws IOException if the file cannot be read.
     */
    protected InputStream getTempInputStream() throws IOException {
        return new FileInputStream(tempFile);
    }

    /**
     *  Removes all password saved in the current session. This is used if a recursive dialog asks often for a password in
    * one session, then the password is saved in a internal Map. To clear the Map use this function.
     */
    public void clearAllPassphrases() {
        passwordMap.clear();
    }

    public void addPasswordToMap(String key, String item) {
        this.passwordMap.put(key, item);
    }
}
