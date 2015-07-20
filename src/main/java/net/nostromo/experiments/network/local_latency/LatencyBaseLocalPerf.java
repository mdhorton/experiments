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

import net.nostromo.libc.Libc;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

public abstract class LatencyBaseLocalPerf {

    protected final Libc libc = Libc.libc;

    protected final int port = 55555;
    protected final String host = "localhost";

    protected final int serverCpu = 8;
    protected final int clientCpu = 9;

    protected final int size;
    protected final int packets;

    protected final CountDownLatch serverLatch = new CountDownLatch(1);
    protected final CountDownLatch startLatch = new CountDownLatch(2);
    protected final CountDownLatch endLatch = new CountDownLatch(2);

    public LatencyBaseLocalPerf(final int size, final int packets) {
        this.size = size;
        this.packets = packets;
    }

    protected abstract NetPerfThread createServer(int cpu);

    public void benchmark() throws Exception {
        final NetPerfThread serverThread = createServer(serverCpu);
        final NetPerfThread clientThread = createClient(clientCpu);

        serverThread.start();
        serverLatch.await();

        clientThread.start();
        startLatch.await();

        clientThread.join();
        serverThread.join();

        long[] clientArr = clientThread.getTstamps();
        long[] serverArr = serverThread.getTstamps();
        long diffs = 0;

        for (int idx = 0; idx < packets; idx++) {
            if (serverArr[idx] == 0) continue;
            diffs += serverArr[idx] - clientArr[idx];
        }

        final double avg = ((double) diffs / packets) / 1_000;

        final String fmt = "%28s  avg: %,8.0f µs\n";
        System.out.printf(fmt, getClass().getSimpleName(), avg);
    }

    protected NetPerfThread createClient(final int cpu) {
        return new NetPerfThread(cpu, packets) {
            @Override
            protected void perfRun() throws Exception {
                final ByteBuffer bytes = ByteBuffer.allocateDirect(size);
                bytes.limit(size);

                final SocketChannel sock = SocketChannel.open(new InetSocketAddress(port));
                startLatch.countDown();

                for (int idx = 0; idx < packets; idx++) {
                    tstamps[idx] = libc.rdtscp();

                    while (bytes.remaining() > 0) {
                        final int len = sock.write(bytes);
                        if (len == -1) throw new RuntimeException("socket closed");
                    }

                    bytes.position(0);
                }

                endLatch.countDown();
                sock.close();
            }
        };
    }

    protected NetPerfThread createNonNioClient(final int cpu) {
        return new NetPerfThread(cpu, packets) {
            @Override
            protected void perfRun() throws Exception {
                final byte[] bytes = new byte[size];

                final Socket sock = new Socket(host, port);
                final OutputStream os = sock.getOutputStream();
                final BufferedOutputStream bos = new BufferedOutputStream(os);
                startLatch.countDown();

                for (int idx = 0; idx < packets; idx++) {
                    bos.write(bytes);
                    bos.flush();
                    tstamps[idx] = libc.rdtscp();
                }

                endLatch.countDown();
                sock.close();
            }
        };
    }
}
