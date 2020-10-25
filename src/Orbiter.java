import javax.swing.JFrame;
import java.io.File;

public class Orbiter extends JFrame{
	public static final int WINDOWSIZE = 600;
	public static void main(String[] args) throws InterruptedException {
		File f = new File(System.getProperty("user.dir") + "/img/");
		for(String file : f.list())System.out.println(file);

		Orbiter frame = new Orbiter();
		Game game = new Game();
		frame.add(game);
		frame.pack();
		frame.setSize(WINDOWSIZE, WINDOWSIZE + frame.getInsets().top + frame.getInsets().bottom);
		frame.setTitle("Orbiter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		game.start();
	}
}
