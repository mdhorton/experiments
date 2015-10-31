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

package net.nostromo.experiments.network.node_latency;

import net.nostromo.libc.LibcUtil;
import org.openjdk.jmh.annotations.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 60, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class LatencyPerf {

    private final int LENGTH = 256;

    private ByteBuffer bytes = ByteBuffer.allocateDirect(LENGTH);
    private SocketChannel sock;

    @Setup
    public void setup() throws Exception {
        LibcUtil.util.setLastCpu();
        bytes.limit(LENGTH);

        sock = SocketChannel.open(new InetSocketAddress("balrog", 55555));
        sock.configureBlocking(false);
    }

    @Benchmark
    public void nioLatency() throws Exception {
        bytes.position(0);
        int remaining = LENGTH;

        while (remaining > 0) {
            final int len = sock.write(bytes);
            remaining -= len;
            if (len == -1) throw new RuntimeException("socket closed");
        }

        bytes.position(0);
        remaining = LENGTH;

        while (remaining > 0) {
            final int len = sock.read(bytes);
            remaining -= len;
            if (len == -1) throw new RuntimeException("socket closed");
        }
    }
}
