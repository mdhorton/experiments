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
import java.nio.channels.*;
import java.util.Set;

public class LatencyNonBlockingNioPerf extends LatencyBaseLocalPerf {

    public LatencyNonBlockingNioPerf(final int size, final int packets) {
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

                final SelectableChannel sock = server.accept();
                sock.configureBlocking(false);

                final Selector selector = Selector.open();
                sock.register(selector, SelectionKey.OP_READ);
                startLatch.countDown();

                int idx = 0;
                while (idx < packets) {
                    selector.select();
                    final Set<SelectionKey> keys = selector.selectedKeys();

                    for (final SelectionKey key : keys) {
                        final SocketChannel sc = (SocketChannel) key.channel();

                        while (true) {
                            final int len = sc.read(bytes);
                            if (len == 0) break;
                            if (len == -1) throw new RuntimeException("socket closed");

                            if (bytes.remaining() == 0) {
                                bytes.clear();
                                tstamps[idx] = libc.rdtscp();
                                idx++;
                                if (idx == packets) break;
                            }
                        }
                    }

                    keys.clear();
                }

                endLatch.countDown();
                sock.close();
                server.close();
            }
        };
    }
}
