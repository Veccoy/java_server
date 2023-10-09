import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
			int length = in.readInt();

			// Réception de l'image à traiter
			long t0 = System.currentTimeMillis();
			byte[] imageData = new byte[length];
			imageData = receiveImageToProcess(imageData, length, in);

			BufferedImage image = byteArrayToBufferedImage(imageData);
			long t1 = System.currentTimeMillis();
			System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + " image in " + (t1-t0) + "ms.");
			String time = LocalDateTime.now().toString().replaceFirst("T", "@");
			System.out.format("[%s - %s:%d - %s]: image %s received for Sobel processing.%n", username, serverAddress, serverPort, time, inputName);

			// Envoi d'un message d'attente
			out.writeUTF("Image received by the server at " + time + " and is being processed...");

			// Traitement de l'image
			BufferedImage processedImage = Sobel.process(image);

			// Envoi de l'image traitée
			byte[] processedImageData = bufferedImageToByteArray(processedImage);
			transmitImageToClient(processedImageData, out);

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

	//Fonction qui permet de recevoir les données en bytes de l'image à filtrer de la fonction transmitImageToServer() de classe Client
	//à l'aide d'une boucle qui recoit petit par petit les bytes de l'image à filtrer et les mets dans une array de bytes.
	private static byte[] receiveImageToProcess(byte[] imageData, int length, DataInputStream in) {
		try {
			byte[] inBuffer = new byte[4096];
			int bytesRead = 0;
			while(bytesRead < length) {
				if (length - bytesRead >= 4096) {
					in.read(inBuffer, 0, 4096);
					for (int i=0 ; i < 4096 ; i++) {
						imageData[bytesRead + i] = inBuffer[i];
					}
					bytesRead += 4096;
				} else {
					int nbLeftBytes = length - bytesRead;
					byte[] smallInBuffer = new byte[nbLeftBytes];
					in.read(smallInBuffer, 0, nbLeftBytes);
					for (int i=0 ; i < nbLeftBytes ; i++) {
						imageData[bytesRead + i] = smallInBuffer[i];
					}
					bytesRead += nbLeftBytes;
				}
			}
			return imageData;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	//Fonction qui permet d'envoyer au client les données en bytes de l'image filtrer à l'aide d'une boucle
	// qui envoie petit par petit les bytes de l'image filtrer au client. Le client recevra les données par la fonction
	//receiveProcessedImage() de la classe Client
	private static void transmitImageToClient(byte[] processedImageData, DataOutputStream out) {
		try {
			byte[] outBuffer = new byte[4096];
			int bytesWritten = 0;
			while(bytesWritten < processedImageData.length) {
				if (processedImageData.length - bytesWritten >= 4096) {
					for (int i=0 ; i < outBuffer.length ; i++) {
						outBuffer[i] = processedImageData[bytesWritten + i];
					}
					out.write(outBuffer, 0, 4096);
					bytesWritten += 4096;
				}
				else {
					int nbLeftBytes = (int) processedImageData.length - bytesWritten;
					byte[] smallOutBuffer = new byte[nbLeftBytes];
					for (int i=0 ; i < smallOutBuffer.length ; i++) {
						smallOutBuffer[i] = processedImageData[bytesWritten + i];
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

	//Fonction qui permet de transformer un objet de type BufferedImage en objet de type byte[]
	private byte[] bufferedImageToByteArray(BufferedImage image) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpg", stream);
		} catch(IOException e) {
			e.printStackTrace();
		}

		return stream.toByteArray();
	}
	
	//Fonction qui permet de transformer un objet de type byte[] en objet de type BufferedImage
	private BufferedImage byteArrayToBufferedImage(byte[] imageData) throws IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(imageData);
		BufferedImage image;
		try {
			image = ImageIO.read(stream);
		} finally {}

		return image;
	}
};