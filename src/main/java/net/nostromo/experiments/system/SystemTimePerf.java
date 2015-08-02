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

import net.nostromo.libc.Libc;
import net.nostromo.libc.LibcConstants;
import net.nostromo.libc.LibcUtil;
import net.nostromo.libc.struct.system.Timespec;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

// RDTSCP is the fastest

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class SystemTimePerf implements LibcConstants {

    private final Libc libc = Libc.libc;
    private final Timespec timespec = new Timespec();
    private final long ptr_timespec = timespec.pointer();

    @Setup
    public void setup() {
        LibcUtil.util.setLastCpu();
    }

    @Benchmark
    public void nanoTime(final Blackhole hole) {
        hole.consume(System.nanoTime());
    }

    @Benchmark
    public void currentTimeMillis(final Blackhole hole) {
        hole.consume(System.currentTimeMillis());
    }

    @Benchmark
    public void tsc(final Blackhole hole) {
        hole.consume(libc.rdtscp());
    }

    @Benchmark
    public void monotonic(final Blackhole hole) {
        libc.clock_gettime(CLOCK_MONOTONIC, ptr_timespec);
        timespec.read();
        hole.consume(timespec.tv_sec);
    }

    @Benchmark
    public void monotonicCoarse(final Blackhole hole) {
        libc.clock_gettime(CLOCK_MONOTONIC_COARSE, ptr_timespec);
        timespec.read();
        hole.consume(timespec.tv_sec);
    }

    @Benchmark
    public void monotonicRaw(final Blackhole hole) {
        libc.clock_gettime(CLOCK_MONOTONIC_RAW, ptr_timespec);
        timespec.read();
        hole.consume(timespec.tv_sec);
    }

    @Benchmark
    public void realtime(final Blackhole hole) {
        libc.clock_gettime(CLOCK_REALTIME, ptr_timespec);
        timespec.read();
        hole.consume(timespec.tv_sec);
    }

    @Benchmark
    public void realtimeCoarse(final Blackhole hole) {
        libc.clock_gettime(CLOCK_REALTIME_COARSE, ptr_timespec);
        timespec.read();
        hole.consume(timespec.tv_sec);
    }
}
