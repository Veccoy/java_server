import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;


public class Server {
	private static ServerSocket Listener; // Application Serveur
	
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		// Compteur incrémenté à chaque connexion d'un client au serveur
		int clientNumber = 0;

		// Adresse et port du serveur
		String serverAddress = "127.0.0.1"; 
		int serverPort = 5000; 
		
		serverAddress = getAddress(serverAddress, scanner);
		serverPort = getPort(serverPort, scanner);
		
		
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
	
	public static String getAddress(String serverAddress, Scanner scanner) {
		boolean isAcceptableAddress = false;
		String patternString = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
		
		System.out.println("Enter your server address : ");
		serverAddress = scanner.nextLine();
		Pattern addressPattern = Pattern.compile(patternString);
		Matcher hasPattern = addressPattern.matcher(serverAddress);
		isAcceptableAddress = hasPattern.find();
		
		if(!isAcceptableAddress) {
			System.out.println("Invalid server address. Please try again!");
			getAddress(serverAddress, scanner);
		}
		return serverAddress;
	}
	
	public static int getPort(int serverPort, Scanner scanner) {
		boolean isAcceptablePort = false;
		
		System.out.println("Enter your server port : ");
		serverPort = Integer.parseInt(scanner.nextLine());
		if((serverPort >= 5000) && (serverPort <= 5050)) {
			isAcceptablePort = true; 
		}
		else {
			System.out.println("Invalid server port. Please try again!");
			getPort(serverPort, scanner);
		}
		return serverPort;
	}
			
};