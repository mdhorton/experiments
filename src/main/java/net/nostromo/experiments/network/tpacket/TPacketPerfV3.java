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

public class TPacketPerfV3 extends TPacketPerf {

    protected final int timeout = 100; // milliseconds

    @Override
    protected TPacketHandler createHandler() {
        final TPacketSocketV3 socket = new TPacketSocketV3(ifname, packetType, protocol, blockSize,
                blockCnt, frameSize, timeout);
        socket.initialize();
        socket.setupFanout(fanoutId, fanoutType);
//        socket.enablePromiscuous();
        return new TPacketHandlerV3(socket);
    }

    public static void main(String[] args) throws Exception {
        final TPacketPerfV3 perf = new TPacketPerfV3();
        perf.ifname = "enp2s0f1";
        perf.startCpu = 16;
        perf.execute();
    }
}
