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

import net.nostromo.experiments.Util;
import net.nostromo.libc.LibcUtil;
import net.nostromo.libc.TheUnsafe;
import sun.misc.Unsafe;

/**
 * This experiment indicates that there is little difference in speed
 * between accessing offheap memory vs. onheap byte[]. Offheap is just a tiny
 * bit faster, but its by a small margin.
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
 * ByteBuffer, which is why I'm using it here.
 * <p>
 * Example results:
 * <p>
 * iteration 3 of 10
 * onheap: 34,881,496 ops/sec
 * offheap: 36,097,836 ops/sec
 */
public class BytesVsMemoryPerf {

    private static final Unsafe unsafe = TheUnsafe.unsafe;
    private static final LibcUtil util = LibcUtil.util;

    public static void main(String[] args) throws Exception {
        final int iterations = 10;
        final long limit = 100_000_000L;

        util.setCpu(8);

        final byte[] arr = new byte[58];
        final long memory = unsafe.allocateMemory(arr.length);

        final Foo foo = new Foo();

        for (int x = 0; x < iterations; x++) {
            System.out.printf("\niteration %d of %d\n", (x + 1), iterations);

            // test onheap bytes
            long start = System.nanoTime();
            for (long idx = 0; idx < limit; idx++) {
                foo.onheapWrite(arr, 0);
                foo.onheapRead(arr, 0);
            }
            long elap = System.nanoTime() - start;
            System.out.printf("onheap: %,.0f ops/sec\n", Util.tps(limit, elap));

            // test offheap memory
            start = System.nanoTime();
            for (long idx = 0; idx < limit; idx++) {
                foo.offheapWrite(memory);
                foo.offheapRead(memory);
            }
            elap = System.nanoTime() - start;
            System.out.printf("offheap: %,.0f ops/sec\n", Util.tps(limit, elap));
        }

        unsafe.freeMemory(memory);
        assert foo.var1 == 11;
    }

    public static class Foo {
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
