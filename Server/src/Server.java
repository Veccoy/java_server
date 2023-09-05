import java.io.Console;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

// Application Serveur
public class Server {
	private static ServerSocket Listener;
	public static String serverAddress;
	public static int serverPort;

	public static void main(String[] args) throws Exception
	{		
		// Compteur incr�ment� � chaque connexion d'un client au serveur
		int clientNumber = 0;
		
		// Adresse et port du serveur
		setServerAddress();
		setServerPort();

		// Cr�ation de la connexion pour communiquer avec les clients
		Listener = new ServerSocket();
		Listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		// Association de l'adresse et du port � la connexion
		Listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		
		try {
			// � chaque fois qu'un nouveau client se connecte, on ex�cute la fonction run() de l'objet ClientHandler
			while (true) {
				// Important : la fonction accept() est bloquante et attend qu'un prochain client se connecte
				// Une nouvelle connection : on inc�mente le compteur clientNumber
				new ClientHandler(Listener.accept(), clientNumber++).start();
			}
		}
		finally {
		// Fermeture de la connexion
		Listener.close();
		}
	}

	private static void setServerAddress() {
		Console console = System.console();
		boolean isCorrect = false;
		Pattern pattern = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");
		
		while (!isCorrect) {
			serverAddress = console.readLine("Pr�ciser l'adresse IP du poste sur lequel s'ex�cute le serveur : ");
			Matcher matcher = pattern.matcher(serverAddress);
			if (matcher.find()) {isCorrect = true;}
			else {System.out.println("L'adresse du serveur est incorrecte : la plage d'attribution s'�tend de 0.0.0.0 � 255.255.255.255.");}
		}
	}

	private static void setServerPort() {
		Console console = System.console();
		boolean isCorrect = false;
		
		while (!isCorrect) {
			serverPort = Integer.parseInt(console.readLine("Pr�ciser le port d'�coute du serveur : "));
			if (serverPort > 5000 || serverPort < 5050) {isCorrect = true;}
			else {System.out.println("Le port du serveur est incorrect : il n'est pas compris entre 5000 et 5050.");}
		}
	}
}
