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
import net.nostromo.libc.struct.system.Timespec;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

// all of these have a minimum sleep of ~58 micros

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class SleepPerf implements LibcConstants {

    @State(Scope.Benchmark)
    public static class Shared {
        private final Libc libc = Libc.libc;
        private Timespec req;
        private Timespec rem;
        private long ptr_req;
        private long ptr_rem;

        @Setup
        public void setup() {
            req = new Timespec();
            rem = new Timespec();

            req.tv_sec = 0;
            req.tv_nsec = 1;

            ptr_req = req.pointer();
            ptr_rem = rem.pointer();
        }
    }

    @Benchmark
    public void parkNanos() {
        LockSupport.parkNanos(1);
    }

    @Benchmark
    public void nanoSleep(final Shared shared) {
        shared.libc.nanosleep(shared.ptr_req, shared.ptr_rem);
    }

    @Benchmark
    public void clockNanoSleep(final Shared shared) {
        shared.libc.clock_nanosleep(CLOCK_REALTIME, 0, shared.ptr_req, shared.ptr_rem);
    }
}
