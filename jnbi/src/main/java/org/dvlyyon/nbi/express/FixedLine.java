package org.dvlyyon.nbi.express;

/**
 * The postion is predictable for every column
 * @author djyang
 *
 */
public class FixedLine extends Line {
	int [] columnPositions = null;
	String [] columns = null;
	
	public FixedLine(int no, String line, int [] columnPositions) {
		super(no, line);
		this.columnPositions = columnPositions;
	}
	
	private void initColumn() {
		int lastPosition = columnPositions[columnPositions.length-1];
		if (lastPosition < this.line.length()) {
			int startIndex = 0;
			columns = new String [columnPositions.length];
			for (int i=1; i<columnPositions.length; i++) {
				columns[i-1] = line.substring(startIndex, columnPositions[i]).trim();
				startIndex = columnPositions[i];
			}
			columns[columns.length-1] = line.substring(lastPosition, line.length()).trim();
		}
	}
	
    public String getColumn(int column) {
    	if (column < 0) return null;
    	if (column == 0) return line.trim();
    	if (columns == null)
    		initColumn();
    	if (columns == null) return null;
        if (column > columns.length ) {
        	if (column == 99) {
        		return columns[columns.length-1];
        	}
        	return null;
        }
        return columns[column-1];   	
    }
    
    public static int [] getHeaderLinePositions(String line, int columnSize) {
    	int [] columnPositions = new int [columnSize];
    	columnPositions[0]=0;
    	char [] chars = line.toCharArray();
    	int i = 0;
    	int foundWhat = 1; //0: whitespace, 1: non-whitespace
    	int columnNum = 0;
    	while (i<chars.length && columnNum < columnSize) {
    		switch (foundWhat) {
    		case 0:
    			if (Character.isWhitespace(chars[i])) {
    				foundWhat = 1;
    			} 
    			break;
    		case 1:
    			if (!Character.isWhitespace(chars[i])) {
    				columnPositions[columnNum++]=i;
    				foundWhat = 0;
    			}
    			break;
    		}
    		i++;
    	}
    	return columnPositions;
    }
}
