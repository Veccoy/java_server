import java.io.DataInputStream;
import java.net.Socket;
import java.util.Scanner;

// Application client
public class Client {
	private static Socket socket;
	public static String username; 
	public static String password;
	public static Server server = new Server();

	public static void main(String[] args) throws Exception {

		// Adresse et port du serveur
		String serverAddress = "127.0.0.1";
		int port = 5000;
		
		// Nom d'utilisateur et mot de passe
		Scanner scanner = new Scanner(System.in);
		getLogin(scanner);

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

	public static void getLogin(Scanner scanner) {
		System.out.println("Enter your username : ");
		username = scanner.nextLine();
		System.out.println("Enter your password : ");
		password = scanner.nextLine();
		boolean isAccount = server.checkLogin(username, password);
		while(!isAccount) {
			System.out.println("Erreur dans la saisie du mot de passe.");
			System.out.println("Enter your password : ");
			password = scanner.nextLine();
			isAccount = server.checkLogin(username, password);
		} 
	}
}