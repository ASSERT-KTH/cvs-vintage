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

package org.columba.mail.coder;

import java.util.Vector;

public class CoderRouter
{
    static Vector encoderList;
    static Vector decoderList;
    static Decoder nullDecoder;
    static Encoder nullEncoder;
    
    public CoderRouter()
    {
        encoderList = new Vector();
        decoderList = new Vector();

        nullDecoder = new NullDecoder();
        nullEncoder = new NullEncoder();
    }


    static public Decoder getDecoder( String encoding )
    {
        int size = decoderList.size();
        Decoder actDecoder;
        
        if( encoding == null ) return (Decoder) nullDecoder.clone();

        encoding = encoding.toLowerCase();

        for( int i=0; i<size; i++ ) {
            actDecoder = (Decoder) decoderList.get(i);

            if( actDecoder.getCoding().equals( encoding ) ) {
            return (Decoder) actDecoder.clone();
            }
        }

        return (Decoder) nullDecoder.clone();
    }
    static public Encoder getEncoder( String encoding )
    {
        int size = encoderList.size();
        Encoder actEncoder;

        if( encoding == null ) return (Encoder) nullEncoder.clone();

        encoding = encoding.toLowerCase();
        
        for( int i=0; i<size; i++ ) {
            actEncoder = (Encoder) encoderList.get(i);

            if( actEncoder.getCoding().equals( encoding ) ) {
                return (Encoder) actEncoder.clone();
            }
        }

        return (Encoder) nullEncoder.clone();
    }

    
    static public void addEncoder( Encoder encoder )
    {
        encoderList.add( encoder );
    }

    static public void addDecoder( Decoder decoder )
    {
        decoderList.add( decoder );
    }
    

    
}


