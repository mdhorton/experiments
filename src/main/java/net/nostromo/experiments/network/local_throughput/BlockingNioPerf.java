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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class BlockingNioPerf extends BaseLocalThroughputPerf {

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

                    final SocketChannel sock = server.accept();
                    setupLatch.countDown();

                    runLatch.await();

                    while (true) {
                        final int len = sock.read(bytes);
                        if (len == -1) break;
                        bytes.clear();
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
