import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


// Application client
public class Client {
	private static Socket socket;
	public static String username;
	private static String imageFolder = "./images/";

	public static void main(String[] args) throws Exception {

		// Obtenir l'adresse et le port du serveur, ainsi que le nom d'utilisateur et mot de passe du client
		Scanner scanner = new Scanner(System.in);
		String serverAddress = getAddress(scanner);
		int serverPort = getPort(scanner);
		checkLogin(scanner);

		// Création d'une nouvelle connexion avec le serveur
		socket = new Socket(serverAddress, serverPort);
		System.out.format("Serveur running on [%s:%d]\n", serverAddress, serverPort);

		// Création d'un canal entrant et d'un canal sortant pour échanger des messages avec le serveur
		DataInputStream in = new DataInputStream(socket.getInputStream()); // canal de réception
		DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // canal d’envoi

		// Attente de la réception du message d'introduction envoyé par le server sur le canal
		System.out.println(in.readUTF());

		// Demande des noms des images d'entrée et de sortie
		String inputName = getImageName(scanner, "input");
		String outputName = getImageName(scanner, "output");

		// Envoi des informations sur le client au serveur
		out.writeUTF(username);
		out.writeUTF(inputName);

		// Envoi des métadonnées de l'image d'entrée au serveur
		BufferedImage image = ImageIO.read(new File(imageFolder + inputName));
		byte[] imageData = bufferedImageToByteArray(image);
		long length = imageData.length;
		int convertedLength = (int) length;
		assert convertedLength == length;
		out.writeInt(convertedLength);

		// Envoi de l'image d'entrée au serveur
		transmitImageToServer(imageData, convertedLength, out);

		// Réception du message d'attente
		System.out.println(in.readUTF());

		// Réception de l'image traitée
		long t0 = System.currentTimeMillis();
		byte[] processedImageData = new byte[convertedLength];
		processedImageData = receiveProcessedImage(processedImageData, convertedLength, in);

		BufferedImage processedImage = byteArrayToBufferedImage(processedImageData);
		long t1 = System.currentTimeMillis();
		System.out.println("Received " + processedImage.getHeight() + "x" + processedImage.getWidth() + " image in " + (t1-t0) + "ms.");
		String time = LocalDateTime.now().toString().replaceFirst("T", "@");
		System.out.format("[%s - %s:%d - %s]: image %s received after Sobel processing.%n", username, serverAddress, serverPort, time, inputName);

		// Enregistrement de l'image dans un fichier
		ImageIO.write(processedImage, outputName.substring(outputName.lastIndexOf('.')+1), new File(imageFolder + outputName));

		// fermeture de La connexion avec le serveur
		socket.close();
	}

	//fonction permettant de valider si l'adresse entrée du client porte un format d'adresse valide, et si oui, retourne l'adresse,
	// si non, le client doit entrée à nouveau une adresse valide. 
	private static String getAddress(Scanner scanner) {
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
	
	//fonction permettant de valider si le port entré du client porte un format d'adresse valide, alors entre 5000 et 5050,
	// et si oui, retourne le port, si non, le client doit entrée un port valide à nouveau. 
	private static int getPort(Scanner scanner) {
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

	//Fonction qui demande au client pour ses informations de connexions, et appelle les fonctions de la classe JsonAccessor 
	// checkUsername() et checkPassword() afin de vérifier si un compte existe déjà avec ses informations, et si non, 
	//on créer un nouveau compte pour le client automatiquement avec les données entrées
	private static void checkLogin(Scanner scanner) {
		JsonAccessor jsonAccessor = new JsonAccessor();

		System.out.println("Enter your username : ");
		username = scanner.nextLine();
		System.out.println("Enter your password : ");
		String password = scanner.nextLine();
		
		if (!jsonAccessor.checkUsername(username)) {
			System.out.println("Unknown username, a new account is being created...");
			jsonAccessor.addUser(username, password);
			//jsonAccessor = new JsonAccessor(); // To reload the database with the new user
		}

		boolean correctPassword = jsonAccessor.checkPassword(username, password);;
		while(!correctPassword) {
			System.out.println("The password doesn't match with the database.");
			System.out.println("Enter your password: ");
			password = scanner.nextLine();
			correctPassword = jsonAccessor.checkPassword(username, password);
		}
	}

	//Fonction permettant au client à entrer le nom de l'image à traité et le nom de l'image traité et gère le nom de ceux dans le cas 
	// ou les extensions ne sont pas valides.
	private static String getImageName(Scanner scanner, String imageType) {
		boolean isValidName = false;
		String imageName = "";
		while(!isValidName) {
			if(imageType == "input") {
				System.out.println("Please write the name of the image you want to be processed (with either .jpg OR .png extension):");
			}
			else {
				System.out.println("Please write the name of the future processed image (with either .jpg OR .png extension):");
			}
			imageName = scanner.nextLine();

			if(imageName.contains(".jpg") || imageName.contains(".png")) {
				isValidName = true;
			}
			else {
				System.out.println("Invalid iamge extension. Please try again!");
			}
		}

		return imageName;
	}
	//Fonction qui permet d'envoyer au serveur les données en bytes de l'image que le client veut filtrer à l'aide d'une boucle
	// qui envoie petit par petit les bytes de l'image qui sera recu par la fonction receiveImageToProcess() de la classe ClientHandler
	private static void transmitImageToServer(byte[] imageData, long length, DataOutputStream out) {
		try {
			byte[] outBuffer = new byte[4096];
			int bytesWritten = 0;
			while(bytesWritten < length) {
				if (length - bytesWritten >= 4096) {
					for (int i=0 ; i < outBuffer.length ; i++) {
						outBuffer[i] = imageData[bytesWritten + i];
					}
					out.write(outBuffer, 0, 4096);
					bytesWritten += 4096;
				}
				else {
					int nbLeftBytes = (int) length - bytesWritten;
					byte[] smallOutBuffer = new byte[nbLeftBytes];
					for (int i=0 ; i < smallOutBuffer.length ; i++) {
						smallOutBuffer[i] = imageData[bytesWritten + i];
					}
					out.write(smallOutBuffer, 0, nbLeftBytes);
					bytesWritten += nbLeftBytes;
				}
			}
			out.flush();
		    System.out.println("Image transferred succesfully !");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//Fonction qui permet de recevoir les données en bytes de l'image filtrer du serveur à l'aide d'une boucle
	// qui recoit petit par petit les bytes de l'image filtrer et les mets dans une array de bytes. La fonction
	// recoit les données à partir de la fonction transmitImageToClient() de la classe ClientHandler
	private static byte[] receiveProcessedImage(byte[] processedImageData, int convertedLength, DataInputStream in) {
		try{
			byte[] inBuffer = new byte[4096];
			int bytesRead = 0;
			while(bytesRead < convertedLength) {
				if (convertedLength - bytesRead >= 4096) {
					in.read(inBuffer, 0, 4096);
					for (int i=0 ; i < 4096 ; i++) {
						processedImageData[bytesRead + i] = inBuffer[i];
					}
					bytesRead += 4096;
				} else {
					int nbLeftBytes = convertedLength - bytesRead;
					byte[] smallInBuffer = new byte[nbLeftBytes];
					in.read(smallInBuffer, 0, nbLeftBytes);
					for (int i=0 ; i < nbLeftBytes ; i++) {
						processedImageData[bytesRead + i] = smallInBuffer[i];
					}
					bytesRead += nbLeftBytes;
				}
			}
			return processedImageData;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	//Fonction qui permet de transformer un objet de type BufferedImage en objet de type byte[]
	private static byte[] bufferedImageToByteArray(BufferedImage image) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpg", stream);
		} catch(IOException e) {
			e.printStackTrace();
		}

		return stream.toByteArray();
	}

	//Fonction qui permet de transformer un objet de type byte[] en objet de type BufferedImage
	private static BufferedImage byteArrayToBufferedImage(byte[] imageData) throws IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(imageData);
		BufferedImage image;
		try {
			image = ImageIO.read(stream);
		} finally {}

		return image;
	}
}