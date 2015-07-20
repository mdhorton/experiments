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

package net.nostromo.experiments.network.local_latency;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;

public class LatencyBlockingNioPerf extends LatencyBaseLocalPerf {

    public LatencyBlockingNioPerf(final int size, final int packets) {
        super(size, packets);
    }

    protected NetPerfThread createServer(final int cpu) {
        return new NetPerfThread(cpu, packets) {
            @Override
            protected void perfRun() throws Exception {
                final ByteBuffer bytes = ByteBuffer.allocateDirect(size);

                final ServerSocketChannel server = ServerSocketChannel.open();
                server.bind(new InetSocketAddress(port));
                serverLatch.countDown();

                final ReadableByteChannel sock = server.accept();
                startLatch.countDown();

                for (int idx = 0; idx < packets; idx++) {
                    while (bytes.remaining() > 0) {
                        final int len = sock.read(bytes);
                        if (len == -1) throw new RuntimeException("socket closed");
                    }

                    bytes.clear();
                    tstamps[idx] = libc.rdtscp();
                }

                endLatch.countDown();
                sock.close();
                server.close();
            }
        };
    }
}
