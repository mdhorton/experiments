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

package net.nostromo.experiments.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * This was a sanity test to verify that a slection key
 * remains active until its operation is no longer valid.
 * IOW if a channel is readable, is will be returned again
 * in the selection list until all bytes have been read.
 */
public class SelectorTest {

    public static void main(String[] args) throws Exception {
        new SelectorTest().foo();
    }

    public void foo() throws Exception {
        final ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(5024));
        server.configureBlocking(false);

        final Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            if (selector.select() == 0) continue;

            final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                final SelectionKey key = iter.next();

                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        final SocketChannel channel = serverChannel.accept();
                        new ConnectionThread(channel).start();
                    }
                }

                iter.remove();
            }
        }
    }

    public class ConnectionThread extends Thread {

        private final SocketChannel channel;

        public ConnectionThread(final SocketChannel channel) {
            this.channel = channel;
            System.out.println("connected");
        }

        @Override
        public void run() {
            final ByteBuffer bb = ByteBuffer.allocate(1 << 14);

            try {
                final Selector selector = Selector.open();
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);

                while (true) {
                    if (selector.select() == 0) continue;

                    final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        System.out.println("processing key");
                        final SelectionKey key = iter.next();
                        iter.remove();

                        if (!key.isValid()) {
                            key.channel().close();
                            key.cancel();
                            continue;
                        }

                        if (key.isReadable()) {
                            final SocketChannel channel = (SocketChannel) key.channel();
                            final int len = channel.read(bb);
                            if (len == -1) {
                                System.out.println("closed");
                                channel.close();
                                continue;
                            }

                            bb.flip();
                            System.out.printf("read %d\n", len);
                            bb.clear();
                        }
                    }
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
