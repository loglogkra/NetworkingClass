import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

/*
 * 
 * Logan Kragt specified and implemented the code for MP1
 * 
 */
public class BoxOfficeTop5 {
	public static void main(String[] args) throws IOException {

		final int DEFAULT_PORT = 80; // HTTP
		final int TIMEOUT = 10 * 1000; // 10 Seconds

		DataInputStream reply = null;
		PrintStream send = null;
		Socket sock = null;

		{
			sock = new Socket("www.boxofficemojo.com", DEFAULT_PORT);
			if (sock != null) {
				reply = new DataInputStream(sock.getInputStream());
				send = new PrintStream(sock.getOutputStream());
				sock.setSoTimeout(TIMEOUT);
			}
		}

		// Setup calendar to retrieve yesterdays date.
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		cal.add(Calendar.DATE, -1); //Yesterday's date

		int pos1, pos2;
		String HTMLline, movie, cmd, buffer;

		boolean eof, more, movieList;
		ArrayList<String> movies = new ArrayList<String>();

		// Start the HTTP protocol with a GET
		
		cmd = "GET /daily/chart/?sortdate=2017-01-24 HTTP/1.1";
		send.println(cmd);
		send.println("Host: boxofficemojo.com");
		send.println(cmd);
		send.println("");

		// Now we will read the HTML lines into the buffer
		eof = false;
		movieList = false;
		movie = "placeHolderMovie";
		buffer = "";
		while (!eof) {
			try {
				HTMLline = reply.readLine(); // deprecated-----ignore
				if (HTMLline != null) {
					if (movieList) {
						buffer += HTMLline;

						if (HTMLline.contains("<big>7</big>")) {
							eof = true;
						}
					} else {
						if (HTMLline.contains("Full Week")) {
							movieList = true;
						}
					}
				} else {
					eof = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				eof = true;
			}
		}
		// Now we have the HTML code we want the movies and figures in the
		// ArrayList
		more = true;
		while (more) {
			// FIRST MOVIE WITH figures
			if (buffer.contains("<big>1</big>")) { // FIND REGEX to possibly get rid of convoluted code below!
				pos1 = buffer.indexOf("<a href=\"/movies");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf("</a></b><br><small><a href=\"/s");
				movie = buffer.substring(pos1 + 1, pos2);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				// Get first figures
				pos1 = buffer.indexOf(" valign=\"top\"><b>");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf("<br><br><nobr><small><font color=\"#0000ff");
				movie = buffer.substring(pos1 + 4, pos2);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				// Get 2nd money Figure
				pos1 = buffer.indexOf("color=\"#800080\"><nobr><sm");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf("</small></nobr></font></b></td><td");
				movie = buffer.substring(pos1 + 14, pos2 - 4);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				more = false;
				// 2nd Movie
				pos1 = buffer.indexOf("<b><a href=\"movies");
				pos1 = buffer.indexOf(".htm", pos1);
				pos2 = buffer.indexOf("</a></b>");
				movie = buffer.substring(pos1 + 6, pos2);
				buffer = buffer.substring(pos2 + 246);
				movies.add(movie);
				// 2nd Movie first money figures
				pos1 = buffer.indexOf("valign=\"top\"><b");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf("<br><br><nobr><small><font color=\"#0000ff\">");
				movie = buffer.substring(pos1 + 4, pos2);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				// 2nd Movie 2nd money Figures
				pos1 = buffer.indexOf("color=\"#800080\"><nobr><sm");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf(
						"</small></nobr></font></b></td><td bgcolor=\"#f4f4ff\"align=\"center\" valign=\"top\"");
				movie = buffer.substring(pos1 + 14, pos2 - 5);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				// Get the 3rd movie
				pos1 = buffer.indexOf("<b><a href=\"movies");
				pos1 = buffer.indexOf(".htm", pos1);
				pos2 = buffer.indexOf("</a></b>");
				movie = buffer.substring(pos1 + 6, pos2);
				buffer = buffer.substring(pos2 + 245);
				movies.add(movie);
				// 3rd movie first money figures
				pos1 = buffer.indexOf("valign=\"top\"><b");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf("<br><br><nobr><small><font color=\"#0000ff\">");
				movie = buffer.substring(pos1 + 4, pos2);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				// 3rd movie 2nd money figures
				pos1 = buffer.indexOf("color=\"#800080\"><nobr><sm");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf(
						"</small></nobr></font></b></td><td bgcolor=\"#ffffff\"align=\"center\" valign=\"top\">");
				movie = buffer.substring(pos1 + 14, pos2 - 5);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				// Get 4th movie
				pos1 = buffer.indexOf("<b><a href=\"movies");
				pos1 = buffer.indexOf(".htm", pos1);
				pos2 = buffer.indexOf("</a></b>");
				movie = buffer.substring(pos1 + 6, pos2);
				buffer = buffer.substring(pos2 + 245);
				movies.add(movie);
				// Get 4th movies 1st money figures
				pos1 = buffer.indexOf("valign=\"top\"><b");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf("<br><br><nobr><small><font color=\"#0000ff\">");
				movie = buffer.substring(pos1 + 4, pos2);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				// Get 4th's 2nd figures
				pos1 = buffer.indexOf("color=\"#800080\"><nobr><sm");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf(
						"</small></nobr></font></b></td><td bgcolor=\"#f4f4ff\"align=\"center\" valign=\"top\"");
				movie = buffer.substring(pos1 + 14, pos2 - 5);
				buffer = buffer.substring(pos2 + 265);
				movies.add(movie);
				// Get 5th movie
				pos1 = buffer.indexOf("<b><a href=\"movies");
				pos1 = buffer.indexOf(".htm", pos1);
				pos2 = buffer.indexOf("</a></b>");
				movie = buffer.substring(pos1 + 6, pos2);
				buffer = buffer.substring(pos2 + 245);
				movies.add(movie);
				// Get 5th's 1st figures
				pos1 = buffer.indexOf("valign=\"top\"><b");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf("<br><br><nobr><small><font color=\"#0000ff\">");
				movie = buffer.substring(pos1 + 4, pos2);
				buffer = buffer.substring(pos2);
				movies.add(movie);
				// Get 5th's 2nd figures
				pos1 = buffer.indexOf("color=\"#800080\"><nobr><sm");
				pos1 = buffer.indexOf(">", pos1);
				pos2 = buffer.indexOf(
						"</small></nobr></font></b></td><td bgcolor=\"#ffffff\"align=\"center\" valign=\"top\"");
				movie = buffer.substring(pos1 + 14, pos2 - 5);
				buffer = buffer.substring(pos2);
				movies.add(movie);
			} else
				more = false;
		}
		//Could possibly loop through list to display for less redundancy! 
		System.out.println("        Box Office Top 5");
		System.out.println("    -----------------------------------");
		System.out.println("1.) " + movies.get(0) + "  (" + movies.get(1) + ") " + " (" + movies.get(2) + ")");
		System.out.println("2.) " + movies.get(3) + "    (" + movies.get(4) + ") " + " (" + movies.get(5) + ")");
		System.out.println("3.) " + movies.get(6) + "    (" + movies.get(7) + ") " + " (" + movies.get(8) + ")");
		System.out.println("4.) " + movies.get(9) + "    (" + movies.get(10) + ") " + " (" + movies.get(11) + ")");
		System.out.println("5.) " + movies.get(12) + "    (" + movies.get(13) + ") " + " (" + movies.get(14) + ")");
		// close the socket.
		sock.close();
	}
}
