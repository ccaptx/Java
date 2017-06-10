package org.dvlyyon.study.interfaces;

import java.util.Vector;

public class Worker implements DriverInf, MgrInf {

    @Override
    public void sendCmd(String cmd, Vector err) {
        // TODO Auto-generated method stub
        System.out.println("I'm sending command....."+this);
    }

    @Override
    public String parse() {
        // TODO Auto-generated method stub
        return "I'm paresing "+this;
    }

}
