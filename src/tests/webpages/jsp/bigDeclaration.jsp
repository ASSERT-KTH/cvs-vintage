<%@page import="java.applet.Applet" %>
<%@page import="java.awt.*" %>
<%@page import="java.awt.event.*" %>
<%@page import="java.io.*" %>
<%@page import="java.lang.*" %>
<%@page import="java.net.*" %>


<html>
<body>
<%!

// The following declarations have been moved out out of the respective
// classes as JDK 1.1 does not support static objects in inner classes.

public static final int Cell_VALUE = 0;
public static final int Cell_LABEL = 1;
public static final int Cell_URL   = 2;
public static final int Cell_FORMULA = 3;

public static final int Node_OP = 0;
public static final int Node_VALUE = 1;
public static final int Node_CELL = 2;

public class SpreadSheet 
    extends Applet
    implements MouseListener, KeyListener {
    String		title;
    Font		titleFont;
    Color		cellColor;
    Color		inputColor;
    int			cellWidth = 100;
    int			cellHeight = 15;
    int			titleHeight = 15;
    int			rowLabelWidth = 15;
    Font		inputFont;
    boolean		isStopped = false;
    boolean		fullUpdate = true;
    int			rows;
    int			columns;
    int			currentKey = -1;
    int			selectedRow = -1;
    int			selectedColumn = -1;
    SpreadSheetInput	inputArea;
    Cell		cells[][];
    Cell		current = null;

    public synchronized void init() {
	String rs;
	
	cellColor = Color.white;
	inputColor = new Color(100, 100, 225);
	inputFont = new Font("Courier", Font.PLAIN, 10);
	titleFont = new Font("Courier", Font.BOLD, 12);
	title = getParameter("title");
	if (title == null) {
	    title = "Spreadsheet";
	}
	rs = getParameter("rows");
	if (rs == null) {
	    rows = 9;
	} else {
	    rows = Integer.parseInt(rs);
	}
	rs = getParameter("columns");
	if (rs == null) {
	    columns = 5;
	} else {
	    columns = Integer.parseInt(rs);
	}
	cells = new Cell[rows][columns];
	char l[] = new char[1];
	for (int i=0; i < rows; i++) {
	    for (int j=0; j < columns; j++) {

		cells[i][j] = new Cell(this,
				       Color.lightGray,
				       Color.black,
				       cellColor,
				       cellWidth - 2,
				       cellHeight - 2);
		l[0] = (char)((int)'a' + j);
		rs = getParameter("" + new String(l) + (i+1));
		if (rs != null) {
		    cells[i][j].setUnparsedValue(rs);
		}
	    }
	}

	Dimension d = getSize();
	inputArea = new SpreadSheetInput(null, this, d.width - 2, cellHeight - 1,
					 inputColor, Color.white);
	resize(columns * cellWidth + rowLabelWidth,
	       (rows + 3) * cellHeight + titleHeight);
	addMouseListener(this);
	addKeyListener(this);
    }

    public void setCurrentValue(float val) {
	if (selectedRow == -1 || selectedColumn == -1) {
	    return;
	}
	cells[selectedRow][selectedColumn].setValue(val);
	repaint();
    }

    public void stop() {
	isStopped = true;
    }

    public void start() {
	isStopped = false;
    }

    public void destroy() {
	for (int i=0; i < rows; i++) {
	    for (int j=0; j < columns; j++) {
		if (cells[i][j].type == Cell_URL) {
		    cells[i][j].updaterThread.stop();
		}
	    }
	}
    }

    public void setCurrentValue(int type, String val) {
	if (selectedRow == -1 || selectedColumn == -1) {
	    return;
	}
	cells[selectedRow][selectedColumn].setValue(type, val);
	repaint();
    }

    public void update(Graphics g) {
	if (! fullUpdate) {
	    int cx, cy;

	    g.setFont(titleFont);
	    for (int i=0; i < rows; i++) {
		for (int j=0; j < columns; j++) {
		    if (cells[i][j].needRedisplay) {
			cx = (j * cellWidth) + 2 + rowLabelWidth;
			cy = ((i+1) * cellHeight) + 2 + titleHeight;
			cells[i][j].paint(g, cx, cy);
		    }
		}
	    }
	} else {
	    paint(g);
	    fullUpdate = false;
	}
    }

    public void recalculate() {
	int	i,j;

	//System.out.println("SpreadSheet.recalculate");
	for (i=0; i < rows; i++) {
	    for (j=0; j < columns; j++) {
		if (cells[i][j] != null && cells[i][j].type == Cell_FORMULA) {
		    cells[i][j].setRawValue(evaluateFormula(cells[i][j].parseRoot));
		    cells[i][j].needRedisplay = true;
		}
	    }
	}
	repaint();
    }

    public float evaluateFormula(Node n) {
	float	val = 0.0f;

	//System.out.println("evaluateFormula:");
	//n.print(3);
	if (n == null) {
	    //System.out.println("Null node");
	    return val;
	}
	switch (n.type) {
	  case Node_OP:
	    val = evaluateFormula(n.left);
	    switch (n.op) {
	      case '+':
		val += evaluateFormula(n.right);
		break;
	      case '*':
		val *= evaluateFormula(n.right);
		break;
	      case '-':
		val -= evaluateFormula(n.right);
		break;
	      case '/':
		val /= evaluateFormula(n.right);
		break;
	    }
	    break;
	  case Node_VALUE:
	    //System.out.println("=>" + n.value);
	    return n.value;
	  case Node_CELL:
	    if (n == null) {
		//System.out.println("NULL at 192");
	    } else {
		if (cells[n.row][n.column] == null) {
		    //System.out.println("NULL at 193");
		} else {
		    //System.out.println("=>" + cells[n.row][n.column].value);
		    return cells[n.row][n.column].value;
		}
	    }
	}

	//System.out.println("=>" + val);
	return val;
    }

    public synchronized void paint(Graphics g) {
	int i, j;
	int cx, cy;
	char l[] = new char[1];


	Dimension d = getSize();

	g.setFont(titleFont);
	i = g.getFontMetrics().stringWidth(title);
	g.drawString((title == null) ? "Spreadsheet" : title,
		     (d.width - i) / 2, 12);
	g.setColor(inputColor);
	g.fillRect(0, cellHeight, d.width, cellHeight);
	g.setFont(titleFont);
	for (i=0; i < rows+1; i++) {
	    cy = (i+2) * cellHeight;
	    g.setColor(getBackground());
	    g.draw3DRect(0, cy, d.width, 2, true);
	    if (i < rows) {
		g.setColor(Color.red);
		g.drawString("" + (i+1), 2, cy + 12);
	    }
	}

	g.setColor(Color.red);
	cy = (rows+3) * cellHeight + (cellHeight / 2);
	for (i=0; i < columns; i++) {
	    cx = i * cellWidth;
	    g.setColor(getBackground());
	    g.draw3DRect(cx + rowLabelWidth,
			  2 * cellHeight, 1, d.height, true);
	    if (i < columns) {
		g.setColor(Color.red);
		l[0] = (char)((int)'A' + i);
		g.drawString(new String(l),
			     cx + rowLabelWidth + (cellWidth / 2),
			     cy);
	    }
	}

	for (i=0; i < rows; i++) {
	    for (j=0; j < columns; j++) {
		cx = (j * cellWidth) + 2 + rowLabelWidth;
		cy = ((i+1) * cellHeight) + 2 + titleHeight;
		if (cells[i][j] != null) {
		    cells[i][j].paint(g, cx, cy);
		}
	    }
	}

	g.setColor(getBackground());
	g.draw3DRect(0, titleHeight,
		      d.width,
		      d.height - titleHeight,
		      false);
	inputArea.paint(g, 1, titleHeight + 1);
    }

      //1.1 event handling
      
  public void mouseClicked(MouseEvent e)
  {}
      
  public void mousePressed(MouseEvent e)
  {
    int x = e.getX();
    int y = e.getY();
    Cell cell;
    if (y < (titleHeight + cellHeight)) {
      selectedRow = -1;
      if (y <= titleHeight && current != null) {
	current.deselect();
	current = null;
      }
      e.consume();
    }
    if (x < rowLabelWidth) {
      selectedRow = -1;
      if (current != null) {
	current.deselect();
		current = null;
      }
      e.consume();
      
    }
    selectedRow = ((y - cellHeight - titleHeight) / cellHeight);
    selectedColumn = (x - rowLabelWidth) / cellWidth;
    if (selectedRow > rows ||
	selectedColumn >= columns) {
      selectedRow = -1;
      if (current != null) {
	current.deselect();
	current = null;
      }
    } else {
      if (selectedRow >= rows) {
	selectedRow = -1;
	if (current != null) {
	  current.deselect();
	  current = null;
	}
	e.consume();
      }
      cell = cells[selectedRow][selectedColumn];
      inputArea.setText(new String(cell.getPrintString()));
      if (current != null) {
	current.deselect();
      }
      current = cell;
      current.select();
      requestFocus();
      fullUpdate = true;
      repaint();
    }
    e.consume();
  }

  public void mouseReleased(MouseEvent e) 
  {}
      
  public void mouseEntered(MouseEvent e)
  {}
      
  public void mouseExited(MouseEvent e) 	
  {}

  public void keyPressed(KeyEvent e)
  {
    fullUpdate=true;
    inputArea.processKey(e);
    e.consume();
  }
  
  public void keyTyped(KeyEvent e) {
  }
  
  public void keyReleased(KeyEvent e)
  {}   
      
  public String getAppletInfo() {
    return "Title: SpreadSheet \nAuthor: Sami Shaio \nA simple spread sheet.";
  }
      
  public String[][] getParameterInfo() {
    String[][] info = {
      {"title", "string", "The title of the spread sheet.  Default is 'Spreadsheet'"},
      {"rows", "int", "The number of rows.  Default is 9."},
      {"columns", "int", "The number of columns.  Default is 5."}
    };
    return info;
  }


}

class CellUpdater extends Thread {
    Cell 	target;
    InputStream dataStream = null;
    StreamTokenizer tokenStream;

    public CellUpdater(Cell c) {
	super("cell updater");
	target = c;
    }

    public void run() {
	try {
	    dataStream = new URL(target.app.getDocumentBase(),
				 target.getValueString()).openStream();
	    tokenStream = new StreamTokenizer(new BufferedReader(new InputStreamReader(dataStream)));
	    tokenStream.eolIsSignificant(false);
	    
	    while (true) {
		switch (tokenStream.nextToken()) {
		case tokenStream.TT_EOF:
		    dataStream.close();
		    return;
		default:
		    break;
		case tokenStream.TT_NUMBER:
		    target.setTransientValue((float)tokenStream.nval);
		    if (! target.app.isStopped && ! target.paused) {
			target.app.repaint();
		    }
		    break;
		}
		try {
		    Thread.sleep(2000);
		} catch (InterruptedException e) {
		    break;
		}
	    }
	} catch (IOException e) {
	    return;
	}
    }
}

class Cell {
    
    Node	parseRoot;
    boolean	needRedisplay;
    boolean selected = false;
    boolean transientValue = false;
    public int	type = Cell_VALUE;
    String	valueString = "";
    String	printString = "v";
    float	value;
    Color	bgColor;
    Color	fgColor;
    Color	highlightColor;
    int		width;
    int		height;
    SpreadSheet app;
    CellUpdater	updaterThread;
    boolean	paused = false;

    public Cell(SpreadSheet app,
		Color bgColor,
		Color fgColor,
		Color highlightColor,
		int width,
		int height) {
	this.app = app;
	this.bgColor = bgColor;
	this.fgColor = fgColor;
	this.highlightColor = highlightColor;
	this.width = width;
	this.height = height;
	needRedisplay = true;
    }
		
    public void setRawValue(float f) {
	valueString = Float.toString(f);
	value = f;
    }
    public void setValue(float f) {
	setRawValue(f);
	printString = "v" + valueString;
	type = Cell_VALUE;
	paused = false;
	app.recalculate();
	needRedisplay = true;
    }

    public void setTransientValue(float f) {
	transientValue = true;
	value = f;
	needRedisplay = true;
	app.recalculate();
    }

    public void setUnparsedValue(String s) {
	switch (s.charAt(0)) {
	  case 'v':
	    setValue(Cell_VALUE, s.substring(1));
	    break;
	  case 'f':
	    setValue(Cell_FORMULA, s.substring(1));
	    break;
	  case 'l':
	    setValue(Cell_LABEL, s.substring(1));
	    break;
	  case 'u':
	    setValue(Cell_URL, s.substring(1));
	    break;
	}
    }

    /**
     * Parse a spreadsheet formula. The syntax is defined as:
     *
     * formula -> value
     * formula -> value op value
     * value -> '(' formula ')'
     * value -> cell
     * value -> <number>
     * op -> '+' | '*' | '/' | '-'
     * cell -> <letter><number>
     */
    public String parseFormula(String formula, Node node) {
	String subformula;
	String restFormula;
	float value;
	int length = formula.length();
	Node left;
	Node right;
	char op;

	if (formula == null) {
	    return null;
	}
	subformula = parseValue(formula, node);
	//System.out.println("subformula = " + subformula);
	if (subformula == null || subformula.length() == 0) {
	    //System.out.println("Parse succeeded");
	    return null;
	}
	if (subformula == formula) {
	    //System.out.println("Parse failed");
	    return formula;
	}

	// parse an operator and then another value
	switch (op = subformula.charAt(0)) {
	  case 0:
	    //System.out.println("Parse succeeded");
	    return null;
	  case ')':
	    //System.out.println("Returning subformula=" + subformula);
	    return subformula;
	  case '+':
	  case '*':
	  case '-':
	  case '/':
	    restFormula = subformula.substring(1);
	    subformula = parseValue(restFormula, right=new Node());
	    //System.out.println("subformula(2) = " + subformula);
	    if (subformula != restFormula) {
		//System.out.println("Parse succeeded");
		left = new Node(node);
		node.left = left;
		node.right = right;
		node.op = op;
		node.type = Node_OP;
		//node.print(3);
		return subformula;
	    } else {
		//System.out.println("Parse failed");
		return formula;
	    }
	  default:
	    //System.out.println("Parse failed (bad operator): " + subformula);
	    return formula;
	}
    }

    public String parseValue(String formula, Node node) {
	char	c = formula.charAt(0);
	String	subformula;
	String	restFormula;
	float	value;
	int	row;
	int	column;

	//System.out.println("parseValue: " + formula);
	restFormula = formula;
	if (c == '(') {
	    //System.out.println("parseValue(" + formula + ")");
	    restFormula = formula.substring(1);
	    subformula = parseFormula(restFormula, node);
	    //System.out.println("rest=(" + subformula + ")");
	    if (subformula == null ||
		subformula.length() == restFormula.length()) {
		//System.out.println("Failed");
		return formula;
	    } else if (! (subformula.charAt(0) == ')')) {
	        //System.out.println("Failed (missing parentheses)");
		return formula;
	    }
	    restFormula = subformula;
	} else if (c >= '0' && c <= '9') {
	    int i;

	    //System.out.println("formula=" + formula);
	    for (i=0; i < formula.length(); i++) {
		c = formula.charAt(i);
		if ((c < '0' || c > '9') && c != '.') {
		    break;
		}
	    }
	    try {
		value = Float.valueOf(formula.substring(0, i)).floatValue();
	    } catch (NumberFormatException e) {
		//System.out.println("Failed (number format error)");
		return formula;
	    }
	    node.type = Node_VALUE;
	    node.value = value;
	    //node.print(3);
	    restFormula = formula.substring(i);
	    //System.out.println("value= " + value + " i=" + i +
		//		       " rest = " + restFormula);
	    return restFormula;
	} else if (c >= 'A' && c <= 'Z') {
	    int i;

	    column = c - 'A';
	    restFormula = formula.substring(1);
	    for (i=0; i < restFormula.length(); i++) {
		c = restFormula.charAt(i);
		if (c < '0' || c > '9') {
		    break;
		}
	    }
	    row = Float.valueOf(restFormula.substring(0, i)).intValue();
	    //System.out.println("row = " + row + " column = " + column);
	    node.row = row - 1;
	    node.column = column;
	    node.type = Node_CELL;
	    //node.print(3);
	    if (i == restFormula.length()) {
		restFormula = null;
	    } else {
		restFormula = restFormula.substring(i);
		if (restFormula.charAt(0) == 0) {
		    return null;
		}
	    }	    
	}

	return restFormula;
    }


    public void setValue(int type, String s) {
	paused = false;
	if (this.type == Cell_URL) {
	    updaterThread.stop();
	    updaterThread = null;
	}

	valueString = new String(s);
	this.type = type;
	needRedisplay = true;
	switch (type) {
	  case Cell_VALUE:
	    setValue(Float.valueOf(s).floatValue());
	    break;
	  case Cell_LABEL:
	    printString = "l" + valueString;
	    break;
	  case Cell_URL:
	    printString = "u" + valueString;
	    updaterThread = new CellUpdater(this);
	    updaterThread.start();
	    break;
	  case Cell_FORMULA:
	    parseFormula(valueString, parseRoot = new Node());
	    printString = "f" + valueString;
	    break;
	}
	app.recalculate();
    }

    public String getValueString() {
	return valueString;
    }

    public String getPrintString() {
	return printString;
    }

    public void select() {
	selected = true;
	paused = true;
    }
    public void deselect() {
	selected = false;
	paused = false;
	needRedisplay = true;
	app.repaint();
    }
    public void paint(Graphics g, int x, int y) {
	if (selected) {
	    g.setColor(highlightColor);
	} else {
	    g.setColor(bgColor);
	}
	g.fillRect(x, y, width - 1, height);
	if (valueString != null) {
	    switch (type) {
	      case Cell_VALUE:
	      case Cell_LABEL:
		g.setColor(fgColor);
		break;
	      case Cell_FORMULA:
		g.setColor(Color.red);
		break;
	      case Cell_URL:
		g.setColor(Color.blue);
		break;
	    }
	    if (transientValue){
		g.drawString("" + value, x, y + (height / 2) + 5);
	    } else {
		if (valueString.length() > 14) {
		    g.drawString(valueString.substring(0, 14),
				 x, y + (height / 2) + 5);
		} else {
		    g.drawString(valueString, x, y + (height / 2) + 5);
		}
	    }
	}
	needRedisplay = false;
    }
}

class Node {

    int		type;
    Node 	left;
    Node 	right;
    int  	row;
    int  	column;
    float	value;
    char	op;

    public Node() {
	left = null;
	right = null;
	value = 0;
	row = -1;
	column = -1;
	op = 0;
	type = Node_VALUE;
    }
    public Node(Node n) {
	left = n.left;
	right = n.right;
	value = n.value;
	row = n.row;
	column = n.column;
	op = n.op;
	type = n.type;
    }
    public void indent(int ind) {
	for (int i = 0; i < ind; i++) {
	    System.out.print(" ");
	}
    }
    public void print(int indentLevel) {
	char l[] = new char[1];
	indent(indentLevel);
	System.out.println("NODE type=" + type);
	indent(indentLevel);
	switch (type) {
	  case Node_VALUE:
	    System.out.println(" value=" + value);
	    break;
	  case Node_CELL:
	    l[0] = (char)((int)'A' + column);
	    System.out.println(" cell=" + new String(l) + (row+1));
	    break;
	  case Node_OP:
	    System.out.println(" op=" + op);
	    left.print(indentLevel + 3);
	    right.print(indentLevel + 3);
	    break;
	}
    }
}

class InputField {
    int		maxchars = 50;
    int		cursorPos = 0;
    Applet	app;
    String	sval;
    char	buffer[];
    int		nChars;
    int		width;
    int		height;
    Color	bgColor;
    Color	fgColor;

    public InputField(String initValue, Applet app, int width, int height,
		      Color bgColor, Color fgColor) {
	this.width = width;
	this.height = height;
	this.bgColor = bgColor;
	this.fgColor = fgColor;
	this.app = app;
	buffer = new char[maxchars];
	nChars = 0;
	if (initValue != null) {
	    initValue.getChars(0, initValue.length(), this.buffer, 0);
	    nChars = initValue.length();
	}
	sval = initValue;
    }

    public void setText(String val) {
	int i;

	for (i=0; i < maxchars; i++) {
	    buffer[i] = 0;
	}
	sval = new String(val);
	if (val == null) {
	    sval = "";
	    nChars = 0;
	    buffer[0] = 0;
	} else {
	    sval.getChars(0, sval.length(), buffer, 0);
	    nChars = val.length();
	    sval = new String(buffer);
	}
    }

    public String getValue() {
	return sval;
    }

    public void paint(Graphics g, int x, int y) {
	g.setColor(bgColor);
	g.fillRect(x, y, width, height);
	if (sval != null) {
	    g.setColor(fgColor);
	    g.drawString(sval, x, y + (height / 2) + 3);
	}
    }

  public void processKey(KeyEvent e) {
    int key = e.getKeyCode();
    if (nChars < maxchars) {
      switch (key) {
      case 8: // delete
	--nChars;
	if (nChars < 0) {
	  nChars = 0;
	}
	buffer[nChars] = 0;
	sval = new String(new String(buffer));
	break;
      case 10: // return
	selected();
	break;
      default:
	if (key >= 48) { //the number 0 in the ASCII char range
	  buffer[nChars++] = e.getKeyChar();
	  sval = new String(new String(buffer));
	}
	break;
      }
    }
    app.repaint();    
  }
  
  public void keyReleased(KeyEvent e) {
  }      
  
  public void selected() {
  }
}

class SpreadSheetInput 
    extends InputField {
    
  public SpreadSheetInput(String initValue,
			  SpreadSheet app,
			  int width,
			  int height,
			  Color bgColor,
			  Color fgColor) {
    super(initValue, app, width, height, bgColor, fgColor);
  }
      
    public void selected() {
	float f;

	switch (sval.charAt(0)) {
	  case 'v':
	      String s= sval.substring(1);
	    try {
		int i;
		for (i = 0; i < s.length(); i++) {
		    char c = s.charAt(i);
		    if (c < '0' || c > '9')
			break;
		}
		s = s.substring(0, i);
		f = Float.valueOf(s).floatValue();
		((SpreadSheet)app).setCurrentValue(f);
	    } catch (NumberFormatException e) {
		System.out.println("Not a float: '" + s + "'");
	    }
	    break;
	  case 'l':
	    ((SpreadSheet)app).setCurrentValue(Cell_LABEL, sval.substring(1));
	    break;
	  case 'u':
	    ((SpreadSheet)app).setCurrentValue(Cell_URL, sval.substring(1));
	    break;
	  case 'f':
	    ((SpreadSheet)app).setCurrentValue(Cell_FORMULA, sval.substring(1));
	    break;
	}
    }
}

%>
</body>
</html>
