package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


public class Server {
			private ObjectInputStream objectFromClient;
			private ObjectOutputStream objectToClient;
			private DataOutputStream dataToClient;
			private DataInputStream dataFromClient;

			private List<Address> addresses = new ArrayList<>();

			private static final int CONCURRENT_CONNECTIONS = 2;

			private static Semaphore semaphore = new Semaphore(CONCURRENT_CONNECTIONS);

			public static void main(String[] args) {		
				new Server();
			}

			public Server() {
				try {
					ServerSocket serverSocket = new ServerSocket(8000);
					System.out.println("Server started ");
					while (true) {
						Socket socket = serverSocket.accept();			

						new Thread(new HandleAClient(socket)).start();
					}
					
				}
				catch (IOException ex ) {
					ex.printStackTrace();
				}
				finally {
					try {
						objectFromClient.close();
						objectToClient.close();
						dataFromClient.close();
						dataToClient.close();
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			class HandleAClient implements Runnable {
				private Socket socket; 

				public  HandleAClient(Socket socket) {
					this.socket = socket;
				}

				public void run() {
					try {
						semaphore.acquire(); 

						dataFromClient = new DataInputStream(socket.getInputStream());
						String command = dataFromClient.readUTF();

						if (command.equals("ADD_STUDENT")) { 
							objectFromClient = new ObjectInputStream(socket.getInputStream());

							Address object = (Address)objectFromClient.readObject();
							addresses.add(object);
							System.out.println("Number of students: " + addresses.size());

							dataToClient = new DataOutputStream(socket.getOutputStream());
							dataToClient.writeInt(addresses.indexOf(object));
							dataToClient.writeInt(addresses.size());
						}
						else if (command.equals("GET_INDEX")) { 
							int index = dataFromClient.readInt();
							System.out.println("The user wants address at index: " + index);
							
						 	if (index >= addresses.size())
						 		index = addresses.size() - 1;

							objectToClient = new ObjectOutputStream(socket.getOutputStream());					
							objectToClient.writeObject(addresses.get(index));

							dataToClient = new DataOutputStream(socket.getOutputStream());
							dataToClient.writeInt(index);

							dataToClient.writeInt(addresses.size()); 
							objectToClient.flush();
							System.out.println("Address at index " + index + " was sent to user.");
						}
						else if (command.equals("GET_LAST")) { 
							int index = addresses.size() - 1;
							objectToClient = new ObjectOutputStream(socket.getOutputStream());					
							objectToClient.writeObject(addresses.get(index));

							dataToClient = new DataOutputStream(socket.getOutputStream());
							dataToClient.writeInt(index);

							dataToClient.writeInt(addresses.size());
							objectToClient.flush();
							System.out.println("Address at index " + index + " sent to user.");
						}
					}
					catch (ClassNotFoundException ex) {
						ex.printStackTrace();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
					catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					finally {
						semaphore.release();
						try{
							socket.close();
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

}
