package net.nostromo.experiments.memcopy;

import net.nostromo.experiments.PerfTest;
import net.nostromo.libc.TheUnsafe;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.SecureRandom;

public class ByteBufferPerf {

    public static void main(String[] args) throws Exception {
        final SecureRandom random = new SecureRandom();

        final Unsafe UNSAFE = TheUnsafe.UNSAFE;
        final long offset = Unsafe.ARRAY_BYTE_BASE_OFFSET;

        final byte[] buffer = new byte[8];
        final LongBuffer lb = LongBuffer.allocate(1);
        final LongBuffer dlb = ByteBuffer.allocateDirect(8).asLongBuffer();
        final ByteBuffer bb = ByteBuffer.allocate(8);

        final long val = random.nextLong();

        UNSAFE.putLong(buffer, offset, val);
        lb.put(0, val);
        dlb.put(0, val);
        bb.putLong(0, val);

        final PerfTest test = new PerfTest(5, 100_000_000L);

        test.timedTest("Unsafe", () -> UNSAFE.getLong(buffer, offset));
        test.timedTest("LongBuffer", () -> lb.get(0));
        test.timedTest("direct LongBuffer", () -> dlb.get(0));
        test.timedTest("ByteBuffer", () -> bb.getLong(0));
    }
}
