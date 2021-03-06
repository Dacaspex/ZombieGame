package map;

import gameState.inGame.InGame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import launcher.GamePanel;
import map.pathFinding.PathFinding;

public class Map {

	private int WIDTH;
	private int HEIGHT;

	private int xOffset;
	private int yOffset;

	public static BufferedImage texture;

	public static ArrayList<GeneralPath> colissionMap;
	public static ArrayList<GeneralPath> pathfindingMap;

	private PathFinding pathFinding;

	public Map() {

		BufferedImage newTexture = new BufferedImage(8192, 8192, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(2, 2);
		AffineTransformOp scaleOp = 
		   new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		texture = scaleOp.filter(texture, newTexture);
		WIDTH = texture.getWidth();
		HEIGHT = texture.getHeight();
		pathFinding = new PathFinding(pathfindingMap);

	}

	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}

	public void update() {

		xOffset = (int) InGame.player.getx() - GamePanel.WINDOW_WIDTH / 2;
		yOffset = (int) InGame.player.gety() - GamePanel.WINDOW_HEIGHT / 2;

	}

	public PathFinding getPathFinding() {
		return pathFinding;
	}

	public int getxOffset() {
		return xOffset;
	}

	public int getyOffset() {
		return yOffset;
	}

	public void draw(Graphics2D g) {
		g.drawImage(texture.getSubimage(xOffset, yOffset,
				GamePanel.WINDOW_WIDTH, GamePanel.WINDOW_HEIGHT), 0, 0,
				GamePanel.WINDOW_WIDTH, GamePanel.WINDOW_HEIGHT, null);

		// Draw collision rectangles
		if (GamePanel.debugMode) {
			g.setStroke(new BasicStroke(1));
			g.setColor(Color.GREEN);
			for (GeneralPath p : colissionMap) {
				PathIterator iterator = p.getPathIterator(null);
				int previousX = -1;
				int previousY = -1;
				int startX = -1;
				int startY = -1;
				while (!iterator.isDone()) {
					double[] coords = new double[6];
					iterator.currentSegment(coords);
					if (previousX != -1 && previousY != -1) {
						g.drawLine(previousX - InGame.map.getxOffset(),
								previousY - InGame.map.getyOffset(),
								(int) (coords[0] == 0 ? startX : coords[0])
										- InGame.map.getxOffset(),
								(int) (coords[1] == 0 ? startY : coords[1])
										- InGame.map.getyOffset());
					} else {
						startX = (int) coords[0];
						startY = (int) coords[1];
					}
					previousX = (int) coords[0];
					previousY = (int) coords[1];
					iterator.next();
				}
			}
		}
	}
}