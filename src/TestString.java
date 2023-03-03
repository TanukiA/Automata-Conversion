import java.util.ArrayList;

public class TestString {
    
    private String[] testInputs;
    private String p0; 
    private ArrayList<String> F; 
    private ArrayList<String> Q;
    private ArrayList<ArrayList<String>> stateList;
    private ArrayList<Character> symbols; 
    private ArrayList<Integer> testResults; // 0 for NO, 1 for OK

    public TestString(String[] testInputs, String p0, ArrayList<String> F, ArrayList<String> Q, ArrayList<ArrayList<String>> stateList, ArrayList<Character> symbols){
        this.testInputs = testInputs;
        this.p0 = p0;
        this.F = new ArrayList<String>();
        this.F.addAll(F);
        this.Q = new ArrayList<String>();
        this.Q.addAll(Q);
        this.stateList = new ArrayList<ArrayList<String>>();
        this.stateList.addAll(stateList);
        this.symbols = new ArrayList<Character>(); 
        this.symbols.addAll(symbols);
        this.testResults = new ArrayList<Integer>();
    }

    public ArrayList<Integer> getTestResults(){
        return testResults;
    }

    // Find the index of current state
    public int findStateIndex(String currentState){

        for(int i = 0; i < Q.size(); i++){
            if(Q.get(i).equals(currentState)){
                return i;
            }
        }
        return 0;
    }

    // Find the index of current symbol to look for
    public int findSymbolIndex(char currentSymbol){

        for(int i = 0; i < symbols.size(); i++){
            if(symbols.get(i).equals(currentSymbol)){
                return i;
            }
        }
        return 0;
    }

    // Check whether the last state found for the test is a final state
    public boolean isFinal(String lastState){

        String[] substrings = lastState.split(",");
        for (String substr : substrings) {
            if (F.contains(substr)) {
                return true;
            }
        }

        return false;
    }

    public void checkString(){
    
        // Iterate through each test input one by one
        for(int i = 0; i < testInputs.length; i++){

            // Checking always start from the initial state
            String currentState = p0;
            String str = testInputs[i];
            boolean epsilon = false;
            
            // Iterate through each symbol contained in the test input
            for(int j = 0; j < str.length(); j++){
                char currentSymbol = str.charAt(j);
                // If the string is epsilon, check whether the first state is also a final state
                // Accept if it is, reject if not
                if(currentSymbol == 'ε'){
                    epsilon = true;
                    if(F.contains(p0)){
                        testResults.add(1);
                    }else{
                        testResults.add(0);
                    }
                    break;

                }else{
                    int stateIndex = findStateIndex(currentState);
                    int symbolIndex = findSymbolIndex(currentSymbol);
                    currentState = stateList.get(symbolIndex).get(stateIndex);
                }
            }

            // For non-epsilon input, check whether the last state found is a final state
            // Accept if it is, reject if not
            if(!epsilon){
                if(isFinal(currentState))
                    testResults.add(1);
                else
                    testResults.add(0);
            }
        }
    }
}