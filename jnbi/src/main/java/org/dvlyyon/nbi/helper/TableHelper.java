package org.dvlyyon.nbi.helper;

import java.util.regex.Pattern;

import org.dvlyyon.nbi.express.FixedLine;
import org.dvlyyon.nbi.express.Line;
import org.dvlyyon.nbi.express.MultiLines;
import org.dvlyyon.nbi.express.TableParseException;

public class TableHelper {

	public static Pattern [] transIgnoredLines (String [] ignoreLines) {
        if(ignoreLines == null) return null;
        Pattern [] ignoreLinePatterns = new Pattern[ignoreLines.length];
        for (int i=0; i<ignoreLines.length; i++) {
            ignoreLinePatterns[i]=Pattern.compile(ignoreLines[i]);
        }
        return ignoreLinePatterns;
    }

    public static boolean ignoreLine(String line, Pattern [] ignoreLines) {
    	line = line.trim();
        if (ignoreLines == null) return false;
        for (Pattern p:ignoreLines) {
            if (p.matcher(line).matches())
                return true;
        }
        return false;
    }

	public static MultiLines getTable(String content, int column, String [] ignoredLines) {
        MultiLines table = new MultiLines();
        if (content == null) return table;
        Pattern [] ignoredLinePatterns = transIgnoredLines(ignoredLines);
        String [] lines = content.split("\n");
        int lineNo = 0;
        int [] columnPositions = null;
        for (String line:lines) {
            lineNo++;
            if (ignoreLine(line, ignoredLinePatterns)) {
                continue;
            }
            if (column > 0) {
            	if (columnPositions == null) {
            		if (Line.getSizeOfColumn(line) == column) {
            			columnPositions = FixedLine.getHeaderLinePositions(line,column);
            		} else 
            			continue; //ignore line before table header
            	}
            	if (Line.getSizeOfColumn(line) >= column)
            		table.addLine(lineNo,line,columnPositions);
            } else {
            	table.addLine(lineNo, line, null);
            }
        }
        return table;		
	}
	
	public static HTable getTable(String content, String [] ignoredLines, String followingHeader) throws TableParseException {
        MultiLines table = new MultiLines();
        if (content == null) 
        	throw new TableParseException("content is empty");
        Pattern [] ignoredLinePatterns = transIgnoredLines(ignoredLines);
        String [] lines = content.split("\n");
        int lineNo = 0;
        int [] columnPositions = null;
        String [] columnHeader = null;
        int columns = 0;
        for (String line:lines) {
            lineNo++;
            if (ignoreLine(line, ignoredLinePatterns)) {
                continue;
            }
            if (line.trim().isEmpty()) continue;
            if (columnPositions == null) { //try to get table header
            	if (lineNo < lines.length && lines[lineNo].trim().matches(followingHeader)) {
            		columnHeader = line.trim().split(" +");
            		columns = columnHeader.length;
            		columnPositions = FixedLine.getHeaderLinePositions(line, columns);
            	}
            } else {
            	if (Line.getSizeOfColumn(line) >= columns && line.trim().length() > columnPositions[columns-1] && !line.matches(followingHeader))
            		table.addLine(lineNo,line,columnPositions);
            } 
        }
        if (columnHeader == null) {
        	throw new TableParseException("Cannot get table header, please make sure it is a table with table header followed by "+followingHeader);
        }
        HTable htable = new HTable();
        htable.setHeader(columnHeader);
        htable.setColumnPositions(columnPositions);
        htable.setData(table);
        return htable;
	}
}
