import java.net.*;
import java.io.*;

public class interpreter {

	public static void main(String args[]) {

		String query, server;
		int port;
		DatagramSocket dgSocket;
		DatagramPacket rec, send;

		// Check the command line for the correct arguments.
		if (args.length < 1) {
			System.err.println("Usage: java MP3 query [server]");
			System.exit(0);
		}
		// Default to port 53 (correct for DNS) and the Hope College DNS server
		// (ebon)
		port = 53;
		server = "ebon.hope.edu";

		// Grab the query, and possibly the server, from the command line.
		query = args[0];
		if (args.length > 1) {
			server = args[1];
		}

		// Determine the address of the server, i.e., the destination of the DNS
		// packet
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(server);
		} catch (UnknownHostException uhe) {
			System.err.println("No such host: " + server);
			System.exit(0);
		}

		// Now we start the packet building. First we create the socket.
		dgSocket = null;
		try {
			dgSocket = new DatagramSocket();

			// Create a byte array that is the packet. We build the packet by
			// creating
			// the DNS application data. The UDP headers are created
			// automatically by Java
			// when we create the DatagramPacket. We have to build the correct
			// packet data.
			// Drawings taken from
			// http://www.ccs.neu.edu/home/amislove/teaching/cs4700/fall09/handouts/project1-primer.pdf
			//
			// +---------------------+
			// | Header |
			// +---------------------+
			// | Question | the question for the name server
			// +---------------------+
			// | Answer | Answers to the question
			// +---------------------+
			// | Authority | Not used in this project
			// +---------------------+
			// | Additional | Not used in this project
			// +---------------------+
			byte[] buf = new byte[16 + query.length() + 2];

			// Start with the header
			// 1 1 1 1 1 1
			// 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | ID |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// |QR| Opcode |AA|TC|RD|RA| Z | RCODE | Flags
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | QUESTION COUNT |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | ANSWER COUNT |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | NAMESERVER COUNT |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			// | ADDITIONAL RESP COUNT |
			// +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
			buf[0] = (byte) 0xAB;
			buf[1] = (byte) 0xCD; // identification number (ABCD)
			buf[2] = 1;
			buf[3] = 0; // flags = 0x0100 (RD = 1, recursion desired)
			buf[4] = 0;
			buf[5] = 1; // 1 question
			buf[6] = buf[7] = 0; // no answers
			buf[8] = buf[9] = 0; // no auth responses
			buf[10] = buf[11] = 0; // no additions

			// Now we fill in the ONE name we want an answer for. We have to
			// fill the name in
			// in the following way:
			// lettercount letters lettercount letters ... 0
			// no "." in the name and each section is preceded by the number of
			// letters in the
			// section
			int ch = 0;
			while (ch < query.length()) {
				int charcount = ch + 12; // position of the lettercount
				int count = 0; // count of letters
				while (ch < query.length() && query.charAt(ch) != '.') {
					buf[13 + ch] = (byte) query.charAt(ch);
					count++;
					ch++;
				}
				buf[charcount] = (byte) count; // fill in the lettercount
				ch++;
			}
			buf[ch + 12] = 0; // terminate the string

			// Finish off the question with two specifiers telling what we are
			// asking
			buf[ch + 13] = 0;
			buf[ch + 14] = 1; // we want an A record
			buf[ch + 15] = 0;
			buf[ch + 16] = 1; // we want an IN record

			// Now send the bloody packet. Create the DatagramPacket from the
			// array we just created and send it.
			send = new DatagramPacket(buf, ch + 17, addr, port);
			dgSocket.send(send);

			// Now we wait for the response.
			byte[] buffer = new byte[65530];
			rec = new DatagramPacket(buffer, buffer.length);
			rec.setLength(65530);
			dgSocket.receive(rec);

			System.out.println("\n\nDomain Name System (response)");

			DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(buffer));
			System.out.println("Transaction ID: 0x" + String.format("%x", dataIn.readShort()));
			System.out.println("Flags: 0x" + String.format("%x", dataIn.readShort()));
			System.out.println("Questions: " + String.format("%x", dataIn.readShort()));
			System.out.println("Answers RRs: " + String.format("%x", dataIn.readShort()));
			System.out.println("Authority RRs: " + String.format("%x", dataIn.readShort()));
			System.out.println("Additional RRs: " + String.format("%x", dataIn.readShort()));

			int recLen = 0;
			while ((recLen = dataIn.readByte()) > 0) {
				byte[] record = new byte[recLen];

				for (int i = 0; i < recLen; i++) {
					record[i] = dataIn.readByte();
				}

				System.out.println("Record: " + new String(record, "UTF-8"));
			}

			System.out.println("Record Type: " + String.format("%x", dataIn.readShort()));
			System.out.println("Class: " + String.format("%x", dataIn.readShort()));
			System.out.println("Field: 0x" + String.format("%x", dataIn.readShort()));
			System.out.println("Type: " + String.format("%x", dataIn.readShort()));
			System.out.println("Class: " + String.format("%x", dataIn.readShort()));
			System.out.println("TTL: " + String.format("%x", dataIn.readInt()));

			short addrLength = dataIn.readShort();
			System.out.println("Len: " + String.format("%x", addrLength) + " Bytes");

			System.out.print("Address: ");
			for (int i = 0; i < addrLength; i++) {
				System.out.print("" + String.format("%d", (dataIn.readByte() & 0xFF)) + ".");
			}
			System.out.println("");
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
}
