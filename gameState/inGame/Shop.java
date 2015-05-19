package gameState.inGame;

import entity.Gun;
import entity.utility.Utility;
import gameState.GameState;
import gfx.Button;
import gfx.Text;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

import launcher.GamePanel;

public class Shop extends GameState {

	private InGame oldState;

	private ArrayList<Button> buttons;
	private ArrayList<Button> gunButtons;
	private ArrayList<Button> utilityButtons;
	private Button button_back;
	private Button button_previous;
	private Button button_next;

	private int page = 1;

	private boolean available;

	private ArrayList<Text> texts;

	public Shop(InGame oldState, boolean available) {

		this.oldState = oldState;

		this.available = available;

		buttons = new ArrayList<>();
		gunButtons = new ArrayList<>();
		utilityButtons = new ArrayList<>();
		texts = new ArrayList<>();

		button_back = new Button(
				(int) (GamePanel.WINDOW_WIDTH - GamePanel.WINDOW_WIDTH * 0.2),
				(int) (GamePanel.WINDOW_HEIGHT - GamePanel.WINDOW_HEIGHT * 0.2),
				"BACK", new Font("Century Gothic", Font.PLAIN, 24));

		button_previous = new Button(
				(int) ((GamePanel.WINDOW_WIDTH * 0.11) + 20 * GamePanel.horScale),
				(int) ((GamePanel.WINDOW_HEIGHT * 0.45) + 20 * GamePanel.vertScale),
				"<<", new Font("Century Gothic", Font.PLAIN, 26));

		button_next = new Button(
				(int) ((GamePanel.WINDOW_WIDTH * 0.855) + 20 * GamePanel.horScale),
				(int) ((GamePanel.WINDOW_HEIGHT * 0.45) + 20 * GamePanel.vertScale),
				">>", new Font("Century Gothic", Font.PLAIN, 26));

		buttons.add(button_back);
		buttons.add(button_previous);
		buttons.add(button_next);

		int x = 1, y = 1;

		Font font = new Font("Century Gothic", Font.PLAIN, 20);

		for (Gun gun : InGame.guns) {
			if (x == 6) {
				x = 1;
				y++;
			}

			String gunLabel = "";

			Gun slot = null;

			if (InGame.player.getInventory().slot1 == gun)
				slot = InGame.player.getInventory().slot1;
			else if (InGame.player.getInventory().slot2 == gun)
				slot = InGame.player.getInventory().slot2;
			else if (InGame.player.getInventory().slot2 == gun)
				slot = InGame.player.getInventory().slot3;

			if (slot != null) {

				int maxBullets = slot.getMaxBullets();
				int bulletsLeft = slot.getBullets();

				int reloadPrice = (int) ((((float) maxBullets - (float) bulletsLeft) / (float) maxBullets) * (float) slot
						.getPrice());
				
				// Add base price
				// Later moet dit in de xml!
				if (slot.getName() == "M1911")
					reloadPrice += 20;
				else if (slot.getName() == "AK47")
					reloadPrice = (int) (reloadPrice * 1.6) + 50;
				else if (slot.getName() == "M16A3")
					reloadPrice = (int) (reloadPrice * 1.8) + 60;
				
				gunLabel = "Reload " + slot.getName() + ": " + reloadPrice
						+ "$";

			} else
				gunLabel = gun.getName() + ": " + gun.getPrice() + "$";

			gunButtons.add(new Button(
					(int) (GamePanel.WINDOW_WIDTH * 0.16 * x++)
							+ (int) (gun.getTexture().getWidth() * 0.1 + 40)
							/ 2
							- GamePanel.g.getFontMetrics(font).stringWidth(
									gunLabel) / 2,
					(int) (GamePanel.WINDOW_HEIGHT * 0.2 * y + gun.getTexture()
							.getHeight() * 0.1)
							+ 45
							+ GamePanel.g.getFontMetrics(font).getHeight(),
					gunLabel, font));

		}

		x = 1;
		y = 1;

		for (Utility utility : InGame.utilities) {
			if (x == 6) {
				x = 1;
				y++;
			}

			String text = utility.getName() + ": " + utility.getPrice() + "$";

			utilityButtons
					.add(new Button(
							(int) (GamePanel.WINDOW_WIDTH * 0.16 * x++)
									+ (int) (utility.getTexture().getWidth() * 0.1 + 40)
									/ 2
									- GamePanel.g.getFontMetrics(font)
											.stringWidth(text) / 2,
							(int) (GamePanel.WINDOW_HEIGHT * 0.2 * y + utility
									.getTexture().getHeight() * 0.1)
									+ 45
									+ GamePanel.g.getFontMetrics(font)
											.getHeight(), text, font));

		}

		oldState.getPlayer().resetKeys();

		GamePanel.getInstance().changeCursor(GamePanel.cursor);

	}

	public void update() {

		oldState.update();

		for (Button b : buttons)
			b.update();

		if (page == 1)
			for (Button b : gunButtons) {
				b.update();
				if (b.isPressed()) {
					String gunName;
					Gun gun = null;
					boolean buyReload = false;
					int gunPrice = 0;

					if (b.getText().startsWith("Reload")) {

						String priceString = b.getText().split(":")[1];
						gunPrice = Integer.parseInt(priceString.substring(1,
								priceString.length() - 1));

						gunName = b.getText().split(" ")[1];
						gunName = gunName.substring(0, gunName.length() - 1);

						buyReload = true;

						for (Gun g : InGame.guns)
							if (g.getName().equalsIgnoreCase(gunName))
								gun = g;

					} else {

						gunName = b.getText().split(":")[0];

						for (Gun g : InGame.guns)
							if (g.getName().equalsIgnoreCase(gunName)) {
								gun = g;
								gunPrice = gun.getPrice();
							}

					}

					// Checking if the player has enough cash
					if (InGame.player.getMoney() < gunPrice) {
						texts.add(new Text("Unsufficient cash!", 2000,
								new Font("Century Gothic", Font.PLAIN, 36),
								Color.RED));
					} else {
						// Player has enough cash

						// Add gun
						if (!buyReload) {
							// Checking if the player has enough room in his
							// inventory
							if (!InGame.player.getInventory().addGun(gun)) {
								texts.add(new Text("Unsufficient room!", 2000,
										new Font("Century Gothic", Font.PLAIN,
												36), Color.RED));
							} else {
								// Player has enough room
								InGame.player.setMoney(InGame.player.getMoney()
										- gunPrice);
								InGame.player.getStats()
										.addSpentMoney(gunPrice);
							}
						} else {
							// Check if the gun isn't full
							if (gun.getMaxBullets() != gun.getBullets()) {

								gun.setBullets(gun.getMaxBullets());
								texts.add(new Text("Supplying ammo", 2000,
										new Font("Century Gothic", Font.PLAIN,
												36), Color.BLUE));

								InGame.player.setMoney(InGame.player.getMoney()
										- gunPrice);
								InGame.player.getStats()
										.addSpentMoney(gunPrice);
							} else {
								// Gun is full
								texts.add(new Text("Gun is already full!",
										2000, new Font("Century Gothic",
												Font.PLAIN, 36), Color.RED));
							}
						}
					}
				}
			}

		if (page == 2)
			for (Button b : utilityButtons) {
				b.update();
				if (b.isPressed()) {
					String name = b.getText().split(":")[0];
					Utility utility = null;
					for (Utility u : InGame.utilities)
						if (u.getName().equalsIgnoreCase(name))
							utility = u;
					if (utility != null)
						if (InGame.player.getMoney() < utility.getPrice())
							texts.add(new Text("Unsufficient cash!", 2000,
									new Font("Century Gothic", Font.PLAIN, 36),
									Color.RED));
						else if (!InGame.player.getInventory().addUtility(
								utility))
							texts.add(new Text("Unsufficient room!", 2000,
									new Font("Century Gothic", Font.PLAIN, 36),
									Color.RED));
						else {
							InGame.player.setMoney(InGame.player.getMoney()
									- utility.getPrice());
							InGame.player.getStats().addSpentMoney(
									utility.getPrice());
						}
				}
			}

		for (int i = 0; i < texts.size(); i++) {
			if (texts.get(i).update()) {
				texts.remove(i);
				i--;
			}
		}

		if (button_back.isPressed()) {
			GamePanel.changeGameState(oldState);
			GamePanel.getInstance().changeCursor(GamePanel.aimCursor);
		}

		if (button_next.isPressed())
			page++;

		if (button_previous.isPressed())
			page--;

		if (page > 2)
			page = 1;

		if (page < 1)
			page = 2;

	}

	public void render(Graphics2D g) {

		oldState.render(g);

		g.setColor(new Color(24, 24, 24, 240));

		g.fillRect((int) (GamePanel.WINDOW_WIDTH * 0.1),
				(int) (GamePanel.WINDOW_HEIGHT * 0.1),
				(int) (GamePanel.WINDOW_WIDTH - GamePanel.WINDOW_WIDTH * 0.2),
				(int) (GamePanel.WINDOW_HEIGHT - GamePanel.WINDOW_HEIGHT * 0.2));

		g.setColor(Color.WHITE);

		for (Button b : buttons)
			b.draw(g);

		Font font = new Font("Century Gothic", Font.PLAIN, 48);

		g.setFont(font);

		if (page == 1) {

			g.drawString("SHOP - GUNS", GamePanel.WINDOW_WIDTH / 2
					- g.getFontMetrics(font).stringWidth("SHOP - GUNS") / 2,
					150);

			if (!available)
				g.setColor(Color.GRAY);

			for (Button b : gunButtons)
				b.draw(g);

			int x = 1, y = 1;

			for (Gun gun : InGame.guns) {
				if (x == 6) {
					x = 1;
					y++;
				}
				g.drawRenderedImage(gun.getTexture(), GamePanel
						.getAffineTransform(gun.getTexture(),
								(int) (GamePanel.WINDOW_WIDTH * 0.16 * x) + 20,
								(int) (GamePanel.WINDOW_HEIGHT * 0.2 * y) + 20,
								0.1, 0));
				g.drawRect((int) (GamePanel.WINDOW_WIDTH * 0.16 * x++),
						(int) (GamePanel.WINDOW_HEIGHT * 0.2 * y), (int) (gun
								.getTexture().getWidth() * 0.1) + 40,
						(int) (gun.getTexture().getHeight() * 0.1) + 40);
			}

		} else if (page == 2) {
			g.drawString("SHOP - UTILITIES", GamePanel.WINDOW_WIDTH / 2
					- g.getFontMetrics(font).stringWidth("SHOP - UTILITIES")
					/ 2, 150);

			if (!available)
				g.setColor(Color.GRAY);

			for (Button b : utilityButtons)
				b.draw(g);

			int x = 1, y = 1;

			for (Utility utility : InGame.utilities) {
				if (x == 6) {
					x = 1;
					y++;
				}
				g.drawRenderedImage(utility.getTexture(), GamePanel
						.getAffineTransform(utility.getTexture(),
								(int) (GamePanel.WINDOW_WIDTH * 0.16 * x) + 20,
								(int) (GamePanel.WINDOW_HEIGHT * 0.2 * y) + 20,
								0.1, 0));
				g.drawRect((int) (GamePanel.WINDOW_WIDTH * 0.16 * x++),
						(int) (GamePanel.WINDOW_HEIGHT * 0.2 * y),
						(int) (utility.getTexture().getWidth() * 0.1) + 40,
						(int) (utility.getTexture().getHeight() * 0.1) + 40);
			}

		}

		for (Text t : texts)
			t.draw(g);

	}

	public ArrayList<Button> getButtons() {
		@SuppressWarnings("unchecked")
		ArrayList<Button> toReturn = (ArrayList<Button>) buttons.clone();
		toReturn.addAll(gunButtons);
		toReturn.addAll(utilityButtons);
		return toReturn;
	}

	public InGame getOldState() {
		return oldState;
	}

}
