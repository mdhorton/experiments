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

import net.nostromo.libc.LibcUtil;
import net.nostromo.libc.TheUnsafe;
import org.openjdk.jmh.annotations.*;
import sun.misc.Unsafe;

import java.util.concurrent.TimeUnit;

// Using copyMemory() for less than ~32 bytes is relatively expensive.
// IOW copying 1 byte is about the same as copying 32 bytes.
// So unless you need to copy many bytes, its best to use some other
// means if possible.
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class CopyMemoryPerf {

    private Unsafe unsafe;

    private long pointer;
    private byte[] byteArray = new byte[32];
    private byte[] objArray = new byte[byteArray.length];

    @Setup
    public void setup() {
        LibcUtil.util.setCpu(8);
        unsafe = TheUnsafe.unsafe;

        pointer = unsafe.allocateMemory(byteArray.length);

    }

    @TearDown
    public void teardown() {
        unsafe.freeMemory(pointer);
    }

    @Benchmark
    public void write() {
        unsafe.copyMemory(objArray, Unsafe.ARRAY_BYTE_BASE_OFFSET, byteArray,
                Unsafe.ARRAY_BYTE_BASE_OFFSET, objArray.length);
    }

    @Benchmark
    public void read() {
        unsafe.copyMemory(byteArray, Unsafe.ARRAY_BYTE_BASE_OFFSET, objArray,
                Unsafe.ARRAY_BYTE_BASE_OFFSET, byteArray.length);
    }
}
