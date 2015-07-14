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

package net.nostromo.experiments.memory;

import net.nostromo.libc.LibcUtil;
import net.nostromo.libc.TheUnsafe;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.TimeUnit;

/**
 * I did this experiment because I needed a fast way to convert bytes
 * into a custom data structure.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class BytesPerf {

    private Unsafe unsafe;

    private long memory;
    private byte[] buffer;
    private LongBuffer lb;
    private LongBuffer lbd;
    private ByteBuffer bb;
    private ByteBuffer bbd;

    @Setup
    public void setup() {
        LibcUtil.util.setCpu(8);
        unsafe = TheUnsafe.unsafe;

        memory = unsafe.allocateMemory(Long.BYTES);
        buffer = new byte[Long.BYTES];
        lb = LongBuffer.allocate(1);
        lbd = ByteBuffer.allocateDirect(Long.BYTES).asLongBuffer();
        bb = ByteBuffer.allocate(Long.BYTES);
        bbd = ByteBuffer.allocateDirect(Long.BYTES);

        final long l = -6392742284531334230L;

        unsafe.putLong(memory, l);
        unsafe.putLong(buffer, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET, l);
        lb.put(0, l);
        lbd.put(0, l);
        bb.putLong(0, l);
        bbd.putLong(0, l);
    }

    @TearDown
    public void teardown() {
        unsafe.freeMemory(memory);
    }

    @Benchmark
    public void unsafePointer(final Blackhole hole) {
        hole.consume(unsafe.getLong(memory, 0L));
    }

    @Benchmark
    public void unsafeBuffer(final Blackhole hole) {
        hole.consume(unsafe.getLong(buffer, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET));
    }

    @Benchmark
    public void longBuffer(final Blackhole hole) {
        hole.consume(lb.get(0));
    }

    @Benchmark
    public void longBufferDirect(final Blackhole hole) {
        hole.consume(lbd.get(0));
    }

    @Benchmark
    public void byteBuffer(final Blackhole hole) {
        hole.consume(bb.getLong(0));
    }

    @Benchmark
    public void byteBufferDirect(final Blackhole hole) {
        hole.consume(bbd.getLong(0));
    }
}
