package org.dvlyyon.study.jni;
/* vim:set shiftwidth=4 ts=8: */
public class Main {
	public native void toSVG(String graph, String fileName);

	public static void main(String[] args) {
	    System.loadLibrary("Main");
	    Main helper = new Main();
	    StringBuilder sb = new StringBuilder();
	    sb.append("digraph G {\n").
	       append("graph [layout=dot rankdir=LR]\n").
	       append("\n").
	       append("vim [href=\"http://www.vim.org/\"]\n").
	       append("dot [href=\"http://www.graphviz.org/\"]\n").
	       append("vimdot [href=\"file:///usr/bin/vimdot\"]\n").
	       append("\n").
	       append("{vim dot} -> vimdot\n").
	       append("}\n");
	    helper.toSVG(sb.toString(), "test.svg");
	    System.out.println("complete");
	}
}
