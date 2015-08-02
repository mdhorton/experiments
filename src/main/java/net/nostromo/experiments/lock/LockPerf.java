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
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// reentrant lock/unlock costs
//   2 threads  ~300ns
//   3 threads  ~400ns
//   4 threads  ~500ns

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(4)
public class LockPerf {

    @State(Scope.Thread)
    public static class ThreadIndex {
        private long idx;
    }

    @State(Scope.Benchmark)
    public static class SharedLock {
        private final Lock lock = new ReentrantLock();
    }

    @Benchmark
    public void lock(final Blackhole hole, final SharedLock shared, final ThreadIndex index) {
        shared.lock.lock();
        try {
            hole.consume(index.idx++);
        }
        finally {
            shared.lock.unlock();
        }
    }
}
