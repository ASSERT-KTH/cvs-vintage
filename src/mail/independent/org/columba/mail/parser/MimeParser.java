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

package org.columba.mail.parser;

import org.columba.mail.gui.message.*;

import java.io.*;
import java.util.*;
import org.columba.mail.message.*;
import org.columba.mail.coder.*;


public class MimeParser extends AbstractParser {

    String[] message;
    Rfc822Header rfc822Header;
    int action;

    public MimeParser()
    {
    }


    public MimeParser( Rfc822Header h, int action )
    {
        this.rfc822Header = h;
        this.action = action;
    }

    public boolean checkMimeVer( String mimeVer )
        {
            return mimeVer.equals("1.0");
        }

    public MimePart parse( String input )
    {
        MimeHeader mimeHeader;
        MimeTypeParser typeParser;
        MimePart attachments = null;

        message = divideMessage( input );

        if( message != null ) {
            mimeHeader = parseHeader( message[0] );
            typeParser = MimeRouter.getInstance().getTypeParser( mimeHeader );
//            typeParser.setHeader( rfc822Header );
//            typeParser.setAction( action );
            attachments = typeParser.parse( mimeHeader, message[1] );
        }

	//System.out.println("Parsed Body:");
	//System.out.println( attachments.get(0).getBody() );

        return attachments;
    }

    public MimeHeader parseHeader(String input)
    {
        MimeHeader output = new MimeHeader();
        String actParam;
        int charPos;
        EncodedWordDecoder decoder = new EncodedWordDecoder();
        StringWriter decodedString;


        if( input == null) return output;

        StringTokenizer paramTokenizer;
        HeaderTokenizer lineTokenizer = new HeaderTokenizer(input);
        String actLine = lineTokenizer.nextLine();

        while( actLine != null ) {

              // Parsing the Mime-Content-Types

            if( actLine.toLowerCase().startsWith("content-type") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    paramTokenizer = new StringTokenizer(actLine.substring(charPos+1),";");
                    actParam = paramTokenizer.nextToken().trim();
                    charPos = actParam.indexOf('/');

                    if( charPos != -1 ) {
                        output.contentType = actParam.substring(0,charPos).trim().toLowerCase();
                        output.contentSubtype = actParam.substring(charPos+1).trim().toLowerCase();
                    }

                    while( paramTokenizer.hasMoreTokens() ) {
                        actParam = paramTokenizer.nextToken().trim();
                        charPos = actParam.indexOf('=');
                        if( charPos != -1 ) {
                            output.putContentParameter(
                                actParam.substring(0,charPos).trim().toLowerCase(), decoder.decode( actParam.substring(charPos+1).trim() )
                                 );
                        }
                    }

                }
            }
            else if( actLine.toLowerCase().startsWith("content-transfer-encoding") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    output.contentTransferEncoding = actLine.substring(charPos+1).trim().toLowerCase();
                }
            }
            else if( actLine.toLowerCase().startsWith("content-id") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    output.contentID = actLine.substring(charPos+1).trim();
                }
            }
            else if( actLine.toLowerCase().startsWith("content-description") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    output.contentDescription = actLine.substring(charPos+1).trim();
                }
            }
            else if( actLine.toLowerCase().startsWith("content-disposition") ) {
                charPos = actLine.indexOf(':');
                if( charPos != -1 ) {
                    paramTokenizer = new StringTokenizer(actLine.substring(charPos+1),";");
                    actParam = paramTokenizer.nextToken().trim();
                    output.contentDisposition = actParam.trim().toLowerCase();

                    while( paramTokenizer.hasMoreTokens() ) {
                        actParam = paramTokenizer.nextToken().trim();
                        charPos = actParam.indexOf('=');
                        if( charPos != -1 ) {
                            output.putDispositionParameter(
                                actParam.substring(0,charPos).trim(),
                                decoder.decode( actParam.substring(charPos+1).trim() ));
                        }
                    }
                }
            }

              // End of Parsing
            actLine = lineTokenizer.nextLine();
        }

        return output;
    }


}










