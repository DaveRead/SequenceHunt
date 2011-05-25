package com.monead.games.android.sequence.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monead.games.android.sequence.event.ColorChoiceListener;
import com.monead.games.android.sequence.model.SequenceHuntGameModel;
import com.monead.games.android.sequence.reporting.GameStatisticsEngine;
import com.monead.games.android.sequence.ui.shape.DiamondShape;
import com.monead.games.android.sequence.ui.shape.TriangleShape;
import com.monead.games.android.sequence.util.Formatter;

import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.view.View.OnTouchListener;

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
 * A view that draws the Sequence Hunt game board
 * 
 * This seemed more easily done programmatically rather than through an XML
 * definition.
 * 
 * @author David Read
 * 
 */
public class SequenceGameBoard extends View implements ColorChoiceListener,
        OnTouchListener {
    /**
     * Minimum space (in pixels) to have between graphical shapes.
     */
    private static final int MINIMUM_SHAPE_SPACING = 2;

    /**
     * Size for text used to display the timer.
     */
    private static final int TIMER_TEXT_FONT_SIZE = 16;

    /**
     * The number of degrees in the arc used to draw the try submit
     * icon.
     */
    private static final int ICON_SUBMIT_TRY_ARC_DEGREES = 270;

    /**
     * The start position for the arc used to draw the try submit
     * icon.
     */
    private static final int ICON_SUBMIT_TRY_ARC_START = 315;

    /**
     * The number of degrees in the arc used to draw the delete
     * color icon.
     */
    private static final int ICON_DELETE_ARC_DEGREES = 270;

    /**
     * The start position for the arc used to draw the delete
     * color icon.
     */
    private static final int ICON_DELETE_ARC_START = 45;

    /**
     * Input value representing a request to remove the last entered color
     * choice.
     */
    public static final int INPUT_DELETE = -1;

    /**
     * Input value representing a request to submit the color guesses.
     */
    public static final int INPUT_SUBMIT_GUESS = -2;

    /**
     * Array index used when populating the inputRange array.
     * 
     * Upper-Left-X position
     */
    private static final int INDEX_ULX = 0;

    /**
     * Array index used when populating the inputRange array.
     * 
     * Upper-Left-Y position
     */
    private static final int INDEX_ULY = 1;

    /**
     * Array index used when populating the inputRange array.
     * 
     * Lower-Right-X position
     */
    private static final int INDEX_LRX = 2;

    /**
     * Array index used when populating the inputRange array.
     * 
     * Lower-Right-Y position
     */
    private static final int INDEX_LRY = 3;

    /**
     * Array index used when populating the inputRange array.
     * 
     * Color value
     */
    private static final int INDEX_COLOR_CODE = 4;

    /**
     * The number of clues that can be displayed in the space occupied by a
     * guess. Currently this is 4, as the clues occupy 1/4 of the guess space,
     * arranged in a 2x2 grid
     */
    private static final int CLUES_PER_GUESS_SPACE = 4;

    /**
     * An array depicting the touch-screen input images.
     * 
     * The first dimension is the color or function. The second dimension is
     * used to store X-Y coordinates and the color or function code.
     * 
     * These values are set when the instance is created and then as the user
     * touches the screen the coordinates of the touch are compared to the areas
     * defined in this array. That information is used to determine what action
     * to take in response to the touch.
     */
    private int[][] inputRange = new int[8][5];

    /**
     * The model for the current game.
     */
    private SequenceHuntGameModel gameModel;

    /**
     * Whether the difficulty is set to hard.
     */
    private boolean difficultyIsHard;

    /**
     * The length of the sequence.
     */
    private int sequenceLength;

    /**
     * Information collected as the game board is setup and used.
     */
    private Map<String, String> runtimeInformation = 
            new HashMap<String, String>();

    /**
     * Setup the game board using the supplied Context.
     * 
     * @param context The context to update
     * @param gameStatistics Statistics calculator
     * @param pSequenceLength The length of the sequence
     */
    public SequenceGameBoard(final Context context,
            final GameStatisticsEngine gameStatistics, 
            final int pSequenceLength) {
        super(context);

        // gameModel = new SequenceHuntGameModel();
        // this.gameStatistics = gameStatistics;
        setSequenceLength(pSequenceLength);
        // newGame();
    }

    /**
     * Start a new game, losing any prior game model.
     */
    public final void newGame() {
        gameModel = new SequenceHuntGameModel(sequenceLength);
        // gameStatistics.addGame(gameModel);
        invalidate();
    }

    /**
     * Return the current game model.
     * 
     * @return The game model
     */
    public final SequenceHuntGameModel getModel() {
        return gameModel;
    }

    /**
     * Set a game model, replacing any current model.
     * 
     * @param model The game model
     */
    public final void setModel(final SequenceHuntGameModel model) {
        gameModel = model;
        // gameStatistics.deleteLastGame();
        invalidate();
    }

    /**
     * Set the game play mode to hard.
     * 
     * @param difficultyHard
     *            Whether the difficulty should be hard
     */
    public final void setDifficultyToHard(final boolean difficultyHard) {
        this.difficultyIsHard = difficultyHard;
    }

    /**
     * Get the current setting of the difficulty switch.
     * 
     * @return True if difficulty is set to hard
     */
    public final boolean isDifficultySetToHard() {
        return difficultyIsHard;
    }

    /**
     * Set the sequence length. If there is no current game or if this is an
     * updated length, a new game is started.
     * 
     * @param pSequenceLength The length of the sequence
     */
    public final void setSequenceLength(final int pSequenceLength) {
        if (getModel() == null
                || getModel().getSequenceLength() != pSequenceLength) {
            this.sequenceLength = pSequenceLength;
            newGame();
        }
    }

    /**
     * Get the length of the sequence.
     * 
     * @return The length of the sequence
     */
    public final int getSequenceLength() {
        return sequenceLength;
    }

    /**
     * Get an array of string reporting runtime information for the game board.
     * This is mostly environment and calculation information used to layout the
     * screen properly for the device capabilities.
     * 
     * @return A list of string reporting runtime information
     */
    public final List<String> getRuntimeInformation() {
        List<String> infoList = new ArrayList<String>();

        for (String key : runtimeInformation.keySet()) {
            infoList.add(key + ": " + runtimeInformation.get(key));
        }

        Collections.sort(infoList);

        return infoList;
    }

    /**
     * Draw the game board on the canvas.
     * 
     * @param canvas The canvas to update
     */
    @Override
    protected final void onDraw(final Canvas canvas) {
        configView(canvas);
    }

    /**
     * Display the game timer.
     * 
     * TODO Complete the implementation of the game timer
     * 
     * @param canvas
     *            The canvas being updated
     * @param circleArea
     *            The height of the area that is available at the top of the
     *            canvas
     * @param vertSpacing The spacing between vertical rows
     */
    @SuppressWarnings("unused")
    private void drawTimer(final Canvas canvas, 
            final int circleArea, final int vertSpacing) {
        Paint paint;

        ShapeDrawable drawable;
        drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(Color.DKGRAY);
        drawable.setBounds(0, 0, getWidth(), circleArea - vertSpacing);
        drawable.draw(canvas);

        paint = new Paint();
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(TIMER_TEXT_FONT_SIZE);
        paint.setColor(Color.WHITE);

        canvas.drawText(
                Formatter.getInstance()
                        .formatTimer(getModel().getElapsedTime()), canvas
                        .getWidth() / 2, circleArea / 2, paint);
    }

    /**
     * Layout the game board on the supplied canvas.
     * 
     * @param canvas The canvas to update
     */
    private void configView(final Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int numberOfClueSpacesNeeded = 
                (gameModel.getSequenceLength() + (CLUES_PER_GUESS_SPACE - 1))
                / CLUES_PER_GUESS_SPACE;

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        int availWidth = getWidth()
                / (gameModel.getSequenceLength() + numberOfClueSpacesNeeded);
        int availHeight = getHeight() / (gameModel.getMaxTrys() + 2);

        int horizSpacing; // = availWidth / 5;
        int vertSpacing; // = availHeight / 12;
        horizSpacing = MINIMUM_SHAPE_SPACING;
        vertSpacing = MINIMUM_SHAPE_SPACING;

        runtimeInformation.put("cv canvasWidth", "" + canvasWidth);
        runtimeInformation.put("cv canvasHeight", "" + canvasHeight);
        runtimeInformation.put("cv viewWidth", "" + viewWidth);
        runtimeInformation.put("cv viewHeight", "" + viewHeight);
        runtimeInformation.put("cv availWidth initial", "" + availWidth);
        runtimeInformation.put("cv availHeight initial", "" + availHeight);

        availWidth -= horizSpacing;
        availHeight -= vertSpacing;

        runtimeInformation.put("cv availWidth final", "" + availWidth);
        runtimeInformation.put("cv availHeight final", "" + availHeight);

        int circleArea = Math.min(availWidth, availHeight);

        int xPadding = (getWidth() - ((circleArea + horizSpacing) * (gameModel
                .getSequenceLength() + numberOfClueSpacesNeeded))) / 2;

        runtimeInformation.put("cv circleArea", "" + circleArea);
        runtimeInformation.put("cv xPadding initial", "" + xPadding);

        if (xPadding < 0) {
            xPadding = 0;
        }
        runtimeInformation.put("cv xPadding final", "" + xPadding);

        ShapeDrawable drawable;
        drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(Color.CYAN);
        drawable.setBounds(0, 0, getWidth(), 1);
        drawable.draw(canvas);

        // drawTimer(canvas, circleArea, vertSpacing);

        for (int row = 0; row < gameModel.getMaxTrys(); ++row) {
            drawRow(canvas, row, xPadding, circleArea, horizSpacing,
                    vertSpacing, numberOfClueSpacesNeeded);
        }

        configInput(canvas, circleArea, (horizSpacing + circleArea)
                * (gameModel.getMaxTrys() + 1));
    }

    /**
     * Draw one try row on the canvas.
     * 
     * @param canvas
     *            The canvas to draw on
     * @param row
     *            The row to draw
     * @param xPadding
     *            The horizontal start position for a row
     * @param circleArea
     *            The area for one guess circle
     * @param horizSpacing
     *            The amount of horizontal padding between shapes
     * @param vertSpacing
     *            The amount of vertical padding between shapes
     * @param numberOfClueSpacesNeeded
     *            The number of guess spaces occupied by the clues
     */
    private void drawRow(final Canvas canvas, final int row, 
            final int xPadding, final int circleArea,
            final int horizSpacing, final int vertSpacing, 
            final int numberOfClueSpacesNeeded) {
        for (int clue = 0; row < gameModel.getCurrentTry()
                && clue < gameModel.getSequenceLength(); ++clue) {
            configClue(canvas, row, xPadding, clue, circleArea, vertSpacing,
                    numberOfClueSpacesNeeded);
        }

        for (int tryNum = 0; tryNum < gameModel.getSequenceLength(); ++tryNum) {
            configTry(canvas, row, xPadding, tryNum, circleArea, horizSpacing,
                    vertSpacing, numberOfClueSpacesNeeded);
        }
    }

    /**
     * Setup the depiction for the clues.
     * 
     * Clues occupy the area of one guess circle
     * 
     * @param canvas
     *            The canvas to draw on
     * @param row
     *            The row to draw
     * @param xPadding
     *            The horizontal start position for a row
     * @param clueNum
     *            The clue number being drawn
     * @param availArea
     *            The available area for the clue
     * @param vertSpacing
     *            The amount of vertical padding between clues
     * @param numberOfClueSpacesNeeded
     *            The number of guess spaces occupied by the clues
     */
    private void configClue(final Canvas canvas, final int row, 
            final int xPadding, final int clueNum,
            final int availArea, final int vertSpacing, 
            final int numberOfClueSpacesNeeded) {
        ShapeDrawable mDrawable;
        boolean hasBorder;

        int circleArea = (availArea / 2) - 1;

        runtimeInformation.put("cc circleArea", "" + circleArea);

        int x = circleArea
                * (clueNum % ((gameModel.getSequenceLength() + 1) / 2));
        if (x > 0) {
            x = x + (clueNum % ((gameModel.getSequenceLength() + 1) / 2));
        }
        x += xPadding;

        int y = (availArea + vertSpacing) * row;
        y += availArea;
        if (clueNum > ((gameModel.getSequenceLength() - 1) / 2)) {
            y += circleArea + 1;
        }

        int width = circleArea;
        int height = circleArea;

        // Correct Guess (color and position)
        if (gameModel.getClueMeaning(row, clueNum) 
                == SequenceHuntGameModel.CLUE_POSIT_CORRECT) {
            mDrawable = new ShapeDrawable(new DiamondShape());
            hasBorder = true;
            // Correct color, incorrect position
        } else if (gameModel.getClueMeaning(row, clueNum) 
                == SequenceHuntGameModel.CLUE_POSIT_INCORRECT) {
            mDrawable = new ShapeDrawable(new TriangleShape());
            hasBorder = true;
        } else {
            mDrawable = new ShapeDrawable(new OvalShape());
            hasBorder = false;
        }

        if (hasBorder) {
            mDrawable.getPaint().setColor(Color.WHITE);
            mDrawable.setBounds(x, y, x + width, y + height);
            mDrawable.draw(canvas);
            x++;
            x++;
            y++;
            y++;
            width -= 4;
            height -= 4;
        }

        if (difficultyIsHard
                || gameModel.getClueMeaning(row, clueNum) 
                == SequenceHuntGameModel.CLUE_COMPLETELY_INCORRECT) {
            mDrawable.getPaint().setColor(Color.LTGRAY);
        } else {
            mDrawable.getPaint().setColor(
                    gameModel.getClueColorCode(row, clueNum));
        }
        mDrawable.setBounds(x, y, x + width, y + height);
        mDrawable.draw(canvas);

        if (gameModel.hasClueIncorrect(row, clueNum)) {
            mDrawable.getPaint().setColor(Color.BLACK);
            canvas.drawLine(x, y, x + width, y + width, mDrawable.getPaint());
            canvas.drawLine(x, y + width, x + width, y, mDrawable.getPaint());
        }
    }

    /**
     * Setup one try (a completed row of guesses and clues).
     * 
     * @param canvas
     *            The canvas to draw on
     * @param row
     *            The row to report
     * @param xPadding
     *            The horizontal start position for a row
     * @param tryNum
     *            The try number being reported
     * @param availArea
     *            The area available to draw a guess shape
     * @param horizSpacing
     *            The amount of horizontal padding between clues
     * @param vertSpacing
     *            The amount of vertical padding between clues
     * @param numberOfClueSpacesNeeded
     *            The number of guess spaces occupied by the clues
     */
    private void configTry(final Canvas canvas, final int row, 
            final int xPadding, final int tryNum,
            final int availArea, final int horizSpacing, final int vertSpacing,
            final int numberOfClueSpacesNeeded) {
        ShapeDrawable mDrawable;

        int x = xPadding + (availArea + horizSpacing)
                * (tryNum + numberOfClueSpacesNeeded);

        int y = availArea * (row + 1);
        y += vertSpacing * (row - 1);

        int width = availArea;
        int height = availArea;

        if (gameModel.hasTryColor(row, tryNum)) {
            mDrawable = new ShapeDrawable(new OvalShape());
            mDrawable.getPaint().setColor(Color.WHITE);
            mDrawable.setBounds(x, y, x + width, y + height);
            mDrawable.draw(canvas);
            x++;
            y++;
            width -= 2;
            height -= 2;
        }

        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(gameModel.getTryColorCode(row, tryNum));
        mDrawable.setBounds(x, y, x + width, y + height);
        mDrawable.draw(canvas);
    }

    /**
     * Setup the input images. These will provide the user with places on the
     * touch screen to use when selecting colors or functions
     * 
     * @param canvas
     *            The canvas to draw on
     * @param maxAvailArea
     *            The area available to a shape
     * @param top
     *            The vertical start position
     */
    private void configInput(final Canvas canvas, 
            final int maxAvailArea, final int top) {
        ShapeDrawable mDrawable;
        int color;
        int inputOption = 0;
        Shape shape;
        int trueAvailableArea;

        int availWidth = getWidth() / 8;
        int spacing = 2;

        runtimeInformation.put("ci top", "" + top);
        runtimeInformation.put("ci availArea initial", "" + maxAvailArea);
        runtimeInformation.put("ci availWidth initial", "" + availWidth);
        runtimeInformation.put("ci spacing", "" + spacing);

        availWidth -= spacing;

        runtimeInformation.put("ci availWidth final", "" + availWidth);

        trueAvailableArea = Math.min(maxAvailArea, availWidth);

        runtimeInformation.put("ci availArea final", "" + trueAvailableArea);

        int xCenterPadding = (getWidth() 
                - ((trueAvailableArea + spacing) * 8)) / 2;
        runtimeInformation.put("ci xCenterPadding initial", "" + spacing);
        if (xCenterPadding < 0) {
            xCenterPadding = 0;
        }
        runtimeInformation.put("ci xCenterPadding final", "" + spacing);

        shape = null;

        for (int button = 0; button < 8; ++button) {
            int x = xCenterPadding + ((trueAvailableArea + spacing) * button);
            int y = top;
            int width = trueAvailableArea;
            int height = trueAvailableArea;

            inputRange[inputOption][INDEX_ULX] = x;
            inputRange[inputOption][INDEX_ULY] = y;
            inputRange[inputOption][INDEX_LRX] = x + width;
            inputRange[inputOption][INDEX_LRY] = y + height;

            switch (button) {
                case 0:
                    color = Color.YELLOW;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        SequenceHuntGameModel.COLOR_YELLOW;
                    shape = new OvalShape();
                    break;
                case 1:
                    color = Color.RED;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        SequenceHuntGameModel.COLOR_RED;
                    shape = new OvalShape();
                    break;
                case 2:
                    color = Color.BLUE;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        SequenceHuntGameModel.COLOR_BLUE;
                    shape = new OvalShape();
                    break;
                case 3:
                    color = Color.GREEN;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        SequenceHuntGameModel.COLOR_GREEN;
                    shape = new OvalShape();
                    break;
                case 4:
                    color = Color.BLACK;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        SequenceHuntGameModel.COLOR_BLACK;
                    shape = new OvalShape();
                    break;
                case 5:
                    color = Color.WHITE;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        SequenceHuntGameModel.COLOR_WHITE;
                    shape = new OvalShape();
                    break;
                case 6:
                    color = Color.DKGRAY;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        INPUT_DELETE;
                    shape = new ArcShape(ICON_DELETE_ARC_START, 
                            ICON_DELETE_ARC_DEGREES);
                    break;
                case 7:
                    color = Color.DKGRAY;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        INPUT_SUBMIT_GUESS;
                    shape = new ArcShape(ICON_SUBMIT_TRY_ARC_START, 
                            ICON_SUBMIT_TRY_ARC_DEGREES);
                    break;
                default:
                    color = Color.LTGRAY;
                    inputRange[inputOption][INDEX_COLOR_CODE] = 
                        SequenceHuntGameModel.COLOR_YELLOW;
                    break;

            }
            ++inputOption;

            mDrawable = new ShapeDrawable(shape);
            mDrawable.getPaint().setColor(Color.WHITE);
            mDrawable.setBounds(x, y, x + width, y + height);
            mDrawable.draw(canvas);
            x++;
            y++;
            width -= 2;
            height -= 2;

            mDrawable = new ShapeDrawable(shape);
            mDrawable.getPaint().setColor(color);
            mDrawable.setBounds(x, y, x + width, y + height);
            mDrawable.draw(canvas);
        }
    }

    /**
     * Process input from the touch screen.
     * 
     * @param view The view receiving the touch event
     * @param event The touch event
     * 
     * @return Whether the event was processed
     */
    public final boolean onTouch(final View view, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            processLocation(event.getX(), event.getY());
            invalidate();
            return true;
        }

        return false;
    }

    /**
     * Determine whether the touch location matches the area of one of the input
     * shapes. If so, the input will be processed.
     * 
     * @param x
     *            The x location of the touch
     * @param y
     *            The y location of the touch
     */
    private void processLocation(final float x, final float y) {
        Integer theEvent = null;
        int ix;
        int iy;

        ix = (int) x;
        iy = (int) y;

        runtimeInformation.put("last touch x", "" + ix);
        runtimeInformation.put("last touch y", "" + iy);

        for (int findEvent = 0; findEvent < 8 
                && theEvent == null; ++findEvent) {
            if (inputRange[findEvent][INDEX_ULX] <= ix
                    && inputRange[findEvent][INDEX_ULY] <= iy
                    && inputRange[findEvent][INDEX_LRX] >= ix
                    && inputRange[findEvent][INDEX_LRY] >= iy) {
                theEvent = inputRange[findEvent][INDEX_COLOR_CODE];
            }
        }

        if (theEvent != null) {
            if (theEvent == INPUT_DELETE) {
                gameModel.removeLastGuess();
            } else if (theEvent == INPUT_SUBMIT_GUESS) {
                gameModel.submitGuess();
            } else {
                gameModel.addGuess(theEvent);
            }
        }
    }

    /**
     * Handle a color choice.
     * 
     * @param color The chosen color
     */
    @Override
    public final void notifyColorChoice(final int color) {
        gameModel.addGuess(color);
        invalidate();
    }

    /**
     * Handle a delete choice. (e.g. remove the last color entered)
     */
    @Override
    public final void notifyDeleteChoice() {
        gameModel.removeLastGuess();
        invalidate();
    }

    /**
     * Handle a try submit request.
     */
    @Override
    public final void notifyTry() {
        gameModel.submitGuess();
        invalidate();
    }
}
