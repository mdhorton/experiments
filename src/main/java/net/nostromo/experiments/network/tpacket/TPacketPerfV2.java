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
import net.nostromo.tpacket.TPacketHandlerV2;
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
        return new TPacketHandlerV2(socket);
    }
}
