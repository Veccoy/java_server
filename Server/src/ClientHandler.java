import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.time.LocalDateTime;

public class ClientHandler extends Thread { // pour traiter la demande de chaque client sur un socket particulier
	private String serverAddress;
	private int serverPort;
	private Socket socket;
	private int clientNumber;
	
	public ClientHandler(Socket socket, int clientNumber) {
		this.serverAddress = socket.getInetAddress().toString().replaceFirst("/", "");
		this.serverPort = socket.getLocalPort();
		this.socket = socket;
		this.clientNumber = clientNumber;
		System.out.println("New connection with client #" + clientNumber + " at " + socket);
	}

	public void run() { // Création de thread qui envoi un message à un client
		try {
			// Création d'un canal entrant et d'un canal sortant pour échanger des messages avec le serveur
			DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // création de canal d’envoi
			DataInputStream in = new DataInputStream(socket.getInputStream()); // création d'un canal de réception

			// Envoi d'un message d'introduction au client
			out.writeUTF("Hello from server, you are client #" + clientNumber + " ! You are connected to the Sobel photo processing service.");
			
			// Réception du nom de l'utilisateur et du nom de l'image à traiter
			String username = in.readUTF();
			String inputName = in.readUTF();

			// Réception des métadonnées de l'image
			int width = in.readInt();
			int height = in.readInt();
			int type = in.readInt();

			// Réception de l'image à traiter
			long t0 = System.currentTimeMillis();
			int bytesRead = 0;
			FileOutputStream fileOutputStream = new FileOutputStream("./images/test.jpg");
			long size = in.readLong();
			byte[] buffer = new byte[4 * 1024];
			while((size > 0) && ((bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)) {
				fileOutputStream.write(buffer, 0, bytesRead);
				size -= bytesRead;
			}
			fileOutputStream.close();
			long t1 = System.currentTimeMillis();
			System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + " image in " + (t1-t0) + "ms.");
			String time = LocalDateTime.now().toString().replaceFirst("T", "@");
			System.out.format("[%s - %s:%d - %s]: image %s received for Sobel processing.%n", username, serverAddress, serverPort, time, inputName);

			// Envoi d'un message d'attente
			out.writeUTF("Image received by the server at " + time + " and is being processed...");

			// Traitement de l'image
			BufferedImage processedImage = Sobel.process(image);

			// Envoi de l'image traitée
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(processedImage, "jpg", byteArrayOutputStream);
			byte[] processedSize = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
			out.write(processedSize);
			out.write(byteArrayOutputStream.toByteArray());
			out.flush();
			System.out.println("Flushed: " + System.currentTimeMillis());

			

			} catch (IOException e) {
				System.out.println("Error handling client #" + clientNumber + ": " + e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("Couldn't close a socket, what's going on?");}
			System.out.println("Connection with client #" + clientNumber+ " closed");
		}
	}
};