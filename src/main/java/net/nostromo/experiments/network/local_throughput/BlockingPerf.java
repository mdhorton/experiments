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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingPerf extends BaseLocalThroughputPerf {

    @Override
    protected Thread createServer() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    LibcUtil.util.setCpu(8);
                    final byte[] bytes = new byte[bufferSize];

                    final ServerSocket server = new ServerSocket(port);
                    serverLatch.countDown();

                    final Socket sock = server.accept();
                    final InputStream is = sock.getInputStream();
                    final BufferedInputStream bis = new BufferedInputStream(is);
                    setupLatch.countDown();

                    runLatch.await();

                    while (true) {
                        final int len = bis.read(bytes);
                        if (len == -1) break;
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
                    final byte[] bytes = new byte[bufferSize];

                    final Socket sock = new Socket(host, port);
                    final OutputStream os = sock.getOutputStream();
                    final BufferedOutputStream bos = new BufferedOutputStream(os);
                    setupLatch.countDown();

                    runLatch.await();

                    for (int x = 0; x < iterations; x++) {
                        bos.write(bytes);
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
