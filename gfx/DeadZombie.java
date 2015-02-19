package gfx;

import java.awt.Color;
import java.awt.Graphics2D;

import launcher.GamePanel;
import entity.Entity;

public class DeadZombie extends Entity {

	private long died;
	private int alpha;
	private int timer;

	public DeadZombie(int x, int y) {
		super(x, y);
		died = System.nanoTime();
		timer = 3000;
		r = 60;
	}

	public boolean update() {
		long diedDif = (System.nanoTime() - died) / 1000000;
		if (diedDif > 3000)
			return true;
		alpha = (int) (255 * Math.cos((Math.PI / 2) * diedDif / timer));
		if (alpha > 255)
			alpha = 255;
		if (alpha < 0)
			alpha = 0;
		return false;
	}

	public void draw(Graphics2D g) {

		int relativeX = x - GamePanel.map.getxOffset();
		int relativeY = y - GamePanel.map.getyOffset();

		if (relativeX - r > 0 && relativeX + r < GamePanel.WINDOW_WIDTH
				&& relativeY - r > 0 && relativeY + r < GamePanel.WINDOW_HEIGHT) {
			g.setColor(new Color(1, 0, 0, alpha));
			g.fillOval(relativeX - r / 2, relativeY - r / 2, r, r);
		}

	}

}
