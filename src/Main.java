import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class Main extends Application{

    private static Stage primaryStage; 

    public static void setPrimaryStage(Stage stage){
        primaryStage = stage;
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
    
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            MainController controller = new MainController();
            loader.setController(controller);
            setPrimaryStage(primaryStage);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Theory of Computation Assignment");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        }catch(IOException e){
            e.printStackTrace();
        } 
    }  
}
