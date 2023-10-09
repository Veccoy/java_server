import java.io.FileReader;
import java.io.FileWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class JsonAccessor {
    private static String databaseFile = "./database/clients.json";

    //Fonction qui est appelé dans la fonction checkLogin() de la classe Client qui lie les username des différents compte client
    // du fichier clients.json et si le username en paramètre correspond à une qui existe, il retourne vrai, sinon il retourne faux.
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

    //Fonction qui est appelé dans la fonction checkLogin de la classe Client qui va aller chercher la valeur du mot de passe associé
    // au username en paramètre associé au compte, si le mot de passe associé au compte est la même que celui dans la base de donnée
    // ca retourne vrai, si non, ca retourne faux
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

    //Fonction qui permet de créer un nouveau compte client dans le cas ou le username que le client a entrée n'est pas associé à une compte existant
    // dans la base de donnée, il va créer un compte pour le client selon le username et le mot de passe que le client avait entrée. 
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