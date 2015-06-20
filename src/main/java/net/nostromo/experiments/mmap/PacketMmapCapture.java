package net.nostromo.experiments.mmap;

import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;
import net.nostromo.libc.AbstractPacketRxLoop;
import net.nostromo.libc.LibcConstants;
import net.nostromo.libc.PacketMmapSocket;
import net.nostromo.libc.Structor;
import net.nostromo.libc.c.sockaddr_ll;
import net.nostromo.libc.c.tpacket_req3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketMmapCapture implements LibcConstants {

    private static final Logger LOG = LoggerFactory.getLogger(PacketMmapCapture.class);

    public static void main(String[] args) {
        try {
            final String ifname = "enp2s0f1";
            final int cpuCore = 0;
            final int timeout = 100; // milliseconds
            final int blockSize = 1 << 20; // should be power of 2 or space is wasted
            final int blockCnt = 1 << 10;

            final int frameSize = 1 << 16; // this should always be a multiple of TPACKET_ALIGNMENT
            final int framesPerBlock = blockSize / frameSize;
            final long mapSize = (long) blockSize * blockCnt;

            LOG.info(fmt("ifname: %s  cpu core: %,d  timeout: %,d", ifname, cpuCore, timeout));
            LOG.info(fmt("frame sz: %,d  block sz: %,d  block cnt: %,d", frameSize, blockSize, blockCnt));
            LOG.info(fmt("frames/block: %,d  frame cnt: %,d", framesPerBlock, mapSize / frameSize));
            LOG.info(fmt("map sz: %,d", mapSize));

            final tpacket_req3 tpReq3 = Structor.tpacket_req3(blockSize, frameSize, blockCnt, timeout, 0, 0);
            final sockaddr_ll saLink = Structor.sockaddr_ll(ifname, (short) PF_PACKET, ETH_P_ALL);

            final PacketMmapSocket socket = new PacketMmapSocket(PF_PACKET, SOCK_RAW, ETH_P_ALL);
            socket.setupTPacketV3();
            socket.setupPacketRxRing(tpReq3);
            final Pointer mmap = socket.mmap(mapSize);
            socket.bind(saLink);

            final long mmapAddress = Pointer.nativeValue(mmap);
            final AbstractPacketRxLoop loop =
                    new AbstractPacketRxLoop(mmapAddress, socket.getFd(), blockSize, blockCnt) {

                    };

            loop.loop();
        } catch (final LastErrorException lle) {
            LOG.error(LIBC.strerror(lle.getErrorCode()));
            LOG.error("error", lle);
        } catch (final Exception ex) {
            LOG.error("error", ex);
        }
    }

    public static String fmt(final String fmt, final Object... args) {
        return String.format(fmt, args);
    }
}
