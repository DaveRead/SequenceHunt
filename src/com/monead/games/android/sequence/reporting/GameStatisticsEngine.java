package com.monead.games.android.sequence.reporting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.monead.games.android.sequence.model.SequenceHuntGameModel;

/**
 * Copyright 2011, David S. Read
 * 
 * This file is part of Sequence Hunt.
 *
 * Sequence Hunt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sequence Hunt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Sequence Hunt.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/**
 * Collect and house game statistics
 */
public class GameStatisticsEngine implements Serializable {
	private static final long serialVersionUID = 2350057697130863738L;

	/**
	 * Maximum number of historical games for which to hold statistics
	 */
	private final static int MAX_GAMES_TO_STORE = 300;

	/**
	 * Class name used for logging
	 */
	private String className = this.getClass().getName();
	
	private final static int INDEX_GAME_COUNT = 0;
	private final static int INDEX_MODE_HARD = 1;
	private final static int INDEX_NUM_TRIES = 2;
	private final static int INDEX_GAME_TIME_MS = 3;
	private final static int INDEX_COLORS_START = 4;

	/**
	 * The total number of games played
	 */
	private long gameCount;

	/**
	 * The historical information for each game
	 */
	private List<String> gameHistory;

	private boolean statsAccurate;

	private GameStatistics statistics;

	/**
	 * Setup the game history array
	 */
	public GameStatisticsEngine() {
		gameHistory = new ArrayList<String>();
	}

	/**
	 * Add a game to history
	 * 
	 * @param model
	 *            The model for the game being added
	 */
	public void addGame(boolean modeHard, SequenceHuntGameModel model) {
		addGame(model, modeHard, model.isWinner() ? "Win" : model.isLoser() ? "Lose"
				: "Unknown");
	}

	/**
	 * Add a game to history, adding a message
	 * 
	 * This method will assure that the MAX_GAMES_TO_STORE limit is enforced.
	 * 
	 * @param model
	 *            The model for the game being added
	 */
	public void addGame(SequenceHuntGameModel model, boolean modeHard, String message) {
		while (gameHistory.size() >= MAX_GAMES_TO_STORE) {
			gameHistory.remove(0);
		}
		gameCount++;
		gameHistory.add(gameCount 
				+ ",'" + modeHard + "'"
				+ "," + model.getCurrentTry() 
				+ "," + model.getElapsedTime() 
				+ "," + model.getAnswerValue() 
				+ ",'" + message + "'");
		setStatsAccurate(false);
		Log.d(className, "Added game: " + gameCount + " gameHistory count=" + gameHistory.size());
	}
	
	private void setStatsAccurate(boolean accurate) {
		statsAccurate = accurate;
	}
	
	private boolean isStatsAccurate() {
		return statsAccurate;
	}

	public GameStatistics getGameStatistics() {
		if (!isStatsAccurate()) {
			calcStats();
		}

		return statistics;
	}

	private void calcStats() {
		String[] parsed;
		int numFields;
		String outcome;
		String modeHard;

		Log.d(className, "Calculate statistics with gameHistory length=" + gameHistory.size());
		
		statistics = new GameStatistics();

		try {
			for (String record : gameHistory) {
				Log.d(className, "gameHistory record [" + record + "]");
				
				// New format ends with a string indicating game result
				if (record.endsWith("'")) {
					parsed = record.split(",");
					numFields = parsed.length;
					Log.d(className, "Record was in new format field count=" + numFields);
					outcome = parsed[numFields - 1].replaceAll("'", "");
					modeHard = parsed[INDEX_MODE_HARD].replaceAll("'", "");
					
					if (outcome.equalsIgnoreCase("win")) {
						statistics.addWin();
						statistics.addWinTimeMS(Long.parseLong(parsed[INDEX_GAME_TIME_MS]));
					} else if (outcome.equalsIgnoreCase("lose")) {
						statistics.addLoss();
						statistics.addLostTimeMS(Long.parseLong(parsed[INDEX_GAME_TIME_MS]));
					} else {
						statistics.addQuit();
					}

					if (modeHard.equalsIgnoreCase("false")) {
						statistics.addEasy();
					} else {
						statistics.addHard();
					}
					
					statistics.addTries(Long.parseLong(parsed[INDEX_NUM_TRIES]));
	
					for (int index = INDEX_COLORS_START;index < parsed.length - 1;++index) {
						statistics.addColor(Integer.parseInt(parsed[index]));
					}
				}
			}

		}
		catch (Throwable throwable) {
			statistics.setStatsError(throwable.getClass().getName() + ": "
					+ throwable.getMessage());
		}

		statsAccurate = true;
	}

	/**
	 * Create a CSV report of each game's history New lines separate each game
	 * 
	 * @return The CSV report
	 */
	public String reportHistoryCSV() {
		StringBuffer history;

		history = new StringBuffer();

		for (String value : gameHistory) {
			history.append(value);
			history.append('\n');
		}

		return history.toString();
	}
}
