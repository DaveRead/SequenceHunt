package com.monead.games.android.sequence.sound;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Handles all sound interactions for the game
 * 
 * @author David Read
 * 
 */
public class SoundManager {
	/**
	 * Singleton instance
	 */
	private static SoundManager instance;

	/**
	 * The context for the sounds
	 */
	private Context context;

	/**
	 * Whether sounds should be played
	 */
	private boolean soundEnabled;

	/**
	 * Sound pool for game sounds
	 */
	SoundPool sounds;

	/**
	 * Relates the sound constants to the SoundPool ids
	 */
	Map<Integer, Integer> soundMap;

	/**
	 * Constructs an instance and defines the sound pool and map
	 */
	private SoundManager() {
		sounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundMap = new HashMap<Integer, Integer>();
	}

	/**
	 * Get the instance
	 * 
	 * @return The sound manager instance
	 */
	public static synchronized SoundManager getInstance() {
		if (instance == null) {
			instance = new SoundManager();
		}

		return instance;
	}

	/**
	 * Set the context. This must be called before any sounds can be added.
	 * 
	 * @param context
	 *            The context for sound file access
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Add a sound to the pool of sounds
	 * 
	 * @param resourceId
	 *            The resource id for the sound file from the context
	 */
	public void addSound(int resourceId) {
		if (context == null) {
			throw new IllegalStateException(
					"The context must be set before calling the addSound() method");
		}

		soundMap.put(resourceId, sounds.load(context, resourceId, 1));
	}

	/**
	 * Play a sound that has been loaded into the sound collection
	 * 
	 * @param resourceId
	 *            The resource id for the sound
	 */
	public void play(int resourceId) {
		if (isSoundEnabled()) {
			sounds.play(soundMap.get(resourceId), 1.0f, 1.0f, 0, 0, 1.0f);
		}
	}

	/**
	 * Set the sound mode (on/off)
	 * 
	 * @param soundEnabled
	 *            True if sound is on
	 */
	public void setSoundEnabled(boolean soundEnabled) {
		this.soundEnabled = soundEnabled;
	}

	/**
	 * Get the sound mode (on/off)
	 * 
	 * @return true if sound is on
	 */
	public boolean isSoundEnabled() {
		return soundEnabled;
	}
}