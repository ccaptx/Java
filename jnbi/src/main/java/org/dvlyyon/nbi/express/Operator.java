package org.dvlyyon.nbi.express;

public class Operator {
    public static final String AND     = "and";
    public static final String OR      = "or";
    public static final String NOT     = "not";
    public static final String GRT     = "greater";
    public static final String LS      = "less";
    public static final String EQL     = "equal";
    public static final String NOTEQL  = "notEqual";
    public static final String GRTEQL  = "greaterEqual";
    public static final String LSEQL   = "lessEqual";
    public static final String MTCH    = "match";
    public static final String CTN     = "contain";
    public static final String TOP     = "top";
    public static final String LAST    = "last";
    public static final String SORT    = "sort";

    static final String [] setFunctionList = {AND,OR,NOT};
    static final String [] compareFunctionList = {GRT,LS,EQL,NOTEQL,GRTEQL,LSEQL};
    static final String [] patternFunctionList = {MTCH,CTN};
    static final String [] subsetFunctionList = {TOP,LAST,SORT};
    public enum OP {
        AND,
        OR,
        NOT,
        GRT,
        LS,
        EQL,
        NOTEQL,
        GRTEQL,
        LSEQL,
        MTCH,
        CTN,
        TOP,
        LAST,
        SORT,
        NA //unknown operator
    }

    public Operator() {
        // TODO Auto-generated constructor stub
    }

    public static boolean include(String name, String[] names) {
        for (String n:names) {
            if (name.equals(n)) return true;
        }
        return false;
    }

    public static boolean isFunction(String name) {
        return isCompareFunction(name) || isPatternFunction(name) ||
                isSetFunction(name) || isSubsetFunction(name);
    }

    public static boolean isCompareFunction(String name) {
        return include(name,compareFunctionList);
    }

    public static boolean isPatternFunction(String name) {
        return include(name,patternFunctionList);
    }

    public static boolean isSetFunction(String name) {
        return include(name,setFunctionList);
    }

    public static boolean isSubsetFunction(String name) {
        return include(name, subsetFunctionList);
    }

    public static OP getOperationType(String name) {
        if (name.equals(AND)) {
            return OP.AND;
        } else if (name.equals(OR)) {
            return OP.OR;
        } else if (name.equals(NOT)) {
            return OP.NOT;
        } else if (name.equals(GRT)) {
            return OP.GRT;
        } else if (name.equals(LS)) {
            return OP.LS;
        } else if (name.equals(EQL)) {
            return OP.EQL;
        } else if (name.equals(NOTEQL)) {
            return OP.NOTEQL;
        } else if (name.equals(GRTEQL)) {
            return OP.GRTEQL;
        } else if (name.equals(LSEQL)) {
            return OP.LSEQL;
        } else if (name.equals(MTCH)) {
            return OP.MTCH;
        } else if (name.equals(CTN)) {
           return OP.CTN;
        } else if (name.equals(TOP)) {
           return OP.TOP;
        } else if (name.equals(LAST)) {
           return OP.LAST;
        } else if (name.equals(SORT)) {
            return OP.SORT;
        } else {
            return OP.NA;
        }
    }

    public static boolean isUnary(String name) {
        switch (getOperationType(name)) {
        case NOT:
        case TOP:
        case LAST:
        case SORT:
            return true;
        }
        return false;
    }
}
