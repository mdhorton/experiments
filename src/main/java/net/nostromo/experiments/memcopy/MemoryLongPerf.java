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

package net.nostromo.experiments.memcopy;

import net.nostromo.libc.TheUnsafe;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Random;

public class MemoryLongPerf {

    public static void main(String[] args) throws Exception {
        final Unsafe unsafe = TheUnsafe.unsafe;
        final long offset = Unsafe.ARRAY_BYTE_BASE_OFFSET;

        final int len = 10;
        final long pointer = unsafe.allocateMemory(len * 8);
        final byte[] buffer = new byte[len * 8];
        final LongBuffer lb = LongBuffer.allocate(len);
        final LongBuffer dlb = ByteBuffer.allocateDirect(len * 8).asLongBuffer();
        final ByteBuffer bb = ByteBuffer.allocate(len * 8);
        final ByteBuffer dbb = ByteBuffer.allocateDirect(len * 8);

        final Random rand = new Random();

        for (int x = 0; x < len; x++) {
            unsafe.putLong(pointer + (x * 8), rand.nextLong());
            unsafe.putLong(buffer, offset + (x * 8), rand.nextLong());
            lb.put(x, rand.nextLong());
            dlb.put(x, rand.nextLong());
            bb.putLong(x * 8, rand.nextLong());
            dbb.putLong(x * 8, rand.nextLong());
        }

        final Timer timer = new Timer(100_000_000L, len);

        for (int x = 0; x < 10; x++) {
            timer.timed("ByteBuffer", new Looper(idx -> bb.getLong(idx * 8)));
            timer.timed("Direct ByteBuffer", new Looper(idx -> dbb.getLong(idx * 8)));
            timer.timed("Direct LongBuffer", new Looper(dlb::get));
            timer.timed("Unsafe byte[]", new Looper(idx -> unsafe.getLong(buffer, offset + (idx * 8))));
            timer.timed("Unsafe memory", new Looper(idx -> unsafe.getLong(pointer + (idx * 8))));
            timer.timed("LongBuffer", new Looper(lb::get));
            System.out.println("*******************");
        }
    }

    public static class Timer {
        private final long loopLimit;
        private final int arrayLen;

        public Timer(final long loopLimit, final int arrayLen) {
            this.loopLimit = loopLimit;
            this.arrayLen = arrayLen;
        }

        public void timed(final String name, final Looper looper) {
            final long start = System.nanoTime();

            long max = Long.MIN_VALUE;

            for (long y = 0; y < loopLimit; y++) {
                final long val = looper.loop(arrayLen);
                if (val > max) max = val;
            }

            final long elapsed = System.nanoTime() - start;
            System.out.printf("%s %,.0f per second  %d\n", name, loopLimit / (elapsed / (double) 1_000_000_000), max);
        }
    }

    public static class Looper {
        private final Action action;

        public Looper(Action action) {
            this.action = action;
        }

        public long loop(final int limit) {
            long max = Long.MIN_VALUE;

            for (int idx = 0; idx < limit; idx++) {
                final long val = action.execute(idx);
                if (val > max) max = val;
            }

            return max;
        }
    }

    public interface Action {
        long execute(int idx);
    }
}
