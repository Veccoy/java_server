import java.io.DataInputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// Application client
public class Client {
	private static Socket socket;
	public static String username;
	//public static String password;
	//public static Server server = new Server();

	public static void main(String[] args) throws Exception {

		// Adresse et port du serveur, Nom d'utilisateur et mot de passe
		Scanner scanner = new Scanner(System.in);
		String serverAddress = getAddress(scanner);
		int port = getPort(scanner);
		checkLogin(scanner);

		// Création d'une nouvelle connexion aves le serveur
		socket = new Socket(serverAddress, port);
		System.out.format("Serveur lancé sur [%s:%d]", serverAddress, port);

		// Céatien d'un canal entrant pour recevoir les messages envoyés, par le serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());

		// Attente de la réception d'un message envoyé par le, server sur le canal
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);

		// fermeture de La connexion avec le serveur
		socket.close();
	}

	public static String getAddress(Scanner scanner) {
		boolean isAcceptableAddress = false;
		String patternString = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
		String address = "";
		
		while (!isAcceptableAddress) {
			System.out.println("Enter the server address : ");
			address = scanner.nextLine();
			Pattern addressPattern = Pattern.compile(patternString);
			Matcher matcher = addressPattern.matcher(address);
			if (matcher.find()){
				isAcceptableAddress = true;
			} else {
				System.out.println("Invalid server address: the IP address must be set between 0.0.0.0 and 255.255.255.255. Please try again!");
			}
		}
			
		return address;
	}
	
	public static int getPort(Scanner scanner) {
		boolean isAcceptablePort = false;
		int port = 0;
		
		while (!isAcceptablePort) {
			System.out.println("Enter the server port : ");
			port = Integer.parseInt(scanner.nextLine());
			if((port >= 5000) && (port <= 5050)) {
				isAcceptablePort = true; 
			} else {
				System.out.println("Invalid server port: the port must be set between 5000 and 5050. Please try again!");
			}
		}

		return port;
	}

	public static void checkLogin(Scanner scanner) {
		JsonAccessor jsonAccessor = new JsonAccessor();

		System.out.println("Enter your username : ");
		username = scanner.nextLine();
		System.out.println("Enter your password : ");
		String password = scanner.nextLine();
		
		if (!jsonAccessor.checkUsername(username)) {
			System.out.println("Nom d'utilisateur inconnu. Création d'un nouveau compte...");
			jsonAccessor.addUser(username, password);
			//jsonAccessor = new JsonAccessor(); // To reload the database with the new user
		}

		boolean correctPassword = jsonAccessor.checkPassword(username, password);;
		while(!correctPassword) {
			System.out.println("Erreur dans la saisie du mot de passe.");
			System.out.println("Enter your password: ");
			password = scanner.nextLine();
			correctPassword = jsonAccessor.checkPassword(username, password);
		}
	}
}