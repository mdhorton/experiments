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

import net.nostromo.tpacket.TPacketHandlerV2;
import net.nostromo.tpacket.TPacketSocket;

public class TPacketHandlerV2Perf extends TPacketHandlerV2 {

    public TPacketHandlerV2Perf(final TPacketSocket socket) {
        super(socket);
    }

    @Override
    protected void handleEthernetPacket(final long linkLayerOffset, final int snaplen) {
        super.handleEthernetPacket(linkLayerOffset, snaplen);
//        System.out.println(ethHdr);
    }

    @Override
    protected void handleTcpPacket(final long offset, final int snaplen) {
        super.handleTcpPacket(offset, snaplen);
        if (Short.toUnsignedInt(tcpHdr.dst_port) == 55555 ||
                Short.toUnsignedInt(tcpHdr.src_port) == 55555) {
            System.out.println(ipHdr);
            System.out.println(tcpHdr);
        }
    }
}
