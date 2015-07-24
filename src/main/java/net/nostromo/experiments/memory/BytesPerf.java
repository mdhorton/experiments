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
 * into a custom data structure. This shows the speed of converting bytes
 * from various buffers into primitives.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class BytesPerf {

    private final long l = 10;

    private Unsafe unsafe;

    private long pointer;
    private byte[] byteArray;
    private LongBuffer lb;
    private LongBuffer lbd;
    private ByteBuffer bb;
    private ByteBuffer bbd;

    @Setup
    public void setup() {
        LibcUtil.util.setCpu(8);
        unsafe = TheUnsafe.unsafe;

        pointer = unsafe.allocateMemory(Long.BYTES);
        byteArray = new byte[Long.BYTES];
        lb = LongBuffer.allocate(1);
        lbd = ByteBuffer.allocateDirect(Long.BYTES).asLongBuffer();
        bb = ByteBuffer.allocate(Long.BYTES);
        bbd = ByteBuffer.allocateDirect(Long.BYTES);

        unsafe.putLong(pointer, l);
        unsafe.putLong(byteArray, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET, l);
        lb.put(0, l);
        lbd.put(0, l);
        bb.putLong(0, l);
        bbd.putLong(0, l);
    }

    @TearDown
    public void teardown() {
        unsafe.freeMemory(pointer);
    }

    @Benchmark
    public void readPointer(final Blackhole hole) {
        hole.consume(unsafe.getLong(pointer));
    }

    @Benchmark
    public void readByteArray(final Blackhole hole) {
        hole.consume(unsafe.getLong(byteArray, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET));
    }

    @Benchmark
    public void readLongBuffer(final Blackhole hole) {
        hole.consume(lb.get(0));
    }

    @Benchmark
    public void readLongBufferDirect(final Blackhole hole) {
        hole.consume(lbd.get(0));
    }

    @Benchmark
    public void readByteBuffer(final Blackhole hole) {
        hole.consume(bb.getLong(0));
    }

    @Benchmark
    public void readByteBufferDirect(final Blackhole hole) {
        hole.consume(bbd.getLong(0));
    }

    @Benchmark
    public void writePointer() {
        unsafe.putLong(pointer, l);
    }

    @Benchmark
    public void writeByteArray() {
        unsafe.putLong(byteArray, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET, l);
    }

    @Benchmark
    public void writeLongBuffer() {
        lb.put(0, l);
    }

    @Benchmark
    public void writeLongBufferDirect() {
        lbd.put(0, l);
    }

    @Benchmark
    public void writeByteBuffer() {
        bb.putLong(0, l);
    }

    @Benchmark
    public void writeByteBufferDirect() {
        bbd.putLong(0, l);
    }
}
