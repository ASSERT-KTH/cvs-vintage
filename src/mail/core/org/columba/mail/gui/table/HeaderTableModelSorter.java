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


package org.columba.mail.gui.table;

import org.columba.core.config.WindowItem;
import org.columba.mail.gui.table.util.TableModelSorter;


public class HeaderTableModelSorter extends TableModelSorter
{


    protected WindowItem config;


    public HeaderTableModelSorter( HeaderTableModel tableModel )
        {
            super( tableModel );


            //collator = Collator.getInstance();


        }


    public void setWindowItem( WindowItem item )
    {
        this.config = item;

		// FIXME
        //sort = config.getSelectedHeader();
        if ( sort == null )
            sort = new String("Status");
        
        // FIXME
            
        //ascending = config.getHeaderAscending();
        ascending = true;


        setSortingColumn( sort );
        setSortingOrder( ascending );

    }

    /*
    public void setDataSorting(boolean b)
        {
            dataSorting = b;
        }

    public boolean getDataSorting()
        {
            return dataSorting;
        }

    public String getSortingColumn()
        {
            return sort;
        }

     public boolean getSortingOrder()
        {
            return ascending;
        }
        */

    public void setSortingColumn(String str)
        {
            sort=str;
            // FIXME
            //config.setSelectedHeader( sort );

        }

     public void setSortingOrder(boolean b)
        {
            ascending=b;
            // FIXME
            //config.setHeaderAscending( ascending );

        }


        /*
    public synchronized void sortTable(String str)
    {


        setSortingColumn( str );

        if ( str.equals("In Order Received") )
        {
            setDataSorting(true);

            MessageNode rootNode = getHeaderTableModel().getRootNode();

            Vector v = rootNode.getVector();

            System.out.println("in order received");

            setDataSorting(false);
        }
        else
        {
        for (int i=0; i < getHeaderTableModel().getColumnCount(); i++)
        {
            if ( str.equals( getHeaderTableModel().getColumnName(i)  ) )
            {

                setDataSorting(true);

                MessageNode rootNode = getHeaderTableModel().getRootNode();
                Vector v = rootNode.getVector();

                System.out.println("starting to sort");
                Collections.sort( v, new MessageHeaderComparator(
                    getHeaderTableModel().getColumnNumber( getSortingColumn() ) ,
                    getSortingOrder() ) );


                //System.out.println("finished sorting");

                //tableModel.update();
                //getHeaderTableModel().fireTreeNodesChanged();
                //getHeaderTableModel().update();

                setDataSorting(false);

            }
        }
        }


    }

    public void sort( int column )
    {

        String c = getHeaderTableModel().getColumnName(column);

        if ( getSortingColumn().equals(c) )
        {
            if ( getSortingOrder() == true ) setSortingOrder( false );
            else setSortingOrder( true );
        }

        setSortingColumn( c );

        sortTable( c );

    }

    public void setSortingColumn( int column )
    {
        String c = getHeaderTableModel().getColumnName( column );

        if ( getSortingColumn().equals(c) )
        {
            if ( getSortingOrder() == true ) setSortingOrder( false );
            else setSortingOrder( true );
        }

        setSortingColumn( c );
    }

    public boolean manipulateModel( int mode )
    {

        sortTable( getSortingColumn() );

        return true;
    }



    public int getSortInt()
    {

        return getHeaderTableModel().getColumnNumber( getSortingColumn() );

    }


    public int getInsertionSortIndex( MessageNode newChild )
    {

        MessageNode rootNode = getHeaderTableModel().getRootNode();
        Vector v = rootNode.getVector();

        if ( getSortingColumn().equals("In Order Received") )
        {
            return rootNode.getChildCount();
        }

        //System.out.println("column name: "+getSortingColumn() );
        //System.out.println("column number: "+ getHeaderTableModel().getColumnNumber( getSortingColumn() ) );

        MessageHeaderComparator comparator = new MessageHeaderComparator(
                                                 getHeaderTableModel().getColumnNumber( getSortingColumn() ),
                                                 getSortingOrder() );

        MessageNode child;
        int compare;

        // no children !
        if ( v == null ) return 0;

        for ( int i=0; i<v.size(); i++ )
        {
            child = (MessageNode) v.get(i);
            compare = comparator.compare( child, newChild );

            if ( compare == -1 )
            {

            }
            else if ( compare == 1 )
            {
                return i;
            }

        }

        return v.size();
    }





    class MessageHeaderComparator implements Comparator
    {

        protected int column;

        protected boolean ascending;

        public MessageHeaderComparator(int sortCol, boolean sortAsc)
            {
                column = sortCol;
                ascending = sortAsc;
            }

        public int compare(Object o1, Object o2)
            {
                //Integer int1 = (Integer) o1;
                //Integer int2 = (Integer) o2;

                MessageNode node1 = (MessageNode) o1;
                MessageNode node2 = (MessageNode) o2;

                //Message message1 = folder.get( int1.intValue() );
                ColumbaHeader header1 = (ColumbaHeader) node1.getUserObject();
		//Message message2 = folder.get( int2.intValue() );
                ColumbaHeader header2 =(ColumbaHeader)  node2.getUserObject();

                if ( ( header1 == null ) || ( header2 == null ) )
                    return 0;







                int result = 0;

                String columnName = getHeaderTableModel().getColumnName( column );

		if ( columnName.equals("Status") )
		    {
			Flags flags1 = header1.getFlags();
			Flags flags2 = header2.getFlags();

                        if ( ( flags1 == null ) || ( flags2 == null ) )
                            result = 0;


                        if ( ( flags1.getSeen() ) && ( !flags2.getSeen() ) )
			    {
				result = -1;
			    }
                        else if ( ( !flags1.getSeen() ) && ( flags2.getSeen() ) )
			    {
				result = 1;
			    }
                        else
                            result = 0;
		    }
		else if ( columnName.equals("Flagged") )
		    {
			Flags flags1 = header1.getFlags();
			Flags flags2 = header2.getFlags();

			boolean f1 = flags1.getFlagged();
			boolean f2 = flags2.getFlagged();

			if (f1 == f2)
			    {
				result =  0;
			    }
			else if (f1)
			    { // define false < true
				result =  1;
			    }
			else
			    {
				result =  -1;
			    }
		    }
		else if ( columnName.equals("Attachment") )
		    {
			boolean f1 = ( (Boolean) header1.get("columba.attachment") ).booleanValue();
			boolean f2 = ( (Boolean) header2.get("columba.attachment") ).booleanValue();

			if (f1 == f2)
			    {
				result =  0;
			    }
			else if (f1)
			    { // define false < true
				result =  1;
			    }
			else
			    {
				result =  -1;
			    }
		    }
		else if ( columnName.equals("Date") )
		    {
			Date d1 = (Date) header1.get("columba.date");
			Date d2 = (Date) header2.get("columba.date");
			if ( ( d1==null) || ( d2==null ) ) result = 0;
			else
			    result = d1.compareTo( d2 );
		    }
		else if ( columnName.equals("Size") )
		    {
			int i1 = ( (Integer) header1.get("columba.size") ).intValue();
			int i2 = ( (Integer) header2.get("columba.size") ).intValue();

			if ( i1 == i2  )
			    {
				result = 0;
			    }
			else if ( i1 > i2 )
			    {
				result = 1;
			    }
			else
			    {
				result = -1;
			    }
		    }
		else
		    {
			Object item1 = header1.get( columnName );
			Object item2 = header2.get( columnName );

			if ( ( item1!=null ) && ( item2 == null ) ) result = 1;
			else if ( ( item1==null ) && ( item2 != null ) ) result = -1;
			else if ( ( item1==null ) && ( item2 == null ) ) result = 0;

			else if ( item1 instanceof String )
			    {
				result = collator.compare( (String) item1, (String) item2 );
			    }
		    }


                if (!ascending)  result = -result;
                return result;
            }


        public boolean equals(Object obj)
        {

            if (obj instanceof MessageHeaderComparator)
            {

                MessageHeaderComparator compObj = (MessageHeaderComparator) obj;

                return (compObj.column==column) && (compObj.ascending==ascending);

            }

            return false;

        }

    }
    */

}







































