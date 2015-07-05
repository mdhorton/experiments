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

package net.nostromo.experiments.system;

import net.nostromo.experiments.Util;
import net.nostromo.libc.Libc;
import net.nostromo.libc.LibcConstants;
import net.nostromo.libc.struct.system.Timespec;

public class CurrentTime implements LibcConstants {

    private final Libc libc = Libc.libc;

    private final long limit;

    public CurrentTime(final long limit) {
        this.limit = limit;
    }

    public long testNanos() {
        for (long idx = 0; idx < limit; idx++) {
            final long now = System.nanoTime();
            if (now < 0) return idx;
        }
        return limit;
    }

    public long testMillis() {
        for (long idx = 0; idx < limit; idx++) {
            final long now = System.currentTimeMillis();
            if (now < 0) return idx;
        }
        return limit;
    }

    public long testNative(final int clockId) {
        final Timespec timespec = new Timespec();
        final long ptr_timespec = timespec.pointer();

        for (long idx = 0; idx < limit; idx++) {
            libc.clock_gettime(clockId, ptr_timespec);
            timespec.read();
            if (timespec.tv_sec < 0) return idx;
        }

        return limit;
    }

    public void results(final String name, final long start, final long res) {
        if (res != limit) throw new RuntimeException("just a test");
        final long elap = System.nanoTime() - start;
        System.out.printf("%s: %,.0f ops/sec - %,.2f ns avg\n", name, Util.tps(limit, elap),
                (double) elap / limit);
    }

    public static void main(final String[] args) {
        final int iterations = 10;
        final long limit = 10_000_000L;

        final CurrentTime nanos = new CurrentTime(limit);

        for (int x = 0; x < iterations; x++) {
            System.out.printf("\niteration %d of %d\n", (x + 1), iterations);

            nanos.results("nano", System.nanoTime(), nanos.testNanos());
            nanos.results("millis", System.nanoTime(), nanos.testMillis());
            nanos.results("monotonic", System.nanoTime(), nanos.testNative(CLOCK_MONOTONIC));
            nanos.results("monotonic coarse", System.nanoTime(),
                    nanos.testNative(CLOCK_MONOTONIC_COARSE));
            nanos.results("monotonic raw", System.nanoTime(),
                    nanos.testNative(CLOCK_MONOTONIC_RAW));
            nanos.results("realtime", System.nanoTime(), nanos.testNative(CLOCK_REALTIME));
            nanos.results("realtime coarse", System.nanoTime(),
                    nanos.testNative(CLOCK_REALTIME_COARSE));
        }
    }
}
