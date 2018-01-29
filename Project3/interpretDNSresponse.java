import java.net.*;
import java.io.*;

public class MP3 {
	
	public static void main(String args[]) {
		
		String query, server;
		int port;
		DatagramSocket dgSocket;
		DatagramPacket rec, send;
		String line;

		// Check the command line for the correct arguments.
		if (args.length < 1) {
			//System.err.println("Usage: java MP3 query [server]");
			//System.exit(0);
		}

		// Default to port 53 (correct for DNS) and the Hope College DNS server (ebon)
		port = 53;
		server = "awsdns.hope.edu";
		
		// Grab the query, and possibly the server, from the command line.
		query = "www.wsj.com"; //args[0];
		if (args.length > 1) {
			server = args[1];
		} 

		// Determine the address of the server, i.e., the destination of the DNS packet
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(server);
		} catch (UnknownHostException uhe) {
			System.err.println("No such host: " + server);
			System.exit(0);
		}

		// Now we start the packet building.  First we create the socket.
		dgSocket = null;
		try {
			dgSocket = new DatagramSocket();
			
			// Create a byte array that is the packet.  We build the packet by creating 
			// the DNS application data.  The UDP headers are created automatically by Java
			// when we create the DatagramPacket.  We have to build the correct packet data.
			// Drawings taken from
			// http://www.ccs.neu.edu/home/amislove/teaching/cs4700/fall09/handouts/project1-primer.pdf
			//
			// +---------------------+
			// | Header              |
			// +---------------------+
			// | Question            | the question for the name server
			// +---------------------+
			// | Answer              | Answers to the question
			// +---------------------+
			// | Authority           | Not used in this project
			// +---------------------+
			// | Additional          | Not used in this project
			// +---------------------+
			byte[] buf = new byte[16 + query.length() + 2];
			
			// Start with the header
			//                                 1  1  1  1  1  1
			//   0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// |                      ID                       |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// |QR|    Opcode |AA|TC|RD|RA|   Z    |   RCODE   |  Flags
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | QUESTION COUNT                                |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | ANSWER COUNT                                  |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | NAMESERVER COUNT                              |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | ADDITIONAL RESP COUNT                         |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			buf[0] = (byte)0xAB; buf[1] = (byte) 0xCD; // identification number (ABCD)
			buf[2] = 1; buf[3] = 0;                    // flags = 0x0100 (RD = 1, recursion desired)
			buf[4] = 0; buf[5] = 1;                    // 1 question
			buf[6] = buf[7] = 0;                       // no answers
			buf[8] = buf[9] = 0;                       // no auth responses
			buf[10] = buf[11] = 0;                     // no additions
			
			// Now we fill in the ONE name we want an answer for.  We have to fill the name in
			// in the following way:
			//   lettercount letters lettercount letters ... 0
			// no "." in the name and each section is preceded by the number of letters in the
			// section
			int ch = 0;
			while (ch < query.length()) {
				int charcount = ch + 12;    // position of the lettercount
				int count = 0;              // count of letters
				while (ch < query.length() && query.charAt(ch) != '.') {
					buf[13 + ch] = (byte) query.charAt(ch);
					count++;
					ch++;
				}
				buf[charcount] = (byte) count;   // fill in the lettercount
				ch++;
			}
			buf[ch + 12] = 0; // terminate the string
			
			// Finish off the question with two specifiers telling what we are asking
			buf[ch + 13] = 0; buf[ch + 14] = 1;    // we want an A record
			buf[ch + 15] = 0; buf[ch + 16] = 1;    // we want an IN record

			// Now send the bloody packet.  Create the DatagramPacket from the 
			// array we just created and send it.
			send = new DatagramPacket(buf, ch + 17, addr, port);
			dgSocket.send(send);
			
			// Now we wait for the response.
			rec = new DatagramPacket(new byte[65530], 65530);
			rec.setLength(65530);
			dgSocket.receive(rec);
			
			// And now we interpret the response!
            byte[] response = rec.getData();
            System.out.println("Transaction ID: 0x" + bytesToHex(response, 0, 1));
            
            System.out.println("Flags: 0x" + bytesToHex(response, 2, 3));
 
            String flags = byteToBinary(response[2]) + byteToBinary(response[3]);
            String message;
            if (flags.charAt(0) == '1') {
                message = "Message is a response";
            } else {
                message = "Message is a query";
            }
            System.out.print("   ");
            printBits(flags, 16, 0, 0, message);
 
            String opcode = flags.substring(1, 5);
            if (opcode.equals("0000")) {
                message = "Standard query (0)";
            } else if (opcode.equals("0001")) {
                message = "Inverse query (1)";
            } else {
                message = "Other opcode";
            }
            System.out.print("   ");
            printBits(flags, 16, 1, 4, message);

            if (flags.charAt(5) == '1') {
                message = "Server is an authority for the domain";
            } else {
                message = "Server is not an authority for the domain";
            }
            System.out.print("   ");
            printBits(flags, 16, 5, 5, message);
 
            if (flags.charAt(6) == '1') {
                message = "Message is truncated";
            } else {
                message = "Message is not truncated";
            }
            System.out.print("   ");
            printBits(flags, 16, 6, 6, message);
 
            if (flags.charAt(7) == '1') {
                message = "Do query recursively";
            } else {
                message = "Do not query recursively";
            }
            System.out.print("   ");
            printBits(flags, 16, 7, 7, message);
 
            if (flags.charAt(8) == '1') {
                message = "Server can do recursive queries";
            } else {
                message = "Server cannot do recursive queries";
            }
            System.out.print("   ");
            printBits(flags, 16, 8, 8, message);

            System.out.print("   ");
            printBits(flags, 16, 9, 11, "Reserved");
            
            String responseCode = flags.substring(12);
            if (responseCode.equals("0000")) {
                message = "No error (0)";
            } else if (responseCode.equals("0001")) {
                message = "Format error (1) - The name server was unable to interpret the query";
            } else if (responseCode.equals("0010")) {
                message = "Server failure (2) - The name server was unable to process this query due to a problem with the name server";
            } else if (responseCode.equals("0011")) {
                message = "Name Error (3) - The domain name referenced in the query does not exist";
            } else if (responseCode.equals("0100")) {
                message = "Not Implemented (4) - The name server does not support the requested kind of query";
            } else if (responseCode.equals("0101")) {
                message = "Refused (5) - The name server refuses to perform the specified operation for policy reasons";
            } else {
                message = "Unknown opcode";
            }
            System.out.print("   ");
            printBits(flags, 16, 12, 16, message);
 
            int questionCount = (response[4] << 8) + response[5];
            System.out.println("Number of Questions: " + questionCount);
            int answerCount = (response[6] << 8) + response[7]; 
            System.out.println("Number of Answers: " + answerCount);
            int serverCount = (response[8] << 8) + response[9]; 
            System.out.println("Number of Authoritative Nameservers: " + serverCount);
            int additionalResponseCount = (response[10] << 8) + response[11];  
            System.out.println("Number of Additional Responses: " + additionalResponseCount);
 
            int position = 12; 
 
            if (questionCount > 0) System.out.println("\n*** Questions ***");            
            while (questionCount > 0) {
                System.out.print("   ");
                position = printDNSName(response, position);
                System.out.print(": type ");
                int qtype = (response[position] << 8) + response[position+1];
                switch (qtype) {
	                case 0: 
	                	System.out.print("I");
	                	break;
	                case 1: 
	                	System.out.print("A");
	                	break;
	                case 2: 
	                	System.out.print("M");
	                	break;
	                case 3: 
	                	System.out.print("MX");
	                	break;
	                case 4:
	                	System.out.print("*");
	                	break;
                }
                System.out.print(", class ");

                position += 2;
                int qclass = (response[position] << 8) + response[position+1];
                if (qclass == 1)
                	System.out.print("IN");
                else 
                	System.out.print("other");
                System.out.println();
                questionCount --;
                position += 2;
            }
 
            if (answerCount > 0) System.out.println("\n*** Answers ***");
            while (answerCount > 0) {
            	position = printAnswer(response, position);
            	
                System.out.println();
                answerCount --;
            }	
            
            if (serverCount > 0) System.out.println("\n*** Authoritative Nameservers ***");
            while (serverCount > 0) {
                System.out.print("   ");
                position = printDNSName(response, position);
                System.out.print(": type ");
                int qtype = (response[position] << 8) + response[position+1];
                switch (qtype) {
                case 1: 
                	System.out.print("A");
                	break;
                case 2: 
                	System.out.print("NS");
                	break;
                case 5: 
                	System.out.print("CNAME");
                	break;
                case 6:
                	System.out.print("SOA");
                	break;
                case 12:
                	System.out.print("PTR");
                	break;
                case 15:
                	System.out.print("MX");
                	break;
                case 16:
                	System.out.print("TXT");
                	break;
                default:
                	System.out.print("ERR");
                	break;
	            }
	            System.out.print(", class ");
	
	            position += 2;
	            int qclass = (response[position] << 8) + response[position+1];
	            if (qclass == 1)
	            	System.out.print("IN");
	            else 
	            	System.out.print("other");
	            System.out.println();
	            position += 2;
	            System.out.println("   Time to live: " + ((response[position] << 24) + 
	            		(response[position+1] << 16) + (response[position+2] << 8) +
	            		response[position+3]));
	            position += 4;
	            System.out.println("   Length of Data: " + ((response[position] << 8) + response[position+1]));
	            position += 2;
	            System.out.print("   Name server: "); 
	            position = printDNSName(response, position);
	            System.out.println();
	            
	            System.out.println();
                serverCount --;
            }

            if (additionalResponseCount > 0) System.out.println("\n*** Additional Responses ***");
            while (additionalResponseCount > 0) {
            	position = printAnswer(response, position);
                System.out.println();

                System.out.println();
                additionalResponseCount --;
            }	
            
		} catch (IOException ie) {
			System.err.println("IO exception in datagram test: " + ie);
		} finally {
			if (dgSocket != null)
				try {
					dgSocket.close();
				} catch (Exception e) {
					// don't do anything with it.
				}
		}
	}
 
    private static String byteToBinary(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }
 
    public static String bytesToHex(byte[] bytes, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
    
    public static void printBits(String bytes, int width, int start, int end, String message) {
    	for (int i=0; i<width; i++) {
    		if (i>0 && i%4 == 0) System.out.print(" ");
    		if (i<start) 
    			System.out.print(".");
    		else if (i>end)
    			System.out.print(".");
    		else {
    			System.out.print(bytes.charAt(i));
    		}
    	}
    	System.out.println(" = " + message);
    }
    
    public static int printDNSName(byte bytes[], int start) {
    	
    	int pos = start;
    	while (bytes[pos] != 0) {
    		if (pos != start) System.out.print(".");
    		int length = bytes[pos];
    		
    		// POINTER! We recursively print from a different place in the
    		// packet
    		if (length == -64) {
    			int pos2 = bytes[pos+1] & 0xFF;
				printDNSName(bytes, pos2);
				pos++;
				break;
			
			// Otherwise the "length" is the number of characters in this
			// part of
			// name.
    		} else {
	    		for (int i=1; i<=length; i++) {
	   				System.out.print((char)bytes[pos+i]);
	    		}
	    		pos += length+1;
    		}
    	}
    	pos ++;
    	return pos;
    }
    
    public static int printAnswer(byte response[], int start) {
    	int position = start;
    	
    	System.out.print("   ");
   		position = printDNSName(response, position);
        System.out.print(": type ");
        int qtype = (response[position] << 8) + response[position+1];
        switch (qtype) {
            case 1: 
            	System.out.print("A");
            	break;
            case 2: 
            	System.out.print("NS");
            	break;
            case 5: 
            	System.out.print("CNAME");
            	break;
            case 6:
            	System.out.print("SOA");
            	break;
            case 12:
            	System.out.print("PTR");
            	break;
            case 15:
            	System.out.print("MX");
            	break;
            case 16:
            	System.out.print("TXT");
            	break;
            default:
            	System.out.print("ERR");
            	break;
        }
        System.out.print(", class ");

        position += 2;
        int qclass = (response[position] << 8) + response[position+1];
        if (qclass == 1)
        	System.out.print("IN");
        else 
        	System.out.print("other");
        System.out.println();
        position += 2;
        System.out.println("   Time to live: " + ((response[position] << 24) + 
        		(response[position+1] << 16) + (response[position+2] << 8) +
        		response[position+3]));
        position += 4;
        System.out.println("   Length of Data: " + ((response[position] << 8) + response[position+1]));
        position += 2;
        switch (qtype) {
        case 1: 
        	System.out.print("   Address: ");
        	System.out.print("" + (response[position]<0 ? 256+response[position++] : response[position++]) + ".");
        	System.out.print("" + (response[position]<0 ? 256+response[position++] : response[position++]) + ".");
        	System.out.print("" + (response[position]<0 ? 256+response[position++] : response[position++]) + ".");
        	System.out.print("" + (response[position]<0 ? 256+response[position++] : response[position++]));
           	break;
        case 2:
        	System.out.print("   Name server: ");
        	position = printDNSName(response, position);
        	break;
        case 5:
        	System.out.print("   Alias for: ");
        	position = printDNSName(response, position);
        	System.out.println();
        	break;
        case 6:
        	System.out.print("   Source of authority for: ");
        	position = printDNSName(response, position);
        	System.out.print("\n      Person responsible: ");
        	position = printDNSName(response, position);
        	System.out.print("\n      Serial number: ");
            System.out.print("" + ((response[position] << 24) + 
            		(response[position+1] << 16) + (response[position+2] << 8) +
            		response[position+3]));
            position += 4;
        	System.out.print("\n      Retry interval: ");
            System.out.print("" + ((response[position] << 24) + 
            		(response[position+1] << 16) + (response[position+2] << 8) +
            		response[position+3]));
            position += 4;
        	System.out.print("\n      Expiration: ");
            System.out.print("" + ((response[position] << 24) + 
            		(response[position+1] << 16) + (response[position+2] << 8) +
            		response[position+3]));
            position += 4;
        	System.out.print("\n      Minimum caching TTL: ");
            System.out.print("" + ((response[position] << 24) + 
            		(response[position+1] << 16) + (response[position+2] << 8) +
            		response[position+3]));
            position += 4;
            break;
        case 12:
        	System.out.print("   Pointer to: ");
        	position = printDNSName(response, position);
        	break;
        case 15:
        	System.out.print("   Mail exchange for: ");
        	int priority = (response[position] << 8) + response[position+1];
        	position += 2;
        	position = printDNSName(response, position);
            System.out.println("(priority = " + priority + ")");
        	break;   
        case 16:
        	System.out.print("   Text: ");
        	char c = ' ';
        	while (c != 0) {
        		c = (char)response[position++];
        		if (c != 0) System.out.print(c);
        		position ++;
        	}
        	System.out.println();
        	break;
        }
        
        return position;
    	
    }
}
