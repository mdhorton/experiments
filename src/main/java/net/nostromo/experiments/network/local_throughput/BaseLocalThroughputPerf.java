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

package net.nostromo.experiments.network.local_throughput;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, batchSize = 1)
@Measurement(iterations = 5, batchSize = 1)
@Fork(1)
@State(Scope.Thread)
public abstract class BaseLocalThroughputPerf {

    public static final int port = 55555;
    public static final String host = "localhost";

    public static final int bufferSize = 1 << 15;
    public static final int iterations = 1 << 15;

    protected CountDownLatch serverLatch;
    protected CountDownLatch setupLatch;
    protected CountDownLatch runLatch;

    protected Thread server;
    protected Thread client;

    protected abstract Thread createServer();

    protected abstract Thread createClient();

    @Setup(Level.Iteration)
    public void setup() throws Exception {
        serverLatch = new CountDownLatch(1);
        setupLatch = new CountDownLatch(2);
        runLatch = new CountDownLatch(1);

        server = createServer();
        server.start();
        // wait for server to fully initialize so that
        // the client doesnt get connection refused
        serverLatch.await();

        client = createClient();
        client.start();

        // wait for everything to fully initialize
        setupLatch.await();
    }

    @Benchmark
    public void blockingRead() throws Exception {
        runLatch.countDown();
        client.join();
        server.join();
    }
}
