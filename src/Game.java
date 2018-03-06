import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
	//public static final Color BACKGROUNDCOLOR = new Color(20, 40, 66);
	public static final Color BACKGROUNDCOLOR = new Color(0, 0, 40);

	
	
	private BufferedImage rocketImg;
	private BufferedImage rocketReverseImg;
	private BufferedImage earthImg;
	private BufferedImage projectileImg;
	private BufferedImage pausedButtonImg;
	private BufferedImage startButtonImg;
	private BufferedImage helpButton;

	private ArrayList<Rocket> rockets; //An arraylist to hold the rocket objects.
	private ArrayList<Projectile> projectiles; //An arraylist to hold the projectile objects.
	private boolean paused; //Is the game paused
	private String gameState; //String that represents the state of the game. Menu, Instructions, Game, etc.
    private int score; //Keeps track of the score.
    private int alpha; //Used for transition between screens
	
	private int counter = 0;
	
	public Game() {
		//Load all sprites needed for objects.
		loadImages();

		//Resets the rocket, projectile and gameState.
		reset();
		
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

    //Resets the rocket, projectile and gameState.
	public void reset(){
        //Initialize the rockets.
        rockets = new ArrayList<Rocket>();
        rockets.add(new Rocket(RADIUS, 0, 1, 1));

        //Initialize the arraylist for current projectiles.
        projectiles = new ArrayList<Projectile>();
        projectiles.add(new Projectile(1.5, 0));

        //Set the game state to the main menu
        gameState = "menu";
        paused = false;
        score = 0;

        alpha = 254;
    }
	
	private void checkCollisions() {
		Set<Projectile> removedProjectiles = new HashSet<Projectile>();
		for(Rocket r : rockets) {
			Polygon rocketHitbox = createRocketPolygon(r);
			for(Projectile p : projectiles) {
				if(rocketHitbox.contains(CENTER + p.getX(), CENTER + p.getY())) {
					removedProjectiles.add(p); //Add projectile to the set that will be removed.
					r.takeDamage(25);
				}
				
				//If the projectile goes off screen.
				if(p.getX() < -1 * Frame.WINDOWSIZE || p.getX() > Frame.WINDOWSIZE || p.getY() < -1 * Frame.WINDOWSIZE || p.getY() > Frame.WINDOWSIZE) {
					removedProjectiles.add(p); //Add projectile to the set that will be removed.
				}
			}
			
		}
		for(Projectile p : removedProjectiles) projectiles.remove(p); //remove projectiles from list.
	}


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


	private void spawnProjectile() {
		Random r = new Random();
		projectiles.add(new Projectile(1.5 + (r.nextInt(200) / 100), r.nextInt(360)));
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
		  
	}

	public void paintComponent(Graphics g)
	{
		if(gameState.equals("menu")) {
            g.setColor(BACKGROUNDCOLOR);
			g.fillRect(0, 0, 600, 600);
			//Draw start button
			g.drawImage(startButtonImg, CENTER + 36, CENTER - 114,128,128, null);

			//Draw help button
			g.drawImage(helpButton, CENTER + 36, CENTER - 14, 128, 128, null);

			g.drawImage(earthImg, CENTER - EARTHSIZE - 75, CENTER - EARTHSIZE / 2, EARTHSIZE, EARTHSIZE, null);

            //Fade in
            g.setColor(new Color(BACKGROUNDCOLOR.getRed(), BACKGROUNDCOLOR.getGreen(), BACKGROUNDCOLOR.getBlue(), alpha));
            g.fillRect(0,0, Frame.WINDOWSIZE, Frame.WINDOWSIZE);
		}
		else if(gameState.equals("game") || gameState.equals("game over")) {
            g.setColor(BACKGROUNDCOLOR);
			g.fillRect(0, 0, Frame.WINDOWSIZE, Frame.WINDOWSIZE);
			Graphics2D g2d=(Graphics2D)g; // Create a Java2D version of g.		  

            //draw projectiles
			for(Projectile p : projectiles) {
				g.drawImage(projectileImg, CENTER + p.getX() - PROJECTILESIZE / 2, CENTER + p.getY() - PROJECTILESIZE / 2, PROJECTILESIZE, PROJECTILESIZE, null);
			}

			//Draw Earth
			g.drawImage(earthImg, CENTER - EARTHSIZE / 2, CENTER - EARTHSIZE / 2, EARTHSIZE, EARTHSIZE, null);
			
			//draw rockets
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

			//Fade out when player loses.
			g.setColor(new Color(BACKGROUNDCOLOR.getRed(), BACKGROUNDCOLOR.getGreen(), BACKGROUNDCOLOR.getBlue(), alpha));
			g.fillRect(0,0, Frame.WINDOWSIZE, Frame.WINDOWSIZE);
		}
	}

	@Override 
	public void mouseClicked(MouseEvent arg0) {
		
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	    if(gameState.equals("menu")){
            gameState = "game";
        }else if(gameState.equals("game")) {
            if (arg0.getButton() == MouseEvent.BUTTON1 && !paused) rockets.get(0).changeDirection();
        }
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == 32) { //If the spacebar is pressed.
			paused = !paused;
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}
