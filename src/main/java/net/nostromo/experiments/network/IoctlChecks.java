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

package net.nostromo.experiments.network;

import net.nostromo.libc.Libc;
import net.nostromo.libc.LibcConstants;
import net.nostromo.libc.TheUnsafe;
import net.nostromo.libc.struct.Struct;
import net.nostromo.libc.struct.network.ifreq.IfReq;
import net.nostromo.libc.struct.network.ifreq.IfReqRnUnion;
import net.nostromo.libc.struct.network.ifreq.IfReqRuUnion;
import sun.misc.Unsafe;

public class IoctlChecks implements LibcConstants {

    private final Libc libc = Libc.libc;
    private final Unsafe unsafe = TheUnsafe.unsafe;
    private final String ifname = "enp2s0f1";

    // this indicates the intel 82576 nic is not doing hardware timestamps,
    // this confirms what's in the driver source code
    public void checkHwTstamp() {
        final int sock = libc.socket(PF_PACKET, SOCK_RAW, ETH_P_ALL);

        final IfReq ifReq = new IfReq(IfReqRnUnion.Name.NAME, IfReqRuUnion.Name.DATA);
        Struct.copyString(ifReq.ifrn.name, ifname);

        libc.ioctl(sock, SIOCGHWTSTAMP, ifReq.pointer());
        ifReq.read();

        final long pointer = ifReq.ifru.ptr_data;
        final int rx_filter = unsafe.getInt(pointer + 8);
        System.out.println(rx_filter);

        libc.close(sock);
    }

    public static void main(String[] args) {
        final IoctlChecks ioctl = new IoctlChecks();
        ioctl.checkHwTstamp();
    }
}
