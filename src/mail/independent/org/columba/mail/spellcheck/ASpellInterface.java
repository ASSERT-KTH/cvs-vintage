// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
