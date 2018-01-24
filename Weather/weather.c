#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <arpa/inet.h> 

int main(int argc, char *argv[])
{
    int sockfd = 0, n = 0;
    char rBuffer[1024];  
	FILE *fp;
	char *p, *x;
	
struct addrinfo hints, *servinfo, *sockInfo;
int rv;

memset(&hints, 0, sizeof hints);
hints.ai_family = AF_UNSPEC; 
hints.ai_socktype = SOCK_STREAM;

if ((rv = getaddrinfo("www.wzzm13.com", "80", &hints, &servinfo)) != 0) {
    fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
    exit(1);
}

for(sockInfo = servinfo; sockInfo != NULL; sockInfo = sockInfo->ai_next) {
    if ((sockfd = socket(sockInfo->ai_family, sockInfo->ai_socktype,
            sockInfo->ai_protocol)) == -1) {
        perror("socket");
        continue;
    }

    if (connect(sockfd, sockInfo->ai_addr, sockInfo->ai_addrlen) == -1) {
        perror("connect");
        close(sockfd);
        continue;
    }
    break;
}

if (sockInfo == NULL) {
    fprintf(stderr, "failed to connect\n");
    exit(2);
}

    freeaddrinfo(servinfo); // all done with this structure
	fp = fopen("page.txt", "ab");
	
	char* request = "GET /weather/loc?city=USMI0396&cityName=Holland%2C%20M HTTP/1.1\r\nHost: www.wzzm13.com\r\nConnection: close\r\n\r\n";
	send(sockfd, request, strlen(request), 0);

    while ( (n = read(sockfd, rBuffer, sizeof(rBuffer)-1)) > 0)
    {
        rBuffer[n] = 0;
		
		p = strstr(rBuffer, "current-temp\">");
		if(p != NULL){
			printf("Current temperature in Holland,MI is: %c%c degrees\n", p[21], p[22]);
		}
        if(fputs(rBuffer, fp) == EOF)
        {
            printf("\n Error : Fputs error\n");
        }
    } 

    if(n < 0)
    {
        printf("\n Read error \n");
    } 

   return 0;
}
