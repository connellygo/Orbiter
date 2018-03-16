import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Game extends JPanel implements KeyListener, MouseListener{
	
	//Constants
	private static final long serialVersionUID = 1L;
	public static final int CENTER = Frame.WINDOWSIZE / 2;
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
	private BufferedImage helpButton;
	private BufferedImage starImg;
	private BufferedImage healthImg;

	//Variables
	private ArrayList<Rocket> rockets; //An arraylist to hold the rocket objects.
	private ArrayList<Projectile> projectiles; //An arraylist to hold the projectile objects.
	private boolean paused; //Is the game paused
	private String gameState; //String that represents the state of the game. Menu, Instructions, Game, etc.
    private int score; //Keeps track of the score.
    private int alpha; //Used for transition between screens
	private Polygon startButtonPoly; //Polygon used for clicking on start button.
	private Polygon helpButtonPoly; //Polygon used for clicking on help button.
    private Point[][] starLocations; //Holds locations for star images.
	private ArrayList<Point> health; //Holds the locations for health packs.
	private int counter = 0; //Incremented every frame. Used for timing projectile and health spawns.
	
	public Game() {
		//Load all sprites needed for objects.
		loadImages();

		//Resets the rocket, projectile and gameState.
		reset();

		//Create polygon objects for clickable buttons.
		createButtonPolygons();

		//Generate locations for stars
		placeStars();

        //Set listeners for the key presses and mouse clicks.
		setFocusable(true); 
		addMouseListener(this);
		addKeyListener(this);
	}
	

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
		        if(alpha == 254){
		            reset();
                }else {
                    alpha += 2;
                }
            }
            else if (gameState.equals("menu to game1")){
		    	alpha += 2;
		    	if(alpha >= 254){
		    		gameState = "menu to game2";
		    		alpha = 254;
				}
			}
			else if(gameState.equals("menu to game2")){
				alpha -= 2;
				if(alpha <= 0){
					alpha = 0;
					gameState = "game";
				}
			}
            else if(gameState.equals("menu")){
		        if(alpha > 0){
		            alpha -= 2;
                }
            }

			//Redraw everything
			repaint();
	
			//Wait .01 seconds.
			Thread.sleep(10);
		}
		
	}

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
        projectiles.add(new Projectile(1.5, 0));

        //Set the game state to the main menu
        gameState = "menu";
        paused = false;
        score = 0;

		//Fade in value
        alpha = 254;
    }
	
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
				if(p.getX() < -1 * Frame.WINDOWSIZE || p.getX() > Frame.WINDOWSIZE || p.getY() < -1 * Frame.WINDOWSIZE || p.getY() > Frame.WINDOWSIZE) {
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

	private void createButtonPolygons(){
        //Boundaries for start button on menu.
        int startButtonXPos[] = {CENTER + 36, CENTER + 164,CENTER + 164, CENTER + 36};
        int startButtonYPos[] = {CENTER - 45, CENTER - 45, CENTER - 5, CENTER - 5};
        startButtonPoly = new Polygon(startButtonXPos, startButtonYPos, 4);

        //Boundaries for start button on menu.
        int helpButtonXPos[] = {CENTER + 36, CENTER + 164,CENTER + 164, CENTER + 36};
        int helpButtonYPos[] = {CENTER + 6, CENTER + 6, CENTER + 46, CENTER + 46};
        helpButtonPoly = new Polygon(helpButtonXPos, helpButtonYPos, 4);
    }

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

	private void placeStars(){
	    int rowSize = Frame.WINDOWSIZE / STARSPACING;
        starLocations = new Point[rowSize][rowSize];
        Random r = new Random();
	    for (int i = 0; i < rowSize; i++){
	        for(int j = 0; j < rowSize; j++){
                starLocations[i][j] = new Point(i * STARSPACING + r.nextInt(STARSPACING - 8), j * STARSPACING + r.nextInt(STARSPACING - 8));
            }
        }
    }

	private void spawnProjectile() {
		Random r = new Random();
		projectiles.add(new Projectile(2 + 2 * (1 -  1 / (1 + score / 200.)) * (2 * r.nextFloat() - 1), r.nextInt(360)));
	}


	private void loadImages() {
		  try {
			  rocketImg = ImageIO.read(new File("rocket.png"));
		  } catch (IOException e) {
			  rocketImg = null;
			  e.printStackTrace();
		  }
		  try {
			  earthImg = ImageIO.read(new File("earth.png"));
		  } catch (IOException e) {
			  earthImg = null;
			  e.printStackTrace();
		  }
		  
		  try {
			  rocketReverseImg = ImageIO.read(new File("rocketReverse.png"));
		  } catch (IOException e) {
			  rocketReverseImg = null;
			  e.printStackTrace();
		  }
		  
		  try {
			  projectileImg = ImageIO.read(new File("projectile.png"));
		  } catch (IOException e) {
			  projectileImg = null;
			  e.printStackTrace();
		  }
		  
		  try {
			  pausedButtonImg = ImageIO.read(new File("pausedButton.png"));
		  } catch (IOException e) {
			  pausedButtonImg = null;
			  e.printStackTrace();
		  }

		try {
			startButtonImg = ImageIO.read(new File("startButton.png"));
		} catch (IOException e) {
			startButtonImg = null;
			e.printStackTrace();
		}

		try {
			helpButton = ImageIO.read(new File("helpButton.png"));
		} catch (IOException e) {
			helpButton = null;
			e.printStackTrace();
		}

        try {
            starImg = ImageIO.read(new File("star.png"));
        } catch (IOException e) {
            starImg = null;
            e.printStackTrace();
        }

		try {
			healthImg = ImageIO.read(new File("health.png"));
		} catch (IOException e) {
			healthImg = null;
			e.printStackTrace();
		}
		  
	}

	public void paintComponent(Graphics g)
	{
		if(gameState.equals("menu") || gameState.equals("menu to game1")) {
            //Set Background Color
		    g.setColor(BACKGROUNDCOLOR);
			g.fillRect(0, 0, 600, 600);

			//Draw Stars
            for(int i = 0; i < starLocations.length; i++){
                for(int j = 0; j < starLocations.length; j++){
                    g.drawImage(starImg, starLocations[i][j].x, starLocations[i][j].y, 8, 8, null);
                }
            }

			//Draw start button
			g.drawImage(startButtonImg, CENTER + 36, CENTER - 89,128,128, null);

			//Draw help button
			g.drawImage(helpButton, CENTER + 36, CENTER - 39, 128, 128, null);

			//Earth for aesthetics and stuff.
			g.drawImage(earthImg, CENTER - EARTHSIZE - 75, CENTER - EARTHSIZE / 2, EARTHSIZE, EARTHSIZE, null);

            //Fade in
            g.setColor(new Color(BACKGROUNDCOLOR.getRed(), BACKGROUNDCOLOR.getGreen(), BACKGROUNDCOLOR.getBlue(), alpha));
            g.fillRect(0,0, Frame.WINDOWSIZE, Frame.WINDOWSIZE);
		}
		else if(gameState.equals("game") || gameState.equals("game over") || gameState.equals("menu to game2")) {
		    //Background color
            g.setColor(BACKGROUNDCOLOR);
			g.fillRect(0, 0, Frame.WINDOWSIZE, Frame.WINDOWSIZE);

            //Draw Stars
            for(int i = 0; i < starLocations.length; i++){
                for(int j = 0; j < starLocations.length; j++){
                    g.drawImage(starImg, starLocations[i][j].x, starLocations[i][j].y, 8, 8, null);
                }
            }

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
				g.drawImage(pausedButtonImg, CENTER - 64, CENTER - 64, 128, 128, null);
			}

			//Test Hitbox
//			g.setColor(Color.YELLOW);
//			Polygon rocketHitbox = createRocketPolygon(rockets.get(0));
//			g.drawPolygon(rocketHitbox);
//			g.fillRect(CENTER, CENTER, 2,2);

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
			g.drawString(scoreString, (Frame.WINDOWSIZE - fm.stringWidth(scoreString)) / 2, fm.getHeight());

			//Fade in and out.
			g.setColor(new Color(BACKGROUNDCOLOR.getRed(), BACKGROUNDCOLOR.getGreen(), BACKGROUNDCOLOR.getBlue(), alpha));
			g.fillRect(0,0, Frame.WINDOWSIZE, Frame.WINDOWSIZE);
		}
	}

	@Override 
	public void mouseClicked(MouseEvent arg0) {
		
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	    if(gameState.equals("menu")){
            //If you click on start button, start game.
	    	if(startButtonPoly.contains(arg0.getPoint())){
	    	    alpha = 0;
	    		gameState = "menu to game1";
            }
            else if(helpButtonPoly.contains(arg0.getPoint())){
	    	    System.out.println("HELP ME");
            }
        }else if(gameState.equals("game")) {
            if (arg0.getButton() == MouseEvent.BUTTON1 && !paused) rockets.get(0).changeDirection();
        }
	}

	@Override
	public void keyPressed(KeyEvent arg0) {

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == 32) { //If the spacebar is pressed.
			paused = !paused;
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}


}
