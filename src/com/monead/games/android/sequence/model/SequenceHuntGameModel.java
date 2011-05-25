package com.monead.games.android.sequence.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.monead.games.android.sequence.R;
import com.monead.games.android.sequence.sound.SoundManager;

import android.content.Context;
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
    /**
     * Serial Id required since this model is serializable.
     */
    private static final long serialVersionUID = 5685518061216785733L;

    /**
     * Maximum number of attempts to discover the sequence.
     */
    private static final int MAX_TRYS_ALLOWED = 10;

    /**
     * Value of a correct guess when calculating the relative
     * score for a try.  This is used solely to rank the
     * how close a try is to the previous try
     * e.g. better, same or worse
     */
    private static final int SCORING_VALUE_OF_CORRECT_GUESS = 10;

    /**
     * Default size of the sequence.
     */
    public static final int DEFAULT_SEQUENCE_LENGTH = 4;

    /**
     * Minimum size of the sequence.
     */
    public static final int MINIMUM_SEQUENCE_LENGTH = 4;

    /**
     * Maximum size of the sequence.
     */
    public static final int MAXIMUM_SEQUENCE_LENGTH = 8;

    /**
     * Value of an unselected position.
     */
    private static final int UNSELECTED = 0;

    // Internal constants used for the colors.
    /**
     * Constant for red.
     */
    public static final int COLOR_RED = 1;

    /**
     * Constant for green.
     */
    public static final int COLOR_GREEN = 2;

    /**
     * Constant for blue.
     */
    public static final int COLOR_BLUE = 3;

    /**
     * Constant for yellow.
     */
    public static final int COLOR_YELLOW = 4;

    /**
     * Constant for white.
     */
    public static final int COLOR_WHITE = 5;

    /**
     * Constant for black.
     */
    public static final int COLOR_BLACK = 6;

    /**
     * The number of colors available in the game.
     * 
     * This must agree with the number of colors defined with constants
     */
    public static final int NUM_COLORS = 6;

    /**
     * CVlue value indicating no match on color.
     * 
     * Must have value 0 since this will be default for new clue array
     */
    public static final int CLUE_COMPLETELY_INCORRECT = 0;

    /**
     * A guess is a correct color but in the wrong position.
     */
    public static final int CLUE_POSIT_INCORRECT = 1;

    /**
     * A guess is the correct color in the correct position.
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
     * The number of metadata elements in the array.
     */
    private static final int NUM_CLUE_METADATA = 2;

    /**
     * Stores the answer for this game.
     * 
     * One dimensional array - containing the colors
     */
    private int[] answer;

    /**
     * Stores the trys (submitted guesses) made so far in the game.
     * 
     * Two dimensional array containing the trys and guesses for each try
     */
    private int[][] guess;

    /**
     * Length of the sequence.
     */
    private int sequenceLength;

    /**
     * Has the game started - typically starts when the user selects the first
     * color.
     */
    private boolean gameStarted;

    /**
     * How long has the game been (actively) going on? This will not count time
     * when the app is hidden or closed.
     */
    private long elapsedMS;

    /**
     * The date value when the elapsedMS value was last updated. Doesn't get
     * persisted since time when app is shutdown should not count toward playing
     * time.
     */
    private transient Date latestStartupDate;

    /**
     * Stores the computed clues for the trys
     * 
     * Three dimensional array containing the the clues for each try. Clues
     * require two elements, the clue itself and the color represented.
     */
    private int[][][] clue;

    /**
     * Flags that the latest try is correct and the user has won.
     */
    private boolean winner;

    /**
     * The relative "score" of the previous try. see: latestTryScore
     */
    private int previousTryScore;

    /**
     * The relative "score" of the latest try. This is used to determine if the
     * latest try is better, worse or the same. The score is essentially the
     * number correct * 10 + number incorrect position
     */
    private int latestTryScore;

    /**
     * The current try number.
     */
    private int currentTry;

    /**
     * The current guess position within a try.
     */
    private int currentPosit;

    /**
     * A random number generator.
     */
    private Random random = new Random();

    /**
     * Class name used for logging.
     */
    private String className = this.getClass().getName();

    /**
     * Setup the model with a generated answer that the user must find.
     * 
     * @param pSequenceLength
     *            The length of the sequence
     */
    public SequenceHuntGameModel(final int pSequenceLength) {
        Log.d(className, "Requested sequence length: " + sequenceLength);
        setSequenceLength(pSequenceLength);
        Log.d(className, "Resulting sequence length: " + getSequenceLength());
        setup();
    }

    /**
     * Setup the model with a new game.
     */
    private void setup() {
        guess = new int[MAX_TRYS_ALLOWED][getSequenceLength()];
        clue = new int[MAX_TRYS_ALLOWED][getSequenceLength()]
                                         [NUM_CLUE_METADATA];
        answer = new int[getSequenceLength()];

        for (int cell = 0; cell < getSequenceLength(); ++cell) {
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
        }

        currentTry = 0;
        currentPosit = 0;
        gameStarted = false;
        elapsedMS = 0;
        latestTryScore = 0;
        previousTryScore = 0;
    }

    /**
     * Sets the sequence length for the model
     * 
     * If the length supplied is less than the minimum allowed or greater than
     * the maximum allowed, it will be set to the default length.
     * 
     * @param pSequenceLength
     *            The length of the sequence, which must be between the constant
     *            values of MIMUMUM_SEQUENCE_LENGTH and MAXIMUM_SEQUENCE_LENGTH
     */
    private void setSequenceLength(final int pSequenceLength) {
        if (pSequenceLength >= MINIMUM_SEQUENCE_LENGTH
                && pSequenceLength <= MAXIMUM_SEQUENCE_LENGTH) {
            this.sequenceLength = pSequenceLength;
            Log.d(className, "setSequenceLength, used supplied value: "
                    + pSequenceLength);
        } else {
            this.sequenceLength = DEFAULT_SEQUENCE_LENGTH;
            Log.d(className, "setSequenceLength, ignored supplied value: "
                    + pSequenceLength + " and used default: "
                    + this.sequenceLength);
        }
    }

    /**
     * Sets the winner status for the model.
     * 
     * @param pWinner
     *            True if a winner
     */
    public final void setWinner(final boolean pWinner) {
        winner = pWinner;
    }

    /**
     * Get the winner status for the model.
     * 
     * @return True if a winner
     */
    public final boolean isWinner() {
        return winner;
    }

    /**
     * Get the loser status.
     * 
     * @return True if a loser (e.g. all trys exhausted)
     */
    public final boolean isLoser() {
        return !winner && currentTry >= MAX_TRYS_ALLOWED;
    }

    /**
     * Get the maximum number of trys allowed.
     * 
     * @return The maximum number of trys allowed
     */
    public final int getMaxTrys() {
        return MAX_TRYS_ALLOWED;
    }

    /**
     * Retrieve the relative improvement of the latest try from the previous
     * one. A positive value indicates that the latest try had more correct than
     * the prior one.
     * 
     * @return Greater than 1 indicates an improvement, 0 for no change, less
     *         than 1 indicates a decline
     */
    public final int getTryProgress() {
        return latestTryScore - previousTryScore;
    }

    /**
     * Signal that a game has started.
     * 
     * TODO Complete the implementation of the game timer
     */
    private void signalGameStart() {
        gameStarted = true;
        latestStartupDate = new Date();
        Log.d(className, "signalGameStart at " + latestStartupDate.getTime());
    }

    /**
     * Get the number of MS the game has been going on. If the game has ended
     * (win or lose) this will be the total number of MS the game took.
     * 
     * @return The number of MS the game has taken.
     */
    public final long getElapsedTime() {
        updateElapsedTime();
        Log.d(className, "getElapsedTime returning " + elapsedMS);
        return elapsedMS;
    }

    /**
     * Update the MS elapsed for the current game.
     * 
     * TODO Complete the implementation of the game timer
     */
    public final void updateElapsedTime() {
        Date date;

        if (gameStarted && !isLoser() && !isWinner()) {
            if (gameStarted && latestStartupDate != null) {
                date = new Date();
                elapsedMS += date.getTime() - latestStartupDate.getTime();
                latestStartupDate = date;
                Log.d(className, "updateElapsedTime update start time ["
                        + latestStartupDate.getTime() + "] date.getTime ["
                        + date.getTime() + "] elapsedMS [" + elapsedMS + "]");
            } else {
                if (gameStarted) {
                    // Probably back from being paused
                    latestStartupDate = new Date();
                    Log.d(className, "updateElapsedTime with no start time ["
                            + latestStartupDate.getTime() + "]");
                }
            }
        }
    }

    /**
     * Signal that a game has ended.
     * 
     * TODO Complete the implementation of the game timer
     */
    public final void signalGameEnd() {
        Log.d(className, "signalGameEnd");
        updateElapsedTime();
        gameStarted = false;
    }

    /**
     * Signal that a game is being paused. Elapsed time will not be counted
     * toward the game.
     * 
     * TODO Complete the implementation of the game timer
     */
    public final void signalGamePaused() {
        Log.d(className, "signalGamePaused");
        updateElapsedTime();
    }

    /**
     * Signal that a game is being restored.
     * 
     * TODO Complete the implementation of the game timer
     */
    public final void signalGameRestored() {
        latestStartupDate = new Date();
        Log.d(className, "signalGameRestored latestStartupDate.getTime ["
                + latestStartupDate.getTime() + "]");
    }

    /**
     * Add a guess to the current try.
     * 
     * @param color
     *            The chosen color
     * 
     * @return True if there was a spot left in the current try for a guess
     */
    public final boolean addGuess(final int color) {
        Log.d(className, "addGuess color [" + color + "]");
        if (!gameStarted) {
            signalGameStart();
        }

        updateElapsedTime();

        if (currentTry < MAX_TRYS_ALLOWED 
                && currentPosit < getSequenceLength()) {
            guess[currentTry][currentPosit] = color;
            ++currentPosit;
            SoundManager.getInstance().play(R.raw.entry);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove the latest guess.
     * 
     * @return True if there was a guess to remove
     */
    public final boolean removeLastGuess() {
        if (currentTry < MAX_TRYS_ALLOWED && currentPosit > 0) {
            --currentPosit;
            guess[currentTry][currentPosit] = UNSELECTED;
            SoundManager.getInstance().play(R.raw.backout);
            return true;
        } else {
            SoundManager.getInstance().play(R.raw.fewercorrect);
        }

        return false;
    }

    /**
     * Submit the guesses.
     * 
     * @return True if a full set of guesses was supplied
     */
    public final boolean submitGuess() {
        if (currentTry < MAX_TRYS_ALLOWED
                && currentPosit == getSequenceLength()) {
            calcClues();
            ++currentTry;
            currentPosit = 0;
            SoundManager.getInstance().play(R.raw.guess);
            if (getTryProgress() < 0) {
                SoundManager.getInstance().play(R.raw.fewercorrect);
            }
            return true;
        } else {
            SoundManager.getInstance().play(R.raw.fewercorrect);
        }

        return false;
    }

    /**
     * Calculate the clues for the submitted try.
     */
    private void calcClues() {
        int clueNum;
        int[] tempGuess;
        int numberOfCorrectPositionClues;

        clueNum = 0;
        tempGuess = new int[getSequenceLength()];

        // Tracking whether this try is better (more accurate)
        // than the last
        previousTryScore = latestTryScore;
        latestTryScore = 0;

        for (int copyGuess = 0; copyGuess < getSequenceLength(); ++copyGuess) {
            tempGuess[copyGuess] = guess[currentTry][copyGuess];
        }

        for (int check = 0; check < getSequenceLength(); ++check) {
            if (tempGuess[check] == answer[check]) {
                clue[currentTry][clueNum][CLUE_METADATA_TYPE] = 
                    CLUE_POSIT_CORRECT;
                clue[currentTry][clueNum++][CLUE_METADATA_COLOR] = 
                    answer[check];
                tempGuess[check] = UNSELECTED;
                latestTryScore += SCORING_VALUE_OF_CORRECT_GUESS;
            }
        }

        if (clueNum == getSequenceLength()) {
            updateElapsedTime();
            signalGameEnd();
            setWinner(true);
        } else if (isLoser()) {
            updateElapsedTime();
            signalGameEnd();
        }

        numberOfCorrectPositionClues = clueNum;

        if (!isWinner()) {
            for (int check = 0; check < getSequenceLength(); ++check) {
                if (guess[currentTry][check] != answer[check]) {
                    for (int findMatch = 0; findMatch 
                            < getSequenceLength(); ++findMatch) {
                        if (answer[check] == tempGuess[findMatch]) {
                            clue[currentTry][clueNum][CLUE_METADATA_TYPE] = 
                                CLUE_POSIT_INCORRECT;
                            clue[currentTry][clueNum++][CLUE_METADATA_COLOR] = 
                                answer[check];
                            tempGuess[findMatch] = UNSELECTED;
                            latestTryScore++;
                            break;
                        }
                    }
                }
            }
        }

        shuffleClues(numberOfCorrectPositionClues, clueNum);
    }

    /**
     * Randomize the order of the clues (keeping each clue type [e.g. position
     * correct, position incorrect] together). This is necessary to prevent the
     * player from using the order of the "position correct" clues to figure out
     * which clue applies to which position.
     * 
     * e.g. if the sequence is yellow, red, yellow, green and the clues are
     * reported back without randomization, then a try of yellow, green, red,
     * yellow will report yellow and red diamonds (in that order everytime)
     * followed by a yellow triangle. The fact that the red diamond follows the
     * yellow diamond would inform the player that it was the first yellow that
     * was in the correct position (since it is the only yellow before the red
     * guess). The randomization prevents the player from finding such a pattern
     * from try to try.
     * 
     * @param numberOfCorrectPositionClues
     *            The number of clues indicating a correct color and position
     * @param numClues
     *            The total number of clues
     */
    private void shuffleClues(final int numberOfCorrectPositionClues,
            final int numClues) {
        List<Integer> temp;

        // Shuffle the correct position clues
        if (numberOfCorrectPositionClues > 1) {
            temp = new ArrayList<Integer>();
            for (int index = 0; index < numberOfCorrectPositionClues; ++index) {
                temp.add(clue[currentTry][index][CLUE_METADATA_COLOR]);
            }
            Collections.shuffle(temp);
            for (int index = 0; index < numberOfCorrectPositionClues; ++index) {
                clue[currentTry][index][CLUE_METADATA_COLOR] = temp.get(index);
            }
        }

        // Shuffle the incorrect position clues
        if (numClues - numberOfCorrectPositionClues > 1) {
            temp = new ArrayList<Integer>();
            for (int index = numberOfCorrectPositionClues; index 
                    < numClues; ++index) {
                temp.add(clue[currentTry][index][CLUE_METADATA_COLOR]);
            }
            Collections.shuffle(temp);
            for (int index = 0; index < temp.size(); ++index) {
                clue[currentTry][index + numberOfCorrectPositionClues]
                                 [CLUE_METADATA_COLOR] = temp
                        .get(index);
            }
        }
    }

    /**
     * Get the color index values for the correct sequence.
     * 
     * @return Color index values for the correct sequence
     */
    public final String getAnswerValue() {
        String answerValue;

        answerValue = "";

        for (int posit = 0; posit < getSequenceLength(); ++posit) {
            if (answerValue.length() > 0) {
                answerValue += ",";
            }
            answerValue += answer[posit];
        }

        return answerValue;
    }

    /**
     * Get a text description of the correct sequence.
     * 
     * @param context
     *            The application context
     * 
     * @return Text description of the generated sequence
     */
    public final String getAnswerText(final Context context) {
        String answerText;
        String colorName;

        answerText = "";

        for (int posit = 0; posit < getSequenceLength(); ++posit) {
            switch (answer[posit]) {
                case COLOR_BLACK:
                    colorName = context.getResources().getString(
                            R.string.color_black);
                    break;
                case COLOR_BLUE:
                    colorName = context.getResources().getString(
                            R.string.color_blue);
                    break;
                case COLOR_GREEN:
                    colorName = context.getResources().getString(
                            R.string.color_green);
                    break;
                case COLOR_RED:
                    colorName = context.getResources().getString(
                            R.string.color_red);
                    break;
                case COLOR_WHITE:
                    colorName = context.getResources().getString(
                            R.string.color_white);
                    break;
                case COLOR_YELLOW:
                    colorName = context.getResources().getString(
                            R.string.color_yellow);
                    break;
                default:
                    colorName = context.getResources().getString(
                            R.string.color_unknown)
                            + " (" + answer[posit] + ")";
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
     * Get the length of the sequence.
     * 
     * @return The length of the sequence
     */
    public final int getSequenceLength() {
        // Log.d(className, "getSequenceLength returning: " + sequenceLength);
        return sequenceLength;
    }

    /**
     * Determine if a guess position has a color.
     * 
     * @param row
     *            The row (try) being checked
     * @param tryNum
     *            The position in the row
     * 
     * @return True of there is a guessed color in the position
     */
    public final boolean hasTryColor(final int row, final int tryNum) {
        return guess[row][tryNum] != UNSELECTED;
    }

    /**
     * Detect if a position has a correct color in the incorrect position.
     * 
     * @param row
     *            The row (try) being checked
     * @param clueNum
     *            The guess position being checked
     * 
     * @return True if the position has a correct color in the wrong place
     */
    public final boolean hasClueIncorrect(final int row, final int clueNum) {
        return row < currentTry
                && clue[row][clueNum][CLUE_METADATA_TYPE] == UNSELECTED;
    }

    /**
     * Get the meaning of the clue for a position.
     * 
     * @param row
     *            The row (try) being checked
     * @param clueNum
     *            The guess position being checked
     * 
     * @return The clue type (CLUE_POSIT_* constants)
     */
    public final int getClueMeaning(final int row, final int clueNum) {
        Log.d("Sequence", "Model getClueMeaning row: " + row + "  clueNum: "
                + clueNum);
        return clue[row][clueNum][CLUE_METADATA_TYPE];
    }

    /**
     * Get the color related to the clue at a position.
     * 
     * @param row
     *            The row (try) being checked
     * @param clueNum
     *            The guess position being checked
     * 
     * @return The color represented by the clue
     */
    public final int getClueColorCode(final int row, final int clueNum) {
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
     * Get the guessed color for a position.
     * 
     * @param row
     *            The row (try) being checked
     * @param tryNum
     *            The guess position being checked
     * 
     * @return The color guessed at the selected position
     */
    public final int getTryColorCode(final int row, final int tryNum) {
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
     * Get the current try number.
     * 
     * @return The current try number
     */
    public final int getCurrentTry() {
        return currentTry;
    }
}
