import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class Twitter {

	public static void main(String[] args) throws IOException, TwitterException {
		twitter4j.Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer("Kq0geX6BffjLugLvkCwcn8sQ2", "WZ9ZVeQJT95jzLb8kCgnMZSrq57DAEi0NWuwCAAJEL1wWWuDtH");

		try {
			// get request token.
			// this will throw IllegalStateException if access token is already
			// available
			RequestToken requestToken = twitter.getOAuthRequestToken();
			AccessToken accessToken = null;

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (null == accessToken) {
				System.out.println("Open the following URL and grant access to your account:");
				System.out.println(requestToken.getAuthorizationURL());
				System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
				Desktop.getDesktop().browse(new URI(requestToken.getAuthorizationURL()));
				String pin = br.readLine();
				try {
					if (pin.length() > 0) {
						accessToken = twitter.getOAuthAccessToken(requestToken, pin);
					} else {
						accessToken = twitter.getOAuthAccessToken(requestToken);
					}
				}

				catch (TwitterException te) {
					if (401 == te.getStatusCode()) {
						System.out.println("Unable to get the access token.");
					} else {
						te.printStackTrace();
					}
				}
			}
		} catch (IllegalStateException ie) {
			// access token is already available, or consumer key/secret is not
			// set.
			if (!twitter.getAuthorization().isEnabled()) {
				System.out.println("OAuth consumer key/secret is not set.");
				System.exit(-1);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		try {
			Query query = new Query("from:@VP");
			QueryResult result;
			query.setCount(10);
			result = twitter.search(query);
			List<Status> tweets = result.getTweets();
			System.out.println("");
			System.out.println("Tweets from our new Vice President: VP Mike Pence!");
			System.out.println("----------------To God Be The Glory---------------");

			for (Status tweet : tweets) {
				// format date
				Date datePlaceHolder = tweet.getCreatedAt();
				SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd yyyy, hh:mm:ss a");
				System.out.println(formatter.format(datePlaceHolder));
				// format text
				String firstPart = (String) tweet.getText().subSequence(0, tweet.getText().toString().length() / 2);
				System.out.println(firstPart);
				String secPart = (String) tweet.getText().subSequence((tweet.getText().toString().length() / 2),
						tweet.getText().toString().length() - 1);
				System.out.println(secPart);
				System.out.println();
			}

			System.exit(0);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to search tweets: " + te.getMessage());
			System.exit(-1);
		}
	}

}
