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

import net.nostromo.tpacket.TPacketHandler;
import net.nostromo.tpacket.TPacketSocketV2;

public class TPacketPerfV2 extends TPacketPerf {

    // currently this is bugged
    protected boolean useCopyThreshold = false;

    public static void main(final String[] args) throws Exception {
        final TPacketPerfV2 perf = new TPacketPerfV2();
        perf.execute();
    }

    @Override
    protected TPacketHandler createHandler() {
        final TPacketSocketV2 socket = new TPacketSocketV2(ifname, packetType, protocol, blockSize,
                blockCnt, frameSize);
        socket.initialize();
        if (useCopyThreshold) socket.setPacketCopyThreshold(frameSize);
        if (threadCnt > 1) socket.setupFanout(fanoutId, fanoutType);
        if (usePromisc) socket.enablePromiscuous();

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

        return new TPacketHandlerV2Perf(socket);
    }
}
