/*
 * Copyright (c) 2015 Mark D. Horton
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABIL-
 * ITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.nostromo.experiments;

public class PerfTest {

    private final long loopLimit;

    public PerfTest(final long loopLimit) {
        this.loopLimit = loopLimit;
    }

    public void timedTest(final String name, final Action action) {
        final long start = System.nanoTime();

        for (long y = 0; y < loopLimit; y++) {
            if (action.execute() == null) {
                throw new RuntimeException("null");
            }
        }

        final long elapsed = System.nanoTime() - start;
        System.out.printf("%s %,.0f per second\n", name, loopLimit / (elapsed / (double) 1_000_000_000));
    }

    public interface Action {
        Object execute();
    }
}
