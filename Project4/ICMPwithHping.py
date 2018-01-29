#!/usr/bin/python
import socket
def main(destName):
        destAddress = socket.gethostbyname(destName)
        port = 33434
        max_hops = 30
        icmp = socket.getprotobyname('icmp')
        udp = socket.getprotobyname('udp')
        timeToLive = 1
        while True:
            recSocket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
            sendSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)
            sendSocket.setsockopt(socket.SOL_IP, socket.IP_TTL, timeToLive)
            recSocket.bind(("", port))
            sendSocket.sendto("", (destName, port))
            recSocket.settimeout(2.0)
            sendSocket.settimeout(2.0)
            curAddress = None
            curName = None
            try:
                _, curAddress = recSocket.recvfrom(512)
                curAddress = curAddress[0]
                try:
                    curName = socket.gethostbyaddr(curAddress)[0]
                except socket.error:
                    curName = curAddress
            except socket.error:
                pass
            finally:
                sendSocket.close()
                recSocket.close()
            if curAddress is not None:
                curHost = "%s (%s)" % (curName, curAddress)
            else:
                curr_host = "* * *"
            print "%d\t%s" % (timeToLive, curHost)
            timeToLive += 1
            if curAddress == destAddress or timeToLive >  max_hops:
                break
if __name__ == "__main__":
   main('www.google.com')
