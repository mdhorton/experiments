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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// tcas scales better than cas, but you need
// ~6+ threads to see a significant difference.

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class CasLockPerf {

    @State(Scope.Benchmark)
    public static class Shared {
        private final AtomicBoolean state = new AtomicBoolean();
    }

    @Benchmark
    @Group("cas")
    @GroupThreads(6)
    public void cas(final Shared shared) {
        while (shared.state.getAndSet(true)) {}
        shared.state.set(false);
    }

    @Benchmark
    @Group("tcas")
    @GroupThreads(6)
    public void tcas(final Shared shared) {
        while (true) {
            while (shared.state.get()) {}
            if (!shared.state.getAndSet(true)) break;
        }

        shared.state.set(false);
    }
}
