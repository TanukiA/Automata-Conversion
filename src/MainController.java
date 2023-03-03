import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button DFAbtn, NFA2btn, NFAbtn, clearBtn, importBtn, minDFAbtn, testBtn, checkBtn, arrowKey, epsilonKey;

    @FXML
    private TextArea inputArea, testArea;

    @FXML
    private Label importFileLabel, Q_label, finalState_label, firstState_label, symbol_label, transition_label, 
        tuple_label, checkResult1, checkResult2, checkResult3, checkResult4, checkResult5;

    @FXML
    private List<Label> checkResultLabels;

    @FXML
    private Text testTitle;

    @FXML
    private AnchorPane outputPane;

    @FXML
    private TableView<List<StringProperty>> table;

    private String buttonClickedStyle = "-fx-background-color: #FF6807; -fx-border-color: #FD5042; -fx-border-width: 2px;";
    private String buttonOriStyle = "-fx-background-color: #000000; -fx-border-color: #000000;";
    private RG_to_eNFA converter1;
    private eNFA_to_NFA converter2;
    private NFA_to_DFA converter3;
    private Minimize_DFA converter4;
    private TestString test;
    private ObservableList<List<StringProperty>> tableData = FXCollections.observableArrayList();

    @FXML
    void NFAbtnClicked(ActionEvent event) {
        resetButtonStyle(0);
        table.getItems().clear();
        table.getColumns().clear();
        NFAbtn.setStyle(buttonClickedStyle);

        String[] rg_rows = inputArea.getText().split("\n");
        // Remove whitespace from the regular grammar
        for(int i=0; i < rg_rows.length; i++){
            rg_rows[i] = rg_rows[i].replaceAll("\\s", "");
        }
        converter1 = new RG_to_eNFA(rg_rows);
        converter1.retrieveVariables();

        // Set Q text
        String Q_value = "";
        for(int i=0; i < converter1.getQ().size(); i++){
            Q_value = Q_value + converter1.getQ().get(i);
            if(i != converter1.getQ().size()-1)
                Q_value = Q_value + ",";
        }
        Q_label.setText("Q = {" + Q_value + "}");

        // Set ∑ text
        String symbol_val = "";
        int n = 0;
        for(Character c: converter1.getSymbols()){
            symbol_val = symbol_val + c;
            if(n != converter1.getSymbols().size()-1)
                symbol_val = symbol_val + ",";
            n++;
        }
        symbol_label.setText("∑ = {" + symbol_val + "}");

        // Set p0 text
        firstState_label.setText("p0 = " + converter1.getP0());

        // Set F text
        String F_value = "";
        for(int i=0; i < converter1.getF().size(); i++){
            F_value = F_value + converter1.getF().get(i);
            if(i != converter1.getF().size()-1)
                F_value = F_value + ",";
        }
        finalState_label.setText("F = {" + F_value + "}");

        // Set table columns of NFA with epsilon
        TableColumn<List<StringProperty>, String> firstCol = new TableColumn<>("δNFA");
        firstCol.setStyle("-fx-alignment: CENTER;");
        firstCol.setCellValueFactory(data -> data.getValue().get(0));
        table.getColumns().add(firstCol);

        for(int i = 0; i < converter1.getSymbolList().size(); i++){
            final int count = i+1;
            TableColumn<List<StringProperty>, String> symbolCol = new TableColumn<>(Character.toString(converter1.getSymbolList().get(i)));
            symbolCol.setStyle("-fx-alignment: CENTER;");
            symbolCol.setCellValueFactory(data -> data.getValue().get(count));
            table.getColumns().add(symbolCol);
        }

        int symbolSize = converter1.getSymbols().size();

        TableColumn<List<StringProperty>, String> lastCol = new TableColumn<>("ε");
        lastCol.setStyle("-fx-alignment: CENTER;");
        lastCol.setCellValueFactory(data -> data.getValue().get(symbolSize + 1));
        table.getColumns().add(lastCol);

        // Compute table values
        converter1.computeTableData();
        ArrayList<String> valuesList = new ArrayList<String>(converter1.getTransitionMap().values());
        
        // Index for retrieving elements from valuesList
        int countValuesList = 0;
        
        for (int i = 0; i < converter1.getRowNum(); i++) {
            List<StringProperty> values = new ArrayList<>();
            // Add alphabets for first column 
            values.add(0, new SimpleStringProperty(converter1.getQ().get(i)));
            // Index for adding column value to the StringProperty list (reset for each row)
            int countCol = 1;
            
            // Add values for second to last column
            for(int j = 0; j < converter1.getColNum(); j++){
                String currentVal = valuesList.get(countValuesList);
                if(!currentVal.equals("∅"))
                    values.add(countCol, new SimpleStringProperty("{" + currentVal + "}"));
                else
                    values.add(countCol, new SimpleStringProperty(currentVal));

                countValuesList++;
                countCol++;
            }
            tableData.add(values);
        }
    
        // Set table rows of NFA with epsilon
        table.setItems(tableData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Q_label.setVisible(true);
        finalState_label.setVisible(true);
        firstState_label.setVisible(true);
        symbol_label.setVisible(true);
        transition_label.setVisible(true);
        tuple_label.setVisible(true);
        table.setVisible(true);
        testTitle.setVisible(false);
        testArea.setVisible(false);
        checkBtn.setVisible(false);
        for(Label label: checkResultLabels){
            label.setVisible(false);
        }
    }
    
    @FXML
    void NFA2btnClicked(ActionEvent event) {

        if(this.converter1 == null){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("Previous function was not run\nInput may be empty");
            alert.setContentText("Please click the 'NFA' button first before proceeding\nPlease ensure there is an input");
            alert.showAndWait();

        }else{

            resetButtonStyle(1);
            table.getItems().clear();
            table.getColumns().clear();
            NFA2btn.setStyle(buttonClickedStyle);

            converter2 = new eNFA_to_NFA(converter1.getTransitionMap());

            // Set table columns of NFA without epsilon
            TableColumn<List<StringProperty>, String> firstCol = new TableColumn<>("δNFA");
            firstCol.setStyle("-fx-alignment: CENTER;");
            firstCol.setCellValueFactory(data -> data.getValue().get(0));
            table.getColumns().add(firstCol);
    
            for(int i = 0; i < converter1.getSymbolList().size(); i++){
                final int count = i+1;
                TableColumn<List<StringProperty>, String> symbolCol = new TableColumn<>(Character.toString(converter1.getSymbolList().get(i)));
                symbolCol.setStyle("-fx-alignment: CENTER;");
                symbolCol.setCellValueFactory(data -> data.getValue().get(count));
                table.getColumns().add(symbolCol);
            }
    
            // Compute table values
            converter2.findTransitionToRemove();
            converter2.findReachableStates();
            converter2.removeEpsilonCol();
            ArrayList<String> noEpsilonList = new ArrayList<String>(converter2.getTransitionMap().values());
           
            // Index for retrieving elements from valuesList
            int countValuesList = 0;
           
            for (int i = 0; i < converter1.getRowNum(); i++) {
                List<StringProperty> values = new ArrayList<>();
                String current_symbol = converter1.getQ().get(i);
    
                // Add alphabets for first column with indication of start and final states
                if(current_symbol.equals(converter1.getP0()) && converter1.getF().contains(current_symbol)){
                    values.add(0, new SimpleStringProperty("→*" + current_symbol));
                }
                else if(current_symbol.equals(converter1.getP0())){
                    values.add(0, new SimpleStringProperty("→" + current_symbol));
                
                }else if(converter1.getF().contains(current_symbol)){
                    values.add(0, new SimpleStringProperty("*" + current_symbol));
                }else{
                    values.add(0, new SimpleStringProperty(current_symbol));
                }
    
                // Index for adding column value to the StringProperty list (reset for each row)
                int countCol = 1;
                
                // Add values for second to last column (without epsilon column)
                for(int j = 0; j < converter1.getColNum()-1; j++){
                    String currentVal = noEpsilonList.get(countValuesList);
                 
                    if(!currentVal.equals("∅"))
                        values.add(countCol, new SimpleStringProperty("{" + currentVal + "}"));
                    else
                        values.add(countCol, new SimpleStringProperty(currentVal));
    
                    countValuesList++;
                    countCol++;
               }
               tableData.add(values);
               testTitle.setVisible(false);
               testArea.setVisible(false);
               checkBtn.setVisible(false);
               for(Label label: checkResultLabels){
                   label.setVisible(false);
               }
            }
       
            // Set table rows of NFA without epsilon
            table.setItems(tableData);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }

    @FXML
    void DFAbtnClicked(ActionEvent event) {

        if(this.converter2 == null){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("Previous function(s) was not run\nInput may be empty");
            alert.setContentText("Please click the 'NFA w/o ε' button first before proceeding\nPlease ensure there is an input");
            alert.showAndWait();

        }else{

            resetButtonStyle(2);
            DFAbtn.setStyle(buttonClickedStyle);
            table.getItems().clear();
            table.getColumns().clear();

            converter3 = new NFA_to_DFA(converter1.getQ(), converter1.getSymbols(), converter2.getTransitionMap());
        
            // Set table columns of DFA
            TableColumn<List<StringProperty>, String> firstCol = new TableColumn<>("δDFA");
            firstCol.setStyle("-fx-alignment: CENTER;");
            firstCol.setCellValueFactory(data -> data.getValue().get(0));
            table.getColumns().add(firstCol);
    
            for(int i = 0; i < converter1.getSymbolList().size(); i++){
                final int count = i+1;
                TableColumn<List<StringProperty>, String> symbolCol = new TableColumn<>(Character.toString(converter1.getSymbolList().get(i)));
                symbolCol.setStyle("-fx-alignment: CENTER;");
                symbolCol.setCellValueFactory(data -> data.getValue().get(count));
                table.getColumns().add(symbolCol);
            }
    
            converter3.computeTableData();
            ArrayList<ArrayList<String>> possibleStateList = converter3.getPossibleStateList();
            converter3.addElementsToNewQ(possibleStateList);
            ArrayList<String> newQ = converter3.getNewQ();
            ArrayList<ArrayList<String>> newStateList = converter3.getNewStateList();
        
            // Check whether the new states are final states or not
            // Use boolean array to store the result of final state checking
            boolean[] finalStates = new boolean[converter3.getRowNum()];
            Arrays.fill(finalStates, false);
            for(int i = 0; i < possibleStateList.size(); i++){
                for(int j = 0; j < possibleStateList.get(i).size(); j++){
                    if(converter1.getF().contains(possibleStateList.get(i).get(j))){
                        finalStates[i] = true;
                        break;
                    }
                }
            }
    
            for (int i = 0; i < converter3.getRowNum(); i++) {
                List<StringProperty> values = new ArrayList<>();
                String current_alphabet = newQ.get(i);
    
                // Add alphabets for first column with indication of start and final states
                if(current_alphabet.equals(converter1.getP0()) && finalStates[i] == true){
                    values.add(0, new SimpleStringProperty("→*" + current_alphabet));
                }
                else if(current_alphabet.equals(converter1.getP0())){
                    values.add(0, new SimpleStringProperty("→" + current_alphabet));
                
                }else if(finalStates[i] == true){
                    values.add(0, new SimpleStringProperty("*" + current_alphabet));
                }else{
                    values.add(0, new SimpleStringProperty(current_alphabet));
                }
    
                // Index for adding column value to the StringProperty list (reset for each row)
                int countCol = 1;
                
                // Add values for second to last column
                for(int j = 0; j < converter1.getColNum()-1; j++){
                    String currentVal = newStateList.get(j).get(i);
                 
                    if(!currentVal.equals("∅"))
                        values.add(countCol, new SimpleStringProperty("{" + currentVal + "}"));
                    else
                        values.add(countCol, new SimpleStringProperty(currentVal));
    
                    countCol++;
                }
                tableData.add(values);
            }
    
            // Set table rows of DFA
            table.setItems(tableData);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
            Q_label.setVisible(false);
            finalState_label.setVisible(false);
            firstState_label.setVisible(false);
            symbol_label.setVisible(false);
            transition_label.setVisible(false);
            tuple_label.setVisible(false);
            testTitle.setVisible(false);
            testArea.setVisible(false);
            checkBtn.setVisible(false);
            for(Label label: checkResultLabels){
                label.setVisible(false);
            }
        }
    }

    @FXML
    void minDFAbtnClicked(ActionEvent event) {

        if(this.converter3 == null){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("Previous function(s) was not run\nInput may be empty");
            alert.setContentText("Please click the 'DFA' button first before proceeding\nPlease ensure there is an input");
            alert.showAndWait();

        }else{

            resetButtonStyle(3);
            minDFAbtn.setStyle(buttonClickedStyle);
            table.getItems().clear();
            table.getColumns().clear();

            converter4 = new Minimize_DFA(converter3.getNewQ(), converter1.getSymbols(), converter3.getNewStateList(), converter1.getF());
        
            // Set table columns of Minimized DFA
            TableColumn<List<StringProperty>, String> firstCol = new TableColumn<>("δDFA");
            firstCol.setStyle("-fx-alignment: CENTER;");
            firstCol.setCellValueFactory(data -> data.getValue().get(0));
            table.getColumns().add(firstCol);
    
            for(int i = 0; i < converter1.getSymbolList().size(); i++){
                final int count = i+1;
                TableColumn<List<StringProperty>, String> symbolCol = new TableColumn<>(Character.toString(converter1.getSymbolList().get(i)));
                symbolCol.setStyle("-fx-alignment: CENTER;");
                symbolCol.setCellValueFactory(data -> data.getValue().get(count));
                table.getColumns().add(symbolCol);
            }
    
            converter4.removeUnreachableStates();
            ArrayList<String> minimized_Q = converter4.getMinimizedQ();
            ArrayList<ArrayList<String>> minimizedStateList = converter4.getMinimizedStateList();
        
            // Convert ArrayList to List of List for minimized_Q
            List<List<String>> minimized_Q_2 = new ArrayList<List<String>>();
    
            for (String e: minimized_Q) {
                minimized_Q_2.add(Arrays.asList(e.split(",")));
            }
    
            // Check whether the states are final states or not
            // Use boolean array to store the result of final state checking
            boolean[] finalStates = new boolean[converter4.getRowNum()];
            Arrays.fill(finalStates, false);
            for(int i = 0; i < minimized_Q_2.size(); i++){
                for(int j = 0; j < minimized_Q_2.get(i).size(); j++){
                    if(converter1.getF().contains(minimized_Q_2.get(i).get(j))){
                        finalStates[i] = true;
                        break;
                    }
                }
            }
    
            for (int i = 0; i < converter4.getRowNum(); i++) {
                List<StringProperty> values = new ArrayList<>();
                String current_alphabet = minimized_Q.get(i);
    
                // Add alphabets for first column with indication of start and final states
                if(current_alphabet.equals(converter1.getP0()) && finalStates[i] == true){
                    values.add(0, new SimpleStringProperty("→*" + current_alphabet));
                }
                else if(current_alphabet.equals(converter1.getP0())){
                    values.add(0, new SimpleStringProperty("→" + current_alphabet));
                
                }else if(finalStates[i] == true){
                    values.add(0, new SimpleStringProperty("*" + current_alphabet));
                }else{
                    values.add(0, new SimpleStringProperty(current_alphabet));
                }
    
                // Index for adding column value to the StringProperty list (reset for each row)
                int countCol = 1;
                
                // Add values for second to last column
                for(int j = 0; j < converter1.getColNum()-1; j++){
                    String currentVal = minimizedStateList.get(j).get(i);
                 
                    if(!currentVal.equals("∅"))
                        values.add(countCol, new SimpleStringProperty("{" + currentVal + "}"));
                    else
                        values.add(countCol, new SimpleStringProperty(currentVal));
    
                    countCol++;
                }
                tableData.add(values);
            }
    
            // Set table rows of Minimized DFA
            table.setItems(tableData);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    
            Q_label.setVisible(false);
            finalState_label.setVisible(false);
            firstState_label.setVisible(false);
            symbol_label.setVisible(false);
            transition_label.setVisible(false);
            tuple_label.setVisible(false);
            testTitle.setVisible(false);
            testArea.setVisible(false);
            checkBtn.setVisible(false);
            for(Label label: checkResultLabels){
                label.setVisible(false);
            }
        }
    }

    @FXML
    void testBtnClicked(ActionEvent event) {
        resetButtonStyle(4);
        testBtn.setStyle(buttonClickedStyle);
        Q_label.setVisible(false);
        finalState_label.setVisible(false);
        firstState_label.setVisible(false);
        symbol_label.setVisible(false);
        transition_label.setVisible(false);
        tuple_label.setVisible(false);
        table.setVisible(false);
        testTitle.setVisible(true);
        testArea.setVisible(true);
        checkBtn.setVisible(true);
    }

    @FXML
    void checkBtnClicked(ActionEvent event) {

        // Clean previous test result
        for(Label label: checkResultLabels){
            label.setText("");
        }
        
        // Put test inputs line by line into array
        String[] testInputs = testArea.getText().split("\n");

        if(testInputs.length == 1 && testInputs[0] == ""){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("Test input is empty");
            alert.setContentText("Please type at least a line of string to be tested");
            alert.showAndWait();

        }
        else if(testInputs.length > 5){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("Input strings are more than 5");
            alert.setContentText("You have exceed the limit of strings. Please reduce to 5 or less strings.");
            alert.showAndWait();

        }
        else{

            test = new TestString(testInputs, converter1.getP0(), converter1.getF(), converter4.getMinimizedQ(), converter4.getMinimizedStateList(), converter1.getSymbolList());
            test.checkString();
            ArrayList<Integer> testResults = test.getTestResults();
            // If result is 0 means rejected, if result is 1 means accepted
            for(int i = 0; i < testResults.size(); i++){
                if(testResults.get(i).equals(0)){
                    checkResultLabels.get(i).setText("NO");
                }else{
                    checkResultLabels.get(i).setText("OK");
                }
                checkResultLabels.get(i).setVisible(true);
            }
        }
    }

    @FXML
    void clearBtnClicked(ActionEvent event) {
        inputArea.clear();
        importFileLabel.setText("");
    }

    @FXML
    void importBtnClicked(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        Stage stage = Main.getPrimaryStage();
        // Choose file
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {            
            importFileLabel.setText(file.getName());
        }

        // Read file
        try(Scanner sc = new Scanner(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            while(sc.hasNext()){
                inputArea.appendText(sc.nextLine().replaceAll("\\s", "") + "\n");
            }
    
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void arrowKeyClicked(ActionEvent event) {
        String currentInput = inputArea.getText();
        currentInput = currentInput + "→";
        inputArea.setText(currentInput);
    }

    @FXML
    void epsilonKeyClicked(ActionEvent event) {
        String currentInput = inputArea.getText();
        currentInput = currentInput + "ε";
        inputArea.setText(currentInput);
    }

    void resetButtonStyle(int btn) {
        if(btn != 0){
            NFAbtn.setStyle(buttonOriStyle);
        }
        if(btn != 1){
            NFA2btn.setStyle(buttonOriStyle);
        }   
        if(btn != 2){
            DFAbtn.setStyle(buttonOriStyle);
        }
        if(btn != 3){
            minDFAbtn.setStyle(buttonOriStyle);
        }
        if(btn != 4){
            testBtn.setStyle(buttonOriStyle);
        }
    }

    @FXML
    void initialize() {
     
    }
}
