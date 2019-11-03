package no.ntnu.imt3281.ludo.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.messages.ClientLogin;
import no.ntnu.imt3281.ludo.client.messages.ClientThrowDice;
import no.ntnu.imt3281.ludo.client.messages.Message;

/**
 * 
 * This is the main class for the client. 
 * **Note, change this to extend other classes if desired.**
 * 
 * @author 
 *
 */
public class Client extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("../gui/Ludo.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//launch(args);

		// for testing
		ClientSocket lol = new ClientSocket();
		//lol.establishConnectionToServer("0.0.0.0", -1);
		//lol.sendMessageToServer(new ClientLogin("someAction", "someUser", "somePass"));

		//Message asd = new ClientLogin("someAction", "someUser", "somePass");
		//((ClientLogin) asd).getPassword();


	}
}
