package com.nyansa.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class DailySummary {
	// Time complexity is O(N*log(N)). N is 
	static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}

	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: DailySummary <data file path>");
			return;
		}
		String fileName = args[0];
		String patternSort = "yyyyMMdd";
		SimpleDateFormat dateSort = new SimpleDateFormat(patternSort);
		dateSort.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

		Map<String, Map<String, Long>> dateLinkMap = new TreeMap<String, Map<String, Long>>();
		Map<String, Long> linkVisitMap = null; // added for each new date

		DailySummary ds = new DailySummary();

		try {
			File file = new File(fileName);
			Scanner scanner = new Scanner(file);
			/*
			 * Let 
			 * N : Total number of visit
			 * L : Number of unique link
			 * K : number of days
			 * M : Average visit per link per day = N/(L*K)
			 * N_i : total number of visit of link i
			 * then
			 * N = M*L*K;
			 * M = N/(L*K) = Sum(N_i)/(L*K) = Sum(N_i/K)/L
			 * L': Average number of unique link per day = f*L : this is proportional to total number of link L
			 * f : is proportion factor varies by data set
			 * 
			 * 
			 * 
			 * Time complexity of first while loop is O(N) = O(M*L*K) ------------------------ (1)
			 * Time complexity of second for loop is O(K*L'*log(L')) = O(K*f*L*log(f*L)) ----- (2)
			 * 
			 * Since K*L is common to (1) and (2), overall time complexity depends on M and f*log(f*L)
			 * if M is bigger than f*log(f*L) then overall time complexity is O(N) = O(M*L*K)
			 * if M is smaller than f*log(f*L) then overall time complexity is O(K*f*L*log(f*L)) ~ O(K*L*log(L))
			 * 
			 * From description "cardinality of hit count values and the number of days are much smaller than the number of unique URLs",
			 * it is probable that M is smaller than f*log(f*L) then dominating time complexity is O(N)
			 */
			
			// Time complexity of first while loop is O(N) = O(M*L*K)
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] parsedLine = line.split("\\|");
				String timeStampS = parsedLine[0];
				long timeStamp = Long.parseLong(timeStampS);
				String link = parsedLine[1];

				Date time = new Date((long) timeStamp * 1000);
				String dateToSort = dateSort.format(time);

				if (dateLinkMap.containsKey(dateToSort)) {
					// date alreay in the dateLinkMap
					linkVisitMap = dateLinkMap.get(dateToSort); // get the linkVisit map for the given date
					if (linkVisitMap.containsKey(link)) {
						// link is already in the map, increment visit count
						linkVisitMap.put(link, linkVisitMap.get(link) + 1);
					} else {
						linkVisitMap.put(link, 1L);
					}
				} else {
					// first link visit entry for the date, add new linkVisitMap for the date
					linkVisitMap = new HashMap<String, Long>();
					linkVisitMap.put(link, 1L);
					dateLinkMap.put(dateToSort, linkVisitMap);
				}
			}
			scanner.close();

			// print for the given format
			// for loop is O(K). Collections.sort is O(L'*log(L'))
			// K is number of days. L' is average number of unique links for a day
			
			// Time complexity of second loop is  O(K*f*L*log(f*L)) ~ O(K*L*log(L)
			String displayDate = null;
			for (String date : dateLinkMap.keySet()) {
				displayDate = date.substring(4, 6) + "/" + date.substring(6, 8) + "/" + date.substring(0, 4);
				System.out.println(displayDate + " GMT");
				linkVisitMap = dateLinkMap.get(date);
				List<Entry<String, Long>> sortedEntries = entriesSortedByValues(linkVisitMap);
				for (int i=0; i<sortedEntries.size(); i++) {
					Entry<String, Long> entry = sortedEntries.get(i);
					System.out.println(entry.getKey() + " " + entry.getValue());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
