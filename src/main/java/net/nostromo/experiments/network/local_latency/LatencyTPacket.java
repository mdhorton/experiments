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
import net.nostromo.libc.LibcConstants;
import net.nostromo.libc.LibcUtil;
import net.nostromo.libc.struct.io.PollFd;
import net.nostromo.tpacket.TPacketHandlerV2;
import net.nostromo.tpacket.TPacketSocket;
import net.nostromo.tpacket.TPacketSocketV2;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class LatencyTPacket extends LatencyBaseLocalPerf implements LibcConstants {

    protected String ifname = "lo";
    protected int packetType = SOCK_RAW;
    protected int protocol = ETH_P_ALL;

    protected int blockSize = 1 << 20;
    protected int blockCnt = 1 << 10;
    protected int frameSize = 1 << 15;

    protected int pid = Libc.libc.getpid();

    public LatencyTPacket(final int size, final int packets) {
        super(size, packets);
    }

    @Override
    protected NetPerfThread createServer(final int cpu) {
        return new NetPerfThread(cpu, packets) {
            @Override
            protected void perfRun() throws Exception {
                final TPacketSocketV2 socket = new TPacketSocketV2(ifname, packetType, protocol,
                        blockSize, blockCnt, frameSize);
                socket.initialize();
//                socket.setupFanout(pid, PACKET_FANOUT_LB | PACKET_FANOUT_FLAG_DEFRAG);

                final Object[][] filterObjs = {
                        {0x28, 0, 0, 0x0000000c},
                        {0x15, 25, 0, 0x000086dd},
                        {0x15, 0, 24, 0x00000800},
                        {0x30, 0, 0, 0x00000017},
                        {0x15, 0, 22, 0x00000006},
                        {0x28, 0, 0, 0x00000014},
                        {0x45, 20, 0, 0x00001fff},
                        {0xb1, 0, 0, 0x0000000e},
                        {0x48, 0, 0, 0x00000010},
                        {0x15, 0, 17, 0x0000d903},
                        {0x28, 0, 0, 0x00000010},
                        {0x2, 0, 0, 0x00000001},
                        {0x30, 0, 0, 0x0000000e},
                        {0x54, 0, 0, 0x0000000f},
                        {0x64, 0, 0, 0x00000002},
                        {0x7, 0, 0, 0x00000005},
                        {0x60, 0, 0, 0x00000001},
                        {0x1c, 0, 0, 0x00000000},
                        {0x2, 0, 0, 0x00000005},
                        {0xb1, 0, 0, 0x0000000e},
                        {0x50, 0, 0, 0x0000001a},
                        {0x54, 0, 0, 0x000000f0},
                        {0x74, 0, 0, 0x00000002},
                        {0x7, 0, 0, 0x00000009},
                        {0x60, 0, 0, 0x00000005},
                        {0x1d, 1, 0, 0x00000000},
                        {0x6, 0, 0, 0x00040000},
                        {0x6, 0, 0, 0x00000000}
                };

                socket.attachFilter(filterObjs);

                final TPacketHandlerV2 handler = new Foo(socket, tstamps);
                serverLatch.countDown();
                startLatch.countDown();

                handler.loop();
            }
        };
    }

    public void benchmark() throws Exception {
        final NetPerfThread serverThread = createServer(serverCpu);
        final NetPerfThread clientThread = createClient(clientCpu);

        final CountDownLatch server2Latch = new CountDownLatch(1);
        final CountDownLatch clientLatch = new CountDownLatch(1);

        final Thread server2 = createSocketServer(10, server2Latch, clientLatch);
        server2.start();
        server2Latch.await();

        serverThread.start();
        serverLatch.await();

        clientThread.start();
        clientLatch.await();
        startLatch.await();

        clientThread.join();
        serverThread.join();
        server2.join();

        long[] clientArr = clientThread.getTstamps();
        long[] serverArr = serverThread.getTstamps();
        long diffs = 0;

        for (int idx = 0; idx < packets; idx++) {
            diffs += serverArr[idx] - clientArr[idx];
        }

        final double avg = ((double) diffs / packets) / 1_000;

        final String fmt = "size: %,d  pkts: %,d  avg: %,.3f us\n";
        System.out.printf(fmt, size, packets, avg);
    }

    protected Thread createSocketServer(final int cpu, final CountDownLatch serverLatch,
            final CountDownLatch clientLatch) {
        return new Thread() {
            @Override
            public void run() {
                try {
                    LibcUtil.util.setCpu(cpu);

                    final ByteBuffer bytes = ByteBuffer.allocateDirect(size);

                    final ServerSocketChannel server = ServerSocketChannel.open();
                    server.bind(new InetSocketAddress(port));
                    serverLatch.countDown();

                    final ReadableByteChannel sock = server.accept();
                    clientLatch.countDown();

                    while (true) {
                        final int len = sock.read(bytes);
                        if (len == -1) break;
                        if (bytes.remaining() == 0) bytes.clear();
                    }

                    sock.close();
                    server.close();
                }
                catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    private class Foo extends TPacketHandlerV2 {

        private final long[] tstamps;
        private int idx;

        public Foo(final TPacketSocket socket, final long[] tstamps) {
            super(socket);
            this.tstamps = tstamps;
        }

        @Override
        public void loop() {
            final PollFd pollfd = new PollFd();
            pollfd.fd = sock.getSock();
            pollfd.events = (short) POLLIN;

            int index = 0;

            while (true) {
                System.out.println(index);

                final long startOffset = (long) sock.getFrameSize() * index;
                final long statusOffset = sock.getMmap() + startOffset;

                poll(pollfd, startOffset, statusOffset);

//                if (idx == packets) break;

                index++;
                if (index == sock.getFrameCnt()) index = 0;
            }
        }

        @Override
        protected void handleIpV4Packet(final long offset, final int snaplen) {
            super.handleIpV4Packet(offset, snaplen);
//            tstamps[idx] = libc.rdtscp();
//            idx++;

            System.out.println(ethHdr);
            System.out.println(ipHdr);
            System.out.println(tcpHdr);
        }
    }
}
