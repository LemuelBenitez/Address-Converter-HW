package application;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class Main extends Application {

	String host = "localhost";

	int index, size = 0;

	private TextField tfName = new TextField();
	private TextField tfStreet = new TextField();
	private TextField tfCity = new TextField();
	private TextField tfState = new TextField();
	private TextField tfZip = new TextField();

	private Button btAdd = new Button("Add");
	private Button btFirst = new Button("First");
	private Button btNext = new Button("Next");
	private Button btPrevious = new Button("Previous");
	private Button btLast = new Button("Last");

	ObjectOutputStream objectToServer = null;
	ObjectInputStream objectFromServer = null;
	DataInputStream dataFromServer = null;
	DataOutputStream dataToServer = null;

	private void setTextFields(Address s) {
		tfName.setText(s.getName());
		tfStreet.setText(s.getStreet());
		tfCity.setText(s.getCity());
		tfState.setText(s.getState());
		tfZip.setText(s.getZip());
	}

	private void addAddress() {
		try {
			Socket socket = new Socket(host, 8000);

			dataToServer = new DataOutputStream(socket.getOutputStream());

			dataToServer.writeUTF("ADD_STUDENT");
			dataToServer.flush();

			String name = tfName.getText().trim();
			String street = tfStreet.getText().trim();
			String city = tfCity.getText().trim();
			String state = tfState.getText().trim();
			String zip = tfZip.getText().trim();

			objectToServer = new ObjectOutputStream(socket.getOutputStream());
			Address s = new Address(name, street, city, state, zip);
			objectToServer.writeObject(s);

			dataFromServer = new DataInputStream(socket.getInputStream());
			index = dataFromServer.readInt(); 
			size = dataFromServer.readInt(); 
			System.out.println("Address index: " + index);

			tfName.clear();
			tfStreet.clear();
			tfCity.clear();
			tfState.clear();
			tfZip.clear();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void getAddress(int i) {
		try {
			Socket socket = new Socket(host, 8000);

			dataToServer = new DataOutputStream(socket.getOutputStream());
			dataToServer.writeUTF("GET_INDEX");
			dataToServer.writeInt(i);
			System.out.println("Get index: " + i);
			dataToServer.flush();

			objectFromServer = new ObjectInputStream(socket.getInputStream());
			Address s = (Address) objectFromServer.readObject();

			setTextFields(s);

			dataFromServer = new DataInputStream(socket.getInputStream());
			index = dataFromServer.readInt(); 
			size = dataFromServer.readInt(); 
			System.out.println("Address index: " + index);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void getFirst() {
		if (index < 0) {
			System.out.println("Database is empty");
		} else {
			getAddress(0);
		}
	}

	private void getNext() {
		getAddress(index + 1);
	}

	private void getPrevious() {
		if (index < 1) {
			System.out.println("Database is empty");
		} else {
			getAddress(index - 1);
		}
	}

	private void getLast() {
		try {
			Socket socket = new Socket(host, 8000);

			dataToServer = new DataOutputStream(socket.getOutputStream());
			dataToServer.writeUTF("GET_LAST");
			dataToServer.flush();

			objectFromServer = new ObjectInputStream(socket.getInputStream());
			Address s = (Address) objectFromServer.readObject();

			setTextFields(s);

			dataFromServer = new DataInputStream(socket.getInputStream());
			index = dataFromServer.readInt(); 
			size = dataFromServer.readInt(); 
			System.out.println("Address index: " + index);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override 
	public void start(Stage primaryStage) {
		GridPane pane = new GridPane();
		Scene scene = new Scene(pane, 390, 195);
		primaryStage.setTitle("Student Address Database"); 
		primaryStage.setScene(scene); 
		
		pane.add(new Label("Name"), 0, 0);
		pane.add(tfName, 1, 0);
		pane.add(new Label("Street"), 0, 1);
		pane.add(tfStreet, 1, 1);
		pane.add(new Label("City"), 0, 2);

		HBox hBox = new HBox(5);
		pane.add(hBox, 1, 2);
		hBox.getChildren().addAll(tfCity, new Label("State"), tfState, new Label("Zip"), tfZip);

		HBox paneForButtons = new HBox(5);
		paneForButtons.getChildren().addAll(btAdd, btFirst, btNext, btPrevious, btLast);
		pane.add(paneForButtons, 1, 3);
		pane.setHgap(5);
		pane.setVgap(5);

		pane.setAlignment(Pos.CENTER);
		tfName.setPrefColumnCount(15);
		tfState.setPrefColumnCount(15);
		tfCity.setPrefColumnCount(10);
		tfState.setPrefColumnCount(2);
		tfZip.setPrefColumnCount(3);

		btAdd.setOnAction(e -> addAddress());
		btFirst.setOnAction(e -> getFirst());
		btNext.setOnAction(e -> getNext());
		btPrevious.setOnAction(e -> getPrevious());
		btLast.setOnAction(e -> getLast());

		primaryStage.show(); 

	}

}
