package org.dvlyyon.nbi.express;

public class Line implements Comparable<Line> {

    int no;
    String line;
    public Line(int no, String line) {
        this.no = no;
        this.line = line;
    }
    public int getNo() {
        return no;
    }
    public String getLine() {
        return line;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + no;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Line other = (Line) obj;
        if (no != other.no)
            return false;
        return true;
    }
    @Override
    public int compareTo(Line o) {
        return this.no - o.no;
    }
    
    public String getColumn(int column) {
    	if (column < 0) return null;
    	if (column == 0) return line.trim();
        String [] columns = line.trim().split(MultiLines.COLUMN_SEPARATOR);
        if (column > columns.length ) {
        	if (column == 99) {
        		return columns[columns.length-1];
        	}
        	return null;
        }
        return columns[column-1];   	
    }
    
    public static int getSizeOfColumn(String line) {
    	if (line == null || line.trim().isEmpty()) return 0;
    	String [] columns = line.trim().split(MultiLines.COLUMN_SEPARATOR);
    	return columns.length;
    }
}
