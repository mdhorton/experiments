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

    private static final Libc libc = Libc.libc;

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

        final long[] vals = new long[2];

        for (long idx = 0; idx < limit; idx++) {
            libc.clock_gettime(clockId, ptr_timespec);
            timespec.read();
            if (timespec.tv_sec < 0) return idx;
            if (idx > limit - 3) vals[((int) (limit - idx - 1))] = timespec.tv_nsec;
        }

        final long diff = vals[0] - vals[1];
        System.out.printf("last 2 values: %,d  %,d  diff: %,d\n", vals[1], vals[0], diff);

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

        final CurrentTime time = new CurrentTime(limit);

        for (int x = 0; x < iterations; x++) {
            System.out.printf("\niteration %d of %d\n", (x + 1), iterations);

            time.results("nano", System.nanoTime(), time.testNanos());
            time.results("millis", System.nanoTime(), time.testMillis());
            time.results("monotonic", System.nanoTime(), time.testNative(CLOCK_MONOTONIC));
            time.results("monotonic coarse", System.nanoTime(),
                    time.testNative(CLOCK_MONOTONIC_COARSE));
            time.results("monotonic raw", System.nanoTime(), time.testNative(CLOCK_MONOTONIC_RAW));
            time.results("realtime", System.nanoTime(), time.testNative(CLOCK_REALTIME));
            time.results("realtime coarse", System.nanoTime(),
                    time.testNative(CLOCK_REALTIME_COARSE));
        }
    }
}
