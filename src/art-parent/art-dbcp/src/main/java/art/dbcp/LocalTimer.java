/*
 * art-dbcp. Provides connection pooling.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
