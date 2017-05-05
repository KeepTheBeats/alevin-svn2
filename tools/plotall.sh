#!/bin/bash

LC_ALL="en_US.UTF-8"
experiment=$1;
nodes=$2;

if [ "$nodes" == "sNodes" ]; then
 echo "plotting sNodes"
elif [ "$nodes" == "vNodes" ]; then
 echo "plotting vNodes"
if [ "$nodes" == "vNets" ]; then
 echo "plotting vNets"
else
 echo "choose sNodes, vNodes, or vNets!"
 exit 1
fi

metricNames2d=(AvAllPathLength AcceptedVnrRatio RejectedNetworksNumber AvPathLength Cost CostRevenue PowerConsumption MaxPathLength RatioMappedRevenue RevenueCost RunningNodes SolelyForwardingHops BFMessageCounter MessagesPerLink NormalMessageCounter NotifyMessageCounter AverageSentMsgsPerNode NodesUsedSolelyForForwarding ClusterCounter AvSecSpreadDemProv MaxSecSpreadDemProv AvSecSpreadProvDem MaxSecSpreadProvDem Runtime)


execpath=`dirname "$0"`;

#$execpath/2dplot_time.sh "$experiment" "$param2d1" "$param2d2";

for metric in "${metricNames2d[@]}"; do
  $execpath/2dplot.sh "$experiment" "$metric" "$nodes";
done

