package test;

import common.java.httpServer.booter;
import common.java.nlogger.nlogger;

public class TestCommon {
    public static void main(String[] args) {
        booter booter = new booter();
        try {
            System.out.println("Common");
            System.setProperty("AppName", "Common");
            booter.start(1009);
        } catch (Exception e) {
            nlogger.logout(e);
        }
    }
}
