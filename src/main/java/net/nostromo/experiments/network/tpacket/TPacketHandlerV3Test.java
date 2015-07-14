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

import net.nostromo.tpacket.TPacketHandlerV3;
import net.nostromo.tpacket.TPacketSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TPacketHandlerV3Test extends TPacketHandlerV3 {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public TPacketHandlerV3Test(final TPacketSocket socket) {
        super(socket);
    }

    @Override
    protected void handleTcpPacket(final long offset, final int snaplen) {
        super.handleTcpPacket(offset, snaplen);
//        if (tcpHdr.dst_port == 22 && Util.inetNtoA(ipHdr.dst_ip).equals("54.175.51.235")) {
            log.info("{}", Integer.toUnsignedLong(tp3Hdr.hv1_u.hv1.rxhash));
//        }
    }
}
