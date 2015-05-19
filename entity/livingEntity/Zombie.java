package entity.livingEntity;

import entity.Bullet;
import gameState.inGame.Endless;
import gameState.inGame.InGame;
import gameState.inGame.Shop;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import launcher.GamePanel;
import map.Map;
import map.Vertex;
import sfx.Sound;

public class Zombie extends LivingEntity {

	// private ZombieType type;

	public static BufferedImage texture;

	private int attackStrength;
	private boolean canAttack;
	private long canAttackTimer;

	public static Random random;

	private ArrayList<Vertex> path;
	private int findPathTimer = 0;

	private boolean hit = false;
	private long hitTimer;

	private double rotationGoal;

	private boolean moveWait = false;

	private long soundTimer = -1;
	private Sound moanSound;

	public Zombie(ZombieType type, double x, double y) {

		super(x, y);
		r = (int) (30 * GamePanel.horScale);
		health = 300;
		int waveNumber = 0;
		if (GamePanel.getGameState() instanceof Endless)
			waveNumber = ((Endless) GamePanel.getGameState()).getWaveNumber();
		else if (GamePanel.getGameState() instanceof Shop)
			waveNumber = ((Endless) ((Shop) GamePanel.getGameState())
					.getOldState()).getWaveNumber();
		speed = (random.nextInt(3) + 5) / 10.0 + waveNumber / 30.0;

		canAttack = true;
		attackStrength = 10;

		soundTimer = System.nanoTime();
		moanSound = new Sound("/sounds/ZombieMoan1.wav");

	}

	public boolean update() {

		findPathTimer++;

		int distanceToPlayer = (int) Math.sqrt(Math.pow(
				x - InGame.player.getx(), 2)
				+ Math.pow(y - InGame.player.gety(), 2));

		if (soundTimer == -1)
			soundTimer = System.nanoTime();

		if ((System.nanoTime() - soundTimer) / 1000000 > 2000) {
			soundTimer = System.nanoTime();
			if (random.nextInt(4) == 0) {
				moanSound.changeVolume((float) (-distanceToPlayer * 0.03));
				moanSound.play();
			}
		}

		if ((r + InGame.player.getr()) * 0.8 < distanceToPlayer) {

			if (findPathTimer > 30) {
				path = findPath();
				findPathTimer = 0;
			}

			if (path != null) {

				double vertexX = path.get(path.size() - 2).getX();
				double vertexY = path.get(path.size() - 2).getY();

				double angle = Math.acos((vertexX - x)
						/ (Math.sqrt(Math.pow(vertexX - x, 2)
								+ Math.pow(vertexY - y, 2))));
				if (vertexY < y)
					angle = 2 * Math.PI - angle;

				rotationGoal = Math.toDegrees(angle) + 90;
				if (rotationGoal >= 360)
					rotationGoal -= 360;

				rotation = rotationGoal;

				dx = Math.cos(angle) * speed;
				dy = Math.sin(angle) * speed;

			}

		} else {

			if (!canAttack
					&& (System.nanoTime() - canAttackTimer) / 1000000 > 3000)
				canAttack = true;

			if (canAttack) {
				InGame.player.damage(attackStrength);
				canAttack = false;
				canAttackTimer = System.nanoTime();
			}

			dx = 0;
			dy = 0;
		}

		for (int i = 0; i < InGame.bullets.size(); i++) {

			Bullet b = InGame.bullets.get(i);

			if (Math.sqrt(Math.pow(b.getx() - x, 2) + Math.pow(b.gety() - y, 2)) <= r
					+ b.getr()
					&& !b.getHits().contains(this)) {
				damage(b.getDamage());
				if (!isDead())
					b.addHit(this);
				x += b.getdx() * 0.4;
				y += b.getdy() * 0.4;
				hit = true;
				hitTimer = System.nanoTime();
				InGame.player.getStats().addDamageDealt(b.getDamage());
				if (random.nextBoolean())
					InGame.bullets.remove(b);
				i--;
			}

		}

		if (!hit) {
			x += dx;
			y += dy;
		} else if ((System.nanoTime() - hitTimer) / 1000000 > 500)
			hit = false;

		return false;
	}

	public ArrayList<Vertex> findPath() {

		if (InGame.map.getPathFinding() != null)
			return InGame.map.getPathFinding().findPath(
					new Vertex(x, y, InGame.map.getPathFinding()),
					new Vertex(InGame.player.getx(), InGame.player.gety(),
							InGame.map.getPathFinding()));
		return null;
	}

	public boolean isMoveWaiting() {
		return moveWait;
	}

	public boolean checkCollisions(double dx, double dy) {

		Rectangle2D.Double movementRect = new Rectangle2D.Double(x + dx - 5, y
				+ dy - 5, 5, 5);

		for (GeneralPath p : Map.colissionMap)

			if (p.intersects(movementRect))
				return true;

		return false;
	}

	public void draw(Graphics2D g) {

		int relativeX = (int) x - InGame.map.getxOffset();
		int relativeY = (int) y - InGame.map.getyOffset();

		if (relativeX - r + texture.getWidth() > 0
				&& relativeX + r - texture.getWidth() < GamePanel.WINDOW_WIDTH
				&& relativeY - r + texture.getHeight() > 0
				&& relativeY + r - texture.getHeight() < GamePanel.WINDOW_HEIGHT) {

			double horScale = 0.2 / GamePanel.horScale;
			double vertScale = 0.2 / GamePanel.vertScale;

			// Calculate new x and y position
			int x = (int) (relativeX - texture.getWidth() * horScale / 2);
			int y = (int) (relativeY - texture.getHeight() * vertScale / 2);

			g.drawRenderedImage(texture, GamePanel.getAffineTransform(texture,
					x, y, horScale, vertScale, Math.toRadians(rotation)));

			if (GamePanel.debugMode) {
				g.setStroke(new BasicStroke(1));
				g.setColor(Color.RED);
				g.drawOval(relativeX - r, relativeY - r, r * 2, r * 2);
				if (path != null) {
					for (int i = 0; i < path.size() - 1; i++) {
						g.setColor(Color.ORANGE);
						g.drawLine(
								(int) path.get(i).getX()
										- InGame.map.getxOffset(),
								(int) path.get(i).getY()
										- InGame.map.getyOffset(),
								(int) path.get(i + 1).getX()
										- InGame.map.getxOffset(),
								(int) path.get(i + 1).getY()
										- InGame.map.getyOffset());
					}
				}
			}
		}
	}

	public enum ZombieType {

		SWARMER, BOSS;

	}

}
