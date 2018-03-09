import javax.swing.JFrame;
public class Frame extends JFrame{
	public static final int WINDOWSIZE = 600;
	public static void main(String[] args) throws InterruptedException {
		Frame frame = new Frame();
		Game game = new Game();
		frame.add(game);
		frame.pack();
		frame.setSize(WINDOWSIZE, WINDOWSIZE + frame.getInsets().top + frame.getInsets().bottom);
		frame.setTitle("Orbiter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setResizable(false);
		frame.setVisible(true);
		game.start();
	}
}