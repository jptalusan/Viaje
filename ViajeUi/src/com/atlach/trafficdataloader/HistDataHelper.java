package com.atlach.trafficdataloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.InflaterInputStream;

public class HistDataHelper {
	public static final int STATUS_OK = 0;
	public static final int STATUS_FAILED = -1;
	private static final String locStr[] = { "Balintawak",
		"Kaingin Road", "Munoz", "Bansalangin",
		"North Ave.", "Trinoma", "Quezon Ave.",
		"NIA Road", "Timog", "Kamuning",
		"New York - Nepa Q-Mart", "Monte De Piedad",
		"Aurora Blvd.", "Mc Arthur - Farmers",
		"P. Tuazon", "Main Ave.", "Santolan",
		"White Plains - Connecticut", "Ortigas Ave.",
		"SM Megamall", "Shaw Blvd.", "Reliance",
		"Pioneer - Boni", "Guadalupe", "Orense",
		"Kalayaan - Estrella", "Buendia", "Ayala Ave.",
		"Arnaiz - Pasay Road", "Magallanes", "Malibay",
		"Tramo", "Taft Ave.", "F.B. Harrison",
		"Roxas Boulevard", "Macapagal Ave.",
		"Mall of Asia", "Batasan",
		"St. Peter's Church", "Ever Gotesco",
		"Diliman Preparatory School",
		"Zuzuarregi",
		"General Malvar Hospital",
		"Tandang Sora Eastside",
		"Tandang Sora Westside", "Central Ave",
		"Magsaysay Ave", "University Ave",
		"Philcoa", "Elliptical Road",
		"Agham Road", "Bantayog Road",
		"Edsa", "SGT. Esguera",
		"Scout Albano", "Scout Borromeo",
		"Scout Santiago", "Timog",
		"Scout Reyes", "Scout Magbanua",
		"Roces Avenue", "Roosevelt Avenue",
		"Dr. Garcia Sr.", "Scout Chuatoco",
		"G. Araneta Ave.", "Sto. Domingo",
		"Biak na Bato", "Banawe",
		"Cordillera", "D. Tuazon",
		"Speaker Perez", "Apo Avenue",
		"Kanlaon", "Mayon",
		"Welcome Rotunda", "Bluementritt",
		"A. Maceda", "Antipolo", "Vicente Cruz",
		"Gov. Forbes - Lacson", "P.Noval", "Lerma",
		"Tandang Sora", "Capitol Hills",
		"University of the Philippines", "C.P. Garcia",
		"Miriam College", "Ateneo De Manila University",
		"Xavierville", "Aurora Boulevard", "P. Tuazon",
		"Bonny Serrano", "Libis Flyover", "Eastwood",
		"Green Meadows", "Ortigas Ave.", "J. Vargas",
		"Lanuza", "Bagong Ilog", "Kalayaan",
		"Market! Market!", "Santolan", "Madison",
		"Roosevelt", "Club Filipino", "Wilson",
		"Connecticut", "La Salle Greenhills",
		"POEA", "EDSA Shrine", "San Miguel Ave",
		"Meralco Ave", "Medical City",
		"Lanuza Ave", "Greenmeadows Ave",
		"C5 Flyover", "SM City Marikina",
		"LRT-2 Station", "Dona Juana",
		"Amang Rodriguez",
		"F. Mariano Ave",
		"Robinson's Metro East",
		"San Benildo School", "Anda Circle",
		"Finance Road", "U.N. Avenue",
		"Pedro Gil", "Rajah Sulayman",
		"Quirino", "Pablo Ocampo",
		"Buendia", "Edsa Extension",
		"Baclaran", "Airport Road",
		"Coastal Road", "Magallanes", "Nichols",
		"C5 On-ramp", "Merville Exit", "Bicutan Exit",
		"Sucat Exit", "Alabang Exit" };
	public static final String dayOfWeekStr[] = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
	public final static String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	

	/** Public Methods **/
	
	public static String decodeLineDataString(String str) {
		return normalizeLineDataString(decompressString(str));
	}
	
	public static String getLocationName(int index) {
		if ((index < 0) || (index >= locStr.length)) {
			return "UNKNOWN";
		}
		return locStr[index];
	}
	
	public String getMostLikelyTagMatch(int dayOfWeek, String weatherCond, List<String> tagList) {
		String tagStr = "";
		String matchTagStr = "";
		
		if ((dayOfWeek < 0) || (dayOfWeek > 6)) {
			System.out.println("[getMostLikelyTagMatch] Error: Invalid day of week value!");
			return "";
		}
		
		if ((dayOfWeek == 0) || (dayOfWeek == 6)) {
			matchTagStr += ("Weekend|" + dayOfWeekStr[dayOfWeek]);
		} else {
			matchTagStr += ("Weekday|" + dayOfWeekStr[dayOfWeek]);
		}
		
		if ((weatherCond != null) && !(weatherCond.equals(""))) {
			matchTagStr += ", ";
			if (weatherCond.contains("rain")) {
				matchTagStr += "Weather|Rain";
				if (weatherCond.contains("heavy")) {
					matchTagStr += "|Heavy";
				} else if (weatherCond.contains("moderate")) {
					matchTagStr += "|Moderate";
				} else if (weatherCond.contains("light")) {
					matchTagStr += "|Light";
				} else if (weatherCond.contains("storm")) {
					matchTagStr += "|Storm";
				}
			} else if (weatherCond.contains("cloudy")) {
				if (weatherCond.contains("partly")) {
					matchTagStr += "Weather|Overcast|Cool";
				} else {
					matchTagStr += "Weather|Overcast|Cold";
				}
			} else {
				matchTagStr += "Weather|Clear";
			}
		}
		
		System.out.println("[getMostLikelyTagMatch] Generated Match Tag String: " + matchTagStr);
		
		/* Match the created string against the elements in the tag list */
		int matchIndexArr[] = new int[tagList.size()];
		
		/* Calculate the match index value between the created match string
		 * and each element in the tagList */
		for (int i = 0; i < tagList.size(); i++) {
			matchIndexArr[i] = calculateMatchIndex(matchTagStr, tagList.get(i));
		}
		
		int highestMatchIdx = -1;
		int highestMatch = 0;
		for (int i = 0; i < tagList.size(); i++) {
			if (highestMatch < matchIndexArr[i]) {
				highestMatchIdx = i;
				highestMatch = matchIndexArr[i];
			}
		}
		
		if (highestMatchIdx < 0) {
			System.out.println("[getMostLikelyTagMatch] Error: No closest match found!");
			return "";
		}
		
		return tagList.get(highestMatchIdx);
	}
	
	public HistData loadHistDataTagFromFile(File loadFile, String targetTagStr, boolean isCompressed) throws IOException {
		HistData histData = null;
		String line = "";
		
		if (!loadFile.exists()) {
			System.out.println("[loadHistDataTagFromFile] ERROR: Hist Data File Not Found!");
			return null;
		}

		BufferedReader rd = null;
		FileInputStream fInp = null;
		InflaterInputStream iInp = null;

		try {
			fInp = new FileInputStream(loadFile);
			
			if (isCompressed) {
				iInp = new InflaterInputStream(fInp);
				rd = new BufferedReader(new InputStreamReader(iInp));
			} else {
				rd = new BufferedReader(new InputStreamReader(fInp));
			}
			
			boolean shouldExtractData = false;
			
			while((line = rd.readLine()) != null) {
//				System.out.println("Decompressed Out: " + line);
				/* Check if we are dealing with the tag indicator */
				if ((line.length() > 0) && (line.charAt(0) == '>')) {
					/* Extract the tagset value */
					String tagStr = line.substring(3,line.length()-1);
					
					/* If the correct tag has been found, flag data
					 * extraction to begin */
					if (tagStr.equals(targetTagStr)) {
						System.out.println("Found matching tagset: [" + tagStr + "]");
						shouldExtractData = true;
						histData = new HistData("UNKNOWN", tagStr);
						continue;
					} else {
						shouldExtractData = false;
					}
				}

				/* Skip over things until we reach the correct tag */
				if (!shouldExtractData) {
					continue;
				}
				
				String lineStr[] = line.split(":");
				if (lineStr.length != 2) {
					continue;
				}
				
				/* Create a temporary LineInfo object */
				LineInfo lineInfo = new LineInfo(lineStr[0], lineStr[1]);

				/* Some kind of mechanism to insert this properly to the data list */
				insertToDataList(lineInfo, histData.dataList);
			}
			
			/* If histData is still null, then the tag has not been found */
			if (histData == null) {
				System.out.println("[loadHistDataTagFromFile] Warning: Tag not found! (" + targetTagStr + ")");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			/* Close the input streams we used */
			if (iInp != null) {
				iInp.close();
			}
			if (fInp != null) {
				fInp.close();
			}
		}
		
		return histData;
	}
	
	public HistDataFileInfo getHistDataFileInfo(File histDataFile, boolean isCompressed) {
		if (histDataFile == null) {
			return null;
		}
		
		/* Safety just in case this file does not exist */
		if (!histDataFile.exists()) {
			return null;
		}
		
		FileInputStream fInp = null;
		InflaterInputStream iInp = null;
		HistDataFileInfo hdFileInfo = null;
		
		try {
			BufferedReader rd = null;
			fInp = new FileInputStream(histDataFile);
			
			// Wrap a BufferedReader around the InputStream
			if (isCompressed) {
				iInp = new InflaterInputStream(fInp);
				rd = new BufferedReader(new InputStreamReader(iInp));
			} else {
				rd = new BufferedReader(new InputStreamReader(fInp));
			}
			
			String line = "";
			String datesCovered = "";
			List<String> tagList = new ArrayList<String>();
			boolean isLiftingTags = false;
			boolean hasFoundContentTag = false;
	
			// Read response until the end
			while ((line = rd.readLine()) != null) {
				/* Check for the DatesCovered tag */
				if (line.contains("[DatesCovered]")) {
					String lineStr[] = line.split("]");
					
					datesCovered = lineStr[1].trim();
					continue;
				}
				
				/* Check for the TagIndexStart tag */
				if (line.contains("[TagIndexStart]")) {
					isLiftingTags = true;
					continue;
				}

				/* Check for the TagIndexEnd tag */
				if (line.contains("[TagIndexEnd]")) {
					isLiftingTags = false;
					continue;
				}
				
				/* If we are currently in the tag index, assume that
				 * we can copy each line as a tag to the taglist */
				if (isLiftingTags) {
					tagList.add(line.trim());
				}


				/* Check for the Content tag */
				if (line.contains("[Content]")) {
					hasFoundContentTag = true;
					continue;
				}
				
				/* Quit reading once we hit the body of the file */
				if ((line.length() > 0) && (line.charAt(0) == '>')) {
					break;
				}
			}
			
			if (!hasFoundContentTag) {
				System.out.println("WARNING: [Content] tag not found for this HistData file!");
			} else {
				System.out.println("INFO: [Content] tag found for this HistData file.");
			}
			
			/* Close the input streams */
			if (fInp != null) {
				fInp.close();
			}
			if (iInp != null) {
				iInp.close();
			}
			
			/* Create the HistDataFileInfo object */
			hdFileInfo = new HistDataFileInfo(datesCovered, tagList);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return hdFileInfo;
	}
	
	/** Private Methods **/
	private int calculateMatchIndex(String matchStr, String baseStr) {
		String matchStrSplit[] = matchStr.split(",");
		String baseStrSplit[] = baseStr.split(",");
		
		int matchIndex = 0;
		
		for (int i = 0; i < matchStrSplit.length; i++) {
			String matchSubSplit[] = matchStrSplit[i].trim().split("\\|");
			
			for (int j = 0; j < baseStrSplit.length; j++) {
				String baseSubSplit[] = baseStrSplit[j].trim().split("\\|");
				
				int len = ((matchSubSplit.length < baseSubSplit.length) ? matchSubSplit.length : baseSubSplit.length);
				for (int k = 0; k < len; k++) {
					if (matchSubSplit[k].equals(baseSubSplit[k])) {
						matchIndex += (k + 1);
					} else {
						break;
					}
				}
			}
		}
		return matchIndex;
	}
	
	private int insertToDataList(LineInfo lineInfo, List<LineInfo> dataList) {
		int insertId = getDataListInsertionIndex(lineInfo.timestamp, dataList);
		if (insertId != -1) {
			dataList.add(insertId, lineInfo);
		} else {
			dataList.add(lineInfo);
		}
		return STATUS_OK;
	}
	
	private int getDataListInsertionIndex(String timeStr, List<LineInfo> dataList) {
		int timeVal = Integer.parseInt(timeStr);
		int guessVal = 0;
		
		int floorId = 0;
		int ceilId = dataList.size()-1;
		int guessId = ceilId;
		int insId = 0;
		
		if (dataList.size() > 0) {
			guessVal = Integer.parseInt(dataList.get(guessId).timestamp);
		} else {
			return -1;
		}
		
		if (timeVal > guessVal) {
			return -1;
		}
		
		while (dataList.size() > 0) {
			if (timeVal < guessVal) {
				if ((guessId-floorId) == 1) {
					if (Integer.parseInt(dataList.get(floorId).timestamp) > timeVal) {
						insId = floorId;
					} else {
						insId = guessId;
					}
					break; 
				}
				
				if (ceilId == floorId) {
					break;
				}
				
				ceilId = guessId;
				guessId = ((ceilId - floorId) / 2) + floorId;
				guessVal = Integer.parseInt(dataList.get(guessId).timestamp);
				
			} else { /* timeVal > guessVal */
				if ((ceilId-guessId) == 1) {
					if (Integer.parseInt(dataList.get(ceilId).timestamp) < timeVal) {
						insId = guessId;
					} else {
						insId = ceilId;
					}
					insId = ceilId;
					break; 
				}
				
				if (ceilId == floorId) {
					break;
				}
				
				floorId = guessId;
				guessId = (((ceilId - floorId) / 2) + floorId);
				guessVal = Integer.parseInt(dataList.get(guessId).timestamp);
				
			}
		}
		return insId;
	}
	

	/**
	 * Normalizes the traffic condition values inside a Line Data string.
	 * This method basically gets the average of all traffic condition
	 * values previously 'merged' into the line data string. This is
	 * usually done after parsing in multiple traffic data files and we
	 * want to obtain the average traffic condition to be used as reference
	 * for future merge-ins.
	 * @param lineDataStr - the line data string to be normalized
	 * @return the normalized line data string
	 */
	private static String normalizeLineDataString(String lineDataStr) {
		String normalizedStr = "";
		int i = 0;
		
		/* Check if the length of the new line data string is sane.
		 * Since there are usually 142 distinct lines, the length
		 * of this string should approximately be 284 characters since
		 * we are counting differently for both northbound and
		 * southbound traffic */
		if (lineDataStr.length() != 284) {
			System.out.println("[normalizeLineDataString] Warning: Weird line data string length: " + lineDataStr.length());
			return normalizedStr;
		}
		
		for (i = 0; i < lineDataStr.length(); i++) {
			byte b[] = new byte[4];
			
			int charIdx = base64chars.indexOf(lineDataStr.charAt(i));
			b[0] = (byte) ((charIdx >> 6) & 3);
			b[1] = (byte) ((charIdx >> 4) & 3);
			b[2] = (byte) ((charIdx >> 2) & 3);
			b[3] = (byte) (charIdx & 3);
			
			int weighedSum = 0;
			float finDiv = 0.0f;
			for (int j = 0; j < b.length; j++) {
				weighedSum += b[j] * (j+1);
				if (b[j] != 0) {
					finDiv += (float) (j+1);
				}
			}
			
			int finInt = Math.round(weighedSum / finDiv);
			
			normalizedStr += base64chars.charAt( finInt == 0 ? 1 : finInt );
		}
		return normalizedStr;
	}

	/**
	 * Compresses a given string by removing adjacent repeating characters. 
	 * The resulting string may later be fed into the decompressString()
	 * utility to restore the original string.
	 * @param str - the original, uncompressed String
	 * @return the compressed String
	 */
	private static String compressString(String str) {
		String newStr = "";
		char next = 0;
		boolean isCompressing = false;
		boolean shouldSupressCompression = false;
		byte sameCounter = 1;
		
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			if (i+1 < str.length()) {
				next = str.charAt(i+1);
				
				/* Determine if compressing this is worth it */
				if ((i+2 < str.length()) && (!isCompressing)) {
					/* If this repeating character is only two
					 * characters long, don't bother trying to
					 * compress it since we're effectively
					 * adding 50% more to its uncompressed
					 * size instead. */
					if (c != str.charAt(i+2)) {
						shouldSupressCompression = true;
					} else {
						shouldSupressCompression = false;
					}
				}
			} else {
				next = 0;
			}
			
			if ((c == next) && (!shouldSupressCompression)) {
				if (!isCompressing) {
					sameCounter++;
					isCompressing = true;
					newStr += ".";
				} else {
					if (sameCounter < base64chars.length() - 1) {
						sameCounter++;
					} else {
						/* Have to prematurely break the compression */
						isCompressing = false;
						newStr += base64chars.charAt(sameCounter);
						sameCounter = 1;
						newStr += c;
					}
				}
			} else {
				if (isCompressing) {
					isCompressing = false;
					newStr += base64chars.charAt(sameCounter);
					sameCounter = 1;
				}
				newStr += c;
			}
			
			/* Reset suppress compression value if it has been set */
			if (shouldSupressCompression) {
				shouldSupressCompression = false;
			}
		}
		return newStr;
	}

	/**
	 * Decompresses a string which has previously been compressed using the 
	 * compressString() utility. All omitted repeating characters are restored.
	 * @param str - the compressed String
	 * @return the decompressed String
	 */
	private static String decompressString(String str) {
		String newStr = "";

		boolean shouldDecompress = false;
		byte decompVal = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '.') {
				shouldDecompress = true;
				continue;
			}
			if (shouldDecompress) {
				if (decompVal == 0) {
					decompVal = (byte)(base64chars.indexOf(c));
				} else {
					for (int j = 0; j < decompVal; j++) {
						newStr += c;
					}
					shouldDecompress = false;
					decompVal = 0;
				}
			} else {
				newStr += c;
			}
		}
		
		if (newStr.length() != 284) {
			System.out.println("[decompressString] Warning: Invalid decompressed string length: " + newStr.length());
		}
		return newStr;
	}
	
	
	/** Inner Classes **/
	public static class LineInfo {
		public String timestamp;
		public String lineDataStr;

		/* Constructor */
		public LineInfo(String dateTime, String lineData) {
			timestamp = dateTime;
			lineDataStr = lineData;
			
			if (lineDataStr.equals(""))
				System.out.println("Warning: Blank line data string! (" + this.timestamp + ")");
		}
	}
	
	public static class HistData {
		/* Fields */
		public List<LineInfo> dataList;
		public String date;
		public String tagset;
		public boolean hasChanged = false;

		/* Constructor */
		public HistData(String d) {
			dataList = new ArrayList<LineInfo>();
			date = d;
		}
		public HistData(String d, String t) {
			dataList = new ArrayList<LineInfo>();
			date = d;
			tagset = t;
		}
	}
	
	public static class HistDataFileInfo {
		public String datesCovered = "";
		public List<String> tagList = null;
		
		public HistDataFileInfo (String datesCovered, List<String> tagList) {
			this.datesCovered = datesCovered;
			this.tagList = tagList;
		}
		
		public void printInfo() {
			System.out.println("Dates Covered: " + this.datesCovered);
			System.out.println("Tags:");
			for (int i = 0; i < tagList.size(); i++) {
				System.out.println("-" + tagList.get(i));
			}
		}
	}
}
