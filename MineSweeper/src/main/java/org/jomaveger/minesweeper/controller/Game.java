package org.jomaveger.minesweeper.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jomaveger.lang.exceptions.ExceptionUtils;
import org.jomaveger.minesweeper.model.Board;
import org.jomaveger.minesweeper.model.Cell;
import org.jomaveger.minesweeper.model.statistics.Score;
import org.jomaveger.minesweeper.model.statistics.Time;
import org.jomaveger.minesweeper.view.UI;
import org.jomaveger.util.IResourcesLoader;

public class Game implements MouseListener, ActionListener, WindowListener {

	private boolean playing;

	private Board board;

	private UI gui;

	private Score score;

	public Game() {
		score = new Score();

		UI.setLook("Nimbus");

		createBoard();

		this.gui = new UI(board.getRows(), board.getCols(), board.getNumberOfMines());
		this.gui.setButtonListeners(this);

		this.playing = false;

		gui.setVisible(true);

		gui.setIcons();
		gui.hideAll();
	}

	public void setButtonImages() {
		Cell cells[][] = board.getCells();
		JButton buttons[][] = gui.getButtons();

		for (int y = 0; y < board.getRows(); y++) {
			for (int x = 0; x < board.getCols(); x++) {
				buttons[x][y].setIcon(null);

				if (cells[x][y].getContent().equals("")) {
					buttons[x][y].setIcon(gui.getIconTile());
				} else if (cells[x][y].getContent().equals("F")) {
					buttons[x][y].setIcon(gui.getIconFlag());
					buttons[x][y].setBackground(Color.blue);
				} else if (cells[x][y].getContent().equals("0")) {
					buttons[x][y].setBackground(Color.lightGray);
				} else {
					buttons[x][y].setBackground(Color.lightGray);
					buttons[x][y].setText(cells[x][y].getContent());
					gui.setTextColor(buttons[x][y]);
				}
			}
		}
	}

	public void createBoard() {
		int mines = 10;

		int r = 9;
		int c = 9;

		this.board = new Board(mines, r, c);
	}

	public void newGame() {
		this.playing = false;

		createBoard();

		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(board.getNumberOfMines());
	}

	public void restartGame() {
		this.playing = false;

		board.resetBoard();

		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(board.getNumberOfMines());
	}

	private void endGame() {
		playing = false;
		showAll();
	}

	public void gameWon() {
		score.incCurrentStreak();
		score.incCurrentWinningStreak();
		score.incGamesWon();
		score.incGamesPlayed();

		gui.interruptTimer();
		endGame();

		JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);

		JLabel message = new JLabel("Congratulations, you won the game!", SwingConstants.CENTER);

		JPanel statistics = new JPanel();
		statistics.setLayout(new GridLayout(6, 1, 0, 10));

		List<Time> bTimes = score.getBestTimes();

		if (bTimes.isEmpty() || (bTimes.get(0).getTimeValue() > gui.getTimePassed())) {
			statistics.add(new JLabel("    You have the fastest time for this difficulty level!    "));
		}

		score.addTime(gui.getTimePassed(), new Date(System.currentTimeMillis()));

		JLabel time = new JLabel("  Time:  " + Integer.toString(gui.getTimePassed()) + " seconds            Date:  "
				+ new Date(System.currentTimeMillis()));

		JLabel bestTime = new JLabel();

		if (bTimes.isEmpty()) {
			bestTime.setText("  Best Time:  ---                  Date:  ---");
		} else {
			bestTime.setText("  Best Time:  " + bTimes.get(0).getTimeValue() + " seconds            Date:  "
					+ bTimes.get(0).getDateValue());
		}

		JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
		JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
		JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");

		statistics.add(time);
		statistics.add(bestTime);
		statistics.add(gPlayed);
		statistics.add(gWon);
		statistics.add(gPercentage);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		statistics.setBorder(loweredetched);

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2, 10, 0));

		JButton exit = new JButton("Exit");
		JButton playAgain = new JButton("Play Again");

		exit.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			windowClosing(null);
		});

		playAgain.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			newGame();
		});

		buttons.add(exit);
		buttons.add(playAgain);

		JPanel c = new JPanel();
		c.setLayout(new BorderLayout(20, 20));
		c.add(message, BorderLayout.NORTH);
		c.add(statistics, BorderLayout.CENTER);
		c.add(buttons, BorderLayout.SOUTH);

		c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
				newGame();
			}
		});

		dialog.setTitle("Game Won");
		dialog.add(c);
		dialog.pack();
		dialog.setLocationRelativeTo(gui);
		dialog.setVisible(true);
	}

	public void gameLost() {
		score.decCurrentStreak();
		score.incCurrentLosingStreak();
		score.incGamesPlayed();

		gui.interruptTimer();

		endGame();

		JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);

		JLabel message = new JLabel("Sorry, you lost this game. Better luck next time!", SwingConstants.CENTER);

		JPanel statistics = new JPanel();
		statistics.setLayout(new GridLayout(5, 1, 0, 10));

		JLabel time = new JLabel("  Time:  " + Integer.toString(gui.getTimePassed()) + " seconds");

		JLabel bestTime = new JLabel();

		List<Time> bTimes = score.getBestTimes();

		if (bTimes.isEmpty()) {
			bestTime.setText("                        ");
		} else {
			bestTime.setText("  Best Time:  " + bTimes.get(0).getTimeValue() + " seconds            Date:  "
					+ bTimes.get(0).getDateValue());
		}

		JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
		JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
		JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");

		statistics.add(time);
		statistics.add(bestTime);
		statistics.add(gPlayed);
		statistics.add(gWon);
		statistics.add(gPercentage);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		statistics.setBorder(loweredetched);

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 3, 2, 0));

		JButton exit = new JButton("Exit");
		JButton restart = new JButton("Restart");
		JButton playAgain = new JButton("Play Again");

		exit.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			windowClosing(null);
		});
		restart.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			restartGame();
		});
		playAgain.addActionListener((ActionEvent e) -> {
			dialog.dispose();
			newGame();
		});

		buttons.add(exit);
		buttons.add(restart);
		buttons.add(playAgain);

		JPanel c = new JPanel();
		c.setLayout(new BorderLayout(20, 20));
		c.add(message, BorderLayout.NORTH);
		c.add(statistics, BorderLayout.CENTER);
		c.add(buttons, BorderLayout.SOUTH);

		c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
				newGame();
			}
		});

		dialog.setTitle("Game Lost");
		dialog.add(c);
		dialog.pack();
		dialog.setLocationRelativeTo(gui);
		dialog.setVisible(true);
	}

	public void showScore() {

		JDialog dialog = new JDialog(gui, Dialog.ModalityType.DOCUMENT_MODAL);

		JPanel bestTimes = new JPanel();
		bestTimes.setLayout(new GridLayout(5, 1));

		List<Time> bTimes = score.getBestTimes();

		for (int i = 0; i < bTimes.size(); i++) {
			JLabel t = new JLabel("  " + bTimes.get(i).getTimeValue() + "           " + bTimes.get(i).getDateValue());
			bestTimes.add(t);
		}

		if (bTimes.isEmpty()) {
			JLabel t = new JLabel("                               ");
			bestTimes.add(t);
		}

		TitledBorder b = BorderFactory.createTitledBorder("Best Times");
		b.setTitleJustification(TitledBorder.LEFT);

		bestTimes.setBorder(b);

		JPanel statistics = new JPanel();

		statistics.setLayout(new GridLayout(6, 1, 0, 10));

		JLabel gPlayed = new JLabel("  Games Played:  " + score.getGamesPlayed());
		JLabel gWon = new JLabel("  Games Won:  " + score.getGamesWon());
		JLabel gPercentage = new JLabel("  Win Percentage:  " + score.getWinPercentage() + "%");
		JLabel lWin = new JLabel("  Longest Winning Streak:  " + score.getLongestWinningStreak());
		JLabel lLose = new JLabel("  Longest Losing Streak:  " + score.getLongestLosingStreak());
		JLabel currentStreak = new JLabel("  Current Streak:  " + score.getCurrentStreak());

		statistics.add(gPlayed);
		statistics.add(gWon);
		statistics.add(gPercentage);
		statistics.add(lWin);
		statistics.add(lLose);
		statistics.add(currentStreak);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		statistics.setBorder(loweredetched);

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2, 10, 0));

		JButton close = new JButton("Close");
		JButton reset = new JButton("Reset");

		close.addActionListener((ActionEvent e) -> {
			dialog.dispose();
		});
		reset.addActionListener((ActionEvent e) -> {
			URL quest = null;
			try {
				quest = IResourcesLoader.getResourceFileURL("question.png");
			} catch (IOException e1) {
				System.out.println(ExceptionUtils.getExpandedMessage(e1));
			}
			ImageIcon question = new ImageIcon(quest);

			int option = JOptionPane.showOptionDialog(null, "Do you want to reset all your statistics to zero?",
					"Reset Statistics", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, question, null, null);

			switch (option) {
			case JOptionPane.YES_OPTION:

				score.resetScore();
				dialog.dispose();
				showScore();
				break;

			case JOptionPane.NO_OPTION:
				break;
			}
		});

		buttons.add(close);
		buttons.add(reset);

		if (score.getGamesPlayed() == 0)
			reset.setEnabled(false);

		JPanel c = new JPanel();
		c.setLayout(new BorderLayout(20, 20));
		c.add(bestTimes, BorderLayout.WEST);
		c.add(statistics, BorderLayout.CENTER);
		c.add(buttons, BorderLayout.SOUTH);

		c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setTitle("Minesweeper Statistics - José María Vegas Gertrudix");
		dialog.add(c);
		dialog.pack();
		dialog.setLocationRelativeTo(gui);
		dialog.setVisible(true);
	}

	private void showAll() {
		String cellSolution;

		Cell cells[][] = board.getCells();
		JButton buttons[][] = gui.getButtons();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				cellSolution = cells[x][y].getContent();

				if (cellSolution.equals("")) {
					buttons[x][y].setIcon(null);

					cellSolution = Integer.toString(cells[x][y].getSurroundingMines());

					if (cells[x][y].getMine()) {
						cellSolution = "M";

						buttons[x][y].setIcon(gui.getIconMine());
						buttons[x][y].setBackground(Color.lightGray);
					} else {
						if (cellSolution.equals("0")) {
							buttons[x][y].setText("");
							buttons[x][y].setBackground(Color.lightGray);
						} else {
							buttons[x][y].setBackground(Color.lightGray);
							buttons[x][y].setText(cellSolution);
							gui.setTextColor(buttons[x][y]);
						}
					}
				}

				else if (cellSolution.equals("F")) {

					if (!cells[x][y].getMine()) {
						buttons[x][y].setBackground(Color.orange);
					} else
						buttons[x][y].setBackground(Color.green);
				}
			}
		}
	}

	public boolean isFinished() {
		boolean isFinished = true;
		String cellSolution;

		Cell cells[][] = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				cellSolution = Integer.toString(cells[x][y].getSurroundingMines());

				if (cells[x][y].getMine())
					cellSolution = "";

				if (!cells[x][y].getContent().equals(cellSolution)) {
					isFinished = false;
					break;
				}
			}
		}

		return isFinished;
	}

	private void checkGame() {
		if (isFinished()) {
			gameWon();
		}
	}

	public void findZeroes(int xCo, int yCo) {
		int neighbours;

		Cell cells[][] = board.getCells();
		JButton buttons[][] = gui.getButtons();

		for (int x = board.makeValidCoordinateX(xCo - 1); x <= board.makeValidCoordinateX(xCo + 1); x++) {
			for (int y = board.makeValidCoordinateY(yCo - 1); y <= board.makeValidCoordinateY(yCo + 1); y++) {
				if (cells[x][y].getContent().equals("")) {

					neighbours = cells[x][y].getSurroundingMines();

					cells[x][y].setContent(Integer.toString(neighbours));

					if (!cells[x][y].getMine())
						buttons[x][y].setIcon(null);

					if (neighbours == 0) {

						buttons[x][y].setBackground(Color.lightGray);
						buttons[x][y].setText("");
						findZeroes(x, y);
					} else {

						buttons[x][y].setBackground(Color.lightGray);
						buttons[x][y].setText(Integer.toString(neighbours));
						gui.setTextColor(buttons[x][y]);
					}
				}
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (playing) {
			URL quest = null;
			try {
				quest = IResourcesLoader.getResourceFileURL("question.png");
			} catch (IOException e1) {
				System.out.println(ExceptionUtils.getExpandedMessage(e1));
			}
			ImageIcon question = new ImageIcon(quest);
			
			int quit = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Exit Game", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, question);

			switch (quit) {
			case JOptionPane.OK_OPTION:

				System.exit(0);
				break;

			case JOptionPane.CANCEL_OPTION:
				break;
			}
		} else
			System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem menuItem = (JMenuItem) e.getSource();

		if (menuItem.getName().equals("New Game")) {
			if (playing) {
				URL quest = null;
				try {
					quest = IResourcesLoader.getResourceFileURL("question.png");
				} catch (IOException e1) {
					System.out.println(ExceptionUtils.getExpandedMessage(e1));
				}
				ImageIcon question = new ImageIcon(quest);

				Object[] options = { "Quit and Start a New Game", "Restart", "Keep Playing" };

				int startNew = JOptionPane.showOptionDialog(null, "What do you want to do with the game in progress?",
						"New Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, question, options,
						options[2]);

				switch (startNew) {
				case JOptionPane.YES_OPTION:

					newGame();
					score.incGamesPlayed();
					break;

				case JOptionPane.NO_OPTION:
					score.incGamesPlayed();
					restartGame();
					break;

				case JOptionPane.CANCEL_OPTION:
					break;
				}
			}
		}

		else if (menuItem.getName().equals("Exit")) {
			windowClosing(null);
		}

		else {
			showScore();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!playing) {
			gui.startTimer();
			playing = true;
		}

		if (playing) {

			JButton button = (JButton) e.getSource();

			String[] co = button.getName().split(",");

			int x = Integer.parseInt(co[0]);
			int y = Integer.parseInt(co[1]);

			boolean isMine = board.getCells()[x][y].getMine();
			int neighbours = board.getCells()[x][y].getSurroundingMines();

			if (SwingUtilities.isLeftMouseButton(e)) {
				if (!board.getCells()[x][y].getContent().equals("F")) {
					button.setIcon(null);

					if (isMine) {

						button.setIcon(gui.getIconRedMine());
						button.setBackground(Color.red);
						board.getCells()[x][y].setContent("M");

						gameLost();
					} else {
						
						board.getCells()[x][y].setContent(Integer.toString(neighbours));
						button.setText(Integer.toString(neighbours));
						gui.setTextColor(button);

						if (neighbours == 0) {
							
							button.setBackground(Color.lightGray);
							button.setText("");
							findZeroes(x, y);
						} else {
							button.setBackground(Color.lightGray);
						}
					}
				}
			}
			
			else if (SwingUtilities.isRightMouseButton(e)) {
				if (board.getCells()[x][y].getContent().equals("F")) {
					board.getCells()[x][y].setContent("");
					button.setText("");
					button.setBackground(new Color(0, 110, 140));

					button.setIcon(gui.getIconTile());
					gui.incMines();
				} else if (board.getCells()[x][y].getContent().equals("")) {
					board.getCells()[x][y].setContent("F");
					button.setBackground(Color.blue);

					button.setIcon(gui.getIconFlag());
					gui.decMines();
				}
			}

			checkGame();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}