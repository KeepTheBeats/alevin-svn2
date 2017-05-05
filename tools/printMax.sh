#!/bin/bash

metric="$1"; for n in `ls *_out.txt`; do echo ""; echo "$n:"; max="-1"; lastNet=""; while read -r i; do network=`echo "$i" | cut -d_ -f1`; val=`echo "$i" | cut -f2`; if [ "$val" == "NaN" ]; then continue; fi; if [[ "$lastNet" != "" && "$lastNet" != "$network" ]]; then echo "$lastNet $max"; max=$val; fi;  max=`echo "$max $val" | awk '{if ($1 > $2) print $1; else print $2}'`; lastNet=$network; done < <(grep "$metric" "$n" | cut -f2,3 | sort -k1,1V); echo "$lastNet $max"; done

