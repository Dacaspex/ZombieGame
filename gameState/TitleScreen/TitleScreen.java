package gameState.TitleScreen;

import gameState.AlertBox;
import gameState.GameState;
import gameState.inGame.Endless;
import gfx.Button;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import launcher.GamePanel;

public class TitleScreen extends GameState {

	public static ArrayList<Button> buttons;
	private Button button_start;
	private Button button_help;
	private Button button_quit;

	private BufferedImage backgroundImage;
	
	private Clip backgroundSound;

	public TitleScreen() {

		buttons = new ArrayList<>();

		Font font = new Font("Century Gothic", Font.PLAIN, 42);

		// Add buttons to the screen, will be perfected later

		button_start = new Button(true, 250, "Start Game", font);
		button_help = new Button(true, 350, "Help", font);
		button_quit = new Button(true, 450, "Quit", font);

		buttons.add(button_start);
		buttons.add(button_help);
		buttons.add(button_quit);

		// For testing
		try {
			backgroundImage = ImageIO.read(GamePanel.class
					.getResource("/sprites/temp.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Load Titlescreen music
		try {
			Clip clip = AudioSystem.getClip();
			AudioInputStream ais = AudioSystem.getAudioInputStream(GamePanel.class.getResource("/sounds/TitleScreen.wav"));
			clip.open(ais);
			backgroundSound = clip;
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
		
		backgroundSound.loop(Clip.LOOP_CONTINUOUSLY);
		
	}

	public void update() {

		for (Button b : buttons)
			b.update();

		if (button_start.isPressed()) {
			backgroundSound.stop();
			Endless endless = new Endless();
			String message = "Welcome to Zombie Game version 1.0. We hope that you won't encounter  any bugs what so ever, but please mind that that could happen. We hope that you'll enjoy this game. Here is a quick overview of the controls:	 W, A, S, D = walkking  |  R = reaload gun  |  SPACE = open shop  |  ESCAPE = pause  |  Mouse = shoot";
			
			AlertBox alertBox = new AlertBox(endless, message);
			GamePanel.changeGameState(alertBox);
		}
		
		if (button_help.isPressed()) {
			 Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			 String errorMessage = "Sorry, we couldn't open the help page :( To view the help page, open a browser and go to: www.github.com/Dacaspex/ZombieGame/blob/master/README.md#the-game ";
			    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			        try {
			        	URL helpPage = new URL("https://github.com/Dacaspex/ZombieGame/blob/master/README.md#the-game");
			            desktop.browse(helpPage.toURI());
			        } catch (Exception e) {
			        	GamePanel.changeGameState(new AlertBox(this, errorMessage));
			        }
			    } else {
			    	GamePanel.changeGameState(new AlertBox(this, errorMessage));
			    }
		}
		
		if (button_quit.isPressed()) {
			backgroundSound.stop();
			GamePanel.running = false;
		}
	}

	public void render(Graphics2D g) {
		// g.setColor(Color.BLACK);
		// g.fillRect(0, 0, GamePanel.WINDOW_WIDTH, GamePanel.WINDOW_HEIGHT);

		g.drawImage(backgroundImage, 0, 0, GamePanel.WINDOW_WIDTH,
				GamePanel.WINDOW_HEIGHT, null);

		g.setColor(Color.WHITE);

		for (Button b : buttons)
			b.draw(g);

		// Debug mode
		if (GamePanel.debugMode) {

			y = 5;

			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			g.setColor(Color.WHITE);

			g.drawString("Debug Mode", 10, updateY());
			g.drawString("Mouse: ", 10, updateY());
			g.drawString("X: " + GamePanel.mouseX + " Y: " + GamePanel.mouseY,
					20, updateY());
			g.drawString("Button: ", 10, updateY());
			g.drawString("X: " + buttons.get(1).getx() + " Y: "
					+ buttons.get(0).gety(), 20, updateY());
			g.drawString("Width: " + buttons.get(2).getWidth() + " Heigth: "
					+ buttons.get(2).getHeight(), 20, updateY());
			g.drawString("Hover: " + buttons.get(2).isHover(), 20, updateY());
			g.drawString("Pressed: " + buttons.get(2).isPressed(), 20,
					updateY());

		}
	}

	// for debug mode
	private static int y;

	private static int updateY() {
		y += 15;
		return y;
	}

	public ArrayList<Button> getButtons() {
		return buttons;
	}

}
