package input;

import gameState.AlertBox;
import gameState.PauseMenu;
import gameState.StatScreen;
import gameState.TitleScreen.TitleScreen;
import gameState.inGame.InGame;
import gameState.inGame.Shop;
import gfx.Button;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import launcher.GamePanel;

public class MouseListener implements java.awt.event.MouseListener,
		java.awt.event.MouseWheelListener {

	public MouseListener() {
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (GamePanel.getGameState() == null)
			return;
		if (GamePanel.getGameState() instanceof TitleScreen
				|| GamePanel.getGameState() instanceof Shop
				|| GamePanel.getGameState() instanceof PauseMenu
				|| GamePanel.getGameState() instanceof AlertBox
				|| GamePanel.getGameState() instanceof StatScreen)
			switch (event.getButton()) {
			case 1:
				for (Button b : GamePanel.getGameState().getButtons()) {
					if (event.getX() >= b.getx()
							&& event.getX() <= b.getx() + b.getWidth()
							&& event.getY() <= b.gety() + b.getHeight()
							&& event.getY() >= b.gety())
						b.setPressed(true);
				}
			}
		else if (GamePanel.getGameState() instanceof InGame
				&& InGame.player != null)
			switch (event.getButton()) {
			case 1:
				InGame.player.setFiring(true);
				break;
			}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (GamePanel.getGameState() == null)
			return;
		if (GamePanel.getGameState() instanceof TitleScreen
				|| GamePanel.getGameState() instanceof Shop
				|| GamePanel.getGameState() instanceof PauseMenu
				|| GamePanel.getGameState() instanceof AlertBox
				|| GamePanel.getGameState() instanceof StatScreen)
			switch (event.getButton()) {
			case 1:
				for (Button b : GamePanel.getGameState().getButtons()) {
					if (b.isPressed())
						b.setPressed(false);
				}
			}
		else if (GamePanel.getGameState() instanceof InGame
				&& InGame.player != null)
			switch (event.getButton()) {
			case 1:
				InGame.player.setFiring(false);
				break;
			}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		if (GamePanel.getGameState() == null)
			return;
		if (GamePanel.getGameState() instanceof InGame && InGame.player != null)
			if (event.getWheelRotation() < 0)
				for (int x = 0; x < -event.getWheelRotation(); x++)
					InGame.player.getInventory().cycleSelectedSlot(false);
			else
				for (int x = 0; x < event.getWheelRotation(); x++)
					InGame.player.getInventory().cycleSelectedSlot(true);
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

}
