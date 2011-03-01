package com.monead.games.android.sequence.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.graphics.Color;
import android.util.Log;

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
 * This is the model that holds the state for a game of Sequence Hunt.
 * 
 * @author David Read
 * 
 */
public class SequenceHuntGameModel implements Serializable {
	private static final long serialVersionUID = 5685518061216785730L;

	/**
	 * Maximum number of attempts to discover the sequence
	 */
	private static int MAX_TRYS_ALLOWED = 10;
	
	/**
	 * Fixed size of the sequence
	 */
	private static int SEQUENCE_LENGTH = 4;
	
	/**
	 * Value of an unselected position
	 */
	private static final int UNSELECTED = 0;

	/**
	 * Internal constants used for the colors
	 */
	public static final int COLOR_RED = 1;
	public static final int COLOR_GREEN = 2;
	public static final int COLOR_BLUE = 3;
	public static final int COLOR_YELLOW = 4;
	public static final int COLOR_WHITE = 5;
	public static final int COLOR_BLACK = 6;

	/**
	 * The number of colors available in the game
	 * 
	 * This must agree with the number of colors 
	 * defined with constants
	 */
	private static final int NUM_COLORS = 6;

	/**
	 * CVlue value indicating no match on color
	 * 
	 * Must have value 0 since this will be default 
	 * for new clue array
	 */
	public static final int CLUE_COMPLETELY_INCORRECT = 0;

	/**
	 * A guess is a correct color but in the wrong position
	 */
	public static final int CLUE_POSIT_INCORRECT = 1;

	/**
	 * A guess is the correct color in the correct position
	 */
	public static final int CLUE_POSIT_CORRECT = 2;

	// Constants for the clue three-dimensional array
	/**
	 * Array index used when populating the clue array.
	 * 
	 * The element represents the type (CLUE_POSIT_*)
	 */
	private static final int CLUE_METADATA_TYPE = 0;

	/**
	 * Array index used when populating the clue array.
	 * 
	 * The element represents the color
	 */
	private static final int CLUE_METADATA_COLOR = 1;

	/**
	 * The number of metadata elements in the array
	 */
	private static final int NUM_CLUE_METADATA = 2;

	/**
	 * Stores the answer for this game
	 * 
	 * One dimensional array - containing the colors
	 */
	private int[] answer;

	/**
	 * Stores the trys (submitted guesses) made so far in the game
	 * 
	 * Two dimensional array containing the trys and guesses for each try
	 */
	private int[][] guess;

	/**
	 * Stores the computed clues for the trys
	 * 
	 * Three dimensional array containing the the clues for each try. Clues
	 * require two elements, the clue itself and the color represented.
	 */
	private int[][][] clue;

	/**
	 * The count of randomly selected colors so far chosen by this model. Allows
	 * for manual checking of randomness.
	 */
	private static Map<Integer, Integer> colorCounts;

	/**
	 * Initialize the selected color counts at 0
	 */
	static {
		colorCounts = new HashMap<Integer, Integer>();

		colorCounts.put(COLOR_BLACK, 0);
		colorCounts.put(COLOR_BLUE, 0);
		colorCounts.put(COLOR_GREEN, 0);
		colorCounts.put(COLOR_RED, 0);
		colorCounts.put(COLOR_WHITE, 0);
		colorCounts.put(COLOR_YELLOW, 0);
	}

	/**
	 * Flags that the latest try is correct and the user has won.
	 */
	private boolean winner;

	/**
	 * The current try number
	 */
	private int currentTry;

	/**
	 * The current guess position within a try
	 */
	private int currentPosit;

	/**
	 * A random number generator
	 */
	private Random random = new Random();

	/**
	 * Setup the model with a generated answer that the user must find
	 */
	public SequenceHuntGameModel() {
		guess = new int[MAX_TRYS_ALLOWED][SEQUENCE_LENGTH];
		clue = new int[MAX_TRYS_ALLOWED][SEQUENCE_LENGTH][NUM_CLUE_METADATA];
		answer = new int[SEQUENCE_LENGTH];

		for (int cell = 0; cell < SEQUENCE_LENGTH; ++cell) {
			switch (Math.abs(random.nextInt() % NUM_COLORS)) {
				case 0:
					answer[cell] = COLOR_BLACK;
					break;
				case 1:
					answer[cell] = COLOR_BLUE;
					break;
				case 2:
					answer[cell] = COLOR_GREEN;
					break;
				case 3:
					answer[cell] = COLOR_RED;
					break;
				case 4:
					answer[cell] = COLOR_WHITE;
					break;
				case 5:
					answer[cell] = COLOR_YELLOW;
					break;
				default:
					answer[cell] = COLOR_YELLOW;
					break;
			}
			trackColorChoice(answer[cell]);
		}

		currentTry = 0;
		currentPosit = 0;
	}

	/**
	 * Maintain the counts of each selected color
	 * 
	 * @param color
	 *            The chosen color
	 */
	private void trackColorChoice(int color) {
		Integer currentCount;

		currentCount = colorCounts.get(color);
		currentCount++;
		colorCounts.put(color, currentCount);
	}

	/**
	 * Return a message containing the counts of each color selected by this
	 * model
	 * 
	 * @return A text message with color counts
	 */
	public String reportColorCounts() {
		StringBuffer report = new StringBuffer();

		report.append("Black: " + colorCounts.get(COLOR_BLACK));
		report.append('\n');
		report.append("Blue: " + colorCounts.get(COLOR_BLUE));
		report.append('\n');
		report.append("Green: " + colorCounts.get(COLOR_GREEN));
		report.append('\n');
		report.append("Red: " + colorCounts.get(COLOR_RED));
		report.append('\n');
		report.append("White: " + colorCounts.get(COLOR_WHITE));
		report.append('\n');
		report.append("Yellow: " + colorCounts.get(COLOR_YELLOW));

		return report.toString();
	}

	/**
	 * Set the winner status for the model
	 * 
	 * @param winner
	 *            True if a winner
	 */
	public void setWinner(boolean winner) {
		this.winner = winner;
	}

	/**
	 * Get the winner status for the model
	 * 
	 * @return True if a winner
	 */
	public boolean isWinner() {
		return winner;
	}

	/**
	 * Get the loser status
	 * 
	 * @return True if a loser (e.g. all trys exhausted)
	 */
	public boolean isLoser() {
		return !winner && currentTry >= MAX_TRYS_ALLOWED;
	}

	/**
	 * Get the maximum number of trys allowed
	 * 
	 * @return The maximum number of trys allowed
	 */
	public int getMaxTrys() {
		return MAX_TRYS_ALLOWED;
	}

	/**
	 * Add a guess to the current try
	 * 
	 * @param color
	 *            The chosen color
	 * 
	 * @return True if there was a spot left in the current try for a guess
	 */
	public boolean addGuess(int color) {
		if (currentTry < MAX_TRYS_ALLOWED && currentPosit < SEQUENCE_LENGTH) {
			guess[currentTry][currentPosit] = color;
			++currentPosit;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Remove the latest guess
	 * 
	 * @return True if there was a guess to remove
	 */
	public boolean removeLastGuess() {
		if (currentTry < MAX_TRYS_ALLOWED && currentPosit > 0) {
			--currentPosit;
			guess[currentTry][currentPosit] = UNSELECTED;
			return true;
		}

		return false;
	}

	/**
	 * Submit the guesses
	 * 
	 * @return True if a full set of guesses was supplied
	 */
	public boolean submitGuess() {
		if (currentTry < MAX_TRYS_ALLOWED && currentPosit == SEQUENCE_LENGTH) {
			calcClues();
			++currentTry;
			currentPosit = 0;
			return true;
		}

		return false;
	}

	/**
	 * Calculate the clues for the submitted try
	 */
	private void calcClues() {
		int clueNum;
		int tempGuess[];

		clueNum = 0;
		tempGuess = new int[SEQUENCE_LENGTH];

		for (int copyGuess = 0; copyGuess < SEQUENCE_LENGTH; ++copyGuess) {
			tempGuess[copyGuess] = guess[currentTry][copyGuess];
		}

		for (int check = 0; check < SEQUENCE_LENGTH; ++check) {
			if (tempGuess[check] == answer[check]) {
				clue[currentTry][clueNum][CLUE_METADATA_TYPE] = CLUE_POSIT_CORRECT;
				clue[currentTry][clueNum++][CLUE_METADATA_COLOR] = answer[check];

				tempGuess[check] = UNSELECTED;
			}
		}

		if (clueNum == SEQUENCE_LENGTH) {
			setWinner(true);
		}

		if (!isWinner()) {
			for (int check = 0; check < SEQUENCE_LENGTH; ++check) {
				if (guess[currentTry][check] != answer[check]) {
					for (int findMatch = 0; findMatch < SEQUENCE_LENGTH; ++findMatch) {
						if (answer[check] == tempGuess[findMatch]) {
							clue[currentTry][clueNum][CLUE_METADATA_TYPE] = CLUE_POSIT_INCORRECT;
							clue[currentTry][clueNum++][CLUE_METADATA_COLOR] = answer[check];
							tempGuess[findMatch] = UNSELECTED;
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Get the color index values for the correct sequence
	 * 
	 * @return Color index values for the correct sequence
	 */
	public String getAnswerValue() {
		String answerValue;
		
		answerValue = "";
		
		for (int posit = 0;posit < SEQUENCE_LENGTH;++posit) {
			if (answerValue.length() > 0) {
				answerValue += ", ";
			}
			answerValue += answer[posit];
		}
		
		return answerValue;
	}
	
	/**
	 * Get a text description of the correct sequence
	 * 
	 * @return Text description of the generated sequence
	 */
	public String getAnswerText() {
		String answerText;
		String colorName;

		answerText = "";

		for (int posit = 0; posit < SEQUENCE_LENGTH; ++posit) {
			switch (answer[posit]) {
				case COLOR_BLACK:
					colorName = "Black";
					break;
				case COLOR_BLUE:
					colorName = "Blue";
					break;
				case COLOR_GREEN:
					colorName = "Green";
					break;
				case COLOR_RED:
					colorName = "Red";
					break;
				case COLOR_WHITE:
					colorName = "White";
					break;
				case COLOR_YELLOW:
					colorName = "Yellow";
					break;
				default:
					colorName = "Unknown (" + answer[posit] + ")";
					break;
			}
			if (answerText.length() > 0) {
				answerText += ", ";
			}
			answerText += colorName;
		}

		return answerText;
	}

	/**
	 * Get the length of the sequence
	 * 
	 * @return The length of the sequence
	 */
	public int getSequenceLength() {
		return SEQUENCE_LENGTH;
	}

	/**
	 * Determine if a guess position has a color
	 * 
	 * @param row
	 *            The row (try) being checked
	 * @param tryNum
	 *            The position in the row
	 * 
	 * @return True of there is a guessed color in the position
	 */
	public boolean hasTryColor(int row, int tryNum) {
		return guess[row][tryNum] != UNSELECTED;
	}

	/**
	 * Detect if a position has a correct color in the incorrect position
	 * 
	 * @param row
	 *            The row (try) being checked
	 * @param clueNum
	 *            The guess position being checked
	 * 
	 * @return True if the position has a correct color in the wrong place
	 */
	public boolean hasClueIncorrect(int row, int clueNum) {
		return row < currentTry
				&& clue[row][clueNum][CLUE_METADATA_TYPE] == UNSELECTED;
	}

	/**
	 * Get the meaning of the clue for a position
	 * 
	 * @param row
	 *            The row (try) being checked
	 * @param clueNum
	 *            The guess position being checked
	 * 
	 * @return The clue type (CLUE_POSIT_* constants)
	 */
	public int getClueMeaning(int row, int clueNum) {
		Log.d("Sequence", "Model getClueMeaning row: " + row + "  clueNum: "
				+ clueNum);
		return clue[row][clueNum][CLUE_METADATA_TYPE];
	}

	/**
	 * Get the color related to the clue at a position
	 * 
	 * @param row
	 *            The row (try) being checked
	 * @param clueNum
	 *            The guess position being checked
	 * 
	 * @return The color represented by the clue
	 */
	public int getClueColorCode(int row, int clueNum) {
		int color;

		switch (clue[row][clueNum][CLUE_METADATA_COLOR]) {
			case COLOR_BLACK:
				color = Color.BLACK;
				break;
			case COLOR_BLUE:
				color = Color.BLUE;
				break;
			case COLOR_GREEN:
				color = Color.GREEN;
				break;
			case COLOR_RED:
				color = Color.RED;
				break;
			case COLOR_WHITE:
				color = Color.WHITE;
				break;
			case COLOR_YELLOW:
				color = Color.YELLOW;
				break;
			default:
				color = Color.GRAY;
				break;
		}

		return color;
	}

	/**
	 * Get the guessed color for a position
	 * 
	 * @param row The row (try) being checked
	 * @param tryNum The guess position being checked
	 * 
	 * @return The color guessed at the selected position
	 */
	public int getTryColorCode(int row, int tryNum) {
		int color;

		switch (guess[row][tryNum]) {
			case COLOR_BLACK:
				color = Color.BLACK;
				break;
			case COLOR_BLUE:
				color = Color.BLUE;
				break;
			case COLOR_GREEN:
				color = Color.GREEN;
				break;
			case COLOR_WHITE:
				color = Color.WHITE;
				break;
			case COLOR_RED:
				color = Color.RED;
				break;
			case COLOR_YELLOW:
				color = Color.YELLOW;
				break;
			default:
				color = Color.GRAY;
				break;
		}

		return color;
	}

	/**
	 * Get the current try number
	 * 
	 * @return The current try number
	 */
	public int getCurrentTry() {
		return currentTry;
	}
}
