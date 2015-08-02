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

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class BytesObjectPerf {

    private final boolean useVar10 = false;

    private Unsafe unsafe;
    private Foo foo;

    private long pointer;
    private byte[] byteArray;

    @Setup
    public void setup() {
        LibcUtil.util.setLastCpu();
        unsafe = TheUnsafe.unsafe;
        foo = new Foo();

        pointer = unsafe.allocateMemory(Foo.BYTES);
        byteArray = new byte[Foo.BYTES];
    }

    @TearDown
    public void teardown() {
        unsafe.freeMemory(pointer);
    }

    @Benchmark
    public void readByteArray(final Blackhole hole) {
        hole.consume(foo.byteArrayRead(byteArray, 0L));
    }

    @Benchmark
    public void writeByteArray() {
        foo.byteArrayWrite(byteArray, 0L);
    }

    @Benchmark
    public void readPointer(final Blackhole hole) {
        hole.consume(foo.readPointer(pointer));
    }

    @Benchmark
    public void writePointer() {
        foo.writePointer(pointer);
    }

    private class Foo {

        public final static int BYTES = 58;

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

        public long byteArrayRead(final byte[] arr, long offset) {
            offset += Unsafe.ARRAY_BYTE_BASE_OFFSET;
            var1 = unsafe.getShort(arr, offset);
            offset += 2;
            var2 = unsafe.getShort(arr, offset);
            offset += 2;
            var3 = unsafe.getShort(arr, offset);
            offset += 2;
            var4 = unsafe.getLong(arr, offset);
            offset += 8;
            var5 = unsafe.getLong(arr, offset);
            offset += 8;
            var6 = unsafe.getLong(arr, offset);
            offset += 8;
            var7 = unsafe.getInt(arr, offset);
            offset += 4;
            var8 = unsafe.getInt(arr, offset);
            offset += 4;
            var9 = unsafe.getInt(arr, offset);
            if (useVar10) {
                offset += 4;
                unsafe.copyMemory(arr, offset, var10, Unsafe.ARRAY_BYTE_BASE_OFFSET, var10.length);
            }

            return var1 + var2 - var3 + var4 - var5 + var6 - var7 + var8 - var9;
        }

        public void byteArrayWrite(final byte[] arr, long offset) {
            offset += Unsafe.ARRAY_BYTE_BASE_OFFSET;
            unsafe.putShort(arr, offset, var1);
            offset += 2;
            unsafe.putShort(arr, offset, var2);
            offset += 2;
            unsafe.putShort(arr, offset, var3);
            offset += 2;
            unsafe.putLong(arr, offset, var4);
            offset += 8;
            unsafe.putLong(arr, offset, var5);
            offset += 8;
            unsafe.putLong(arr, offset, var6);
            offset += 8;
            unsafe.putInt(arr, offset, var7);
            offset += 4;
            unsafe.putInt(arr, offset, var8);
            offset += 4;
            unsafe.putInt(arr, offset, var9);
            if (useVar10) {
                offset += 4;
                unsafe.copyMemory(var10, Unsafe.ARRAY_BYTE_BASE_OFFSET, arr, offset, var10.length);
            }
        }

        public long readPointer(long offset) {
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
            if (useVar10) {
                offset += 4;
                unsafe.copyMemory(null, offset, var10, Unsafe.ARRAY_BYTE_BASE_OFFSET, var10.length);
            }

            return var1 + var2 - var3 + var4 - var5 + var6 - var7 + var8 - var9;
        }

        public void writePointer(long offset) {
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
            if (useVar10) {
                offset += 4;
                unsafe.copyMemory(var10, Unsafe.ARRAY_BYTE_BASE_OFFSET, null, offset, var10.length);
            }
        }
    }
}
