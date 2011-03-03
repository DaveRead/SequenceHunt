package com.monead.games.android.sequence;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.monead.games.android.sequence.R;
import com.monead.games.android.sequence.model.SequenceHuntGameModel;
import com.monead.games.android.sequence.reporting.GameStatistics;
import com.monead.games.android.sequence.ui.SequenceGameBoard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
 * This is the activity class for the SequenceHunt game.
 * 
 * TODO Move all text to values TODO Allow user to change number of positions in
 * a sequence TODO add time limit feature
 * 
 * @author David Read
 * 
 */
public class Sequence extends Activity implements OnTouchListener {
	/**
	 * File used to save preferences
	 */
	private final static String PREFERENCES_FILE_NAME = "Sequence.preferences";

	/**
	 * File used to save an in-progress game state
	 */
	private final static String SERIALIZED_MODEL_FILE_NAME = "Sequence_Model.ser";

	/**
	 * File used to save game history
	 */
	private final static String SERIALIZED_GAME_STATISTICS_FILE_NAME = "Sequence_Game_Statistics.txt";

	/**
	 * Key for persisting the difficulty level choice
	 */
	private static final String PREF_MODE_HARD = "ModeHard";

	/**
	 * Key for persisting whether a current game state has been written to a
	 * local file
	 */
	private static final String PREF_USE_SAVED_MODEL = "UseSavedModel";

	/**
	 * Key for persisting whether this is the first use of this application
	 */
	private static final String PREF_FIRST_USE = "FirstUseFlagValue";

	/**
	 * A value that is stored as indicating first use If a major version change
	 * necessitates having the first use screen appear again to upgrade users
	 * then this value should be changed.
	 */
	private final static String FIRST_USE_FLAG_VALUE = "B";

	// Constants for the dialogs
	private static final int DIALOG_WIN = 1;
	private static final int DIALOG_LOSE = 2;
	private static final int DIALOG_STATS = 3;
	private static final int DIALOG_INFO = 4;
	private static final int DIALOG_ABOUT = 5;

	/**
	 * The game board view
	 */
	private SequenceGameBoard gameBoard;

	/**
	 * Track statistics about game operation
	 */
	private GameStatistics gameStatistics;

	/**
	 * Track whether the gameboard is in control
	 */
	private boolean gameBoardIsDisplayed;

	/**
	 * Program name retrieved from manifest
	 */
	private String programName;

	/**
	 * Program version retrieved from manifest
	 */
	private String programVersion;

	/**
	 * Class name used for logging
	 */
	private String className = this.getClass().getName();

	/**
	 * Life cycle method - called when program is first loaded
	 * 
	 * This will setup the gameboard and load any prior game in progress.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadGameStatistics();
		gameBoard = new SequenceGameBoard(this, gameStatistics);
		loadModel();
		setup();
		gameBoard.setOnTouchListener(this);

		if (firstUse()) {
			setupForFirstUse();
		} else {
			setContentView(gameBoard);
			gameBoardIsDisplayed = true;
		}
	}

	/**
	 * Display the first use screen
	 */
	private void setupForFirstUse() {
		setContentView(R.layout.first_use);
		gameBoardIsDisplayed = false;

		((Button) findViewById(R.id.button_read_instructions))
				.setOnClickListener(firstUseReadInstructionsClick);
		((Button) findViewById(R.id.button_play))
				.setOnClickListener(firstUsePlayClick);
	}

	/**
	 * Determine if this is the first use of this application If so, record the
	 * fact that first use has occurred
	 * 
	 * @return Whether the first use screen should appear
	 */
	private boolean firstUse() {
		boolean isFirstUse;

		SharedPreferences settings = getSharedPreferences(
				PREFERENCES_FILE_NAME, MODE_PRIVATE);

		isFirstUse = false;

		if (!settings.getString(PREF_FIRST_USE, "")
				.equals(FIRST_USE_FLAG_VALUE)) {
			isFirstUse = true;
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(PREF_FIRST_USE, FIRST_USE_FLAG_VALUE);
			editor.commit();
		}

		return isFirstUse;
	}

	/**
	 * Retrieve information about the application, including its name and
	 * version from the manifest. The method will also configure the game
	 * settings based on any stored preferences.
	 */
	private void setup() {
		SharedPreferences settings = getSharedPreferences(
				PREFERENCES_FILE_NAME, 0);

		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					"com.monead.games.android.sequence", 0);
			programName = getPackageManager().getApplicationLabel(
					getApplicationInfo()).toString();
			programVersion = pi.versionName;
		}
		catch (Throwable throwable) {
			Log.e(className,
					getResources().getString(
							R.string.errormessage_program_or_version_name),
					throwable);
			programName = getResources().getString(R.string.message_undefined);
			programVersion = getResources().getString(
					R.string.message_undefined);
		}

		Log.d(className,
				getResources().getString(
						R.string.message_report_program_and_version_names)
						+ ": " + programName + ", " + programVersion);

		gameBoard.setDifficultyToHard(settings
				.getBoolean(PREF_MODE_HARD, false));
	}

	/**
	 * Switch the game play difficulty level
	 * 
	 * @param difficultyHard
	 *            Game play will be harder if true
	 */
	private void setDifficultyToHard(boolean difficultyHard) {
		gameBoard.setDifficultyToHard(difficultyHard);

		SharedPreferences settings = getSharedPreferences(
				PREFERENCES_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREF_MODE_HARD, difficultyHard);
		editor.commit();
	}

	/**
	 * Life cycle method - called when activity loses focus
	 * 
	 * Store the current state of the game so that it can be restored when the
	 * user returns.
	 */
	@Override
	protected void onPause() {
		super.onPause();

		saveModel();
		saveGameStatistics();
	}

	/**
	 * Life cycle method - called when the activity regains focus
	 * 
	 * Restore the previous state of the game.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		loadModel();
	}

	/**
	 * Presents the setup screen to the user
	 * 
	 * This screen allows the user to set preferences related to game play.
	 */
	private void showSetupScreen() {
		setContentView(R.layout.setup);
		gameBoardIsDisplayed = false;

		RadioButton easy = (RadioButton) findViewById(R.id.radio_easy);
		RadioButton hard = (RadioButton) findViewById(R.id.radio_hard);
		((Button) findViewById(R.id.button_save))
				.setOnClickListener(setupSaveClick);
		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(setupCancelClick);

		if (gameBoard.isDifficultySetToHard()) {
			hard.setChecked(true);
		} else {
			easy.setChecked(true);
		}
	}

	/**
	 * Presents the help screen to the user.
	 * 
	 * TODO Internationalize the instructions
	 * 
	 * This screen contains instructions for game play and operation.
	 */
	private void showHelpScreen() {
		setContentView(R.layout.help);
		gameBoardIsDisplayed = false;

		WebView instructions = (WebView) findViewById(R.id.instructions);
		instructions.loadUrl("file:///android_asset/help/instructions.html");

		((Button) findViewById(R.id.button_close))
				.setOnClickListener(helpDoneClick);
	}

	/**
	 * Presents the license screen to the user.
	 * 
	 * TODO Are there internationalized versions of the Afferno license text?
	 * 
	 * This screen displays the software license
	 */
	private void showLicenseScreen() {
		setContentView(R.layout.license);
		gameBoardIsDisplayed = false;

		WebView instructions = (WebView) findViewById(R.id.license);
		instructions.loadUrl("file:///android_asset/license/license.html");
	}

	/**
	 * Saves the current game state to a file on the device.
	 */
	private void saveModel() {
		ObjectOutputStream out = null;

		SharedPreferences settings = getSharedPreferences(
				PREFERENCES_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREF_USE_SAVED_MODEL, false);
		editor.commit();

		try {
			out = new ObjectOutputStream(openFileOutput(
					SERIALIZED_MODEL_FILE_NAME, MODE_PRIVATE));
			out.writeObject(gameBoard.getModel());
			editor.putBoolean(PREF_USE_SAVED_MODEL, true);
			editor.commit();
		}
		catch (Throwable throwable) {
			Log.e(className,
					getResources().getString(
							R.string.errormessage_model_write_failed),
					throwable);
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (Throwable throwable) {
					Log.e(className,
							getResources()
									.getString(
											R.string.errormessage_model_output_file_close_failed),
							throwable);
				}
			}
		}
	}

	/**
	 * Retrieves a saved game state from a file on the device.
	 */
	private void loadModel() {
		ObjectInputStream in = null;

		SharedPreferences settings = getSharedPreferences(
				PREFERENCES_FILE_NAME, 0);

		if (settings.getBoolean(PREF_USE_SAVED_MODEL, false)) {
			try {
				in = new ObjectInputStream(
						openFileInput(SERIALIZED_MODEL_FILE_NAME));
				SequenceHuntGameModel model = (SequenceHuntGameModel) in
						.readObject();
				if (!model.isWinner() && !model.isLoser()) {
					gameBoard.setModel(model);
				}
			}
			catch (Throwable throwable) {
				Log.w(className,
						getResources().getString(
								R.string.errormessage_model_read_failed),
						throwable);
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (Throwable throwable) {
						Log.e(className,
								getResources()
										.getString(
												R.string.errormessage_model_input_file_close_failed),
								throwable);
					}
				}
			}
		}
	}

	/**
	 * Saves the game statistics to a file on the device.
	 */
	private void saveGameStatistics() {
		ObjectOutputStream out = null;

		try {
			out = new ObjectOutputStream(openFileOutput(
					SERIALIZED_GAME_STATISTICS_FILE_NAME, MODE_PRIVATE));
			out.writeObject(gameStatistics);
		}
		catch (Throwable throwable) {
			Log.e(className,
					getResources().getString(
							R.string.errormessage_stats_write_failed),
					throwable);
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (Throwable throwable) {
					Log.e(className,
							getResources()
									.getString(
											R.string.errormessage_stats_output_file_close_failed),
							throwable);
				}
			}
		}
	}

	/**
	 * Retrieves the saved game statistics from a file on the device.
	 */
	private void loadGameStatistics() {
		ObjectInputStream in = null;

		try {
			in = new ObjectInputStream(
					openFileInput(SERIALIZED_GAME_STATISTICS_FILE_NAME));
			gameStatistics = (GameStatistics) in.readObject();
		}
		catch (Throwable throwable) {
			Log.w(className,
					getResources().getString(
							R.string.errormessage_stats_read_failed), throwable);
			gameStatistics = new GameStatistics();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Throwable throwable) {
					Log.e(className,
							getResources()
									.getString(
											R.string.errormessage_stats_input_file_close_failed),
							throwable);
				}
			}
		}
	}

	/**
	 * Retrieves the application's menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_menu, menu);
		return true;
	}

	/**
	 * Processes the user's selection of a menu item
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.how_to_play:
				showHelpScreen();
				return true;
			case R.id.setup:
				showSetupScreen();
				return true;
			case R.id.new_game:
				startNewGame();
				return true;
			case R.id.quit:
				quit();
				return true;
			case R.id.about:
				showAboutDialog();
				return true;
			case R.id.license:
				showLicenseScreen();
				return true;
			case R.id.stats:
				showStatisticsDialog();
				return true;
			case R.id.history:
				showHistory();
				return true;
			case R.id.debug:
				showDebugInfoDialog();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Start a new game, replacing the current game state
	 */
	private void startNewGame() {
		gameBoard.newGame();
	}

	/**
	 * Leave the game
	 * 
	 * The game state will not be lost
	 */
	private void quit() {
		Sequence.this.finish();
	}

	/**
	 * Show internal statistics about the game's operation
	 */
	private void showStatisticsDialog() {
		showDialog(DIALOG_STATS);
	}

	/**
	 * Show high-level information about the game
	 */
	private void showAboutDialog() {
		showDialog(DIALOG_ABOUT);
	}

	/**
	 * Display the list of historical color choices
	 * 
	 * This may be used to collect data in order to test the randomness of the
	 * internal random number generator
	 */
	private void showHistory() {
		setContentView(R.layout.history);
		gameBoardIsDisplayed = false;

		TextView history = (TextView) findViewById(R.id.history);
		((Button) findViewById(R.id.button_clipboard))
				.setOnClickListener(historyClipboardClick);
		history.setText(getResources().getString(R.string.label_version) + ": "
				+ programVersion + "\n" + gameStatistics.reportHistoryCSV());
	}

	/**
	 * Show low-level operational data
	 */
	private void showDebugInfoDialog() {
		showDialog(DIALOG_INFO);
	}

	/**
	 * Handle the creation of a dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		Dialog dialog;

		switch (id) {
			case DIALOG_WIN:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(
						getResources().getString(R.string.message_win)
								+ "\n\n"
								+ getResources().getString(
										R.string.question_play_again))
						.setCancelable(false)
						.setPositiveButton(
								getResources().getString(R.string.button_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Sequence.this.startNewGame();
									}
								})
						.setNegativeButton(
								getResources().getString(R.string.button_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Sequence.this.finish();
									}
								});
				dialog = builder.create();
				break;
			case DIALOG_LOSE:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(
						getResources().getString(R.string.message_lose)
								+ "\n\n"
								+ getResources().getString(
										R.string.question_play_again))
						.setCancelable(false)
						.setPositiveButton(
								getResources().getString(R.string.button_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Sequence.this.startNewGame();
									}
								})
						.setNegativeButton(
								getResources().getString(R.string.button_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Sequence.this.finish();
									}
								});
				dialog = builder.create();
				break;
			case DIALOG_ABOUT:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(
						programName
								+ "\n"
								+ getResources().getString(
										R.string.label_version)
								+ ": "
								+ programVersion
								+ "\n\nDavid Read\nDavid.Read@monead.com\nwww.monead.com\n\nThe source code is located at: https://github.com/DaveRead/SequenceHunt\n\nThis program is free software released under the GNU Affero General Public License.  See the License menu option for the full license.")
						.setCancelable(true)
						.setNegativeButton(
								getResources().getString(R.string.button_close),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			case DIALOG_STATS:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(
						getResources().getString(
								R.string.label_color_statistics))
						.setCancelable(true)
						.setNeutralButton(
								getResources().getString(R.string.button_close),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			case DIALOG_INFO:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(
						getResources().getString(
								R.string.label_runtime_information))
						.setCancelable(true)
						.setNeutralButton(
								getResources().getString(R.string.button_close),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			default:
				dialog = null;
		}

		return dialog;
	}

	/**
	 * For dialogs whose values are not static, handle the imminent presentation
	 * of the dialog
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
		switch (id) {
			case DIALOG_LOSE:
				((AlertDialog) dialog).setMessage(getResources().getString(
						R.string.message_lose)
						+ "\n"
						+ getResources()
								.getString(R.string.message_pattern_was)
						+ ":\n"
						+ gameBoard.getModel().getAnswerText(this)
						+ "\n\n"
						+ getResources()
								.getString(R.string.question_play_again));
				break;
			case DIALOG_STATS:
				((AlertDialog) dialog)
						.setMessage(getResources().getString(
								R.string.label_color_statistics)
								+ "\n\n"
								+ gameBoard.getModel().reportColorCounts(this));
				break;
			case DIALOG_INFO:
				StringBuffer info;
				info = new StringBuffer();
				for (String detail : gameBoard.getRuntimeInformation()) {
					info.append(detail);
					info.append('\n');
				}
				((AlertDialog) dialog).setMessage(getResources().getString(
						R.string.label_runtime_information)
						+ "\n\n" + info.toString());
				break;
		}
	}

	/**
	 * Handle touch screen interaction
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (v instanceof OnTouchListener) {
			Bundle args = new Bundle();
			args.putString("XY",
					"(" + (int) event.getX() + "," + (int) event.getY() + ")");
			args.putString("Precision", "(" + event.getXPrecision() + ","
					+ event.getYPrecision() + ")");
			args.putString("Raw", "(" + (int) event.getRawX() + ","
					+ (int) event.getRawY() + ")");

			Log.d(className,
					"XY (" + (int) event.getX() + "," + (int) event.getY()
							+ ")");
			Log.d(className, "Precision (" + event.getXPrecision() + ","
					+ event.getYPrecision() + ")");
			Log.d(className, "Raw (" + (int) event.getRawX() + ","
					+ (int) event.getRawY() + ")");
			// showDialog(DIALOG_ALERT, args);
			((OnTouchListener) v).onTouch(v, event);
			if (gameBoard.getModel().isWinner()) {
				showDialog(DIALOG_WIN);
			} else if (gameBoard.getModel().isLoser()) {
				showDialog(DIALOG_LOSE);
			}
			return true;
		}

		return false;
	}

	/**
	 * Handle keyboard input
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_ENTER:
				gameBoard.notifyTry();
				break;
			case KeyEvent.KEYCODE_DEL:
				gameBoard.notifyDeleteChoice();
				break;
			case KeyEvent.KEYCODE_Y:
				gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_YELLOW);
				break;
			case KeyEvent.KEYCODE_R:
				gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_RED);
				break;
			case KeyEvent.KEYCODE_B:
				gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_BLUE);
				break;
			case KeyEvent.KEYCODE_G:
				gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_GREEN);
				break;
			case KeyEvent.KEYCODE_K:
				gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_BLACK);
				break;
			case KeyEvent.KEYCODE_W:
				gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_WHITE);
				break;
			default:
				return super.onKeyDown(keyCode, event);
		}

		if (gameBoard.getModel().isWinner()) {
			showDialog(DIALOG_WIN);
		} else if (gameBoard.getModel().isLoser()) {
			showDialog(DIALOG_LOSE);
		}

		return true;
	}

	/**
	 * Handle back button operation
	 */
	@Override
	public void onBackPressed() {
		if (gameBoardIsDisplayed) {
			Sequence.this.finish();
		} else {
			setContentView(gameBoard);
			gameBoardIsDisplayed = true;
		}
	}

	/**
	 * Inner class to process setup choice persistence request
	 */
	private OnClickListener setupSaveClick = new OnClickListener() {
		public void onClick(View v) {
			RadioButton hard = (RadioButton) findViewById(R.id.radio_hard);
			setDifficultyToHard(hard.isChecked());
			setContentView(gameBoard);
			gameBoardIsDisplayed = true;
		}
	};

	/**
	 * Inner class to handle cancel of setup choice changes
	 */
	private OnClickListener setupCancelClick = new OnClickListener() {
		public void onClick(View v) {
			setContentView(gameBoard);
			gameBoardIsDisplayed = true;
		}
	};

	/**
	 * Inner class to handle user dismissal of help screen
	 */
	private OnClickListener helpDoneClick = new OnClickListener() {
		public void onClick(View v) {
			setContentView(gameBoard);
			gameBoardIsDisplayed = true;
		}
	};

	/**
	 * Inner class to handle copy of history to clipboard
	 */
	private OnClickListener historyClipboardClick = new OnClickListener() {
		public void onClick(View v) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(gameStatistics.reportHistoryCSV());
		}
	};

	/**
	 * Inner class to handle first use read instructions request
	 */
	private OnClickListener firstUseReadInstructionsClick = new OnClickListener() {
		public void onClick(View v) {
			showHelpScreen();
		}
	};

	/**
	 * Inner class to handle first use play game request
	 */
	private OnClickListener firstUsePlayClick = new OnClickListener() {
		public void onClick(View v) {
			setContentView(gameBoard);
			gameBoardIsDisplayed = true;
		}
	};
}