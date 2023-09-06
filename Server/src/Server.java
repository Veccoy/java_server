import java.util.HashMap;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;


public class Server {
	private static ServerSocket Listener; // Application Serveur

	private HashMap<String, String> userData = new HashMap<String, String>();

	private static String serverAddress = "127.0.0.1";
	private static int serverPort = 5000;
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);

		// Compteur incrémenté à chaque connexion d'un client au serveur
		int clientNumber = 0;

		// Adresse et port du serveur
		serverAddress = getAddress(scanner);
		serverPort = getPort(scanner);
		
		// Création de la connexien pour communiquer ave les, clients
		Listener = new ServerSocket();
		Listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		// Association de l'adresse et du port à la connexien
		Listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		
		try {
			// À chaque fois qu'un nouveau client se, connecte, on exécute la fonstion
			// run() de l'objet ClientHandler

			while (true) {
				// Important : la fonction accept() est bloquante: attend qu'un prochain client se connecte
				// Une nouvetle connection : on incémente le compteur clientNumber new ClientHandler(Listener.accept(), clientNumber++).start();
			}
		} finally {
			// Fermeture de la connexion
			Listener.close();
		} 
	}
	
	public static String getAddress(Scanner scanner) {
		boolean isAcceptableAddress = false;
		String patternString = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
		
		System.out.println("Enter your server address : ");
		serverAddress = scanner.nextLine();
		Pattern addressPattern = Pattern.compile(patternString);
		Matcher hasPattern = addressPattern.matcher(serverAddress);
		isAcceptableAddress = hasPattern.find();
		
		if(!isAcceptableAddress) {
			System.out.println("Invalid server address. Please try again!");
			getAddress(scanner);
		}
		return serverAddress;
	}
	
	public static int getPort(Scanner scanner) {
		boolean isAcceptablePort = false;
		
		System.out.println("Enter your server port : ");
		serverPort = Integer.parseInt(scanner.nextLine());
		if((serverPort >= 5000) && (serverPort <= 5050)) {
			isAcceptablePort = true; 
		}
		else {
			System.out.println("Invalid server port. Please try again!");
			getPort(scanner);
		}
		return serverPort;
	}

	public boolean checkLogin(String username, String password) {
		if(!userData.containsKey(username)) {
			userData.put(username, password); 
		}
		else if(userData.get(username).compareTo(password) != 0) {
			return false; 
		}
		return true;
	}
};