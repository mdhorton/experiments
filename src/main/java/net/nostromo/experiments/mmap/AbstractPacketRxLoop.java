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

import net.nostromo.libc.NativeHeapBuffer;
import net.nostromo.libc.TheUnsafe;
import net.nostromo.libc.struct.io.PollFd;
import net.nostromo.libc.struct.network.header.EthHdr;
import net.nostromo.libc.struct.network.header.IpHdr;
import net.nostromo.libc.struct.network.header.TcpHdr;
import net.nostromo.libc.struct.network.tpacket.block.TPacketBdHeaderU;
import net.nostromo.libc.struct.network.tpacket.block.TPacketBlockDesc;
import net.nostromo.libc.struct.network.tpacket.header.TPacket3Hdr;
import net.nostromo.libc.struct.network.tpacket.header.TPacketHdrVariant1Union;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

public abstract class AbstractPacketRxLoop implements LibcConstants {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final Unsafe unsafe = TheUnsafe.unsafe;

    protected final TPacketBlockDesc blockHdr;
    protected final TPacket3Hdr tp3Hdr;
    protected final EthHdr ethHdr;
    protected final IpHdr ipHdr;
    protected final TcpHdr tcpHdr;

    protected final long mmapAddress;
    protected final int sockFd;
    protected final int blockSize;
    protected final int blockCnt;
    protected final NativeHeapBuffer buffer;

    public AbstractPacketRxLoop(final long mmapAddress, final int sockFd, final int blockSize, final int blockCnt) {
        this.mmapAddress = mmapAddress;
        this.sockFd = sockFd;
        this.blockSize = blockSize;
        this.blockCnt = blockCnt;
        buffer = new NativeHeapBuffer(blockSize, mmapAddress);

        blockHdr = new TPacketBlockDesc(buffer, TPacketBdHeaderU.Name.BH1);
        tp3Hdr = new TPacket3Hdr(buffer, TPacketHdrVariant1Union.Name.HV1);
        ethHdr = new EthHdr(buffer);
        ipHdr = new IpHdr(buffer);
        tcpHdr = new TcpHdr(buffer);
    }

    public void loop() {
        final PollFd pollfd = new PollFd();
        pollfd.fd = sockFd;
        pollfd.events = (short) (POLLIN | POLLERR);
        pollfd.revents = 0;
        pollfd.write();

        final long statusOffset = 8;
        int blockIdx = 0;

        while (true) {
            final long blockStart = (long) blockSize * blockIdx;
            final long blockStatusOffset = mmapAddress + blockStart + statusOffset;

            buffer.read(blockStart, TPacketBlockDesc.SIZE);
            blockHdr.read(buffer, 0);

            // poll the block until the kernel gives it back to user space
            while ((blockHdr.hdr.bh1.block_status & TP_STATUS_USER) == 0) {
//                libc.poll(pollfd, 1, -1);
                buffer.read(blockStart, TPacketBlockDesc.SIZE);
                blockHdr.read(buffer, 0);
            }

            final long loopCnt = 1;
            final long start = System.nanoTime();
            for (long x = 0; x < loopCnt; x++) {
                // fill the heap buffer from the off-heap memory
                buffer.read(blockStart, Integer.toUnsignedLong(blockHdr.hdr.bh1.blk_len));
                blockHdr.read(buffer, 0);
                log.info("\n{}", blockHdr);

                // TODO: check block status

                walkBlock(blockHdr);
            }
//            final long elap = System.nanoTime() - start;
//            log.info(String.format("millis: %,.3f  cnt: %,d  pkt/sec: %,.0f  avg pkt sz: %,.0f",
//                    elap / 1_000_000.0,
//                    blockHdr.num_packets * loopCnt,
//                    (blockHdr.num_packets * loopCnt) / (elap / 1_000_000_000.0),
//                    blockHdr.block_len / (double) blockHdr.num_packets));

            // return the block to the kernel
            unsafe.putInt(blockStatusOffset, TP_STATUS_KERNEL);

            // next block
            blockIdx++;
            if (blockIdx == blockCnt) blockIdx = 0;
        }
    }

    protected void walkBlock(final TPacketBlockDesc blockHdr) {
        int tp3HdrOffset = blockHdr.hdr.bh1.offset_to_first_pkt;

        for (int idx = 0; idx < blockHdr.hdr.bh1.num_pkts; idx++) {
            tp3Hdr.read(buffer, tp3HdrOffset);
            log.info("\n{}", tp3Hdr);
            handleEthernetPacket(tp3HdrOffset + tp3Hdr.tp_mac);
            tp3HdrOffset += tp3Hdr.tp_next_offset;
        }
    }

    protected void handleEthernetPacket(final int linkLayerOffset) {
        ethHdr.read(buffer, linkLayerOffset);
        log.info(ethHdr.toString());
        final int inetLayerOffset = linkLayerOffset + EthHdr.SIZE;

        switch (Short.toUnsignedInt(ethHdr.eth_type)) {
            case ETH_P_IP:
                handleIpV4Packet(inetLayerOffset);
                break;
            case ETH_P_IPV6:
                handleIpV6Packet(inetLayerOffset);
                break;
            case ETH_P_ARP:
                handleArpPacket(inetLayerOffset);
                break;
            case ETH_P_RARP:
                handleRarpPacket(inetLayerOffset);
                break;
            default:
                handleUnknownEtherType();
        }
    }

    protected void handleIpV4Packet(final int offset) {
        ipHdr.read(buffer, offset);
        log.info(ipHdr.toString());
        final int nextOffset = offset + ipHdr.hdr_len;

        switch (ipHdr.protocol) {
            case IPPROTO_TCP:
                handleTcpPacket(nextOffset);
        }
    }

    protected void handleIpV6Packet(final int offset) {
//        log.info("ipv6");
    }

    protected void handleArpPacket(final int offset) {
//        log.info("arp");
    }

    protected void handleRarpPacket(final int offset) {
//        log.info("rarp");
    }

    protected void handleUnknownEtherType() {
        log.info("unknown ether type: {}", ethHdr.eth_type);
    }

    protected void handleTcpPacket(final int offset) {
        tcpHdr.read(buffer, offset);
    }
}
