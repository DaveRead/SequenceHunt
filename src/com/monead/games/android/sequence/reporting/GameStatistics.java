package com.monead.games.android.sequence.reporting;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
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
public class GameStatistics implements Serializable {
	private static final long serialVersionUID = 2350057697130863735L;

	/**
	 * Maximum number of historical games for which to
	 * hold statistics
	 */
	private final static int MAX_GAMES_TO_STORE = 300;
	
	/**
	 * The total number of games played
	 */
	private long gameCount;
	
	/**
	 * The historical information for each game
	 */
	private List<String> gameHistory;
	
	/**
	 * Setup the game history array
	 */
	public GameStatistics() {
		gameHistory = new ArrayList<String>();
	}

	/**
	 * Add a game to history
	 * 
	 * This method will assure that the MAX_GAMES_TO_STORE
	 * limit is enforced.
	 * 
	 * @param model The model for the game being added
	 */
	public void addGame(SequenceHuntGameModel model) {
		while (gameHistory.size() >= MAX_GAMES_TO_STORE) {
			gameHistory.remove(0);
		}
		gameCount++;
		gameHistory.add(gameCount + "," + model.getAnswerValue());
	}
	
	/**
	 * Flag a game as deleted, meaning that it
	 * will not be played.
	 * 
	 * The method does a logical delete, so that the
	 * information will be retained but flagged as
	 * deleted.
	 */
	public void deleteLastGame() {
		String lastGameStatistics;
		
		if (gameHistory.size() > 0) {
			lastGameStatistics = gameHistory.remove(gameHistory.size() - 1);
			if (lastGameStatistics.indexOf("Deleted") == -1) {
				lastGameStatistics += ",Deleted";
			}
			gameHistory.add(lastGameStatistics);
		}
	}
	
	/**
	 * Create a CSV report of each game's history
	 * New lines separate each game
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
