package sfx;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import launcher.GamePanel;

public class Sound {

	private Clip clip;
	private FloatControl gainControl;
	private float volume = 0;
	private boolean running = false;
	
	private boolean loaded = true;
	
	private static boolean mute = false;

	public Sound(String fileName) {

		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(GamePanel.class.getResource(fileName));
			clip = AudioSystem.getClip();
			clip.open(sound);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException("Sound: Malformed URL: " + e);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			throw new RuntimeException("Sound: Unsupported Audio File: " + e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Sound: Input/Output Error: " + e);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Sound: Line Unavailable Exception Error: " + e);
		} catch (IllegalArgumentException e) {
			loaded = false;
		}
		
		if (!loaded)
			return;
		gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(volume);

	}
	
	public Sound(String fileName, float volume) {

		this(fileName);

		if (!loaded)
			return;
		changeVolume(volume);
		
	}

	public void play() {
		if (!loaded)
			return;
		if (mute)
			return;
		clip.setFramePosition(0);
		clip.start();
		running = true;
	}
	
	public void forcePlay() {
		if (!loaded)
			return;
		if (mute)
			return;
		if (isRunning())
			stop();
		play();
	}

	public void loop() {
		if (!loaded)
			return;
		if (mute)
			return;
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		running = true;
	}

	public void stop() {
		if (!loaded)
			return;
		clip.stop();
		running = false;
	}
	
	public void changeVolume(float volume) {
		if (!loaded)
			return;
		this.volume = Math.min(gainControl.getMaximum(), Math.max(gainControl.getMinimum(), volume));
		gainControl.setValue(this.volume);
	}
	
	public float getVolume() {
		return volume;
	}
	
	public boolean isRunning() {
		if (!loaded)
			return false;
		if (clip.getFramePosition() >= clip.getFrameLength())
			stop();
		return running;
	}
	
	public static void mute() {
		mute = !mute;
	}
	
	public static boolean isMuted() {
		return mute;
	}
	
}