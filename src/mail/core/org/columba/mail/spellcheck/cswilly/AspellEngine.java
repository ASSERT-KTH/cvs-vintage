/*
 * $Revision: 1.2 $
 * $Date: 2004/08/22 15:55:07 $
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Models a spelling checker
 *<p>
 *
 */
public class AspellEngine implements Engine {
    BufferedReader _aSpellReader;
    BufferedWriter _aSpellWriter;
    String _aSpellWelcomeMsg;
    Process _aSpellProcess;

    public AspellEngine(String aSpellCommandLine) throws SpellException {
        try {
            Runtime runtime = Runtime.getRuntime();
            _aSpellProcess = runtime.exec(aSpellCommandLine);

            _aSpellReader = new BufferedReader(new InputStreamReader(
                        _aSpellProcess.getInputStream()));

            _aSpellWriter = new BufferedWriter(new OutputStreamWriter(
                        _aSpellProcess.getOutputStream()));

            _aSpellWelcomeMsg = _aSpellReader.readLine();
        } catch (IOException e) {
            String msg = "Cannot create aspell process.";
            throw new SpellException(msg, e);
        }
    }

    /**
 * Spell check a list of words
 *<p>
 * Spell checks the list of works in <code>words</code> and returns a list of
 * {@link Result}s. There is one {@link Result} for each word in
 * <code>words</code>.
 *<p>
 * @param words {@link String} with list of works to be spell checked.
 * @return List of {@link Result}
 */
    public List checkLine(String line) throws SpellException {
        try {
            List results = new ArrayList();

            final String spellCheckLinePrefix = "^";
            _aSpellWriter.write(spellCheckLinePrefix + line);
            _aSpellWriter.newLine();
            _aSpellWriter.flush();

            String response = _aSpellReader.readLine();

            while ((response != null) && !response.equals("")) {
                Result result = new Result(response);
                results.add(result);

                response = _aSpellReader.readLine();
            }

            return results;
        } catch (IOException e) {
            String msg = "Cannot access aspell process.";
            throw new SpellException(msg, e);
        }
    }

    public String getVersion() {
        return _aSpellWelcomeMsg;
    }

    public void stop() {
        _aSpellProcess.destroy();
    }
}
