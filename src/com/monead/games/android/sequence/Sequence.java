package com.monead.games.android.sequence;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import com.monead.games.android.sequence.R;
import com.monead.games.android.sequence.model.SequenceHuntGameModel;
import com.monead.games.android.sequence.reporting.GameStatistics;
import com.monead.games.android.sequence.reporting.GameStatisticsEngine;
import com.monead.games.android.sequence.ui.SequenceGameBoard;
import com.monead.games.android.sequence.util.Formatter;
import com.monead.games.android.sequence.util.KeyCodeConverter;
import com.monead.games.android.sequence.sound.SoundManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.ClipboardManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

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
 * TODO Move all text to values TODO add time limit feature
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
	 * Key for persisting the sequence length choice
	 */
	private static final String PREF_SEQUENCE_LENGTH = "SequenceLength";

	/**
	 * Key for persisting whether a current game state has been written to a
	 * local file
	 */
	private static final String PREF_USE_SAVED_MODEL = "UseSavedModel";

	/**
	 * Key for persisting whether game sounds are enabled
	 */
	private static final String PREF_SOUND_ENABLED = "SoundEnabled";
	
	/**
	 * Key for persisting whether this is the first use of this application
	 */
	private static final String PREF_FIRST_USE = "FirstUseFlagValue";

	/**
	 * A value that is stored as indicating first use If a major version change
	 * necessitates having the first use screen appear again to upgrade users
	 * then this value should be changed.
	 */
	private final static String FIRST_USE_FLAG_VALUE = "C";

	// Constants for the dialogs
	private static final int DIALOG_WIN = 1;
	private static final int DIALOG_LOSE = 2;
	private static final int DIALOG_STATS = 3;
	private static final int DIALOG_INFO = 4;
	private static final int DIALOG_ABOUT = 5;

	// Constants for sounds
	//private static final int SOUND_NEW_GAME = 0;
	//private static final int SOUND_ENTER_COLOR = 1;
	//private static final int SOUND_BACKOUT_COLOR = 2;
	//private static final int SOUND_ENTER_TRY = 3;
	//private static final int SOUND_FEWER_CORRECT = 4;
	//private static final int SOUND_WIN = 5;
	//private static final int SOUND_LOSE = 6;
	
	/**
	 * The game board view
	 */
	private SequenceGameBoard gameBoard;

	/**
	 * Track statistics about game operation
	 */
	private GameStatisticsEngine gameStatistics;

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
		gameBoard = new SequenceGameBoard(this, gameStatistics,
				getSharedPreferences(PREFERENCES_FILE_NAME, MODE_PRIVATE).getInt(
						PREF_SEQUENCE_LENGTH,
						SequenceHuntGameModel.DEFAULT_SEQUENCE_LENGTH));
		loadModel();
		setup();
		gameBoard.setOnTouchListener(this);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		if (firstUse()) {
			Log.d(className, "Need to display first use screen");
			setupForFirstUse();
		} else {
			displayGameboard();
			// setContentView(gameBoard);
			// gameBoardIsDisplayed = true;
		}
	}

	/**
	 * Display the gameboard
	 */
	private void displayGameboard() {
		setContentView(gameBoard);
		gameBoardIsDisplayed = true;
		gameBoard.getModel().signalGameRestored();
	}

	/**
	 * Flag that the gameboard is not currently displayed
	 */
	private void setGameBoardNotVisible() {
		gameBoard.getModel().signalGamePaused();
		gameBoardIsDisplayed = false;
	}

	/**
	 * Display the first use screen
	 */
	private void setupForFirstUse() {
		Log.d(className, "Displaying first use screen");
		setContentView(R.layout.first_use);
		Log.d(className, "First use screen set as view");

		setGameBoardNotVisible();
		// gameBoardIsDisplayed = false;

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
				PREFERENCES_FILE_NAME, MODE_PRIVATE);

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
		
		SoundManager.getInstance().setSoundEnabled(settings.getBoolean(PREF_SOUND_ENABLED, true));
		
		setupSounds();
	}
	
	/**
	 * Setup the sound effects used in the game
	 */
	private void setupSounds() {	
		SoundManager.getInstance().setContext(this);

		SoundManager.getInstance().addSound(R.raw.backout);
		SoundManager.getInstance().addSound(R.raw.guess);
		SoundManager.getInstance().addSound(R.raw.entry);
		SoundManager.getInstance().addSound(R.raw.fewercorrect);
		SoundManager.getInstance().addSound(R.raw.lose);
		SoundManager.getInstance().addSound(R.raw.newgame);
		SoundManager.getInstance().addSound(R.raw.win);
	}
	
	/**
	 * Switch the game play difficulty level
	 * 
	 * @param difficultyHard
	 *            Game play will be harder if true
	 */
	private void setDifficultyToHard(boolean difficultyHard) {
		SharedPreferences settings = getSharedPreferences(
				PREFERENCES_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREF_MODE_HARD, difficultyHard);
		editor.commit();

		gameBoard.setDifficultyToHard(difficultyHard);
	}
	
	/**
	 * Set the sound configuration
	 * 
	 * @param enabled Whether sounds should be played
	 */
	private void setSound(boolean enabled) {
		SharedPreferences settings = getSharedPreferences(
				PREFERENCES_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREF_SOUND_ENABLED, enabled);
		editor.commit();

		SoundManager.getInstance().setSoundEnabled(enabled);
	}

	/**
	 * Set the length for the sequence
	 * 
	 * @param sequenceLength The sequence length
	 */
	private void setSequenceLength(int sequenceLength) {
		SharedPreferences settings = getSharedPreferences(
				PREFERENCES_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PREF_SEQUENCE_LENGTH, sequenceLength);
		editor.commit();

		gameBoard.setSequenceLength(sequenceLength);
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

		setGameBoardNotVisible();
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
		int selectedLengthPosition;

		setContentView(R.layout.setup);
		setGameBoardNotVisible();
		// gameBoardIsDisplayed = false;

		RadioButton easy = (RadioButton) findViewById(R.id.radio_easy);
		RadioButton hard = (RadioButton) findViewById(R.id.radio_hard);
		RadioButton rbSoundOn = (RadioButton) findViewById(R.id.sound_on);
		RadioButton rbSoundOff = (RadioButton) findViewById(R.id.sound_off);
		((Button) findViewById(R.id.button_save))
				.setOnClickListener(setupSaveClick);
		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(setupCancelClick);
		Spinner spinnerSequenceLength = (Spinner) findViewById(R.id.spinner_sequence_length);

		ArrayAdapter<CharSequence> adapterSequenceLengthOptions = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		adapterSequenceLengthOptions
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSequenceLength.setAdapter(adapterSequenceLengthOptions);

		selectedLengthPosition = 0;
		for (int position = 0, length = SequenceHuntGameModel.MINIMUM_SEQUENCE_LENGTH; length <= SequenceHuntGameModel.MAXIMUM_SEQUENCE_LENGTH; ++length, position++) {
			adapterSequenceLengthOptions.add(length + "");
			if (gameBoard.getSequenceLength() == length) {
				selectedLengthPosition = position;
			}
		}

		if (gameBoard.isDifficultySetToHard()) {
			hard.setChecked(true);
		} else {
			easy.setChecked(true);
		}

		if (SoundManager.getInstance().isSoundEnabled()) {
			rbSoundOn.setChecked(true);
		} else {
			rbSoundOff.setChecked(true);
		}
		
		spinnerSequenceLength.setSelection(selectedLengthPosition);
	}

	/**
	 * Presents the help screen to the user.
	 * 
	 * TODO Internationalize the instructions Locale.getDefault().getLanguage()
	 * 
	 * This screen contains instructions for game play and operation.
	 */
	private void showHelpScreen() {
		String fileUrl;

		setContentView(R.layout.help);
		setGameBoardNotVisible();
		// gameBoardIsDisplayed = false;

		fileUrl = "file:///android_asset/help/";
		fileUrl += Locale.getDefault().getLanguage();
		fileUrl += "/instructions.html";

		// Test for existence of the internationalized file
		// if (!getFileStreamPath("android_asset/" +
		// Locale.getDefault().getLanguage() +
		// "/help/instructions.html").exists()) {
		if (!hasFiles("help/" + Locale.getDefault().getLanguage())) {
			// If it doesn't exist, use default file
			fileUrl = "file:///android_asset/help/instructions.html";
		}

		WebView instructions = (WebView) findViewById(R.id.instructions);
		instructions.loadUrl(fileUrl);

		((Button) findViewById(R.id.button_close))
				.setOnClickListener(helpDoneClick);
	}

	/**
	 * Check if a directory in the assets tree exists Test for existance is
	 * whether any files are found. So, this method will return false for empty
	 * directories.
	 * 
	 * @param directoryName
	 *            The directory to look for
	 * 
	 * @return True if the directory exists and contains files
	 */
	private boolean hasFiles(String directoryName) {
		String files[];

		AssetManager am = getAssets();
		try {
			files = am.list(directoryName);
		}
		catch (Throwable throwable) {
			Log.w(className,
					"Cannot get asset file list for: " + directoryName,
					throwable);
			files = new String[0];
		}

		return files.length > 0;
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
		setGameBoardNotVisible();
		// gameBoardIsDisplayed = false;

		WebView instructions = (WebView) findViewById(R.id.license);
		instructions.loadUrl("file:///android_asset/license/license.html");
	}

	/**
	 * Saves the current game state to a file on the device.
	 */
	private void saveModel() {
		ObjectOutputStream out = null;

		// gameBoard.getModel().signalGamePaused();
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
				PREFERENCES_FILE_NAME, MODE_PRIVATE);

		if (settings.getBoolean(PREF_USE_SAVED_MODEL, false)) {
			try {
				in = new ObjectInputStream(
						openFileInput(SERIALIZED_MODEL_FILE_NAME));
				SequenceHuntGameModel model = (SequenceHuntGameModel) in
						.readObject();
				if (!model.isWinner() && !model.isLoser()) {
					gameBoard.setModel(model);
					// gameBoard.getModel().signalGameRestored();
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
			gameStatistics = (GameStatisticsEngine) in.readObject();
		}
		catch (Throwable throwable) {
			Log.w(className,
					getResources().getString(
							R.string.errormessage_stats_read_failed), throwable);
			gameStatistics = new GameStatisticsEngine();
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
		SequenceHuntGameModel model = gameBoard.getModel();
		if (model != null && !model.isLoser() && !model.isWinner()) {
			gameStatistics.addGame(gameBoard.getModel(),
					gameBoard.isDifficultySetToHard(), "New");
		}

		gameBoard.newGame();
		SoundManager.getInstance().play(R.raw.newgame);
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
		setGameBoardNotVisible();
		// gameBoardIsDisplayed = false;

		TextView history = (TextView) findViewById(R.id.history);
		((Button) findViewById(R.id.button_clipboard))
				.setOnClickListener(historyClipboardClick);
		history.setText(getResources().getString(R.string.label_version) + ": "
				+ programVersion + "\n" + "Android: "
				+ android.os.Build.VERSION.SDK_INT + "\n"
				+ gameStatistics.reportHistoryCSV());
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
								+ "\n\n"
								+ getResources().getString(
										R.string.message_about))
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
						getResources().getString(R.string.label_stats_title))
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
	 * 
	 * This call is included to backward compatibility. The method is
	 * deprecated, but for pre-API Level 8 (e.g. pre Android 2.2) installs it is
	 * this method that will be called.
	 * 
	 * The method simply calls the replacement version, passing null for the new
	 * "Bundle" parameter.
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (android.os.Build.VERSION.SDK_INT < 8) {
			onPrepareDialog(id, dialog, null);
		}
	}

	/**
	 * For dialogs whose values are not static, handle the imminent presentation
	 * of the dialog
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
		switch (id) {
			case DIALOG_WIN:
				SoundManager.getInstance().play(R.raw.win);
				
				((AlertDialog) dialog).setMessage(getResources().getString(
						R.string.message_win)
						+ "\n\n"
						+ String.format(
								getResources().getString(
										R.string.message_playing_time),
								Formatter.getInstance().formatTimer(
										gameBoard.getModel().getElapsedTime()))
						+ "\n\n"
						+ getResources()
								.getString(R.string.question_play_again));
				break;
			case DIALOG_LOSE:
				SoundManager.getInstance().play(R.raw.lose);
				((AlertDialog) dialog).setMessage(getResources().getString(
						R.string.message_lose)
						+ "\n"
						+ getResources()
								.getString(R.string.message_pattern_was)
						+ ":\n"
						+ gameBoard.getModel().getAnswerText(this)
						+ "\n\n"
						+ String.format(
								getResources().getString(
										R.string.message_playing_time),
								Formatter.getInstance().formatTimer(
										gameBoard.getModel().getElapsedTime()))
						+ "\n\n"
						+ getResources()
								.getString(R.string.question_play_again));
				break;
			case DIALOG_STATS:
				((AlertDialog) dialog).setMessage(getResources().getString(
						R.string.label_stats_title)
						+ "\n\n"
						// + gameBoard.getModel().reportColorCounts(this)
						+ reportStatistics());

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
	 * Return a message containing game statistics
	 * 
	 * @return A text message with game statistics
	 */
	public String reportStatistics() {
		GameStatistics statistics;
		String errorMsg;

		statistics = gameStatistics.getGameStatistics();

		StringBuffer report = new StringBuffer();

		errorMsg = statistics.getStatsError();

		if (errorMsg != null && errorMsg.length() > 0) {
			report.append(getResources().getString(R.string.label_stats_error));
			report.append(": ");
			report.append(errorMsg);
		} else {
			report.append(getResources().getString(
					R.string.label_stats_num_games));
			report.append(": ");
			report.append(statistics.getNumGamesStored());
			report.append("\n");
			report.append(getResources()
					.getString(R.string.label_stats_num_won));
			report.append(": ");
			report.append(statistics.getNumWins());
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_num_lost));
			report.append(": ");
			report.append(statistics.getNumLosses());
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_num_quit));
			report.append(": ");
			report.append(statistics.getNumQuits());
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_num_easy));
			report.append(": ");
			report.append(statistics.getNumEasy());
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_num_hard));
			report.append(": ");
			report.append(statistics.getNumHard());
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_avg_tries_per_game));
			report.append(": ");
			report.append(statistics.getAverageTries());
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_avg_time_per_game_won));
			report.append(": ");
			report.append(Formatter.getInstance().formatTimer(
					statistics.getAverageWinTimeMs()));
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_avg_time_per_game_lost));
			report.append(": ");
			report.append(Formatter.getInstance().formatTimer(
					statistics.getAverageLoseTimeMs()));
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_total_tries));
			report.append(": ");
			report.append(statistics.getTotalTries());
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_total_time_games_won));
			report.append(": ");
			report.append(Formatter.getInstance().formatTimer(
					statistics.getTotalWinTimeMs()));
			report.append("\n");
			report.append(getResources().getString(
					R.string.label_stats_total_time_games_lost));
			report.append(": ");
			report.append(Formatter.getInstance().formatTimer(
					statistics.getTotalLostTimeMs()));
			report.append("\n");

			report.append("\n");

			report.append(getResources().getString(R.string.label_stats_colors));

			report.append("\n");

			report.append(getResources().getString(R.string.color_black)
					+ ": "
					+ statistics
							.getColorCount(SequenceHuntGameModel.COLOR_BLACK));
			report.append('\n');
			report.append(getResources().getString(R.string.color_blue)
					+ ": "
					+ statistics
							.getColorCount(SequenceHuntGameModel.COLOR_BLUE));
			report.append('\n');
			report.append(getResources().getString(R.string.color_green)
					+ ": "
					+ statistics
							.getColorCount(SequenceHuntGameModel.COLOR_GREEN));
			report.append('\n');
			report.append(getResources().getString(R.string.color_red) + ": "
					+ statistics.getColorCount(SequenceHuntGameModel.COLOR_RED));
			report.append('\n');
			report.append(getResources().getString(R.string.color_white)
					+ ": "
					+ statistics
							.getColorCount(SequenceHuntGameModel.COLOR_WHITE));
			report.append('\n');
			report.append(getResources().getString(R.string.color_yellow)
					+ ": "
					+ statistics
							.getColorCount(SequenceHuntGameModel.COLOR_YELLOW));
		}

		return report.toString();
	}

	/**
	 * Handle touch screen interaction
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean processed;

		if (v instanceof OnTouchListener) {
			/*
			 * Bundle args = new Bundle(); args.putString("XY", "(" + (int)
			 * event.getX() + "," + (int) event.getY() + ")");
			 * args.putString("Precision", "(" + event.getXPrecision() + "," +
			 * event.getYPrecision() + ")"); args.putString("Raw", "(" + (int)
			 * event.getRawX() + "," + (int) event.getRawY() + ")");
			 * 
			 * Log.d(className, "XY (" + (int) event.getX() + "," + (int)
			 * event.getY() + ")"); Log.d(className, "Precision (" +
			 * event.getXPrecision() + "," + event.getYPrecision() + ")");
			 * Log.d(className, "Raw (" + (int) event.getRawX() + "," + (int)
			 * event.getRawY() + ")");
			 */
			// showDialog(DIALOG_ALERT, args);
			Log.d(className, "Screen touch detected, process");
			processed = ((OnTouchListener) v).onTouch(v, event);

			if (processed) {
				if (gameBoard.getModel().isWinner()) {
					Log.d(className, "Screen touch processed, winner detected");
					gameStatistics.addGame(gameBoard.isDifficultySetToHard(),
							gameBoard.getModel());
					showDialog(DIALOG_WIN);
				} else if (gameBoard.getModel().isLoser()) {
					Log.d(className, "Screen touch processed, loser detected");
					gameStatistics.addGame(gameBoard.isDifficultySetToHard(),
							gameBoard.getModel());
					showDialog(DIALOG_LOSE);
				//} else if (gameBoard.getModel().getTryProgress() < 0) {
					//SoundManager.getInstance().play(R.raw.fewercorrect);
				}
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
		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			gameBoard.notifyTry();
			if (gameBoard.getModel().isWinner()) {
				gameStatistics.addGame(gameBoard.isDifficultySetToHard(),
						gameBoard.getModel());
				showDialog(DIALOG_WIN);
			} else if (gameBoard.getModel().isLoser()) {
				gameStatistics.addGame(gameBoard.isDifficultySetToHard(),
						gameBoard.getModel());
				showDialog(DIALOG_LOSE);
			//} else if (gameBoard.getModel().getTryProgress() < 0) {
				//SoundManager.getInstance().play(R.raw.fewercorrect);
			}
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			gameBoard.notifyDeleteChoice();
		} else if (keyCode == KeyCodeConverter.getKeyCode(getResources()
				.getString(R.string.key_black).charAt(0))) {
			gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_BLACK);
		} else if (keyCode == KeyCodeConverter.getKeyCode(getResources()
				.getString(R.string.key_blue).charAt(0))) {
			gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_BLUE);
		} else if (keyCode == KeyCodeConverter.getKeyCode(getResources()
				.getString(R.string.key_green).charAt(0))) {
			gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_GREEN);
		} else if (keyCode == KeyCodeConverter.getKeyCode(getResources()
				.getString(R.string.key_red).charAt(0))) {
			gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_RED);
		} else if (keyCode == KeyCodeConverter.getKeyCode(getResources()
				.getString(R.string.key_white).charAt(0))) {
			gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_WHITE);
		} else if (keyCode == KeyCodeConverter.getKeyCode(getResources()
				.getString(R.string.key_yellow).charAt(0))) {
			gameBoard.notifyColorChoice(SequenceHuntGameModel.COLOR_YELLOW);
		} else {
			return super.onKeyDown(keyCode, event);
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
			displayGameboard();
			// setContentView(gameBoard);
			// gameBoardIsDisplayed = true;
		}
	}

	/**
	 * Inner class to process setup choice persistence request
	 */
	private OnClickListener setupSaveClick = new OnClickListener() {
		public void onClick(View v) {
			RadioButton hard = (RadioButton) findViewById(R.id.radio_hard);
			RadioButton rbSoundOn = (RadioButton) findViewById(R.id.sound_on);
			Spinner spinnerSequenceLength = (Spinner) findViewById(R.id.spinner_sequence_length);
			try {
				setSequenceLength(Integer.parseInt(spinnerSequenceLength
						.getSelectedItem().toString()));
			}
			catch (Throwable throwable) {
				Log.e(className, "Unable to set new sequence length: "
						+ spinnerSequenceLength.getSelectedItem().toString(),
						throwable);
			}
			setDifficultyToHard(hard.isChecked());
			setSound(rbSoundOn.isChecked());

			displayGameboard();
			// setContentView(gameBoard);
			// gameBoardIsDisplayed = true;
		}
	};

	/**
	 * Inner class to handle cancel of setup choice changes
	 */
	private OnClickListener setupCancelClick = new OnClickListener() {
		public void onClick(View v) {
			displayGameboard();
			// setContentView(gameBoard);
			// gameBoardIsDisplayed = true;
		}
	};

	/**
	 * Inner class to handle user dismissal of help screen
	 */
	private OnClickListener helpDoneClick = new OnClickListener() {
		public void onClick(View v) {
			displayGameboard();
			// setContentView(gameBoard);
			// gameBoardIsDisplayed = true;
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
			displayGameboard();
			// setContentView(gameBoard);
			// gameBoardIsDisplayed = true;
		}
	};
}