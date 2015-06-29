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

package net.nostromo.experiments.mmap;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import net.nostromo.experiments.Socket;
import net.nostromo.libc.Util;
import net.nostromo.libc.struct.network.tpacket.TPacketReq3;

public class PacketMmapSocket extends Socket {

    public PacketMmapSocket(final int domain, final int type, final int protocol) {
        super(domain, type, protocol);
    }

    public void setupTPacketV3() {
        final IntByReference ref = new IntByReference(TPACKET_V3);
        libc.setsockopt(fd, SOL_PACKET, PACKET_VERSION, ref.getPointer(), Util.SIZE_OF_INT);
    }

    public void setupPacketRxRing(final TPacketReq3 req3) {
//        libc.setsockopt(fd, SOL_PACKET, PACKET_RX_RING, req3.getPointer(), req3.size());
    }

    public void setupPacketFanout(final int fanoutType) {
        final int pid = libc.getpid();
        setupPacketFanout(fanoutType, pid);
    }

    public void setupPacketFanout(final int fanoutType, final int pid) {
        final int fanoutVal = ((pid & 0xffff) | (fanoutType << 16));
        final IntByReference ref = new IntByReference(fanoutVal);
        libc.setsockopt(fd, SOL_PACKET, PACKET_FANOUT, ref.getPointer(), Util.SIZE_OF_INT);
    }

    public Pointer mmap(final long mapSize) {
        return mmap(mapSize, 0);
    }

    public Pointer mmap(final long mapSize, final int extraFlags) {
        final Pointer p0 = new Pointer(0);
        return libc.mmap(p0, mapSize, PROT_READ | PROT_WRITE, MAP_SHARED | extraFlags, fd, 0);
    }
}
