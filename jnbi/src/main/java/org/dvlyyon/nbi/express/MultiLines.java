package org.dvlyyon.nbi.express;

import java.util.ArrayList;

import org.dvlyyon.nbi.util.CommonUtils;

public class MultiLines {
    public static final String COLUMN_SEPARATOR = "[ \t]+";

    private ArrayList<Line> contents;

    public MultiLines() {
        contents = new ArrayList<Line> ();
    }

    public void addLine(Line line) {
        contents.add(line);
    }

    public void addLine(int no, String line, int [] columnPositions) {//this is only used by compiler to create multitable object from string
    	if (columnPositions == null)
    		contents.add(new Line(no,line));
    	else
    		contents.add(new FixedLine(no,line,columnPositions));
    }

    public int size() {
        return contents.size();
    }

    public Line getLine(int index) {
        return contents.get(index);
    }

    public boolean contain(Line line) {
        return contents.contains(line);
    }

    public void merge(MultiLines table1, int index1, MultiLines table2, int index2, MultiLines targetTable) {
        if (index1 == table1.size() && index2 == table2.size()) return;
        if (index1 == table1.size()) {
            targetTable.contents.addAll(table2.contents.subList(index2, table2.size()));
            index2 = table2.size();
            return;
        } else if (index2 == table2.size()) {
            targetTable.contents.addAll(table1.contents.subList(index1, table1.size()));
            index1 = table1.size();
            return;
        }
        Line line1 = table1.getLine(index1);
        Line line2 = table2.getLine(index2);
        if (line1.compareTo(line2)>0) {
            targetTable.addLine(line2);
            merge(table1,index1,table2,index2+1,targetTable);
        } else if (line1.compareTo(line2) == 0) {
            targetTable.addLine(line1);
            merge(table1,index1+1,table2,index2+1,targetTable);
        } else {
            targetTable.addLine(line1);
            merge(table1,index1+1,table2,index2,targetTable);
        }
    }

    public MultiLines merge(MultiLines table) {
        MultiLines newTable;
        if (this.size() == 0) newTable = table;
        else if (table.size()==0) newTable = this;
        else {
            newTable = new MultiLines();
            merge(this, 0, table, 0, newTable);
        }
//        if (newTable.size()>this.size()) {
//            this.contents.clear();
//            this.contents.addAll(newTable.contents);
//        }
        return newTable;
    }

    public MultiLines subtract(MultiLines table) {
        MultiLines newTable;
        if (table.size()==0 || this.size()==0){
            newTable = this;
            return newTable;
        }
        newTable = new MultiLines();
        for (Line l:contents) {
            if (table.contain(l)) continue;
            newTable.addLine(l);
        }
        return newTable;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Line l:contents) {
            sb.append(l.no+": "+l.line+"\n");
        }
        return sb.toString();
    }

    public MultiLines top(int howMany) {
        if (howMany >= this.size()) return this;
        MultiLines table = new MultiLines();
        table.contents.addAll(this.contents.subList(0, howMany));
        return table;
    }

    public MultiLines last(int howMany) {
        if (howMany >= this.size()) return this;
        MultiLines table = new MultiLines();
        table.contents.addAll(this.contents.subList(this.size()-howMany, this.size()));
        return table;
    }

    public MultiLines sor(int column) {
        //TBD
        return this;
    }
    
    /**
     * if columnArray include $0 and other column, then the other column will be ignored
     * @param columnArray
     * @return
     */
    public String getColumns(String [] columnArray) {
    	StringBuilder sbc = new StringBuilder();
    	StringBuilder sbl = new StringBuilder();
		StringBuilder sb = new StringBuilder();
    	boolean includeSelectLine = false;
    	for (Line line:contents) {
    		sb.delete(0, sb.length()); //clear
			boolean first = true;
    		boolean ignoreLine = false;
    		for (String column:columnArray) {
    			if (includeSelectLine || column.trim().equals("$0")) {//select the whole line
    				sb.append(line.line);
    				includeSelectLine = true;
    				break;
    			} else {
    				String c = line.getColumn(CommonUtils.parseInt(column.substring(1)));
    				if (c==null) {
    					ignoreLine = true;
    					break;
    				}
    				if (first) { 
    					sb.append(c); 
    					first = false;
    				} else sb.append(" "+c);
    			}
    		}
    		if (!ignoreLine) {
    			if (includeSelectLine) sbl.append(sb.toString()+"\n");
    			else sbc.append(sb.toString()+"\n");
    		}
    	}
    	if (includeSelectLine) return sbl.toString();
    	return sbc.toString();
    }

    public static void main(String argv[]) {
        MultiLines table1 = new MultiLines();
        table1.addLine(0,"this is 0 line", null);
        table1.addLine(1,"this is 1 line", null);
        table1.addLine(2,"this is 2 line", null);
        MultiLines table2 = new MultiLines();
        table2.addLine(0,"this is 0 line", null);
        table2.addLine(table1.getLine(1));
        table2.addLine(table1.getLine(2));
        MultiLines table3 = table1.merge(table2);
        MultiLines table4 = table1.subtract(table2);
        System.out.println(table1);
        System.out.println(table2);
        System.out.println(table3);
        System.out.println(table4);
    }
}
