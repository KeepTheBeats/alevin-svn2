#!/bin/bash

t=`grep MemTotal /proc/meminfo | sed -e 's/MemTotal: *//' -e 's/ *kB//'`
f=`grep MemFree /proc/meminfo | sed -e 's/MemFree: *//' -e 's/ *kB//'`

echo "$t+$f" | bc -l

