package com.monead.games.android.sequence.reporting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameStatistics implements Serializable {
	private static final long serialVersionUID = 3931550416819079788L;

	/**
	 * The count of randomly selected colors so far chosen by this model. Allows
	 * for manual checking of randomness.
	 */
	private Map<Integer, Integer> colorCounts;
	
	private long numWins;
	private long numLosses;
	private long numQuits;
	private long numEasy;
	private long numHard;
	private long totalTries;
	private long totalWinTimeMs;
	private long totalLostTimeMs;

	private String statsError;

	public GameStatistics() {
		colorCounts = new HashMap<Integer, Integer>();
	}
	
	public long getNumGamesStored() {
		return numWins + numLosses + numQuits;
	}
	
	public void addColor(int color) {
		Integer current;
		
		current = colorCounts.get(color);
		if (current == null) {
			current = 0;
		}
		
		current++;
		
		colorCounts.put(color, current);
	}
	
	public int getColorCount(int colorIndex) {
		Integer count;
		
		count = colorCounts.get(colorIndex);
		
		if (count == null) {
			count = 0;
		}
		
		return count;
	}

	public long getNumWins() {
		return numWins;
	}

	public void addWin() {
		++numWins;
	}

	public long getNumLosses() {
		return numLosses;
	}

	public void addLoss() {
		++numLosses;
	}

	public long getNumQuits() {
		return numQuits;
	}

	public void addQuit() {
		++numQuits;
	}

	public long getNumEasy() {
		return numEasy;
	}

	public void addEasy() {
		++numEasy;
	}

	public long getNumHard() {
		return numHard;
	}

	public void addHard() {
		++numHard;
	}

	public long getTotalTries() {
		return totalTries;
	}

	public void addTries(long numTries) {
		totalTries += numTries;
	}

	public long getTotalWinTimeMs() {
		return totalWinTimeMs;
	}

	public void addWinTimeMS(long winTimeMs) {
		totalWinTimeMs += winTimeMs;
	}

	public long getTotalLostTimeMs() {
		return totalLostTimeMs;
	}

	public void addLostTimeMS(long lostTimeMs) {
		totalLostTimeMs += lostTimeMs;
	}

	public int getAverageTries() {
		if (getNumGamesStored() > 0) {
			return (int)((double) getTotalTries() / getNumGamesStored());
		} else {
			return 0;
		}
	}

	public long getAverageWinTimeMs() {
		if (getNumWins() > 0) {
			return (long)((double) getTotalWinTimeMs() / getNumWins());
		} else {
			return 0l;
		}
	}

	public long getAverageLoseTimeMs() {
		if (getNumLosses() > 0) {
			return (long)((double) getTotalLostTimeMs() / getNumLosses());
		} else {
			return 0l;
		}
	}

	public String getStatsError() {
		return statsError;
	}

	public void setStatsError(String statsError) {
		this.statsError = statsError;
	}

}
