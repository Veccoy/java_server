import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
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

		// Envoi de l'image d'entrée au serveur
		int bytesRead = 0;
		BufferedImage image = ImageIO.read(new File(imageFolder + inputName));
		byte[] imageData = bufferedImageToByteArray(image);
		out.write(image.getWidth());
		out.write(image.getHeight());
		out.write(image.getType());
		byte[] buffer = new byte[4 * 1024];
		while((bytesRead = fileInputStream.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
			out.flush();
		}
		fileInputStream.close();
		System.out.println("Image transferred succesfully");

		// Réception du message d'attente
		System.out.println(in.readUTF());

		// Réception de l'image traitée
		long t0 = System.currentTimeMillis();
		int bytes = 0;
		FileOutputStream fileOutputStream = new FileOutputStream("./images/test.jpg");
		long size = in.readLong();
		byte[] buffer = new byte[4 * 1024];
		while((size > 0) && ((bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)) {
			fileOutputStream.write(buffer, 0, bytes);
			size -= bytes;
		}
		fileOutputStream.close();
		long t1 = System.currentTimeMillis();
		System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + " processed image in " + (t1-t0) + "ms.");
		ImageIO.write(processedImage, "jpg", new File(imageFolder + outputName));
		System.out.format("The processed image has been received after Sobel processing and can be found in %s.%n", imageFolder + outputName);

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

	private static BufferedImage bufferedImageFromWrapper(ImageWrapper wrapper){
		BufferedImage bufferedImage = new BufferedImage(wrapper.getWidth(), wrapper.getHeight(), wrapper.getType());
		bufferedImage.setData(Raster.createRaster(bufferedImage.getSampleModel(), new DataBufferByte(wrapper.getImage(), wrapper.getImage().length), new Point()));

		return bufferedImage;
	}
}