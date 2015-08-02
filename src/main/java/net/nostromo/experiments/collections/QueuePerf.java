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

import java.util.concurrent.*;

// CLQ is faster than ABQ which is faster than LBQ.
// CLQ can potentially generate a lot of garbage
// because its not blocking and must poll() repeatedly.

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class QueuePerf {

    @State(Scope.Benchmark)
    public static class Shared {
        private final Object obj = new Object();
        private final BlockingQueue<Object> abq = new ArrayBlockingQueue<>(1 << 20);
        private final BlockingQueue<Object> lbq = new LinkedBlockingQueue<>();
        private final ConcurrentLinkedQueue<Object> clq = new ConcurrentLinkedQueue<>();
    }

    @Benchmark
    @Group("abq")
    public void arrayBlockingQueuePut(final Shared shared) throws Exception {
        shared.abq.put(shared.obj);
    }

    @Benchmark
    @Group("abq")
    public void arrayBlockingQueuePoll(final Blackhole hole, final Shared shared, final Control cnt)
            throws Exception {
        if (cnt.stopMeasurement) return;
        hole.consume(shared.abq.take());
    }

    @Benchmark
    @Group("lbq")
    public void linkedBlockingQueueOffer(final Shared shared) throws Exception {
        shared.lbq.put(shared.obj);
    }

    @Benchmark
    @Group("lbq")
    public void linkedBlockingQueuePoll(final Blackhole hole, final Shared shared,
            final Control cnt) throws Exception {
        if (cnt.stopMeasurement) return;
        hole.consume(shared.lbq.take());
    }

    @Benchmark
    @Group("clq")
    public void concurrentLinkedQueueOffer(final Shared shared) {
        shared.clq.offer(shared.obj);
    }

    @Benchmark
    @Group("clq")
    public void concurrentLinkedQueuePoll(final Shared shared, final Control cnt) {
        while (!cnt.stopMeasurement && shared.clq.poll() == null) {}
    }
}
