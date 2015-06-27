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

import net.nostromo.experiments.PerfTest;
import net.nostromo.libc.TheUnsafe;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Random;

public class ByteBufferPerf {

    public static void main(String[] args) throws Exception {
        final Unsafe unsafe = TheUnsafe.unsafe;
        final long offset = Unsafe.ARRAY_BYTE_BASE_OFFSET;

        final int len = 10;
        final byte[] buffer = new byte[len * 8];
        final LongBuffer lb = LongBuffer.allocate(len);
        final LongBuffer dlb = ByteBuffer.allocateDirect(len * 8).asLongBuffer();
        final ByteBuffer bb = ByteBuffer.allocate(len * 8);
        final ByteBuffer dbb = ByteBuffer.allocateDirect(len * 8);

        final Random rand = new Random();

        for (int x = 0; x < len; x++) {
            unsafe.putLong(buffer, offset + (x * 8), rand.nextLong());
            lb.put(x, rand.nextLong());
            dlb.put(x, rand.nextLong());
            bb.putLong(x * 8, rand.nextLong());
            dbb.putLong(x * 8, rand.nextLong());
        }

        final PerfTest test = new PerfTest(100_000_000L);

        for (int x = 0; x < len; x++) {
            final int y = x;
            test.timedTest("ByteBuffer", () -> bb.getLong(y * 8));
            test.timedTest("Direct ByteBuffer", () -> bb.getLong(y * 8));
            test.timedTest("direct LongBuffer", () -> dlb.get(y));
            test.timedTest("Unsafe", () -> unsafe.getLong(buffer, offset + (y * 8)));
            test.timedTest("LongBuffer", () -> lb.get(y));
            System.out.println("********************");
        }
    }
}
