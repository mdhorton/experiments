#!/bin/bash

IF_NAME="enp2s0f1"
START_IRQ="38"

# each motherboard socket has 8 cpus
CPU_LIST=`seq 0 7`

# we have 8 rx queues total
# we assign 2 cores to each queue

# for each 4 core bitmask:
# cpu 0 has the irq for RSS
# cpus 1-2 are for RPS
# cpu 3 will be for OS and JVM
# binary = 0110, hex = 6
RPS_CPU_MASK="6"

echo ${CPU_LIST}

# RSS
for i in ${CPU_LIST}; do
  irq=$((i + START_IRQ))
  cpu=$((i * 4))

  fname="/proc/irq/${irq}/smp_affinity_list"

  echo ${cpu}   ${fname}
  echo ${cpu} > ${fname}
done

# RPS
for i in ${CPU_LIST}; do
  mask=""

  for m in ${CPU_LIST}; do
    if [ ${i} -eq ${m} ]; then
      mask="${RPS_CPU_MASK}${mask}"
    else
      mask="0${mask}"
    fi
  done

  fname="/sys/class/net/${IF_NAME}/queues/rx-${i}/rps_cpus"

  echo 00000000,${mask}   ${fname}
  echo 00000000,${mask} > ${fname}
done
