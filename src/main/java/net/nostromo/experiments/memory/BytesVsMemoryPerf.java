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
import sun.misc.Unsafe;

import java.util.concurrent.TimeUnit;

/**
 * This experiment indicates that there is little difference in speed
 * between accessing offheap memory vs. onheap byte[].
 * <p>
 * The reason I conducted this experiment is that I wanted to determine the
 * fastest way to convert offheap C/C++ data structures into onheap java data
 * structures.
 * <p>
 * My first idea was to copy the offheap bytes in bulk into intermediate
 * onheap byte arrays. Then to convert the byte arrays to java data
 * structures.
 * <p>
 * My assumption was that it would be costly to invoke the Unsafe getters
 * frequently. But this experiment indicates that the intermediary byte array
 * is not needed. Its faster to simply read the bytes directly from offheap
 * into the given data structure.
 * <p>
 * In my previous experiment, BytesPerf, I learned that unsafe is faster than
 * ByteBuffer, which is why I'm not using ByteBuffer here.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class BytesVsMemoryPerf {

    private Unsafe unsafe;

    private byte[] arr;
    private long memory;
    private Foo foo;

    @Setup
    public void setup() {
        LibcUtil.util.setCpu(8);
        unsafe = TheUnsafe.unsafe;

        arr = new byte[58];
        memory = unsafe.allocateMemory(arr.length);
        foo = new Foo();
    }

    @TearDown
    public void teardown() {
        unsafe.freeMemory(memory);
    }

    @Benchmark
    public void onHeapWrite() {
        foo.onheapWrite(arr, 0);
    }

    @Benchmark
    public void onHeapRead() {
        foo.onheapRead(arr, 0);
    }

    @Benchmark
    public void offHeapRead() {
        foo.offheapRead(memory);
    }

    @Benchmark
    public void offHeapWrite() {
        foo.offheapWrite(memory);
    }

    private class Foo {

        public short var1 = 11;
        public short var2 = 12;
        public short var3 = 13;
        public long var4 = 14;
        public long var5 = 15;
        public long var6 = 16;
        public int var7 = 17;
        public int var8 = 18;
        public int var9 = 19;
        public byte[] var10 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

        public void onheapRead(final byte[] arr, long offset) {
            var1 = unsafe.getShort(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 2;
            var2 = unsafe.getShort(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 2;
            var3 = unsafe.getShort(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 2;
            var4 = unsafe.getLong(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 8;
            var5 = unsafe.getLong(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 8;
            var6 = unsafe.getLong(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 8;
            var7 = unsafe.getInt(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 4;
            var8 = unsafe.getInt(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 4;
            var9 = unsafe.getInt(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET);
            offset += 4;
            unsafe.copyMemory(arr, offset, var10, Unsafe.ARRAY_BYTE_BASE_OFFSET, var10.length);
        }

        public void onheapWrite(final byte[] arr, long offset) {
            unsafe.putShort(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var1);
            offset += 2;
            unsafe.putShort(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var2);
            offset += 2;
            unsafe.putShort(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var3);
            offset += 2;
            unsafe.putLong(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var4);
            offset += 8;
            unsafe.putLong(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var5);
            offset += 8;
            unsafe.putLong(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var6);
            offset += 8;
            unsafe.putInt(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var7);
            offset += 4;
            unsafe.putInt(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var8);
            offset += 4;
            unsafe.putInt(arr, offset + Unsafe.ARRAY_BYTE_BASE_OFFSET, var9);
            offset += 4;
            unsafe.copyMemory(var10, Unsafe.ARRAY_BYTE_BASE_OFFSET, arr, offset, var10.length);
        }

        public void offheapRead(long offset) {
            var1 = unsafe.getShort(offset);
            offset += 2;
            var2 = unsafe.getShort(offset);
            offset += 2;
            var3 = unsafe.getShort(offset);
            offset += 2;
            var4 = unsafe.getLong(offset);
            offset += 8;
            var5 = unsafe.getLong(offset);
            offset += 8;
            var6 = unsafe.getLong(offset);
            offset += 8;
            var7 = unsafe.getInt(offset);
            offset += 4;
            var8 = unsafe.getInt(offset);
            offset += 4;
            var9 = unsafe.getInt(offset);
            offset += 4;
            unsafe.copyMemory(null, offset, var10, Unsafe.ARRAY_BYTE_BASE_OFFSET, var10.length);
        }

        public void offheapWrite(long offset) {
            unsafe.putShort(offset, var1);
            offset += 2;
            unsafe.putShort(offset, var2);
            offset += 2;
            unsafe.putShort(offset, var3);
            offset += 2;
            unsafe.putLong(offset, var4);
            offset += 8;
            unsafe.putLong(offset, var5);
            offset += 8;
            unsafe.putLong(offset, var6);
            offset += 8;
            unsafe.putInt(offset, var7);
            offset += 4;
            unsafe.putInt(offset, var8);
            offset += 4;
            unsafe.putInt(offset, var9);
            offset += 4;
            unsafe.copyMemory(var10, Unsafe.ARRAY_BYTE_BASE_OFFSET, null, offset, var10.length);
        }
    }
}
