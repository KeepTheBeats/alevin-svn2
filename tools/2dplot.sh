#!/bin/bash

LC_ALL="en_US.UTF-8"
experiment=$1;
metric=$2;
nodes=$3;

networkColumn=2;
plotname="";
if [ "$nodes" == "sNodes" ]; then
 echo "plotting sNodes"
 networkColumn=1;
 plotname="#sNodes"
elif [ "$nodes" == "vNodes" ]; then
 echo "plotting vNodes"
 networkColumn=3;
 plotname="#vNodes"
elif [ "$nodes" == "vNets" ]; then
 echo "plotting vNets"
 networkColumn=2;
 plotname="#vNets"
else
 echo "choose sNodes, vNodes, or vNets!"
 exit 1
fi

outdir=`echo "$experiment" | sed -e 's/_out.txt$//'`
filenameprefix=$outdir"/"$metric;
if [ -e "$outdir" ]; then
  rm -f "$filenameprefix".gnuplot;
  rm -f "$filenameprefix".data;
  rm -f "$filenameprefix".sorteddata;
  rm -f "$filenameprefix".png;
  rm -f "$filenameprefix".pdf;
else
  mkdir "$outdir";
fi

# echo "set terminal pdfcairo noenhanced dashed color" >> $filenameprefix".gnuplot";
# echo "set terminal pngcairo noenhanced dashed color size 1200,1200" >> $filenameprefix".gnuplot";
echo "set terminal pngcairo noenhanced dashed color size 350,350" >> $filenameprefix".gnuplot";
echo "set output \""$filenameprefix".png\"" >> $filenameprefix".gnuplot";
# echo "set title '$metric'" >> $filenameprefix".gnuplot";
echo "set xlabel 'number of substrate nodes'" >> $filenameprefix".gnuplot";
echo "set ylabel '$metric' rotate left" >> $filenameprefix".gnuplot";
echo "set border 3" >> $filenameprefix".gnuplot";
echo "set xtics nomirror" >> $filenameprefix".gnuplot";
echo "set ytics nomirror" >> $filenameprefix".gnuplot";
echo -n "plot '"$filenameprefix".sorteddata' notitle with yerrorbars" >> $filenameprefix".gnuplot";

scenario="";
thisScenario="";

network="";
thisLineNetwork="";

value="0.0";
values=();
sum="0.0";
numOf=0;
avg="0.0";
while read -r i; do

  thisScenario=`echo "$i" | cut -f2`;
  thisLineNetwork=`echo "$thisScenario" | cut -f$networkColumn -d "_"`;

  value=`echo "$i" | cut -f 3`  
  if [ "$value" == "NaN" ]; then
    continue;
  fi
  
  if [ -z $network ] || [ $network != $thisLineNetwork ]; then
    if [ "$network" != "" ]; then
      if [ "$numOf" -eq 0 ]; then
        avg="0";
      else
        avg=`echo "$sum/$numOf" | bc -l`;
      fi

      s=`echo ${values[@]} | sed -e 's/ /,/g'`;
      sd=`Rscript -e "t=c($s); n <- length(t); se <- sd(t)/sqrt(n); m <- mean(t); cv <- qt(0.975,df=n-1); c(m-cv*se,m+cv*se)" | sed -e 's/\[.*\]//' -e 's/^ *//'`;
      sd1=`echo "$sd" | cut -f1 -d' '`
      sd2=`echo "$sd" | cut -f2 -d' '`
      values=();
      echo "$network	`printf "%.3f" "$avg"`	$sd1	$sd2" >> $filenameprefix".data";
    fi

    network=$thisLineNetwork;
    sum="0.0";
    numOf=0;

  fi
  
  values[${#values[@]}]="$value";

  if [ "$value" != "NaN" ]; then
    numOf=$((numOf+1));
    sum=`echo "$sum+$value" | bc -l`;
  fi

done < <(grep "^$metric	" "$experiment" | sed -e 's/_vbandwidthWeight:10.0_delegationNodesLevel:0_fullKnowledgeNodesLevel:0_partitions:2//' | cut -f 1,2,3,4,5 -d "_" | sort -t_ -k"$networkColumn","$networkColumn"V)

if [ "$numOf" -eq 0 ]; then
  avg="0";
else
  avg=`echo "$sum/$numOf" | bc -l`;
fi

if [ "$network" != "" ]; then
  s=`echo ${values[@]} | sed -e 's/ /,/g'`;
  sd=`Rscript -e "t=c($s); n <- length(t); se <- sd(t)/sqrt(n); m <- mean(t); cv <- qt(0.975,df=n-1); c(m-cv*se,m+cv*se)" | sed -e 's/\[.*\]//' -e 's/^ *//'`;
  sd1=`echo "$sd" | cut -f1 -d' '`
  sd2=`echo "$sd" | cut -f2 -d' '`
  echo "$network	`printf "%.3f" "$avg"`	$sd1	$sd2" >> $filenameprefix".data";
else
  echo "0	0.000" >> $filenameprefix".data";
fi

sort -V $filenameprefix".data" -o $filenameprefix".sorteddata";
rm $filenameprefix".data";
gnuplot $filenameprefix".gnuplot";

