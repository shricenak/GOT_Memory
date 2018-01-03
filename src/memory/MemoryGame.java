package memory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * A graphical representation of the Memory game featuring characters and music
 * from the HBO series Game of Thrones.
 *
 * @author Steven Hricenak
 */
public class MemoryGame {

    private MemoryEngine me;
    private Point firstTile = null;
    private JButton[][] buttons;
    private JFrame frame;
    private ImageIcon cardback;
    private Color backgroundColor;
    private boolean clickable; //Prevents selection of 3+ tiles
    private SongPlayer sp;
    private Timer gameTimer;

    /**
     * Creates and displays the title screen, which will lead to the rest of the
     * game. Contained a glitch where the width was passed as the height and the
     * height was passed for the width. To account for this, the two arguments
     * simply switched places.
     */
    public MemoryGame() {
        frame = new JFrame("Memory");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setResizable(false);

        cardback = new ImageIcon("images/cardback0.jpg");
        backgroundColor = Color.BLACK;
        ImageIcon bg = new ImageIcon("images/background.jpg");
        JLabel bgLabel = new JLabel(bg);

        JPanel settingsPanel = new JPanel();

        JLabel widthLabel = new JLabel("Width: ");
        String[] str = {"2", "3", "4", "5", "6"};
        JComboBox widthBox = new JComboBox(str);
        widthBox.setSelectedItem("4");
        JLabel heightLabel = new JLabel("Height: ");
        String[] str2 = {"2", "4", "6"};
        JComboBox heightBox = new JComboBox(str2);
        heightBox.setSelectedItem("4");

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(new ActionListener() {

            /**
             * Takes the values in the dropdown menus and creates a starts a new
             * memory game with them.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                String widthStr = (String) widthBox.getSelectedItem();
                int width = Integer.parseInt(widthStr);
                String heightStr = (String) heightBox.getSelectedItem();
                int height = Integer.parseInt(heightStr);

                frame.remove(settingsPanel);
                frame.remove(bgLabel);
                newGame(height, width);
            }

        });

        settingsPanel.add(widthLabel);
        settingsPanel.add(widthBox);
        settingsPanel.add(heightLabel);
        settingsPanel.add(heightBox);
        settingsPanel.add(startButton);

        frame.add(bgLabel, "Center");
        frame.add(settingsPanel, "South");
        frame.setVisible(true);

        gameTimer = new Timer(500, null);
        //changeSong(1);
    }

    /**
     * Creates a new board of the memory game.
     *
     * @param w width of the board
     * @param h height of the board
     */
    private void newGame(int w, int h) {
        JPanel timerPanel = new JPanel();
        timerPanel.add(new JLabel("Time: "));
        JTextField gameTimeField = new JTextField();
        gameTimeField.setEditable(false);
        gameTimeField.setPreferredSize(new Dimension(50, 25));
        timerPanel.add(gameTimeField);

        timerPanel.add(new JLabel("Best time: "));
        JTextField bestTimeField = new JTextField();
        bestTimeField.setEditable(false);
        bestTimeField.setPreferredSize(new Dimension(50, 25));
        bestTimeField.setText("59:59");
        timerPanel.add(bestTimeField);

        /**
         * Extends the Timer class in order to hold two extra variables and a
         * method that is to be referenced in an action listener (which can't
         * mutate the variable itself).
         */
        class GameClock extends Timer {

            private int timePassed;
            private int bestTime;
            private boolean scoreUpdated;
            //Needed to prevent glitch when reseting best time.

            public GameClock() {
                super(1000, null);

                this.addActionListener(new ActionListener() {

                    /**
                     * Increments and displays the seconds since the game
                     * started. Also checks if game is won.
                     */
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (!me.gameIsWon()) {
                            timePassed++;
                        } else if (timePassed < bestTime && !scoreUpdated) {
                            bestTime = timePassed;
                            bestTimeField.setText(timeFormat(bestTime));
                            scoreUpdated = true;
                        }
                        gameTimeField.setText(timeFormat(timePassed));
                    }

                });

                timePassed = 0;
                bestTime = 3599;
                scoreUpdated = false;
            }

            /**
             * Resets the timer to zero seconds.
             */
            public void resetTime() {
                timePassed = 0;
                scoreUpdated = false;
            }

            /**
             * Resets the high score.
             */
            public void resetBest() {
                bestTime = 3599;
                bestTimeField.setText("59:59");
            }
        }

        GameClock playTimer = new GameClock();

        gameTimer.addActionListener(new TimerListener(w, h));
        
        me = new MemoryEngine(w, h);

        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(w, h));

        firstTile = null;

        buttons = new JButton[w][h];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                buttons[i][j] = new JButton(cardback);
                buttons[i][j].addActionListener(new ButtonListener(i, j));
                buttons[i][j].setFocusable(false);
                gamePanel.add(buttons[i][j]);
            }
        }

        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");

        JMenuItem resetItem = new JMenuItem("Reset Game");
        resetItem.addActionListener(new ActionListener() {

            /**
             * Resets the board and time.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                playTimer.resetTime();
                playTimer.restart();
                firstTile = null;
                me = new MemoryEngine(w, h);
            }

        });

        JMenuItem newBoardItem = new JMenuItem("New Board");
        newBoardItem.addActionListener(new ActionListener() {

            /**
             * Calls a helper method that will create a new board size.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                getNewSize();
                gameTimer.stop();
            }

        });

        JMenu speedMenu = new JMenu("Speed");
        JMenuItem[] speedItem = new JMenuItem[3];
        
        speedItem[0] = new JMenuItem("Slow");
        speedItem[1] = new JMenuItem("Normal");
        speedItem[2] = new JMenuItem("Fast");
        
        /**
         * Changes the speed at which the cards stay revealed.
         */
        class SpeedListener implements ActionListener{

            private int speed;
            
            public SpeedListener(int speed){
                this.speed = speed;
            }
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                gameTimer.setInitialDelay(750 - 250*speed);
            }
            
        }
        
        for (int i = 0; i < 3; i++) {
            speedItem[i].addActionListener(new SpeedListener(i));
            speedMenu.add(speedItem[i]);
        }
        
        JMenuItem resetBestItem = new JMenuItem("Reset Best Time");
        resetBestItem.addActionListener(new ActionListener() {

            /**
             * Resets the best time.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                playTimer.resetBest();
            }

        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {

            /**
             * Closes the game.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(1); //Closes Java
            }
        });

        gameMenu.add(resetItem);
        gameMenu.add(newBoardItem);
        gameMenu.add(speedMenu);
        gameMenu.add(resetBestItem);
        gameMenu.add(exitItem);

        JMenu cardMenu = new JMenu("Cards");

        JMenuItem[] cardItems = new JMenuItem[4];

        cardItems[0] = new JMenuItem("Stark (Direwolf)");
        cardItems[1] = new JMenuItem("Lannister (Lion)");
        cardItems[2] = new JMenuItem("Baratheon (Stag)");
        cardItems[3] = new JMenuItem("Targaryen (Dragon)");

        /**
         * A listener that will change the backs of the cards depending on the
         * value passed to it in the constructor.
         */
        class CardBackListener implements ActionListener {

            int cardNum;

            public CardBackListener(int cardNum) {
                this.cardNum = cardNum;
            }

            /**
             * Changes the image on the back of the cards, depending on the item
             * selected.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                changeCardBacks(cardNum);
            }
        }

        for (int i = 0; i < 4; i++) {
            cardItems[i].addActionListener(new CardBackListener(i));
            cardMenu.add(cardItems[i]);
        }

        JMenu musicMenu = new JMenu("Music");

        JMenu songMenu = new JMenu("Song");

        JMenuItem[] songsItem = new JMenuItem[3];

        songsItem[0] = new JMenuItem("Standard");
        songsItem[1] = new JMenuItem("Rock");
        songsItem[2] = new JMenuItem("8-bit");

        /**
         * Creates listeners for the song options.
         */
        class SongListener implements ActionListener {

            private int songID;

            public SongListener(int songID) {
                this.songID = songID + 1;
                //+1 added because audio files begin at 1, not 0
            }

            /**
             * Changes the song being played, based on the song number passed.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                sp.pause();
                changeSong(songID);
            }

        }

        for (int i = 0; i < 3; i++) {
            songsItem[i].addActionListener(new SongListener(i));
            songMenu.add(songsItem[i]);
        }

        JCheckBoxMenuItem musicOn = new JCheckBoxMenuItem("Play Music");
        if (sp != null) {
            musicOn.setSelected(true);
        } else {
            musicOn.setSelected(false);
        }
        musicOn.addItemListener(new ItemListener() {

            /**
             * Plays music if checked, and stops music if unchecked.
             */
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    changeSong(1);
                    songMenu.setEnabled(true);
                } else {
                    sp.pause();
                    sp = null;
                    songMenu.setEnabled(false);
                }
            }

        });

        musicMenu.add(musicOn);
        musicMenu.add(songMenu);

        JMenu helpMenu = new JMenu("Help");

        JMenuItem helpItem = new JMenuItem("How to Play");
        helpItem.addActionListener(new ActionListener() {

            /**
             * Displays a text box with rules for the game.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                String message = "Welcome to the Seven Kingdoms of Westeros, "
                        + "where there are (too) many faces to remember.\n"
                        + "Click on a card to reveal the picture, then try "
                        + "to find its match. If you guess correctly, the two\n"
                        + "images will remain revealed, but if you choose "
                        + "wrong, they will only stay revealed for a brief "
                        + "moment.\nOnce you match all the pictures, you've "
                        + "won, so try again to beat your score, or maybe "
                        + "try another board size.\n"
                        + "You can also choose different card backs, change "
                        + "the music, or mute it completely.\nGood luck!";
                JOptionPane.showMessageDialog(frame, message);
            }

        });

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {

            /**
             * Displays a text box with information about the program.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                String message = "Game created by Steven Hricenak\n\n"
                        + "Based on the original game by Hasbro\n\n"
                        + "Theme based on HBO's 'Game of Thrones'\n"
                        + "which is in turn based on George R. R. \n"
                        + "Martin's 'A Song of Ice and Fire' series\n\n"
                        + "Game of Thrones Theme Song by Ramin Djawadi\n\n"
                        + "Rock remix by Benjamin Belmonte\n\n"
                        + "8-bit remix by 'Floating Point Music'\n\n"
                        + "Thanks for playing!";
                JOptionPane.showMessageDialog(frame, message);
            }

        });

        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(cardMenu);
        //menuBar.add(musicMenu);
        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);

        playTimer.start();
        gameTimer.start();

        frame.add(timerPanel, "North");
        frame.add(gamePanel, "Center");
        frame.setVisible(true);
    }

    /**
     * A helper method that creates a board of a new size.
     */
    private void getNewSize() {
        JFrame optionsFrame = new JFrame("New Board");
        JPanel optionsPanel = new JPanel();

        JLabel widthLabel = new JLabel("Width: ");
        String[] str = {"2", "3", "4", "5", "6"};
        JComboBox widthBox = new JComboBox(str);
        widthBox.setSelectedItem("4");
        JLabel heightLabel = new JLabel("Height: ");
        String[] str2 = {"2", "4", "6"};
        JComboBox heightBox = new JComboBox(str2);
        heightBox.setSelectedItem("4");

        JButton startButton = new JButton("Start New Game");
        startButton.addActionListener(new ActionListener() {

            /**
             * Takes the values in the dropdown menus and creates a starts a new
             * memory game with them.
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                String widthStr = (String) widthBox.getSelectedItem();
                int width = Integer.parseInt(widthStr);
                String heightStr = (String) heightBox.getSelectedItem();
                int height = Integer.parseInt(heightStr);

                frame.getContentPane().removeAll();
                
                for (ActionListener al : gameTimer.getActionListeners()) {
                    gameTimer.removeActionListener(al);
                }
                //Removes timer listener, so the one of correct size can
                //be added in the newGame method
                
                newGame(height, width);
                optionsFrame.dispose();
            }

        });

        optionsPanel.add(widthLabel);
        optionsPanel.add(widthBox);
        optionsPanel.add(heightLabel);
        optionsPanel.add(heightBox);
        optionsPanel.add(startButton);

        optionsFrame.add(optionsPanel);
        optionsFrame.setSize(200, 100);
        Point center = new Point(frame.getX() + 200, frame.getY() + 250);
        optionsFrame.setLocation(center);
        optionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        optionsFrame.setVisible(true);
        optionsFrame.setAlwaysOnTop(true);
    }

    /**
     * Converts the number of seconds passed in to a 00:00 format.
     */
    private String timeFormat(int time) {
        DecimalFormat df = new DecimalFormat("00");
        int seconds = time % 60;
        int minutes = time / 60;
        return df.format(minutes) + ":" + df.format(seconds);
    }

    /**
     * Changes the image that is displayed on the back of the cards.
     */
    private void changeCardBacks(int cardNum) {
        cardback = new ImageIcon("images/cardback" + cardNum + ".jpg");
        switch (cardNum) {
            case 1:
                backgroundColor = new Color(237, 28, 36);
                break;
            case 2:
                backgroundColor = new Color(243, 193, 20);
                break;
            default:
                backgroundColor = Color.BLACK;
        }
        
        for (JButton[] button : buttons) {
            for (JButton button1 : button) {
                button1.setBackground(backgroundColor);
            }
        }
    }

    /**
     * Flips all unmatched cards to their backs. Helper method used by timer
     * listener.
     *
     * @param w width of board
     * @param h height of board
     */
    private void flipCards(int w, int h) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (!me.tileIsMatched(i, j)) {
                    buttons[i][j].setIcon(cardback);
                    buttons[i][j].setBackground(backgroundColor);
                }
            }
        }
    }

    /**
     * Changes/starts a song.
     *
     * @param i the song's number (1: Standard, 2: Rock, 3: 8-bit)
     */
    private void changeSong(int i) {
        sp = new SongPlayer(i);
        sp.start(null);
    }

    /**
     * A class that returns all unmatched cards to their back sides.
     */
    class TimerListener implements ActionListener {

        private int width;
        private int height;

        public TimerListener(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Returns unmatched cards to their back sides.
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (firstTile == null) {
                flipCards(width, height);
                clickable = true;
            }
        }

    }

    /**
     * Contains the listener for all the tile buttons in the game.
     */
    class ButtonListener implements ActionListener {

        private int row;
        private int col;

        public ButtonListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        /**
         * Reveals the tile, and if it is the second tile revealed, checks if
         * the two are a match.
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (clickable) {
                if (firstTile == null) {
                    firstTile = new Point(row, col);
                } else if (buttons[firstTile.x][firstTile.y]
                        != buttons[row][col]) {
                    me.compare(firstTile.x, firstTile.y, row, col);
                    firstTile = null;
                    clickable = false;
                    gameTimer.restart();
                }

                int id = me.getTileID(row, col);
                String imageName = "images/Image"
                        + Integer.toString(id) + ".jpg";
                ImageIcon icon = new ImageIcon(imageName);
                buttons[row][col].setIcon(icon);
            }
        }

    }

    public static void main(String[] args) {
        new MemoryGame();
    }

}
