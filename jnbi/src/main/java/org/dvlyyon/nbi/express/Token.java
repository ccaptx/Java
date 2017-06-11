package org.dvlyyon.nbi.express;

import static org.dvlyyon.nbi.CommonMetadata.*;

public class Token {

    String  name;

	int 	startAt;
    int     nextPosition;
    char    brokenBy;

    public boolean isFunction() {
        return brokenBy == '(' && Operator.isFunction(name);
    }

    public boolean isColumn() {
        return ",".indexOf(brokenBy)>=0 && name.matches("^\\$\\d+$");
    }

    public boolean isConstant() {
        return ")".indexOf(brokenBy)>=0 && !name.matches(".*[\\(\\)].*");
    }

    public boolean isUnary() {
        return Operator.isUnary(name);
    }
    
    public boolean isSet() {
    	return Operator.isSetFunction(name);
    }
    
    public boolean isSubSet() {
    	return Operator.isSubsetFunction(name);
    }

	public boolean isHelperCall() {
		// TODO Auto-generated method stub
		String prefix = HELPER_METHOD_INVOKE_IDENTIFIER_PREFIX;
		String suffix  = HELPER_METHOD_INVOKE_IDENTIFIER_SUFFIX;
		return ")".indexOf(brokenBy)>=0 && name.startsWith(prefix) && name.endsWith(suffix);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
