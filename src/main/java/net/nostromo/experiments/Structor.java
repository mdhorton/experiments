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

package net.nostromo.experiments;

public class Structor implements LibcConstants {

//    public static sockaddr_ll sockaddr_ll(final String ifname, final short linkFamily, final int protocol) {
//        return sockaddr_ll(ifname, linkFamily, protocol, ZERO_SHORT, ZERO_BYTE, ZERO_BYTE);
//    }
//
//    public static sockaddr_ll sockaddr_ll(final String ifname, final short linkFamily, final int protocol,
//            final short hatype, final byte pkttype, final byte halen) {
//        final sockaddr_ll saLink = new sockaddr_ll();
//
//        saLink.sll_family = linkFamily;
//        saLink.sll_protocol = Util.htons((short) protocol);
//        saLink.sll_ifindex = libc.if_nametoindex(ifname);
//        saLink.sll_hatype = hatype;
//        saLink.sll_pkttype = pkttype;
//        saLink.sll_halen = halen;
//        saLink.write();
//
//        return saLink;
//    }
//
//    public static tpacket_req3 tpacket_req3(final int blockSize, final int frameSize, final int blockCnt,
//            final int timeout, final int privOffset, final int featureReq) {
//        final tpacket_req3 req = new tpacket_req3();
//
//        req.tp_block_size = blockSize;
//        req.tp_frame_size = frameSize;
//        req.tp_block_nr = blockCnt;
//        req.tp_frame_nr = (int) (((long) blockSize * blockCnt) / frameSize);
//        req.tp_retire_blk_tov = timeout;
//        req.tp_sizeof_priv = privOffset;
//        req.tp_feature_req_word = featureReq;
//        req.write();
//
//        return req;
//    }
}
