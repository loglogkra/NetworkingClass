import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Calendar;

public class BoxOfficeTop5 {

	static final int DEFAULT_HTTP_PORT = 80;
	protected int port;
	protected Socket socket = null;
	protected DataInputStream receive = null;
	protected PrintStream send = null;

	//*** Constructors just set the port number.
	
	public BoxOfficeTop5() {
		this(DEFAULT_HTTP_PORT);
	}

	public BoxOfficeTop5(int port) {
		this.port = port;
	}
	
	//*** Get the box office rankings.  We do this by accessing one web page 
	//*** from boxofficemojo.com.

	public String getRankings(Calendar referenceDay) throws Exception {
		String HTMLline, page, topmovie;
		
		// Open the socket.  Set the timeout.  Derive the I/O objects.
        socket = new Socket("www.boxofficemojo.com", port);
        socket.setSoTimeout(10*1000);
        receive = new DataInputStream(socket.getInputStream());
        send = new PrintStream(socket.getOutputStream());
        
        // Set up the HTTP request.  This includes the date sent as a parameter  
        // and a bunch of headers retrieved from looking at this in Wireshark.
		String requestline = "GET /daily/chart/?sortdate=";
		requestline += referenceDay.get(Calendar.YEAR) + "-" + (referenceDay.get(Calendar.MONTH)+1) + "-" + referenceDay.get(Calendar.DAY_OF_MONTH) + " HTTP/1.1\r\n";
		requestline += "Host: www.boxofficemojo.com\r\n"+
			    "Connection: keep-alive\r\n" +
			    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36\r\n" +
			    "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n" +
			    "DNT: 1\r\n" +
			    //"Accept-Encoding: gzip, deflate, sdch\r\n" +   NOT THIS ONE!  
			    "Accept-Language: en-US,en;q=0.8";
		
		// Make the HTTP request.
       	send.println(requestline);
        send.println();
        
        // Retrieve the server response.  Here we just grab the Web page data  
        boolean eof = false;
        page = "";
        while (! eof) {
        	try {
	        	HTMLline = receive.readLine();
	        	if (HTMLline == null) 
	        		eof = true;
	        	else 
	        		page += HTMLline;
        	} catch (SocketTimeoutException ste) {
        		eof = true;
        	}
        }

        // Do some string processing to extract the box office ranking strings
        // from the Web page.
        int pos, pos2;
        String rankings = "";
        
        pos = page.indexOf("\"submit\" value=\"Go\"");
        pos = page.indexOf("<tr><td", pos);
        for (int i=0; i<5; i++) {
        	pos = page.indexOf("<tr><td", pos+1);
        	pos = page.indexOf("<a href", pos);
        	pos = page.indexOf("\">", pos);
        	pos2 = page.indexOf("</a>", pos);
        	rankings += page.substring(pos+2, pos2);
        	
        	pos = page.indexOf("\"top\"", pos);
        	pos = page.indexOf("$", pos);
        	pos2 = page.indexOf("<br>", pos);
        	rankings += " (" + page.substring(pos,pos2);
        	
        	pos = page.indexOf("<small>$", pos);
        	pos2 = page.indexOf(" ", pos);
        	rankings += " / " + page.substring(pos+7, pos2) + ")\n";
        }
      
        // We are done.  Close socket. 
        socket.close();
        
        // Return the retrieved data.
        return rankings;
	}

	//*** Main method.  As for and display the movie rankings.
	
	public static void main(String[] args) throws IOException {
		BoxOfficeTop5 bom = null;
		String rankings = "";
		
		// Figure out the date
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.HOUR, -24);

		// Now get the rankings for yesterday
		try {
			bom = new BoxOfficeTop5();
			rankings = bom.getRankings(yesterday);
			System.out.println(rankings);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("There is an error in access boxofficemojo.com.");
		}

	}

}
