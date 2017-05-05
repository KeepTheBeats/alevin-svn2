echo "=== /proc/cpuinfo ===
" > "$1"
cat /proc/cpuinfo >> "$1"

echo "

=== /proc/meminfo ===
" >> "$1"
cat /proc/meminfo >> "$1"

echo "

=== /etc/lsb-release ===
" >> "$1"
cat /etc/lsb-release >> "$1"

