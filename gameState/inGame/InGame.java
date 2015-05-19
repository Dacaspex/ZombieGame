package gameState.inGame;

import entity.Bullet;
import entity.Gun;
import entity.Gun.GunType;
import entity.livingEntity.Player;
import entity.livingEntity.Zombie;
import entity.utility.Grenade;
import entity.utility.MedKit;
import entity.utility.Utility;
import gameState.GameState;
import gfx.DeadZombie;
import gfx.MuzzleFlash;
import gfx.Text;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import launcher.GamePanel;
import map.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import sfx.Sound;

public class InGame extends GameState {

	public static Player player;

	public static Map map;

	public static ArrayList<Gun> guns;
	public static ArrayList<Utility> utilities;

	public static ArrayList<Zombie> zombies;
	public static ArrayList<DeadZombie> deadZombies;
	public static ArrayList<Bullet> bullets;
	public static ArrayList<Utility> deployedUtilities;
	public static ArrayList<MuzzleFlash> muzzleFlashes;
	public static ArrayList<Text> texts;

	public static ArrayList<String> deathMessages;

	private static ArrayList<GeneralPath> shapeList;
	public static ArrayList<Point2D.Float> spawnLocations;

	public static Sound backgroundSound;

	public InGame() {

		if (!loadGuns() || !loadUtilities() || !loadSprites()
				|| !loadCollisionMap() || !loadSpawnLocations()
				|| !loadSounds() || !loadMisc()) {

			GamePanel.running = false;
			return;

		}

		backgroundSound.loop();

	}

	public void update() {

		map.update();

		player.update();

		// bullet update
		for (int i = 0; i < bullets.size(); i++) {
			if (bullets.get(i).update()) {
				bullets.remove(i);
				i--;
			}
		}

		// update utilities
		for (int i = 0; i < deployedUtilities.size(); i++) {
			if (deployedUtilities.get(i).update()) {
				deployedUtilities.remove(i);
				i--;
			}
		}

		// zombie update
		for (int i = 0; i < zombies.size(); i++) {
			zombies.get(i).update();
		}

		// check dead zombie
		for (int i = 0; i < zombies.size(); i++) {
			if (zombies.get(i).isDead()) {
				deadZombies.add(new DeadZombie(zombies.get(i).getx(), zombies
						.get(i).gety()));
				player.setMoney(player.getMoney() + 20);
				player.getStats().addKills(1);
				player.getStats().addEarnedMoney(20);
				zombies.remove(i);
				i--;
			}
		}

		// update deadzombies
		for (int i = 0; i < deadZombies.size(); i++) {
			if (deadZombies.get(i).update()) {
				deadZombies.remove(i);
				i--;
			}
		}

		// update muzzleFlashes
		for (int i = 0; i < muzzleFlashes.size(); i++) {
			if (muzzleFlashes.get(i).update()) {
				muzzleFlashes.remove(i);
				i--;
			}
		}

		// update texts
		for (int i = 0; i < texts.size(); i++) {
			if (texts.get(i).update()) {
				texts.remove(i);
				i--;
			}
		}

	}

	public void resume(long pauseTimer) {
	}

	public void render(Graphics2D g) {

		map.draw(g);

		for (DeadZombie d : deadZombies)
			d.draw(g);

		for (Zombie z : zombies)
			z.draw(g);

		for (MuzzleFlash m : muzzleFlashes)
			m.draw(g);

		if (GamePanel.debugMode)
			for (Bullet b : bullets)
				b.draw(g);

		for (Utility u : deployedUtilities)
			u.draw(g);

		g.setFont(new Font("Century Gothic", Font.PLAIN, 20));

		player.draw(g);

		// Draw the inventory, health bar and current selectedItem details
		// Background
		g.setColor(new Color(24, 24, 24, 100));
		g.fillRect(0, GamePanel.WINDOW_HEIGHT - 160, 500, 160);
		g.setStroke(new BasicStroke(2));
		g.drawRect(-2, GamePanel.WINDOW_HEIGHT - 163, 504, 164);
		g.setStroke(new BasicStroke(1));

		// Inventory
		player.getInventory().draw(g);

		// Player health
		g.setColor(Color.WHITE);

		g.setFont(new Font("Century Gothic", Font.PLAIN, 20));

		g.drawString("Health:", 20, GamePanel.WINDOW_HEIGHT - 50);
		g.setColor(new Color(102, 0, 0));
		g.fillRect(20, GamePanel.WINDOW_HEIGHT - 45, 400, 40);
		g.setColor(new Color(150, 0, 0));
		g.fillRect(23, GamePanel.WINDOW_HEIGHT - 42, 394 * player.getHealth()
				/ player.getMaxHealth(), 34);

		// Draw gun properties
		g.setColor(Color.WHITE);

		if (player.getInventory().hasGunEquipped()) {
			g.drawString("Gun:", 370, GamePanel.WINDOW_HEIGHT - 130);
			g.drawString(player.getInventory().getCurrentGun().getName(), 370,
					GamePanel.WINDOW_HEIGHT - 110);
		}
		if (player.isReloading())
			g.setColor(Color.RED);
		if (player.getInventory().hasGunEquipped())
			g.drawString(player.getInventory().getCurrentGun().getCurrentClip()
					+ " / "
					+ player.getInventory().getCurrentGun().getBullets(), 370,
					GamePanel.WINDOW_HEIGHT - 92);

		// Money
		g.setColor(Color.WHITE);
		g.drawString("$: " + player.getMoney(), 370,
				GamePanel.WINDOW_HEIGHT - 60);

		for (Text t : texts)
			t.draw(g);

		// Debug mode
		if (GamePanel.debugMode) {

			debugHeight = 5;

			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			g.setColor(Color.WHITE);

			g.drawString("Debug Mode", 10, updateDebugHeight());
			g.drawString("Player: ", 10, updateDebugHeight());
			g.drawString(
					"Coordinates: " + player.getx() + ", " + player.gety(), 20,
					updateDebugHeight());
			g.drawString("Rotation: " + player.getRotation(), 20,
					updateDebugHeight());
			g.drawString("Reloading: " + player.isReloading(), 20,
					updateDebugHeight());
			g.drawString("Going Right: " + player.getRight(), 20,
					updateDebugHeight());
			g.drawString("Going Left: " + player.getLeft(), 20,
					updateDebugHeight());
			g.drawString("Going Up: " + player.getUp(), 20, updateDebugHeight());
			g.drawString("Going Down: " + player.getDown(), 20,
					updateDebugHeight());
			g.drawString("Gun:", 10, updateDebugHeight());
			if (player.getInventory().hasGunEquipped()) {
				g.drawString("Name: "
						+ player.getInventory().getCurrentGun().getName(), 20,
						updateDebugHeight());
				g.drawString("Damage: "
						+ player.getInventory().getCurrentGun().getDamage(),
						20, updateDebugHeight());
				g.drawString("FireRate: "
						+ player.getInventory().getCurrentGun().getFireRate(),
						20, updateDebugHeight());
				g.drawString("Reload Speed: "
						+ player.getInventory().getCurrentGun()
								.getReloadSpeed(), 20, updateDebugHeight());
				g.drawString("Clip Size: "
						+ player.getInventory().getCurrentGun().getClipSize(),
						20, updateDebugHeight());
				g.drawString("Current Bullets: "
						+ player.getInventory().getCurrentGun()
								.getCurrentClip(), 20, updateDebugHeight());
				g.drawString(
						"Max Bullets: "
								+ player.getInventory().getCurrentGun()
										.getMaxBullets(), 20,
						updateDebugHeight());
			}

		}
	}

	// for debug mode
	private static int debugHeight;

	private static int updateDebugHeight() {
		debugHeight += 15;
		return debugHeight;
	}

	private static GraphicsEnvironment env = GraphicsEnvironment
			.getLocalGraphicsEnvironment();
	private static GraphicsDevice device = env.getDefaultScreenDevice();
	private static GraphicsConfiguration config = device
			.getDefaultConfiguration();

	public static BufferedImage loadCompatibleImage(BufferedImage rawTexture) {
		BufferedImage texture = config.createCompatibleImage(
				rawTexture.getWidth(), rawTexture.getHeight(),
				Transparency.TRANSLUCENT);
		Graphics g = texture.getGraphics();
		g.drawImage(rawTexture, 0, 0, rawTexture.getWidth(),
				rawTexture.getHeight(), null);
		g.dispose();
		return texture;
	}

	private static boolean loadMisc() {

		try {

			Endless.random = new Random();

			map = new Map();

			player = new Player(5000, 5000);

			Zombie.random = new Random();
			zombies = new ArrayList<>();
			deadZombies = new ArrayList<>();
			bullets = new ArrayList<>();
			deployedUtilities = new ArrayList<>();
			muzzleFlashes = new ArrayList<>();
			texts = new ArrayList<>();

			deathMessages = new ArrayList<>();

			String deathMessagesRaw = "";

			InputStream in = GamePanel.class.getResourceAsStream("/data/death-messages.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			deathMessagesRaw = sb.toString();

			for (String message : deathMessagesRaw.split("-")) {
				deathMessages.add(message);
			}

			backgroundSound = new Sound("/sounds/InGame.wav", -5);

			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			for (StackTraceElement add : e.getStackTrace())
				GamePanel.errorLog += add + " ";
			return false;
		}

		return true;

	}

	private static SAXReader reader = new SAXReader();

	private static boolean loadGuns() {
		guns = new ArrayList<>();
		try {
			Document document = reader.read(GamePanel.class
					.getResource("/data/guns.xml").toURI().toURL());
			Element root = document.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> gunElements = root.elements();
			for (Element gunElement : gunElements) {
				String name = gunElement.getName();
				int damage = Integer.parseInt(gunElement.element("Damage")
						.getText());
				int fireRate = Integer.parseInt(gunElement.element("Firerate")
						.getText());
				float reloadSpeed = Float.parseFloat(gunElement.element(
						"Reloadspeed").getText());
				int clipSize = Integer.parseInt(gunElement.element("Clipsize")
						.getText());
				int maxBullets = Integer.parseInt(gunElement.element(
						"Maxbullets").getText());
				int weight = Integer.parseInt(gunElement.element("Weight")
						.getText());
				GunType type = GunType.valueOf(gunElement.element("Type")
						.getText().toUpperCase().replace(" ", "_"));
				int price = Integer.parseInt(gunElement.element("Price")
						.getText());
				BufferedImage texture = loadCompatibleImage(ImageIO
						.read(GamePanel.class.getResource("/sprites/guns/"
								+ name + ".png")));
				guns.add(new Gun(name, damage, fireRate, reloadSpeed, clipSize,
						maxBullets, weight, type, price, texture));
			}
		} catch (Exception e) {
			for (StackTraceElement add : e.getStackTrace())
				GamePanel.errorLog += add + " ";
			return false;
		}
		return true;
	}

	private static boolean loadUtilities() {
		utilities = new ArrayList<>();
		try {
			BufferedImage texture = loadCompatibleImage(ImageIO
					.read(GamePanel.class
							.getResource("/sprites/utilities/Medkit.png")));
			BufferedImage textureInHand = loadCompatibleImage(ImageIO
					.read(GamePanel.class
							.getResource("/sprites/utilities/MedkitInHand.png")));
			utilities.add(new MedKit(texture, textureInHand));
			// Temporary
			texture = loadCompatibleImage(ImageIO.read(GamePanel.class
					.getResource("/sprites/utilities/Grenade.png")));
			textureInHand = loadCompatibleImage(ImageIO.read(GamePanel.class
					.getResource("/sprites/utilities/GrenadeInHand.png")));
			utilities.add(new Grenade(texture, textureInHand));
		} catch (IOException e) {
			for (StackTraceElement add : e.getStackTrace())
				GamePanel.errorLog += add + " ";
			return false;
		}
		return true;
	}

	private static boolean loadSprites() {

		try {

			Player.texture_bottom = loadCompatibleImage(ImageIO
					.read(GamePanel.class
							.getResource("/sprites/Player_bottom.png")));
			Player.texture_head = loadCompatibleImage(ImageIO
					.read(GamePanel.class
							.getResource("/sprites/Player_head.png")));
			MuzzleFlash.texture = loadCompatibleImage(ImageIO
					.read(GamePanel.class
							.getResource("/sprites/MuzzleFlash.png")));
			Zombie.texture = loadCompatibleImage(ImageIO.read(GamePanel.class
					.getResource("/sprites/Zombie1.png")));
			DeadZombie.texture = loadCompatibleImage(ImageIO
					.read(GamePanel.class
							.getResource("/sprites/Dead_zombie.png")));
			Map.texture = loadCompatibleImage(ImageIO.read(GamePanel.class
					.getResource("/sprites/Map1.png")));

		} catch (Exception e) {
			for (StackTraceElement add : e.getStackTrace())
				GamePanel.errorLog += add + " ";
			return false;
		}

		return true;

	}

	@SuppressWarnings("unchecked")
	private static boolean loadCollisionMap() {
		shapeList = new ArrayList<>();
		try {
			Document document = reader.read(GamePanel.class
					.getResource("/data/colission-map-1.xml").toURI().toURL());
			Element root = document.getRootElement();
			List<Element> shapeElements = root.elements("shape");

			for (Element shapeElement : shapeElements) {
				GeneralPath polyline = new GeneralPath(
						GeneralPath.WIND_EVEN_ODD, shapeElements.size());
				int index = 1;
				while (shapeElement.element("x" + index) != null) {
					int x = (int) (Integer.parseInt(shapeElement.element(
							"x" + index).getText()));
					int y = (int) (Integer.parseInt(shapeElement.element(
							"y" + index).getText()));
					if (index++ == 1)
						polyline.moveTo(x, y);
					else
						polyline.lineTo(x, y);
				}
				polyline.closePath();

				shapeList.add(polyline);
			}
			Map.colissionMap = (ArrayList<GeneralPath>) shapeList.clone();
			Map.pathfindingMap = (ArrayList<GeneralPath>) shapeList.clone();
			for (int i = 0; i < 4; i++)
				Map.pathfindingMap.remove(0);
		} catch (Exception e) {
			for (StackTraceElement add : e.getStackTrace())
				GamePanel.errorLog += add + " ";
			return false;
		}
		return true;
	}

	private static boolean loadSpawnLocations() {
		spawnLocations = new ArrayList<>();
		try {
			Document document = reader.read(GamePanel.class
					.getResource("/data/spawn-locations.xml").toURI().toURL());
			Element root = document.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> spawnElements = root.elements("spawn");

			for (Element spawnElement : spawnElements) {
				spawnLocations
						.add(new Point2D.Float(Float.parseFloat(spawnElement
								.getText().split(",")[0]),
								Float.parseFloat(spawnElement.getText().split(
										",")[1])));
			}

		} catch (Exception e) {
			for (StackTraceElement add : e.getStackTrace())
				GamePanel.errorLog += add + " ";
			return false;
		}
		return true;
	}

	private static boolean loadSounds() {
		Player.walk_sound1 = new Sound("/sounds/PlayerWalk1.wav", -10);
		Player.walk_sound2 = new Sound("/sounds/PlayerWalk2.wav", -10);
		Player.walk_sound3 = new Sound("/sounds/PlayerWalk3.wav", -10);
		Player.walk_sound4 = new Sound("/sounds/PlayerWalk4.wav", -10);

		Player.machineGun_sound = new Sound("/sounds/MachineGun.wav", -10);
		Player.pistol_sound = new Sound("/sounds/Pistol.wav", -15);

		return true;
	}

	public Player getPlayer() {
		return player;
	}

}
