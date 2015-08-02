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

package net.nostromo.experiments.collections;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.Control;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// ABQ is slightly faster, but put()/take() cost for both
// is ~28 micros, which is simular to park()/unpark()

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class QueueBlockedPerf {

    @State(Scope.Benchmark)
    public static class Shared {
        private final Object obj = new Object();
        private final BlockingQueue<Object> abq = new ArrayBlockingQueue<>(1);
        private final BlockingQueue<Object> lbq = new LinkedBlockingQueue<>(1);
    }

    @Benchmark
    @Group("abq")
    public void arrayBlockingQueuePut(final Shared shared, final Control cnt)
            throws Exception {
        if (cnt.stopMeasurement) return;
        shared.abq.put(shared.obj);
    }

    @Benchmark
    @Group("abq")
    public void arrayBlockingQueueTake(final Blackhole hole, final Shared shared, final Control cnt)
            throws Exception {
        if (cnt.stopMeasurement) return;
        hole.consume(shared.abq.take());
    }

    @Benchmark
    @Group("lbq")
    public void linkedBlockingQueuePut(final Shared shared, final Control cnt)
            throws Exception {
        if (cnt.stopMeasurement) return;
        shared.lbq.put(shared.obj);
    }

    @Benchmark
    @Group("lbq")
    public void linkedBlockingQueueTake(final Blackhole hole, final Shared shared,
            final Control cnt)
            throws Exception {
        if (cnt.stopMeasurement) return;
        hole.consume(shared.lbq.take());
    }
}
