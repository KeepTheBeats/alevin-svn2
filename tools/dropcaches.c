#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

int main()
{
   setuid( 0 );
   system( "echo 3 > /proc/sys/vm/drop_caches" );

   return 0;
}

