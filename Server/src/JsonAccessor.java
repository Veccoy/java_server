import java.io.BufferedReader;
import java.io.FileReader;


public class JsonAccessor {
    private static String filename;

    public JsonAccessor(String JSONFile) {
        filename = JSONFile;
    }
    
    public static String getJSONFromFile() {
        String jsonText = "";

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                jsonText += line + "\n";
            }

             bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonText;
    }

    public boolean checkUsername(String username) {
        try {
            JSONParser parser = new JSONParser();
        }
        return true;
    }

    public boolean checkPassword(String username, String password) {
        return true;
    }

    public void addUser(String username, String password) {

    }


}
