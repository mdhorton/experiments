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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.nostromo.libc.struct.system.Timespec;
import org.slf4j.LoggerFactory;

public class TPacketHandlerV2Old {

    final Timespec timespec1 = new Timespec();

    public void loop() {
//                final int res = libc.poll(pollfd.bufferPointer(), 1, -1);
//                libc.clock_gettime(CLOCK_REALTIME, timespec1.bufferPointer());
//                pollfd.read();
//                log.debug("polled: {}  res: {}  revents: {}", index, res, pollfd.revents);
    }

    protected void handleFrame(final long offset) {
//        if (totPackets > 10_000_000) {
//            timespec1.read();
//            long diff = timespec1.tv_nsec - tp2Hdr.nsec;
//            if (diff < 0) diff += 1_000_000_000;
//            log.info("{} μs", diff / 1000);
//            log.info("\n{}.{}\n{}.{}", timespec1.tv_sec, timespec1.tv_nsec, tp2Hdr.sec,
//                    tp2Hdr.nsec);
//        }
    }

    protected void logts(final Timespec timespec) {
        timespec.read();
//        log.debug("{}.{}", timespec.tv_sec, timespec.tv_nsec);
    }

    protected void enableDebug() {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
    }
}
