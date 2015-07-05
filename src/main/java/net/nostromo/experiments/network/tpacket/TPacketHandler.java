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

import net.nostromo.experiments.Util;
import net.nostromo.libc.Libc;
import net.nostromo.libc.LibcConstants;
import net.nostromo.libc.OffHeapBuffer;
import net.nostromo.libc.TheUnsafe;
import net.nostromo.libc.struct.network.header.EthHdr;
import net.nostromo.libc.struct.network.header.IpHdr;
import net.nostromo.libc.struct.network.header.TcpHdr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import static net.nostromo.experiments.Util.F;

public abstract class TPacketHandler implements LibcConstants {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final Unsafe unsafe = TheUnsafe.unsafe;
    protected final Libc libc = Libc.libc;

    protected final byte[] payload = new byte[65536]; // 65k
    protected final long logIval = (1 << 20) - 1;     // 1M

    protected final TPacketSocket socket;

    protected final OffHeapBuffer buffer;
    protected final EthHdr ethHdr;
    protected final IpHdr ipHdr;
    protected final TcpHdr tcpHdr;

    protected long lastNanons = System.nanoTime();
    protected long lastPacketCnt;
    protected long totPackets;

    public TPacketHandler(final TPacketSocket socket) {
        this.socket = socket;

        buffer = OffHeapBuffer.attach(socket.mmap);
        ethHdr = new EthHdr(buffer);
        ipHdr = new IpHdr(buffer);
        tcpHdr = new TcpHdr(buffer);
    }

    public abstract void loop();

    protected abstract void handleFrame(long offset);

    protected void handleEthernetPacket(final long linkLayerOffset, final int snaplen) {
        totPackets++;
        if ((totPackets & logIval) == 0) {
            final long nanos = System.nanoTime();
            final double tps = Util.tps(totPackets - lastPacketCnt, nanos - lastNanons);
            log.info(F("pkts/sec: %,.0f  pkts: %,d", tps, totPackets));
            lastPacketCnt = totPackets;
            lastNanons = nanos;
        }

        if (snaplen < EthHdr.BYTES) return;

        ethHdr.read(buffer, linkLayerOffset);
        log.debug("{}", ethHdr);

        final long inetLayerOffset = linkLayerOffset + EthHdr.BYTES;
        final int remainder = snaplen - EthHdr.BYTES;

        switch (Short.toUnsignedInt(ethHdr.eth_type)) {
            case ETH_P_IP:
                handleIpV4Packet(inetLayerOffset, remainder);
                break;
            case ETH_P_IPV6:
                handleIpV6Packet(inetLayerOffset, remainder);
                break;
            case ETH_P_ARP:
                handleArpPacket(inetLayerOffset, remainder);
                break;
            case ETH_P_RARP:
                handleRarpPacket(inetLayerOffset, remainder);
                break;
            default:
                handleUnknownEtherType(inetLayerOffset, remainder);
        }
    }

    protected void handleIpV4Packet(final long offset, final int snaplen) {
        if (snaplen < IpHdr.BYTES) return;

        ipHdr.read(buffer, offset);
        log.debug("{}", ipHdr);

        final long protoOffset = offset + ipHdr.hdr_len_bytes;
        final int remainder = snaplen - IpHdr.BYTES;

        switch (ipHdr.protocol) {
            case IPPROTO_TCP:
                handleTcpPacket(protoOffset, remainder);
                break;
            case IPPROTO_ICMP:
                handleIcmpPacket(protoOffset, remainder);
                break;
            default:
                handleUnknownIpType(protoOffset, remainder);
        }
    }

    protected void handleIpV6Packet(final long offset, final int snaplen) {
        log.debug("ipv6");
    }

    protected void handleArpPacket(final long offset, final int snaplen) {
        log.debug("arp");
    }

    protected void handleRarpPacket(final long offset, final int snaplen) {
        log.debug("rarp");
    }

    protected void handleTcpPacket(final long offset, final int snaplen) {
        if (snaplen < TcpHdr.BYTES) return;

        tcpHdr.read(buffer, offset);
        log.debug("{}", tcpHdr);
    }

    protected void handleIcmpPacket(final long offset, final int snaplen) {
        final int payloadLen = ipHdr.tot_len - ipHdr.hdr_len_bytes;
        final int truncated = Math.min(snaplen, payloadLen);

        buffer.getBytes(payload, truncated);
        log.debug("packet length: {} ({})", payloadLen, truncated);
    }

    protected void handleUnknownEtherType(final long offset, final int snaplen) {
        log.debug("unknown ether type: {}", ethHdr.eth_type);
    }

    protected void handleUnknownIpType(final long offset, final int snaplen) {
        log.debug("unknown ip protocol: {}", ipHdr.protocol);
    }
}
