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

package net.nostromo.experiments.network.tpacket;

import net.nostromo.libc.struct.io.PollFd;
import net.nostromo.libc.struct.network.tpacket.header.TPacket2Hdr;
import net.nostromo.libc.struct.system.Timespec;

public class TPacketHandlerV2 extends TPacketHandler {

    final TPacket2Hdr tp2Hdr;

    final Timespec timespec1 = new Timespec();
    final Timespec timespec2 = new Timespec();
    final Timespec timespec3 = new Timespec();

    public TPacketHandlerV2(final TPacketSocket socket) {
        super(socket);
        tp2Hdr = new TPacket2Hdr(buffer);
    }

    @Override
    public void loop() {
        final PollFd pollfd = new PollFd();
        pollfd.fd = socket.sock;
        pollfd.events = (short) POLLIN;
        pollfd.write();

        final int frameCnt = (int) (((long) socket.blockSize * socket.blockCnt) / socket.frameSize);
        int index = 0;

        while (true) {
            final long startOffset = (long) socket.frameSize * index;
            final long statusOffset = socket.mmap + startOffset;

            if ((unsafe.getInt(statusOffset) & TP_STATUS_USER) == 0) {
                final int res = libc.poll(pollfd.bufferPointer(), 1, -1);
                libc.clock_gettime(CLOCK_REALTIME, timespec1.bufferPointer());
                libc.clock_gettime(CLOCK_REALTIME, timespec2.bufferPointer());

//                logts();
//                logts();
//                pollfd.read();
//                log.debug("polled: {}  res: {}  revents: {}", index, res, pollfd.revents);
            }

            handleFrame(startOffset);
            unsafe.putInt(statusOffset, TP_STATUS_KERNEL);
//            unsafe.fullFence();

            index++;
            if (index == frameCnt) index = 0;
        }
    }

    @Override
    protected void handleFrame(final long offset) {
        tp2Hdr.read(offset);
        libc.clock_gettime(CLOCK_REALTIME, timespec3.bufferPointer());
        logts(timespec1);
        logts(timespec2);
        logts(timespec3);
        log.debug("{}  {}", System.nanoTime(), System.nanoTime());
        log.debug("{}", tp2Hdr);
        handleEthernetPacket(offset + tp2Hdr.mac, tp2Hdr.snaplen);
    }

    protected void logts(final Timespec timespec) {
        timespec.read();
        log.debug("{}.{}", timespec.tv_sec, timespec.tv_nsec);
    }
}
