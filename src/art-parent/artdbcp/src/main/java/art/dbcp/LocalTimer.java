/*
 * Copyright (C)   Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the LGPL License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *  
 */
package art.dbcp;

/**
 * 
 * @author Erico Liboni
 */
public class LocalTimer extends Thread {

    TimerListener t;
    long interval;

    /**
     * 
     * @param tl
     * @param interv
     */
    public LocalTimer(TimerListener tl, long interv) {
        t = tl;
        interval = interv;
    }

    @Override
    public void run() {
        try {
            while (true) {
                sleep(interval);
                t.timeElapsed();
            }
        } catch (InterruptedException e) {
            t.timeElapsed();
        }
    }
}
