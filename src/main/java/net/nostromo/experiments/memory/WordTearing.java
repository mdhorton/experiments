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

package net.nostromo.experiments.memory;

import net.nostromo.libc.Libc;
import net.nostromo.libc.LibcConstants;
import net.nostromo.libc.TheUnsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

// Prove that word tearing can exist when using mmap.
public class WordTearing implements LibcConstants {

    private static final Logger LOG = LoggerFactory.getLogger(WordTearing.class);

    private static final Unsafe unsafe = TheUnsafe.unsafe;
    private static final Libc libc = Libc.libc;

    private static final String FNAME = "/tmp/mmap.bin";
    private static final long MAP_SIZE = 128;

    // when OFFSET is between 61-63 it will cause word tearing.
    // this is due to r/w across cache lines.
    // cache lines are typically 64 bytes.
    // set OFFSET <60 and it works fine.
    private static final long OFFSET = 61;

    public static void main(String[] args) throws Exception {
        final long empty = unsafe.allocateMemory(1);

        final int fd = libc.open(FNAME, O_CREAT | O_RDWR, S_IRUSR | S_IWUSR);
        libc.ftruncate(fd, 0);
        libc.lseek(fd, MAP_SIZE - 1, SEEK_SET);
        libc.write(fd, empty, 1);
        libc.lseek(fd, 0, SEEK_SET);

        unsafe.freeMemory(empty);

        final MmapThread mmap1 = new MmapThread(fd);
        final MmapThread mmap2 = new MmapThread(fd);
        mmap1.start();
        mmap2.start();
    }

    private static class MmapThread extends Thread {

        private int fd;

        public MmapThread(final int fd) {
            this.fd = fd;
        }

        @Override
        public void run() {
            try {
                final int a = 482624146;
                final int b = -983725452;

                final long mmap = libc.mmap(0, MAP_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);

                while (true) {
                    final int val = unsafe.getInt(null, mmap + OFFSET);
                    if (val != 0) {
                        if (val != a && val != b) {
                            throw new RuntimeException("val: " + val);
                        }
                    }
                    unsafe.putInt(null, mmap + OFFSET, val == a ? b : a);
                }
            }
            catch (final Exception ex) {
                LOG.error("error", ex);
            }
        }
    }
}
