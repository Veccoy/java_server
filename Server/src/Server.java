import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;

// Application Serveur
public class Server {
	private static ServerSocket Listener;
	private static String serverAddress = "";
	private static int serverPort = 0;	

	public static void main(String[] args) throws Exception {
		// Compteur incrémenté à chaque connexion d'un client au serveur
		int clientNumber = 0;

		// Adresse et port du serveur
		Scanner scanner = new Scanner(System.in);
		setAddress(scanner);
		setPort(scanner);
		
		// Création de la connexion pour communiquer avec les clients
		Listener = new ServerSocket();
		Listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		// Association de l'adresse et du port à la connexien
		Listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		
		try {
			// À chaque fois qu'un nouveau client se connecte, on exécute la fonction run() de l'objet ClientHandler
			while (true) {
				// Important : la fonction accept() est bloquante: attend qu'un prochain client se connecte
				// Une nouvetle connection : on incémente le compteur clientNumber
				new ClientHandler(Listener.accept(), clientNumber++).start();
			}
		} finally {
			// Fermeture de la connexion
			Listener.close();
		} 
	}
	
	// Fonction qui permet d'établir l'adresse selon laquelle le serveur sera lancé et selon laquelle le client pourrait se conneté avec.
	private static void setAddress(Scanner scanner) {
		boolean isAcceptableAddress = false;
		String patternString = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
		
		while (!isAcceptableAddress) {
			System.out.println("Enter the server address : ");
			serverAddress = scanner.nextLine();
			Pattern addressPattern = Pattern.compile(patternString);
			Matcher matcher = addressPattern.matcher(serverAddress);
			if (matcher.find()){
				isAcceptableAddress = true;
			} else {
				System.out.println("Invalid server address: the IP address must be set between 0.0.0.0 and 255.255.255.255. Please try again!");
			}
		}
	}
	
	// Fonction qui permet d'établir le port selon laquelle le serveur sera lancé et selon laquelle le client pourrait se connecté avec.
	private static void setPort(Scanner scanner) {
		boolean isAcceptablePort = false;
		
		while (!isAcceptablePort) {
			System.out.println("Enter the server port : ");
			try {
				serverPort = Integer.parseInt(scanner.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("The server port provided is not a number !");
				continue;
			}
			if((serverPort >= 5000) && (serverPort <= 5050)) {
				isAcceptablePort = true; 
			} else {
				System.out.println("Invalid server port: the port must be set between 5000 and 5050. Please try again!");
			}
		}
	}
};