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

package net.nostromo.experiments.network.local_latency;

public class RunPerfs {
    public static void main(String[] args) throws Exception {
        final int iterations = 1;
        int size = 1500;
        int packets = 1;

        for (int y = 0; y < 1; y++) {
            for (int x = 0; x < iterations; x++) {
                System.out.printf("iteration: %,d of %,d  size: %,d  packets: %,d\n", x + 1,
                        iterations, size, packets);
//                new LatencyBlockingNioPerf(size, packets).benchmark();
//                new LatencyBlockingPerf(size, packets).benchmark();
//                new LatencyNonBlockingNioPerf(size, packets).benchmark();
                new LatencyTPacket(size, packets).benchmark();
                System.out.println();
            }

            size *= 2;
        }
    }
}
