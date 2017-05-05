#!/bin/bash

if [ -e "$2" ]; then
 rm "$2";
fi

alevindir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
cd "$alevindir";
java -cp bin:lib/junit-4.11.jar:lib/JUNG2.jar tests.algorithms.singlenetworkmapping.IOMapper "$1" "$2";

