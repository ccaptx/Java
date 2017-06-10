package org.dvlyyon.study.interfaces;

public class Main {
    public void action (DriverInf driver) {
        MgrInf obj = (MgrInf)driver;
        driver.sendCmd("Hello from world", null);
        System.out.println(obj.parse());
    }
    public static void main(String argv []) {
        Main m = new Main();
        Worker w = new Worker();
        m.action(w);
    }
}
