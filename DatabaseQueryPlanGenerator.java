import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseQueryPlanGenerator {
	public static void main(String[] args) {
        Properties prop = new Properties();
        InputStream input = null;

        try {
        	input = new FileInputStream("config.txt");

        	prop.load(input);
        	System.out.println(prop.getProperty("r"));
        	System.out.println(prop.getProperty("t"));
        	System.out.println(prop.getProperty("l"));
        	System.out.println(prop.getProperty("m"));
        	System.out.println(prop.getProperty("a"));
        	System.out.println(prop.getProperty("f"));
        }  catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
}