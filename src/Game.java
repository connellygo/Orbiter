import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Game extends JPanel implements KeyListener, MouseListener{
	
	//Constants
	private static final long serialVersionUID = 1L;
	public static final int CENTER = Orbiter.WINDOWSIZE / 2;
	public static final int RADIUS = 250;
	public static final int EARTHSIZE = 128;
	public static final int ROCKETSIZE = 64;
	public static final int PROJECTILESIZE = 16;
	public static final int SPAWNSPEED = 25;
	public static final Color BACKGROUNDCOLOR = new Color(0, 0, 40);
	public static final int STARSPACING = 60;

	
	//Images
	private BufferedImage rocketImg;
	private BufferedImage rocketReverseImg;
	private BufferedImage earthImg;
	private BufferedImage projectileImg;
	private BufferedImage pausedButtonImg;
	private BufferedImage startButtonImg;
	private BufferedImage exitButtonImg;
	private BufferedImage starImg;
	private BufferedImage healthImg;
	private BufferedImage backButtonImg;
	private BufferedImage scoresButtonImg;
	private BufferedImage titleImg;

	//Variables
	private ArrayList<Rocket> rockets; //An arraylist to hold the rocket objects.
	private ArrayList<Projectile> projectiles; //An arraylist to hold the projectile objects.
	private boolean paused; //Is the game paused
	private String gameState; //String that represents the state of the game. Menu, Instructions, Game, etc.
    private int score; //Keeps track of the score.
    private int alpha; //Used for transition between screens
	private boolean transitioningTo; //Used for transition between scenes.
	private String nextState; //Used for transition between scenes.
	private Polygon startButtonPoly; //Polygon used for clicking on start button.
	private Polygon exitButtonPoly; //Polygon used for clicking on exit button.
	private Polygon exitGameButtonPoly; //Polygon used for clicking on exit button.
    private Polygon scoresButtonPoly; //Polygon used for clicking on high scores button.
	private Polygon backButtonPoly; //Polygon used for navigating to menu.
    private Point[][] starLocations; //Holds locations for star images.
	private ArrayList<Point> health; //Holds the locations for health packs.
	private int counter = 0; //Incremented every frame. Used for timing projectile and health spawns.
	private ArrayList<Highscore> highscores; //Keeps track of the top 10 scores;
    private String newName; //Name of the new high score.
	private boolean editing; //Whether or not they are entering in a new high score.
	
	public Game() {
		//Load all sprites needed for objects.
		loadImages();

		//Reads the high scores in from file.
		loadScores();

		//Resets the rocket, projectile and gameState.
		reset();
		gameState = "menu";

		//Create polygon objects for clickable buttons.
		createButtonPolygons();

		//Generate locations for stars
		placeStars();

        //Set listeners for the key presses and mouse clicks.
		setFocusable(true); 
		addMouseListener(this);
		addKeyListener(this);
	}
	
	//Runs the game. Used for moving all objects during the game and transition screens.
	public void start() throws InterruptedException {
		while(true) {
		    if(gameState.equals("game")) {
                //If the game isn't paused.
                if (!paused) {
                    //Move the rockets.
                    for (Rocket r : rockets) r.move();

                    //Move the projectiles.
                    for (Projectile p : projectiles) p.move();

                    checkCollisions();

                    //If rocket loses all health, return to main menu
                    if (rockets.get(0).getHealth() <= 0) gameState = "game over";

                    //Spawn a projectile every second.
                    if (counter == SPAWNSPEED) {
                        spawnProjectile();
                        counter = 0;
                        score++;

                        //Spawn health
						if(score % 50 == 0){
							spawnHealth();
						}

                    } else {
                        counter++;
                    }
                }
            } else if(gameState.equals("game over")){
		    	alpha += 2;
		        if(alpha == 254){
		            if(highscores.size() < 10 || score > highscores.get(0).score){
		            	if(highscores.size() != 0) {
		            		int size = highscores.size();
							for (int i = 0; i < size; i++) {
								if (highscores.get(i).score > score) {
									highscores.add(i, new Highscore("", score));
									break;
								} else if(i == highscores.size() - 1){
									highscores.add(new Highscore("", score));
								}
							}
						} else{
		            		highscores.add(new Highscore("", score));
						}
						gameState = "scores";
		                newName = "";
		                editing = true;
                    }
		            else {
                        nextState = "menu";
                        transitioningTo = false;
                    }
                } else {
                    alpha += 2;
                }
            }
            if (!transitioningTo){
		    	alpha += 2;
		    	if(alpha >= 254){
		    		gameState = nextState;
		    		transitioningTo = true;
		    		alpha = 254;
				}
			}
			else if(transitioningTo){
				if(alpha > 0) {
					alpha -= 2;
				} else {
					alpha = 0;
				}
			}

			//Redraw everything
			repaint();
	
			//Wait .01 seconds.
			Thread.sleep(10);
		}
		
	}

	//Loads top scores from file.
	private void loadScores(){
		highscores = new ArrayList<Highscore>();
        try{
            BufferedReader inputStream = new BufferedReader(new FileReader("highscores.dat"));
            String line;
            while((line = inputStream.readLine()) != null){
                String[] lineSplit = line.split(", ");
                int inScore = Integer.parseInt(lineSplit[1]);
                highscores.add(new Highscore(lineSplit[0], inScore));
            }
        } catch(FileNotFoundException e){
			System.out.println("Could not find highscores file.");
        } catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Saves scores to file.
	private void saveScores(){
		try {
			//Open highscores.dat as location to save scores.
			BufferedWriter outputStream = new BufferedWriter(new FileWriter("highscores.dat"));

			//Iterate over the highscores
			for (Highscore hs : highscores) {
				//Write the name associated with each score as well as the score.
				String highscoreString = hs.name + ", " + Integer.toString(hs.score) + "\n";
				outputStream.write(highscoreString);
			}
			outputStream.close();
		} catch(Exception e){
			System.out.println("Could not find file. Scores were not saved.");
		}
	}

	//Places a health object randomly on the rocket path.
	private void spawnHealth() {
		Random r = new Random();
		double angle = Math.toRadians(r.nextInt(360));
		health.add(new Point((int) (CENTER + RADIUS * Math.cos(angle)), (int) (CENTER + RADIUS * Math.sin(angle))));
	}

	//Resets the rocket, projectile and gameState.
	public void reset(){
        //Initialize the rockets.
        rockets = new ArrayList<Rocket>();
        rockets.add(new Rocket(RADIUS, 0, 1, 1));

        health = new ArrayList<Point>();

        //Initialize the arraylist for current projectiles.
        projectiles = new ArrayList<Projectile>();

        counter = -200;

        //Set the game state to the main menu
        paused = false;
        editing = false;
        score = 0;

		//Fade in value
        alpha = 254;
        transitioningTo = true;
    }

    //Check to see whether the the rocket has collided with a health object or projectile.
	//Also removes projectiles that have gone off screen.
	private void checkCollisions() {
		Set<Projectile> removedProjectiles = new HashSet<Projectile>(); //Create set of projectiles that will be removed.
		Set<Point> removedHealth = new HashSet<Point>(); //Create set of health packs that will be removed.

		for(Rocket r : rockets) {
			Polygon rocketHitbox = createRocketPolygon(r); //Creates polygon representing the hitbox for the rocket.

			for(Projectile p : projectiles) {
				//rocket collides with projectile, take damage.
				if(rocketHitbox.contains(CENTER + p.getX(), CENTER + p.getY())) {
					removedProjectiles.add(p); //Add projectile to the set that will be removed.
					r.takeDamage(25);
				}
				
				//If the projectile goes off screen.
				if(p.getX() < -1 * Orbiter.WINDOWSIZE || p.getX() > Orbiter.WINDOWSIZE || p.getY() < -1 * Orbiter.WINDOWSIZE || p.getY() > Orbiter.WINDOWSIZE) {
					removedProjectiles.add(p); //Add projectile to the set that will be removed.
				}
			}

			//If rocket takes health, heal rocket.
			for(Point p : health){
				if(rocketHitbox.contains(p)){
					removedHealth.add(p); //Add health pack to the set that will be removed.
					r.takeDamage(-10); //Heal rocket.
				}
			}
			
		}
		for(Projectile p : removedProjectiles) projectiles.remove(p); //remove projectiles from list.
		for(Point p : removedHealth) health.remove(p); //remove taken health from list.
	}

	//Creates polygon objects for each button.
	private void createButtonPolygons(){
        //Boundaries for start button on menu.
        int startButtonXPos[] = {CENTER + 36, CENTER + 164,CENTER + 164, CENTER + 36};
        int startButtonYPos[] = {CENTER - 45, CENTER - 45, CENTER - 5, CENTER - 5};
        startButtonPoly = new Polygon(startButtonXPos, startButtonYPos, 4);

        //Boundaries for scores button on menu.
        int scoreButtonXPos[] = {CENTER + 36, CENTER + 164,CENTER + 164, CENTER + 36};
        int scoreButtonYPos[] = {CENTER + 6, CENTER + 6, CENTER + 46, CENTER + 46};
        scoresButtonPoly = new Polygon(scoreButtonXPos, scoreButtonYPos, 4);

        //Boundaries for exit button on menu.
        int exitButtonXPos[] = {CENTER + 36, CENTER + 164, CENTER + 164, CENTER + 36};
        int exitButtonYPos[] = {CENTER + 57, CENTER + 57, CENTER + 97, CENTER + 97};
		exitButtonPoly = new Polygon(exitButtonXPos, exitButtonYPos, 4);

		//Boundaries for back buttons.
		int backButtonXPos[] = {32, 160, 160, 32};
		int backButtonYPos[] = {Orbiter.WINDOWSIZE - 72, Orbiter.WINDOWSIZE - 72, Orbiter.WINDOWSIZE - 32, Orbiter.WINDOWSIZE - 32};
		backButtonPoly = new Polygon(backButtonXPos, backButtonYPos, 4);

		//Boundaries for exit game button.
		int exitGameButtonXPos[] = {32, 160, 160, 32};
		int exitGameButtonYPos[] = {Orbiter.WINDOWSIZE - 72, Orbiter.WINDOWSIZE - 72, Orbiter.WINDOWSIZE - 32, Orbiter.WINDOWSIZE - 32};
		exitGameButtonPoly = new Polygon(exitGameButtonXPos, exitGameButtonYPos, 4);    }

    //Create hitbox for rocket
	private Polygon createRocketPolygon(Rocket r) {
		int[] xpoints = new int[4];
		int[] ypoints = new int[4];
		
		//Set the starting points for rocket hitbox.
		xpoints[0] = (int) (r.radius1 * Math.cos(r.angle1 + Math.toRadians(r.getAngle())));
		ypoints[0] = (int) (r.radius1 * Math.sin(r.angle1 + Math.toRadians(r.getAngle())));
		xpoints[1] = (int) (r.radius2 * Math.cos(r.angle2 + Math.toRadians(r.getAngle())));
		ypoints[1] = (int) (r.radius2 * Math.sin(r.angle2 + Math.toRadians(r.getAngle())));
		xpoints[2] = (int) (r.radius3 * Math.cos(r.angle3 + Math.toRadians(r.getAngle())));
		ypoints[2] = (int) (r.radius3 * Math.sin(r.angle3 + Math.toRadians(r.getAngle())));
		xpoints[3] = (int) (r.radius4 * Math.cos(r.angle4 + Math.toRadians(r.getAngle())));
		ypoints[3] = (int) (r.radius4 * Math.sin(r.angle4 + Math.toRadians(r.getAngle())));
		
		for(int i = 0; i < 4; i++) {
			xpoints[i] += CENTER;
			ypoints[i] += CENTER;
		}

		return new Polygon(xpoints, ypoints, 4);
	}

	//Places stars randomly in the background.
	private void placeStars(){
	    int rowSize = Orbiter.WINDOWSIZE / STARSPACING;
        starLocations = new Point[rowSize][rowSize];
        Random r = new Random();
	    for (int i = 0; i < rowSize; i++){
	        for(int j = 0; j < rowSize; j++){
                starLocations[i][j] = new Point(i * STARSPACING + r.nextInt(STARSPACING - 8), j * STARSPACING + r.nextInt(STARSPACING - 8));
            }
        }
    }

    //Spawns projectiles that move in a random direction.
	private void spawnProjectile() {
		Random r = new Random();
		projectiles.add(new Projectile(2.5 + 2.5 * (1 -  1 / (1 + score / 200.)) * (2 * r.nextFloat() - 1), r.nextInt(360)));
	}

	//Loads all images used in the project.
	private void loadImages() {
		  try {
			  rocketImg = ImageIO.read(getClass().getResourceAsStream("img/rocket.png"));
		  } catch (IOException e) {
			  rocketImg = null;
			  e.printStackTrace();
		  }
		  try {
			  earthImg = ImageIO.read(getClass().getResourceAsStream("img/earth.png"));
		  } catch (IOException e) {
			  earthImg = null;
			  e.printStackTrace();
		  }
		  
		  try {
			  rocketReverseImg = ImageIO.read(getClass().getResourceAsStream("img/rocketReverse.png"));
		  } catch (IOException e) {
			  rocketReverseImg = null;
			  e.printStackTrace();
		  }
		  
		  try {
			  projectileImg = ImageIO.read(getClass().getResourceAsStream("img/projectile.png"));
		  } catch (IOException e) {
			  projectileImg = null;
			  e.printStackTrace();
		  }
		  
		  try {
			  pausedButtonImg = ImageIO.read(getClass().getResourceAsStream("img/pausedButton.png"));
		  } catch (IOException e) {
			  pausedButtonImg = null;
			  e.printStackTrace();
		  }

		try {
			startButtonImg = ImageIO.read(getClass().getResourceAsStream("img/startButton.png"));
		} catch (IOException e) {
			startButtonImg = null;
			e.printStackTrace();
		}

		try {
			scoresButtonImg = ImageIO.read(getClass().getResourceAsStream("img/scoresButton.png"));
		} catch (IOException e) {
			scoresButtonImg = null;
			e.printStackTrace();
		}

		try {
			backButtonImg = ImageIO.read(getClass().getResourceAsStream("img/backButton.png"));
		} catch (IOException e) {
			backButtonImg = null;
			e.printStackTrace();
		}

		try {
			exitButtonImg = ImageIO.read(getClass().getResourceAsStream("img/exitButton.png"));
		} catch (IOException e) {
			exitButtonImg = null;
			e.printStackTrace();
		}

        try {
            starImg = ImageIO.read(getClass().getResourceAsStream("img/star.png"));
        } catch (IOException e) {
            starImg = null;
            e.printStackTrace();
        }

		try {
			healthImg = ImageIO.read(getClass().getResourceAsStream("img/health.png"));
		} catch (IOException e) {
			healthImg = null;
			e.printStackTrace();
		}

		try {
			titleImg = ImageIO.read(getClass().getResourceAsStream("img/title.png"));
		} catch (IOException e) {
			titleImg = null;
			e.printStackTrace();
		}

	}

	//Draws objects to the window.
	public void paintComponent(Graphics g) {
        //Background color
        g.setColor(BACKGROUNDCOLOR);
        g.fillRect(0, 0, Orbiter.WINDOWSIZE, Orbiter.WINDOWSIZE);

        //Draw Stars
        for(int i = 0; i < starLocations.length; i++){
            for(int j = 0; j < starLocations.length; j++){
                g.drawImage(starImg, starLocations[i][j].x, starLocations[i][j].y, 8, 8, null);
            }
        }

        //If at the menu.
		if(gameState.equals("menu")) {
			//Draw start button
			g.drawImage(startButtonImg, CENTER + 36, CENTER - 89,128,128, null);

			//Draw scores button
			g.drawImage(scoresButtonImg, CENTER + 36, CENTER - 39, 128, 128, null);

			//Draw exit button
			g.drawImage(exitButtonImg, CENTER + 36, CENTER + 12, 128, 128, null);

			//Earth for aesthetics and stuff.
			g.drawImage(earthImg, CENTER - EARTHSIZE - 75, CENTER - EARTHSIZE / 2, EARTHSIZE, EARTHSIZE, null);

			//Title "Orbiter"
			g.drawImage(titleImg, (Orbiter.WINDOWSIZE - 532) / 2, 20, 512, 128, null);

		}
		//If playing the game.
		else if(gameState.equals("game") || gameState.equals("game over")) {

            //Draw health packs
			for(Point p : health) {
				g.drawImage(healthImg, p.x - 12, p.y - 12, 24, 24, null);
			}

			//Draw projectiles
			for(Projectile p : projectiles) {
				g.drawImage(projectileImg, CENTER + p.getX() - PROJECTILESIZE / 2, CENTER + p.getY() - PROJECTILESIZE / 2, PROJECTILESIZE, PROJECTILESIZE, null);
			}


			//Draw Earth
			g.drawImage(earthImg, CENTER - EARTHSIZE / 2, CENTER - EARTHSIZE / 2, EARTHSIZE, EARTHSIZE, null);

			//draw rockets
			Graphics2D g2d=(Graphics2D)g; // Create a Java2D version of g.
			for(Rocket r : rockets) {
				int x = CENTER + r.getX();
				int y = CENTER + r.getY();
				g2d.rotate(Math.toRadians(r.getAngle() + 90), x, y);  // Rotate the image.
				if (r.getDirection() == 1) g2d.drawImage(rocketImg, x - ROCKETSIZE / 2, y - ROCKETSIZE / 2, ROCKETSIZE, ROCKETSIZE, null);
				else g2d.drawImage(rocketReverseImg, x - ROCKETSIZE / 2, y - ROCKETSIZE / 2, ROCKETSIZE, ROCKETSIZE, null);
				g2d.rotate(Math.toRadians(-(r.getAngle() + 90)), x, y);  // Rotate the image.
			}

			//Pause button
			if(paused) {
				//Draw paused box
				g.drawImage(pausedButtonImg, CENTER - 64, CENTER - 64, 128, 128, null);

				//Draw exit button
				g.drawImage(exitButtonImg, exitGameButtonPoly.xpoints[0], exitGameButtonPoly.ypoints[0] - 45, 128, 128, null);

			}

            //Missing health bar
			g.setColor(Color.RED);
			g.fillRect(25, 25, 100, 25);

			//Remaining health bar
			g.setColor(Color.GREEN);
			g.fillRect(25, 25, rockets.get(0).getHealth(), 25);

			//Score display
			g.setColor(Color.WHITE);
			String scoreString = "SCORE: " + Integer.toString(score);
			g.setFont(new Font("Arial", Font.PLAIN, 20));
            FontMetrics fm = g.getFontMetrics();
			g.drawString(scoreString, (Orbiter.WINDOWSIZE - fm.stringWidth(scoreString)) / 2, fm.getHeight());

		}
		//If viewing or creating a new highscore.
		else if(gameState.equals("scores")){
        	g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, 30));
			if(editing){
				g.drawString("NEW HIGH SCORE:", 40, 40);
				if(highscores.size() > 10) highscores.remove(0);
			} else {
				g.drawString("HIGH SCORES", 40, 40);
			}
			g.setFont((new Font("Arial", Font.BOLD, 28)));
			for(int i = highscores.size() - 1; i >= 0; i--) {
                String number = Integer.toString(highscores.size() - i);
                if(!number.equals("10")) number = "  " + number; //Add space before single digit numbers
                String score = Integer.toString(highscores.get(i).score);
                String name = highscores.get(i).name;
                if (!editing) {
                    g.drawString(number + ". " + name + " - " + score, 100, (highscores.size() - i + 1) * 45);
                } else {
                    if (name == "") {
                        g.setColor(Color.YELLOW);
                        g.drawString(number + ". " + newName + " - " + score, 100, (highscores.size() - i + 1) * 45);
                        g.setColor(Color.WHITE);
                    } else {
                        g.drawString(number + ". " + name + " - " + score, 100, (highscores.size() - i + 1) * 45);
                    }
                }
            }

			g.drawImage(backButtonImg, backButtonPoly.xpoints[0], backButtonPoly.ypoints[0] - 45, 128, 128, null);
        }
		//Fade in and out.
		g.setColor(new Color(BACKGROUNDCOLOR.getRed(), BACKGROUNDCOLOR.getGreen(), BACKGROUNDCOLOR.getBlue(), alpha));
		g.fillRect(0,0, Orbiter.WINDOWSIZE, Orbiter.WINDOWSIZE);
	}

	//Not used, but necessary for compiling.
	@Override 
	public void mouseClicked(MouseEvent arg0) {
		
		
	}

	//Not used, but necessary for compiling.
	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	//Not used, but necessary for compiling.
	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	//Not used, but necessary for compiling.
	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	//Called on mouse click. Used for checking if a button was clicked or changing the direction of the rocket.
	@Override
	public void mouseReleased(MouseEvent arg0) {
	    if(gameState.equals("menu")){ //While on menu.
            //If you click on start button, start game.
	    	if(startButtonPoly.contains(arg0.getPoint())){
	    		reset();
	    	    alpha = 0;
	    		nextState = "game";
	    		transitioningTo = false;
            }
            //If you click the exit button, get a small tutorial.
            else if(exitButtonPoly.contains(arg0.getPoint())){
	    	    System.exit(0);
            }
            //If you click on scores button, display highscores.
            else if(scoresButtonPoly.contains(arg0.getPoint())){
	    		alpha = 0;
	    	    nextState = "scores";
	    	    transitioningTo = false;

            }
        }else if(gameState.equals("game")) { //Playing the game.
			if(!paused) {
				//Change the direction of the rocket on click.
				if (arg0.getButton() == MouseEvent.BUTTON1 && !paused) rockets.get(0).changeDirection();
			} else if(exitGameButtonPoly.contains(arg0.getPoint())){
				alpha = 0;
				nextState = "menu";
				transitioningTo = false;
			}

        } else if (gameState.equals("scores")){ //Displaying highscores.
			//Return to menu if clicked on back button.
	    	if (backButtonPoly.contains(arg0.getPoint())){
				for(Highscore hs : highscores){
					if(hs.name.equals("")) hs.name = newName;
				}
				while(highscores.size() > 10){
	    			highscores.remove(highscores.get(10));
				}
				saveScores();
	    		alpha = 0;
	    		nextState = "menu";
	    		transitioningTo = false;
			}
		}
	}

	//Used to type name when a new high score is entered.
	@Override
	public void keyPressed(KeyEvent arg0) {
	    //Typing name for new high score.
        if(gameState.equals("scores") && editing) {
            if((Character.isAlphabetic(arg0.getKeyChar()) || arg0.getKeyChar() == ' ') && newName.length() < 15){
                newName += Character.toUpperCase(arg0.getKeyChar());
            } else if(arg0.getKeyCode() == 8 && newName.length() != 0){ //Backspace
                newName = newName.substring(0, newName.length() - 1);
            } else if(arg0.getKeyChar() == '\n') { //Save highscore name
                for(Highscore hs : highscores){
                    if(hs.name.equals("")) hs.name = newName;
                }
                while(highscores.size() > 10){
                    highscores.remove(highscores.get(10));
                }
                saveScores();
                alpha = 0;
                nextState = "menu";
                transitioningTo = false;
            }
        }
	}

	//Used to pause the game when playing.
	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == 32) { //If the spacebar is pressed.
			paused = !paused;
		}
		
	}

	//Not used but necessary for compiling.
	@Override
	public void keyTyped(KeyEvent arg0) {

	}


}
