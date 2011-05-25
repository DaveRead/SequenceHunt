package com.monead.games.android.sequence.reporting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks statistics related to game play.
 * 
 * @author David Read
 * 
 */
public class GameStatistics implements Serializable {
    /**
     * Serial Id required since this model is serializable.
     */
    private static final long serialVersionUID = 3931550416819079788L;

    /**
     * The count of randomly selected colors so far chosen by this model. Allows
     * for manual checking of randomness.
     */
    private Map<Integer, Integer> colorCounts;

    /**
     * The number of wins.
     */
    private long numWins;

    /**
     * The number of losses.
     */
    private long numLosses;

    /**
     * The number of games that were not completed.
     */
    private long numQuits;

    /**
     * The number of games played in easy mode.
     */
    private long numEasy;

    /**
     * The number of games played in hard mode.
     */
    private long numHard;

    /**
     * The total number of tries across all games.
     */
    private long totalTries;

    /**
     * The total time (in MS) spent playing games that were won.
     */
    private long totalWinTimeMs;

    /**
     * The total time (in MS) spent playing games that were lost.
     */
    private long totalLostTimeMs;

    /**
     * Error message when statistics cannot be calculated.
     */
    private String statsError;

    /**
     * Create a new satatistics instance.
     */
    public GameStatistics() {
        colorCounts = new HashMap<Integer, Integer>();
    }

    /**
     * Returns the number of games stored in the collection.
     * 
     * @return The number of games tracked
     */
    public final long getNumGamesStored() {
        return numWins + numLosses + numQuits;
    }

    /**
     * Add a color choice from a new game. Used to track to number of times each
     * color is chosen across all games. This is an internal color index value,
     * not a value from the java.awt.Color class.
     * 
     * @param color
     *            The chosen color
     */
    public final void addColor(final int color) {
        Integer current;

        current = colorCounts.get(color);
        if (current == null) {
            current = 0;
        }

        current++;

        colorCounts.put(color, current);
    }

    /**
     * Get the total number of times the selected color has been chosen in a
     * sequence.
     * 
     * @param colorIndex
     *            The color whose count is being retrieved
     * 
     * @return The total number of times the selected color has appeared in a
     *         sequence
     */
    public final int getColorCount(final int colorIndex) {
        Integer count;

        count = colorCounts.get(colorIndex);

        if (count == null) {
            count = 0;
        }

        return count;
    }

    /**
     * Return the number of games won.
     * 
     * @return The number of games won
     */
    public final long getNumWins() {
        return numWins;
    }

    /**
     * Add a win.
     */
    public final void addWin() {
        ++numWins;
    }

    /**
     * Return the number of games lost.
     * 
     * @return The number of games lost
     */
    public final long getNumLosses() {
        return numLosses;
    }

    /**
     * Add a loss.
     */
    public final void addLoss() {
        ++numLosses;
    }

    /**
     * Get the number of games abandoned.
     * 
     * @return The number of games not completed
     */
    public final long getNumQuits() {
        return numQuits;
    }

    /**
     * Add an abandoned game.
     */
    public final void addQuit() {
        ++numQuits;
    }

    /**
     * Return the number of games played in easy mode.
     * 
     * @return The number of games played in easy mode
     */
    public final long getNumEasy() {
        return numEasy;
    }

    /**
     * Add an easy game.
     */
    public final void addEasy() {
        ++numEasy;
    }

    /**
     * Return the number of games played in hard mode.
     * 
     * @return The number of games played in hard mode
     */
    public final long getNumHard() {
        return numHard;
    }

    /**
     * Add a hard game.
     */
    public final void addHard() {
        ++numHard;
    }

    /**
     * Get the total number of tries attempted across all games.
     * 
     * @return The total number of tries attempted
     */
    public final long getTotalTries() {
        return totalTries;
    }

    /**
     * Add a count of tries to the total.
     * 
     * @param numTries
     *            Number of tries to add
     */
    public final void addTries(final long numTries) {
        totalTries += numTries;
    }

    /**
     * Return the total time (in MS) spent playing games that were won.
     * 
     * @return The time spent playing games that were won
     */
    public final long getTotalWinTimeMs() {
        return totalWinTimeMs;
    }

    /**
     * Add the time (in MS) spent playing a game that was won.
     * 
     * @param winTimeMs
     *            The time spent playing a game that was won
     */
    public final void addWinTimeMS(final long winTimeMs) {
        totalWinTimeMs += winTimeMs;
    }

    /**
     * Return the total time (in MS) spent playing games that were lost.
     * 
     * @return The time spent playing games that were lost
     */
    public final long getTotalLostTimeMs() {
        return totalLostTimeMs;
    }

    /**
     * Add the time (in MS) spent playing a game that was lost.
     * 
     * @param lostTimeMs
     *            The time spent playing a game that was lost
     */
    public final void addLostTimeMS(final long lostTimeMs) {
        totalLostTimeMs += lostTimeMs;
    }

    /**
     * Get the average number of tries per game.
     * 
     * @return The average number of tries per game
     */
    public final int getAverageTries() {
        if (getNumGamesStored() > 0) {
            return (int) ((double) getTotalTries() / getNumGamesStored());
        } else {
            return 0;
        }
    }

    /**
     * Get the average time (in MS) spent playing a game that was won.
     * 
     * @return The average time spent playing a game that was won
     */
    public final long getAverageWinTimeMs() {
        if (getNumWins() > 0) {
            return (long) ((double) getTotalWinTimeMs() / getNumWins());
        } else {
            return 0L;
        }
    }

    /**
     * Get the average time (in MS) spent playing a game that was lost.
     * 
     * @return The average time spent playing a game that was lost
     */
    public final long getAverageLoseTimeMs() {
        if (getNumLosses() > 0) {
            return (long) ((double) getTotalLostTimeMs() / getNumLosses());
        } else {
            return 0L;
        }
    }

    /**
     * Get the error message associated with a failure to generate statistics.
     * 
     * @return The error message
     */
    public final String getStatsError() {
        return statsError;
    }

    /**
     * Set an error message indicating a failure when trying to generate
     * statistics.
     * 
     * @param pStatsError
     *            The error message
     */
    public final void setStatsError(final String pStatsError) {
        statsError = pStatsError;
    }
}
