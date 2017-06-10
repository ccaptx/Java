package org.dvlyyon.study.escape;

public class Main {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String s1 = "\u001B[1,4mHello,world\u001B[0m\r\r\n";
        String s2 = "\u001B[3,5mHello,world\u001B[0m\r\n";
        String s3 = "\u001B[1,4mThis is only a test\u001B[0m\r\r\n";
        String ss = s1 + s2 + s3;
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(ss);
        ss = ss.replaceAll("\u001B\\[1,4m", "<u><b>");
        ss = ss.replaceAll("\u001B\\[0m", "</b></u>");
        System.out.println(ss);
    }
}
