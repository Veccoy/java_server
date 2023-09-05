import java.io.Console;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Application client
public class Client {
	private static Socket socket;
	public static String serverAddress;
	public static int serverPort;
	public static String userName;
	
	public static void main(String[] args) throws Exception
	{		
		// Adresse et port du serveur
		setServerAddress();
		setServerPort();
		
		// Nom d'utilisateur et mot de passe du client
		setUser();
		
		// Cr�ation d'une nouvelle connexion aves le serveur
		socket = new Socket(serverAddress, port);
		System.out.format("Serveur lanc� sur [%s:%d]", serverAddress, port);
		// C�atien d'un canal entrant pour recevoir les messages envoy�s, par le serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());
		// Attente de la r�ception d'un message envoy� par le, server sur le canal
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		// fermeture de La connexion avec le serveur
		socket.close();
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
	
	private static void setUser() {
		Console console = System.console();
		boolean nameIsCorrect = false;
		boolean passwordIsCorrect = false;
		
		while (!nameIsCorrect) {
			userName = console.readLine("Pr�ciser l'adresse IP du poste sur lequel s'ex�cute le serveur : ");
			if (isInDatabase(userName)) {nameIsCorrect = true;}
			else {System.out.println("Le nom d'utilisateur est incorrect : il n'est pas enregistr� dans la base de donn�es.");}
		}
		
		while (!passwordIsCorrect) {
			char[] password = console.readPassword("To finish, enter password: ");
			if (password.equals(getUserPassword(userName))) {passwordIsCorrect = true;}
			else {System.out.println("Le mot de passe est incorrect : il ne correspond pas � celui associ� au nom d'utilisateur.");}
		}
	}
	
	private static char[] getUserPassword(String userName) {
		
		return null;
	}

	private static boolean isInDatabase(String userName) {
		
		return false;
	}

}
