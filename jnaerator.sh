#!/usr/bin/env bash

rm -fr jnaerator

java -jar jnaerator-0.13-SNAPSHOT-shaded.jar -runtime JNA -mode Directory \
    -f \
    -o jnaerator \
    -package net.nostromo.libc.struct.c \
    -library c \
    -I/usr/include \
    /usr/include/linux/if_packet.h \
    /usr/include/linux/if_ether.h \
    /usr/include/netinet/ip.h \
    /usr/include/netinet/tcp.h \
    /usr/include/netinet/udp.h \
    /usr/include/netinet/ip_icmp.h \
    /usr/include/sys/mman.h \
    /usr/include/sys/poll.h \
    /usr/include/bits/socket.h \
    /usr/include/net/if.h \
    /usr/include/arpa/inet.h \
    /usr/include/string.h \
    /usr/include/sched.h \
    /usr/include/unistd.h \
    /usr/include/sys/stat.h \
    /usr/include/fcntl.h

