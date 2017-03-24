package nz.ac.vuw.ecs.kcassell.similarity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This implements the Normalized Google Distance (NGD) as described in
 * R.L. Cilibrasi and P.M.B. Vitanyi, "The Google Similarity Distance",
 * IEEE Trans. Knowledge and Data Engineering, 19:3(2007), 370 - 383
 */

public class GoogleDistanceCalculator
implements DistanceCalculatorIfc<String> {
	
	/** A Google URL that will return the number of matches, among other things. */
	private static final String GOOGLE_SEARCH_SITE_PREFIX =
		"http://ajax.googleapis.com/ajax/services/search/web?v=1.0&";

	/** A Yahoo URL that will return the number of matches, among other things. */
	private static final String YAHOO_SEARCH_SITE_PREFIX =
		"http://boss.yahooapis.com/ysearch/web/v1/";
	// + theQueryTerm?appid=YOUR_API_KEY&format=json"
	// see http://developer.yahoo.com/search/boss/
	
	/** The file in the eclipse install directory containing a textual rep.
	 * of the cache.	 */
	protected static final String CACHE_FILE_NAME = "google.cache";

	static int counter = 0;

	/** The logarithm of a number that is (hopefully) greater than or equal
	 *  to the (unpublished) indexed number of Google documents.
	 *  http://googleblog.blogspot.com/2008/07/we-knew-web-was-big.html
	 *  puts this at a trillion or more.  */
	protected final static double logN = Math.log(1.0e12);

    protected static final UtilLogger logger =
    	new UtilLogger("GoogleDistanceCalculator");

	Map<String, Double> cache = new HashMap<String, Double>();
	
	/** Holds the new terms we entered (these are also in the cache) */
	Map<String, Double> newCache = new HashMap<String, Double>();

	/** The key to use for querying Yahoo.   This is read in via the system
	 * property "yahooApiKey".  */
	private static String yahooApiKey = System.getProperty("yahooApiKey");

	public GoogleDistanceCalculator() throws NumberFormatException, IOException {
		cache = setupCache(CACHE_FILE_NAME);
	}

	public void clearCache() {
		cache = new HashMap<String, Double>();
		newCache = new HashMap<String, Double>();
		File cacheFile = new File(CACHE_FILE_NAME);
		cacheFile.delete();
	}
	
	protected Map<String, Double> setupCache(String filename)
			throws NumberFormatException, IOException {

		File cacheFile = new File(filename);

		if (cacheFile.canRead()) {
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			Map<String, Double> cache = new HashMap<String, Double>();
			String line;

			while ((line = reader.readLine()) != null) {
				int lastSpaceIndex = line.lastIndexOf(' ');
				String token = line.substring(0, lastSpaceIndex);
				double count = Double
						.parseDouble(line.substring(lastSpaceIndex + 1));
				cache.put(token, count);
			}

			reader.close();
		}
		return cache;
	}

	/**
	 * Adds the contents of newCache to the specified file
	 * @param filename
	 */
	protected void updateCache(String filename) {

		if (counter++ >= 20) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(filename, true));

				for (Map.Entry<String, Double> entry : newCache.entrySet()) {
					writer.append(entry.getKey() + " " + entry.getValue() + "\n");
				}
				newCache = new HashMap<String, Double>();
				counter = 0;
			} catch (IOException e) {
				// Things will just take longer
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected double numResultsFromWeb(String term)
	throws JSONException, IOException {
		double result = 0;
		
		if (cache.containsKey(term)) {
			result = cache.get(term);
		} else {
			URL url = null;
			InputStream stream = null;
			try {
				url = makeQueryURL(term);
				URLConnection connection = url.openConnection();
//				connection.setConnectTimeout(2000);
				stream = connection.getInputStream();
				InputStreamReader inputReader = new InputStreamReader(stream);
				BufferedReader bufferedReader = new BufferedReader(inputReader);
				double count = getCountFromQuery(bufferedReader);
//				System.out.println(term + ":\t" + count + " hits");
				cache.put(term, count);
				newCache.put(term, count);
				updateCache(CACHE_FILE_NAME);
				result = count;
			}
			finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						logger.warning(e.toString());
					}
				}
			}
		}
		return result;
	}

	private double getCountFromQuery(BufferedReader reader)
			throws JSONException, IOException {
		double count = getCountFromYahooQuery(reader);
//		double count = getCountFromGoogleQuery(bufferedReader);
		return count;
	}

	private double getCountFromYahooQuery(BufferedReader reader)
			throws IOException, JSONException {
//		String line;
//		StringBuilder builder = new StringBuilder();
//
//		while ((line = reader.readLine()) != null) {
//			builder.append(line);
//		}
//		String response = builder.toString();
//		JSONObject json = new JSONObject(response);
		JSONObject json = new JSONObject(new JSONTokener(reader));
		JSONObject searchResponse = json.getJSONObject("ysearchresponse");
		double count = searchResponse.getDouble("totalhits");
		return count;
	}

	@SuppressWarnings("unused")
	private double getCountFromGoogleQuery(BufferedReader bufferedReader) throws JSONException {
		JSONObject json = new JSONObject(new JSONTokener(bufferedReader));
		JSONObject responseData = json.getJSONObject("responseData");
		JSONObject cursor = responseData.getJSONObject("cursor");
		double count = 0;
		
		try {
			count = cursor.getDouble("estimatedResultCount");
		} catch (JSONException e) {
			// exception will be thrown when no matches are found
			count = 0;
		}
		return count;
	}

	protected URL makeQueryURL(String term) throws MalformedURLException, IOException {
		//String searchTerm = term.replaceAll(" ", "+");
		String searchTerm = URLEncoder.encode(term, "UTF-8");
		URL url;
		String urlString = makeYahooQueryString(searchTerm);
//		String urlString = makeGoogleQueryString(searchTerm);
		url = new URL(urlString);
		return url;
	}

	/**
	 * Builds a query string suitable for Google
	 * @param searchTerm
	 * @return
	 */
	@SuppressWarnings("unused")
	private String makeGoogleQueryString(String searchTerm) {
		String urlString = GOOGLE_SEARCH_SITE_PREFIX + "q=" + searchTerm + " ";
		/*
		 * Example queries:
			cassell: q=cassell
			keith cassell: q=keith+cassell
			"keith cassell": q=%22keith+cassell%22
			"keith cassell" betweenness: q=%22keith+cassell%22+betweenness
		 */
		return urlString;
	}

	/**
	 * Builds a query string suitable for Yahoo
	 * @param searchTerm
	 * @return
	 */
	private String makeYahooQueryString(String searchTerm) {
		String urlString = YAHOO_SEARCH_SITE_PREFIX + searchTerm +
			     "?appid=" + yahooApiKey + "&count=0&format=json";
//		System.out.println(urlString);
		return urlString;
	}

	/**
	 * Calculates the normalized Google Distance (NGD) between the two terms
	 * specified.  NOTE: this number can change between runs, because it is
	 * based on the number of web pages found by Google, which changes.
	 * @return a number from 0 (minimally distant) to 1 (maximally distant),
	 *   unless an exception occurs in which case, it is negative
	 *   (RefactoringConstants.UNKNOWN_DISTANCE)
	 */
	public Double calculateDistance(String term1, String term2) {
		double distance = RefactoringConstants.UNKNOWN_DISTANCE.doubleValue();

		try {
			double min = numResultsFromWeb(term1);
			double max = numResultsFromWeb(term2);
			double both = numResultsFromWeb(term1 + " " + term2);

			// if necessary, swap the min and max
			if (max < min) {
				double temp = max;
				max = min;
				min = temp;
			}

			if (min > 0.0 && both > 0.0) {
				distance =
					(Math.log(max) - Math.log(both)) / (logN - Math.log(min));
			} else {
				distance = 1.0;
			}
			
			// Counts change and are estimated, so there would be a possibility
			// of a slightly negative distance.
			if (distance < 0.0) {
				distance = 0.0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return distance;
	}

	public DistanceCalculatorEnum getType() {
		return DistanceCalculatorEnum.GoogleDistance;
	}

}
