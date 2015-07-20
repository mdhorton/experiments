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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class LatencyBlockingPerf extends LatencyBaseLocalPerf {

    public LatencyBlockingPerf(final int size, final int packets) {
        super(size, packets);
    }

    protected NetPerfThread createServer(final int cpu) {
        return new NetPerfThread(cpu, packets) {
            @Override
            protected void perfRun() throws Exception {
                final byte[] bytes = new byte[size];

                final ServerSocket server = new ServerSocket(port);
                serverLatch.countDown();

                final Socket sock = server.accept();
                final InputStream is = sock.getInputStream();
                final BufferedInputStream bis = new BufferedInputStream(is);
                startLatch.countDown();

                for (int idx = 0; idx < packets; idx++) {

                    int remaining = bytes.length;

                    while (remaining > 0) {
                        final int len = bis.read(bytes, bytes.length - remaining, remaining);
                        remaining -= len;
                        if (len == -1) throw new RuntimeException("socket closed");
                    }

                    tstamps[idx] = libc.rdtscp();
                }

                endLatch.countDown();
                sock.close();
                server.close();
            }
        };
    }
}
