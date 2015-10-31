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

package net.nostromo.experiments.network.node_latency;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class LatencyServer {

    private final int size = 256;
    private final int port = 55555;
    private final byte[] bytes = new byte[size];
    private final ByteBuffer bb = ByteBuffer.allocateDirect(size);


    public void launch() throws Exception {
        final ServerSocket server = new ServerSocket(port);

        while (true) {
            try (final Socket sock = server.accept();
                 final BufferedInputStream bis = new BufferedInputStream(sock.getInputStream());
                 final BufferedOutputStream bos = new BufferedOutputStream(
                         sock.getOutputStream())) {
                System.out.println("client connected");

                while (true) {
                    int remaining = bytes.length;

                    while (remaining > 0) {
                        final int len = bis.read(bytes, bytes.length - remaining, remaining);
                        remaining -= len;
                        if (len == -1) break;
                    }

                    bos.write(bytes);
                    bos.flush();
                }
            }
            catch (final Exception ex) {
                System.out.println("socket closed");
            }
        }
    }

    public void poll() throws Exception {
        final ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));

        while (true) {
            try (final SocketChannel sock = server.accept()) {
                sock.configureBlocking(false);
                System.out.println("client connected");

                while (true) {
                    read(sock);
                    write(sock);
                }
            }
            catch (final RuntimeException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void hybrid() throws Exception {
        final ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));

        while (true) {
            try (final SocketChannel sock = server.accept()) {
                sock.configureBlocking(false);
                System.out.println("client connected");

                while (true) {
                    hybridRead(sock);
                    write(sock);
                }
            }
            catch (final RuntimeException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void read(final SocketChannel sock) throws Exception {
        bb.position(0);
        int remaining = bb.capacity();

        while (remaining > 0) {
            final int len = sock.read(bb);
            remaining -= len;
            if (len == -1) throw new RuntimeException("socket closed");
        }
    }

    private void hybridRead(final SocketChannel sock) throws Exception {
        bb.position(0);
        int remaining = bb.capacity();
        long start = System.nanoTime();

        while (remaining > 0) {
            final int len = sock.read(bb);
            remaining -= len;
            if (len == -1) throw new RuntimeException("socket closed");

            if (len > 0) {
                start = System.nanoTime();
                if (sock.isBlocking()) sock.configureBlocking(false);
            }
            else {
                if (System.nanoTime() - start > 1_000_000_000) {
                    sock.configureBlocking(true);
                }
            }
        }
    }

    private void write(final SocketChannel sock) throws Exception {
        bb.position(0);
        int remaining = bb.capacity();

        while (remaining > 0) {
            final int len = sock.write(bb);
            remaining -= len;
            if (len == -1) throw new RuntimeException("socket closed");
        }
    }

    public static void main(final String[] args) throws Exception {
        final LatencyServer server = new LatencyServer();
        server.bb.limit(server.size);

        if (args[0].equals("poll")) {
            server.poll();
        }
        else if (args[0].equals("hybrid")) {
            server.hybrid();
        }
        else {
            server.launch();
        }
    }
}
