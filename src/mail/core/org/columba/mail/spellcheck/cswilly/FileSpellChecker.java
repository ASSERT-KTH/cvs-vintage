/*
 * $Revision: 1.1 $
 * $Date: 2004/06/01 16:11:47 $
 * $Author: fdietz $
 *
 * Copyright (C) 2001 C. Scott Willy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.columba.mail.spellcheck.cswilly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import java.util.List;


/**
 * Models the result of a spell check of a single word.
 *<p>
 */
public class FileSpellChecker {
    private String _aspellExeFilename;
    private AspellEngine _spellEngine = null;
    private Validator _spellValidator = null;

    public FileSpellChecker(String aspellExeFilename) {
        _aspellExeFilename = aspellExeFilename;
    }

    public FileSpellChecker() {
        this("O:\\local\\aspell\\aspell.exe");
    }

    public static void main(String[] args) {
        int exitStatus;

        String inputFilename = "spellTest.txt";

        try {
            BufferedReader input = new BufferedReader(new FileReader(
                        inputFilename));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                        System.out));

            FileSpellChecker checker = new FileSpellChecker();

            checker.checkFile(input, output);

            input.close();
            output.close();

            exitStatus = 0;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            exitStatus = 1;
        }

        System.exit(exitStatus);
    }

    /**
 * @return <i>true</i> if file completely checked and <i>false</i> if the user
 * interupted the checking.
 */
    public boolean checkFile(BufferedReader input, BufferedWriter output)
        throws SpellException {
        try {
            String line = input.readLine();

            while (line != null) {
                String checkedLine;

                if (line.trim().equals("")) {
                    checkedLine = line;
                } else {
                    List results = _getSpellEngine().checkLine(line);

                    checkedLine = _getSpellValidator().validate(line, results);

                    if (checkedLine == null) {
                        return false;
                    }
                }

                output.write(checkedLine);

                line = input.readLine();

                // Force that the last line in buffer does NOT have a newline
                if (line != null) {
                    output.write('\n');
                }
            }
        } catch (Exception e) {
            stop();

            if (e instanceof SpellException) {
                throw (SpellException) e;
            } else {
                throw new SpellException("Error communicating with the aspell subprocess",
                    e);
            }
        }

        return true;
    }

    public String getAspellExeFilename() {
        return _aspellExeFilename;
    }

    public void stop() {
        if (_spellEngine != null) {
            _spellEngine.stop();
            _spellEngine = null;
        }
    }

    private Engine _getSpellEngine() throws SpellException {
        if (_spellEngine == null) {
            String aSpellCommandLine = _aspellExeFilename + " pipe";
            _spellEngine = new AspellEngine(aSpellCommandLine);
        }

        return _spellEngine;
    }

    private Validator _getSpellValidator() {
        if (_spellValidator == null) {
            _spellValidator = new Validator();
        }

        return _spellValidator;
    }
}
