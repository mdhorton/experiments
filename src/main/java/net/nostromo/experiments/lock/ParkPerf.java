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

package net.nostromo.experiments.lock;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Control;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

// park()/unpark() costs ~27 micros

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ParkPerf {

    @State(Scope.Benchmark)
    public static class Shared {
        private final AtomicInteger idx = new AtomicInteger();
        private volatile Thread one;
        private volatile Thread two;
    }

    @State(Scope.Thread)
    public static class ThreadState {
        private int idx;
    }

    @Benchmark
    @Group("park")
    @GroupThreads(2)
    public void park(final Shared shared, final ThreadState ts, final Control control)
            throws Exception {
        if (ts.idx == 0) {
            ts.idx = shared.idx.incrementAndGet();
            if (ts.idx == 1) shared.one = Thread.currentThread();
            else shared.two = Thread.currentThread();
        }

        if (shared.one == null || shared.two == null) return;

        if (ts.idx == 1) LockSupport.unpark(shared.two);
        else LockSupport.unpark(shared.one);

        if (control.stopMeasurement) return;
        LockSupport.park();
    }
}
