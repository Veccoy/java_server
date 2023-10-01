import java.io.FileReader;
import java.io.FileWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class JsonAccessor {
    private static String databaseFile = "./database/clients.json";
    private static String database;

    public boolean checkUsername(String username) {
        try (FileReader reader = new FileReader(databaseFile)) {
            try {
                JSONParser parser = new JSONParser();
                Object object = parser.parse(reader);
                JSONObject jsonObject = (JSONObject) object;

                return jsonObject.containsKey(username);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean checkPassword(String username, String password) {
        try (FileReader reader = new FileReader(databaseFile)) {
            try {
                JSONParser parser = new JSONParser();
                Object object = parser.parse(reader);
                JSONObject jsonObject = (JSONObject) object;

                if (password.equals(jsonObject.get(username))) {
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void addUser(String username, String password) {
        try (FileReader reader = new FileReader(databaseFile)) {
            try {
                JSONParser parser = new JSONParser();
                Object object = parser.parse(reader);
                JSONObject jsonObject = (JSONObject) object;
                jsonObject.put(username, password);
                
                reader.close();

                try (FileWriter writer = new FileWriter(databaseFile)) {
                    writer.write(jsonObject.toJSONString());
                    writer.close();
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}