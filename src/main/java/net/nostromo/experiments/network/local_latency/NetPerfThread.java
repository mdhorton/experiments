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

import net.nostromo.libc.Libc;
import net.nostromo.libc.LibcUtil;

public abstract class NetPerfThread extends Thread {

    protected final Libc libc = Libc.libc;

    protected final int cpu;
    protected final long[] tstamps;

    public NetPerfThread(final int cpu, final int packets) {
        this.cpu = cpu;
        tstamps = new long[packets];
    }

    protected abstract void perfRun() throws Exception;

    public long[] getTstamps() {
        return tstamps;
    }

    @Override
    public void run() {
        try {
            LibcUtil.util.setCpu(cpu);
            perfRun();
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
