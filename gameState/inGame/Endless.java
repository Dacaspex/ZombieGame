package gameState.inGame;

import entity.livingEntity.Zombie;
import entity.livingEntity.Zombie.ZombieType;
import gameState.StatScreen;
import gfx.Text;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

import launcher.GamePanel;

public class Endless extends InGame {

	private int waveNumber = 0;
	private boolean waveInitiating = false;
	private boolean waveText = false;
	private long waveStartTimer;

	private int zombiesAmount;
	private int zombieIndex = 1;
	private int spawnIndex = 0;

	private long restartTimer = -1;
	private boolean restart = false;

	static Random random;

	public Endless() {
		super();
	}

	public void update() {

		if (player.getHealth() <= 0 && !restart) {
			texts.add(new Text(deathMessages.get(random.nextInt(deathMessages
					.size())), 10000,
					new Font("Century Gothic", Font.PLAIN, 60), Color.RED));
			restartTimer = System.nanoTime();
			restart = true;
		}

		if (restart) {
			for (int i = 0; i < texts.size(); i++) {
				if (texts.get(i).update()) {
					texts.remove(i);
					i--;
				}
			}
			return;
		}

		super.update();

		if (zombies.size() == 0 && !waveInitiating) {
			startWave(++waveNumber);
			waveStartTimer = System.nanoTime();
			if (waveNumber != 1) {
				texts.add(new Text("- W A V E   " + (waveNumber - 1)
						+ "   C O M P L E T E D -", 2000, new Font(
						"Century Gothic", Font.PLAIN, 50), Color.WHITE));
				player.getStats().addWave(1);
				player.setMoney(player.getMoney() + waveNumber * 50 + 50);
				player.getStats().addEarnedMoney(waveNumber * 50 + 50);
			}
			waveText = false;
			waveInitiating = true;
		}

		if (waveInitiating) {

			int interWaveTime = 15000;
			if (waveNumber == 1)
				interWaveTime = 4000;

			int timeBetweenZombies = 500;

			double difference = (System.nanoTime() - waveStartTimer) / 1000000;

			if ((difference - interWaveTime)
					/ (zombieIndex * timeBetweenZombies) > 1
					&& (difference - interWaveTime)
							/ ((zombieIndex + 1) * timeBetweenZombies) < 1) {
				if (!waveText) {
					texts.add(new Text("- W A V E   " + waveNumber + "   -",
							3000, new Font("Century Gothic", Font.PLAIN, 60),
							Color.WHITE));
					waveText = true;
				}
				zombieIndex++;
				zombies.add(new Zombie(ZombieType.SWARMER, spawnLocations.get(
						random.nextInt(spawnLocations.size())).getX(),
						spawnLocations.get(spawnIndex).getY()));
			}

			if (difference - interWaveTime > zombiesAmount * timeBetweenZombies) {
				zombieIndex = 0;
				waveInitiating = false;
			}
		}

	}

	private void startWave(int waveNumber) {
		zombiesAmount = (int) (0.5 * Math.pow(waveNumber - 1, 2) + waveNumber + 4);
	}

	public void resume(long pauseTimer) {
		waveStartTimer += System.nanoTime() - pauseTimer;
		if (restartTimer != -1)
			restartTimer += System.nanoTime() - pauseTimer;
		player.resume(pauseTimer);
	}

	public void render(Graphics2D g) {

		super.render(g);

		if (restart) {

			long dif = (System.nanoTime() - restartTimer) / 1000000;

			int alpha;

			if (dif > 6000) {
				backgroundSound.stop();
				GamePanel.changeGameState(new StatScreen(player.getStats()));
			}

			if (dif > 5000)
				dif = 5000;

			alpha = (int) (255 * Math.sin((Math.PI / 2) * dif / 5000));
			if (alpha > 255)
				alpha = 255;
			if (alpha < 0)
				alpha = 0;

			g.setColor(new Color(255, 255, 255, alpha));
			g.fillRect(0, 0, GamePanel.WINDOW_WIDTH, GamePanel.WINDOW_HEIGHT);

			for (Text t : texts)
				t.draw(g);

		}

	}

	public int getWaveNumber() {
		return waveNumber;
	}

}
