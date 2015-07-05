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

import net.nostromo.libc.Libc;
import net.nostromo.libc.LibcConstants;
import net.nostromo.libc.LibcHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static net.nostromo.experiments.Util.F;

public abstract class TPacketPerf implements LibcConstants {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final LibcHelper help = LibcHelper.helper;
    protected final Libc libc = Libc.libc;

    protected String ifname = "lo";
    protected int packetType = SOCK_RAW;
    protected int protocol = ETH_P_ALL;

    protected int fanoutId = libc.getpid();
    protected int fanoutType = PACKET_FANOUT_LB | PACKET_FANOUT_FLAG_DEFRAG;

    protected int blockSize = 1 << 20;
    protected int blockCnt = 1 << 12;
    protected int frameSize = 1 << 9;

    protected int threadCnt = 4;
    protected int startCpu = 2;

    protected int framesPerBlock = blockSize / frameSize;
    protected long mapSize = (long) blockSize * blockCnt;

    protected abstract TPacketHandler createHandler();

    public void execute() throws Exception {
        logConfig();

        final List<Thread> threads = new ArrayList<>();

        for (int idx = 0; idx < threadCnt; idx++) {
            final int cpu = startCpu + idx;

            final Thread thread = new Thread(() -> {
                help.setCpu(cpu);
                createHandler().loop();
            });

            thread.start();
            threads.add(thread);
        }

        for (final Thread thread : threads) {
            thread.join();
        }
    }

    protected void logConfig() {
        log.info(F("ifname: %s", ifname));
        log.info(F("frame sz: %,d  block sz: %,d  block cnt: %,d", frameSize, blockSize, blockCnt));
        log.info(F("frames/block: %,d  frame cnt: %,d", framesPerBlock, mapSize / frameSize));
        log.info(F("map sz: %,d  thread cnt: %,d  start cpu: %,d", mapSize, threadCnt, startCpu));
    }
}
