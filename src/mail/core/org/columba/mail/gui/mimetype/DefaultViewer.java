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

package org.columba.mail.gui.mimetype;

import java.io.File;
import java.net.URL;

import org.columba.mail.parser.*;
import org.columba.mail.message.*;

public abstract class DefaultViewer
{

    public abstract Process openWith( MimeHeader header, File tempFile );

    public abstract Process open( MimeHeader header, File tempFile );

    public abstract Process openURL( URL url );
    
    public abstract Process openWithURL ( URL url );
}
