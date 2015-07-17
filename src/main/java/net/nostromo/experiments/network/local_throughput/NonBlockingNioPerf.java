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

package net.nostromo.experiments.network.local_throughput;

import net.nostromo.libc.LibcUtil;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

public class NonBlockingNioPerf extends BaseLocalThroughputPerf {

    @Override
    protected Thread createServer() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    LibcUtil.util.setCpu(8);
                    final ByteBuffer bytes = ByteBuffer.allocateDirect(bufferSize);

                    final ServerSocketChannel server = ServerSocketChannel.open();
                    server.bind(new InetSocketAddress(port));
                    serverLatch.countDown();

                    final SelectableChannel sock = server.accept();
                    sock.configureBlocking(false);

                    final Selector selector = Selector.open();
                    sock.register(selector, SelectionKey.OP_READ);
                    setupLatch.countDown();

                    runLatch.await();

                    OUTER:
                    while (true) {
                        selector.select();
                        final Set<SelectionKey> keys = selector.selectedKeys();

                        for (final SelectionKey key : keys) {
                            final SocketChannel sc = (SocketChannel) key.channel();

                            while (true) {
                                final int len = sc.read(bytes);
                                if (len < 0) break OUTER;
                                if (len <= 0) break;

                                bytes.clear();
                            }
                        }

                        keys.clear();
                    }

                    sock.close();
                    server.close();
                }
                catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    protected Thread createClient() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    LibcUtil.util.setCpu(9);
                    final ByteBuffer bytes = ByteBuffer.allocateDirect(bufferSize);
                    bytes.limit(bufferSize);

                    final SocketChannel sock = SocketChannel.open(new InetSocketAddress(port));
                    setupLatch.countDown();

                    runLatch.await();

                    int x = 0;
                    while (x < iterations) {
                        sock.write(bytes);
                        if (bytes.remaining() == 0) {
                            bytes.position(0);
                            x++;
                        }
                    }

                    sock.close();
                }
                catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
