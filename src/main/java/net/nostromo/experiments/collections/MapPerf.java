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

package net.nostromo.experiments.collections;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.nostromo.libc.LibcUtil;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.concurrent.TimeUnit;

// trove is still faster.
// although I've seen others tests that say it isnt. YMMV.

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class MapPerf {

    private final Object obj = new Object();

    private Long2ObjectMap<Object> fastLongMap;
    private Int2ObjectMap<Object> fastIntMap;
    private TLongObjectMap<Object> troveLongMap;
    private TIntObjectMap<Object> troveIntMap;
    private Map<Integer, Object> kolobokeIntMap;

    private boolean doPut = true;
    private int size = (1 << 19) - 1;
    private long longIdx;
    private int intIdx;

    @Setup
    public void setup() {
        LibcUtil.util.setLastCpu();

        fastLongMap = new Long2ObjectOpenHashMap<>(size);
        fastIntMap = new Int2ObjectOpenHashMap<>(size);
        troveLongMap = new TLongObjectHashMap<>(size);
        troveIntMap = new TIntObjectHashMap<>(size);
        kolobokeIntMap = HashIntObjMaps.newUpdatableMap(size);

        if (!doPut) {
            for (int x = 0; x <= size; x++) {
                fastLongMap.put(x, obj);
                fastIntMap.put(x, obj);
                troveLongMap.put(x, obj);
                troveIntMap.put(x, obj);
                kolobokeIntMap.put(x, obj);
            }
        }
    }

    @Benchmark
    public void fastutilLongGet(final Blackhole hole) {
        final long idx = longIdx & size;
        if (doPut) fastLongMap.put(idx, obj);
        hole.consume(fastLongMap.get(idx));
        longIdx++;
    }

    @Benchmark
    public void fastutilIntGet(final Blackhole hole) {
        final int idx = intIdx & size;
        if (doPut) fastIntMap.put(idx, obj);
        hole.consume(fastIntMap.get(idx));
        intIdx++;
    }

    @Benchmark
    public void troveLongGet(final Blackhole hole) {
        final long idx = longIdx & size;
        if (doPut) troveLongMap.put(idx, obj);
        hole.consume(troveLongMap.get(idx));
        longIdx++;
    }

    @Benchmark
    public void troveIntGet(final Blackhole hole) {
        final int idx = intIdx & size;
        if (doPut) troveIntMap.put(idx, obj);
        hole.consume(troveIntMap.get(idx));
        intIdx++;
    }

    @Benchmark
    public void kolobokeIntGet(final Blackhole hole) {
        final int idx = intIdx & size;
        if (doPut) kolobokeIntMap.put(idx, obj);
        hole.consume(kolobokeIntMap.get(idx));
        intIdx++;
    }
}
