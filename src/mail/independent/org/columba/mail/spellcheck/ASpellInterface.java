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
package org.columba.mail.spellcheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.columba.mail.spellcheck.cswilly.FileSpellChecker;
import org.columba.mail.spellcheck.cswilly.SpellException;

public class ASpellInterface
{
    public static final String ASPELL_EXE_PROP = "ASpellExecutable";

    private static FileSpellChecker fileSpellChecker = null;
    private static String aspellExeFilename;

    public static  String checkBuffer(String buffer)
    {
        String checkedBuffer;
        FileSpellChecker checker = null;

        try
        {
            BufferedReader input = new BufferedReader(new StringReader(buffer));
            StringWriter stringWriter = new StringWriter(buffer.length());
            BufferedWriter output = new BufferedWriter(stringWriter);

            checker = getFileSpellChecker();

            boolean checkingNotCanceled = checker.checkFile(input, output);

            input.close();
            output.close();

            if (checkingNotCanceled)
                checkedBuffer = stringWriter.toString();
            else
                checkedBuffer = null;
        }
        catch (SpellException e)
        {
            String msg = "Cannot check selection.\nError (Aspell) is: " + e.getMessage();
            System.out.println(msg);
            checkedBuffer = null;
        }
        catch (IOException e)
        {
            String msg = "Cannot check selection.\nError (Interface) is: " + e.getMessage();
            System.out.println(msg);
            checkedBuffer = null;
        }

        return checkedBuffer;
    }

    private static FileSpellChecker getFileSpellChecker()
    {
        String aspellExeFilename = getAspellExeFilename();

        if (fileSpellChecker == null)
        {
            fileSpellChecker = new FileSpellChecker(aspellExeFilename);
        }
        else if (!aspellExeFilename.equals(fileSpellChecker.getAspellExeFilename()))
        {
            fileSpellChecker.stop();
            fileSpellChecker = new FileSpellChecker(aspellExeFilename);
        }

        return fileSpellChecker;
    }

    public static String getAspellExeFilename()
    {
        if (aspellExeFilename == null || aspellExeFilename.equals(""))
        {
            aspellExeFilename = "aspell.exe";
        }

        return aspellExeFilename;
    }

    public static void setAspellExeFilename(String exeFilename)
    {
        aspellExeFilename = exeFilename;
    }
}
