package org.dvlyyon.study.exception;

public class Main {

    public Main() {
        // TODO Auto-generated constructor stub
    }

    public void throwNullPointerExceptin() {
        String s = null;
        int i = s.charAt('a');
    };

    public void execute() {
        try {
            this.throwNullPointerExceptin();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("this is a null point exception");
        }
        System.out.println("I can reach here");
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Main m = new Main();
        m.execute();
    }

}
