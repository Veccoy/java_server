import java.util.HashMap;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;

// Application Serveur
public class Server {
	private static ServerSocket Listener;
	private HashMap<String, String> userData = new HashMap<String, String>();
	private static String serverAddress = "";
	private static int serverPort = 0;

	public static void main(String[] args) throws Exception {
		// Compteur incrémenté à chaque connexion d'un client au serveur
		int clientNumber = 0;

		// Adresse et port du serveur
		Scanner scanner = new Scanner(System.in);
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
	
	public static String getAddress(Scanner scanner) {
		boolean isAcceptableAddress = false;
		String patternString = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
		
		while (!isAcceptableAddress) {
			System.out.println("Enter your server address : ");
			serverAddress = scanner.nextLine();
			Pattern addressPattern = Pattern.compile(patternString);
			Matcher matcher = addressPattern.matcher(serverAddress);
			if (matcher.find()){
				isAcceptableAddress = true;
			} else {
				System.out.println("Invalid server address. Please try again!");
			}
		}
			
		return serverAddress;
	}
	
	public static int getPort(Scanner scanner) {
		boolean isAcceptablePort = false;
		
		while (!isAcceptablePort) {
			System.out.println("Enter your server port : ");
			serverPort = Integer.parseInt(scanner.nextLine());
			if((serverPort >= 5000) && (serverPort <= 5050)) {
				isAcceptablePort = true; 
			} else {
				System.out.println("Invalid server port. Please try again!");
			}
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