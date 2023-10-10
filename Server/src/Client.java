import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.Socket;
import java.time.LocalDateTime;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


// Application client
public class Client {
	private static Socket socket;
	public static String username;
	private static String imageFolder = "./images/";

	public static void main(String[] args) throws Exception {

		// Adresse et port du serveur, Nom d'utilisateur et mot de passe
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
		System.out.println("Please write the name of the image you want to be processed (with .jpg extension):"); // Ask for the name of the input image
		String inputName = scanner.nextLine();
		System.out.println("Please write the name of the future processed image (with .jpg extension):"); // Ask for the name of the ouptut image
		String outputName = scanner.nextLine();

		// Envoi d'informations sur le client
		out.writeUTF(username);
		out.writeUTF(inputName);

		// Envoi des métadonnées de l'image d'entrée au serveur
		BufferedImage image = ImageIO.read(new File(imageFolder + inputName));
		byte[] imageData = bufferedImageToByteArray(image);
		long length = imageData.length;
		int convertedLength = (int) length;
		assert convertedLength == length;
		out.writeInt(image.getWidth());
		out.writeInt(image.getHeight());
		out.writeInt(image.getType());
		out.writeInt(convertedLength);

		// Envoi de l'image d'entrée au serveur
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

		// Réception du message d'attente
		System.out.println(in.readUTF());

		// Réception de l'image traitée
		long t0 = System.currentTimeMillis();
		byte[] processedImageData = new byte[convertedLength];
		byte[] inBuffer = new byte[4096];
		int bytesRead = 0;
		while(bytesRead < length) {
			if (length - bytesRead >= 4096) {
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
		BufferedImage processedImage = byteArrayToBufferedImage(processedImageData);
		long t1 = System.currentTimeMillis();
		System.out.println("Received " + processedImage.getHeight() + "x" + processedImage.getWidth() + " image in " + (t1-t0) + "ms.");
		String time = LocalDateTime.now().toString().replaceFirst("T", "@");
		System.out.format("[%s - %s:%d - %s]: image %s received after Sobel processing.%n", username, serverAddress, serverPort, time, inputName);

		// Enregistrement de l'image dans un fichier
		ImageIO.write(processedImage, "jpg", new File(imageFolder + outputName));

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
			try {
				port = Integer.parseInt(scanner.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("The server port provided is not a number !");
				continue;
			}
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

	private static byte[] bufferedImageToByteArray(BufferedImage image) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpg", stream);
		} catch(IOException e) {
			e.printStackTrace();
		}

		return stream.toByteArray();
	}

	private static BufferedImage byteArrayToBufferedImage(byte[] imageData) throws IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(imageData);
		BufferedImage image;
		try {
			image = ImageIO.read(stream);
		} finally {}

		return image;
	}
}