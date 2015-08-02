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

package net.nostromo.experiments.misc;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// this shows how volatile reads and volatile writes interfere with each other.
// change @GroupThreads(X) to see the effects.
// when both are 1 they slow down, expecially the write.
// when one is 0, the other is fast of course.

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class VolatilePerf {

    @State(Scope.Benchmark)
    public static class Shared {
        private final AtomicLong vol = new AtomicLong();
    }

    @Benchmark
    @Group("volatile")
    @GroupThreads(1)
    public void write(final Blackhole hole, final Shared shared) {
        hole.consume(shared.vol.incrementAndGet());
    }

    @Benchmark
    @Group("volatile")
    @GroupThreads(1)
    public void read(final Blackhole hole, final Shared shared) {
        hole.consume(shared.vol.get());
    }
}
