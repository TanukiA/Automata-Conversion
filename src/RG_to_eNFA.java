import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class RG_to_eNFA {

    private String[] rg_rows;
    private ArrayList<String[]> rg_splitted;
    private ArrayList<String> Q; 
    private Set<Character> symbols;   
    private ArrayList<Character> symbolList; 
    private String p0; 
    private ArrayList<String> F; 
    // Hold the resulting transition values of NFA table with epsilon
    // Format: {"A0":"C", "A1":"B", "Aε":"∅"...}
    private TreeMap<String, String> transitionMap;

    public RG_to_eNFA(String[] rg_rows){
        this.rg_rows = rg_rows;
        rg_splitted = new ArrayList<String[]>();
        Q = new ArrayList<String>();
        symbols = new TreeSet<Character>();   
        F = new ArrayList<String>();
    }

    public ArrayList<String> getQ(){
        return Q;
    }

    public Set<Character> getSymbols(){
        return symbols;
    }

    public ArrayList<Character> getSymbolList(){
        return symbolList;
    }

    public String getP0(){
        return p0;
    }

    public ArrayList<String> getF(){
        return F;
    }

    public TreeMap<String, String> getTransitionMap(){
        return transitionMap;
    }

    public int getRowNum(){
        return Q.size();
    }

    public int getColNum(){
        return symbolList.size() + 1; // extra 1 for epsilon column 
    }

    public void retrieveVariables(){
       
        // Split regular grammar by → and | for each statement
        for(int i=0; i < rg_rows.length; i++){
            rg_splitted.add(rg_rows[i].split("[→//|]"));
        }

        // Store Q (set of states)
        for(int i=0; i < rg_splitted.size(); i++){
            Q.add(rg_splitted.get(i)[0]);
        }
        
        for(int i=0; i < rg_splitted.size(); i++){
            for(int j=1; j < rg_splitted.get(i).length; j++){
                // Store ∑ (set of input symbols)
                if(rg_splitted.get(i)[j].length() > 1){
                    symbols.add(rg_splitted.get(i)[j].charAt(0));
                }
                // Store final state
                if(rg_splitted.get(i)[j].equals("ε")){
                    F.add(rg_splitted.get(i)[0]);
                }
            }
        }
        
        symbolList = new ArrayList<Character>(symbols);
        // Store first state
        p0 = Q.get(0);

    }

    public void computeTableData(){

        transitionMap = new TreeMap<String, String>();

        // Set keys of TreeMap as "alphabet x symbol" (e.g. A0, A1, Aε, B0, B1, Bε,...)
        // Set all values of TreeMap as ∅ (null) first
        for(String s: Q){
            for(int i = 0; i < symbolList.size(); i++){
                transitionMap.put(s + symbolList.get(i), "∅");
                if(i == symbols.size()-1){
                    transitionMap.put(s + "ε", "∅");
                }
            }
        }

        // Set values of TreeMap (transition) row by row
        for(String[] row: rg_splitted){
            // Temporary store the symbol which already has value
            // Will be reset after each row of splitted regular grammar
            Set<Character> filledSymbols = new HashSet<Character>(); 
            Set<String> filledEpsilon = new HashSet<String>(); 

            for(int i=1; i<row.length; i++) {
                for(Character symbol: symbols){
                    // For each symbol found in the row, check whether there is value already or not for the
                    // relevant symbol within current row. If yes, take the existing value to concat with current value (e.g. "A" + "," + "B"),
                    // otherwise just store the current value.
                    if(row[i].charAt(0) == symbol){
                        if(filledSymbols.contains(symbol)){
                            String temp = transitionMap.get(row[0] + row[i].charAt(0));
                            temp = temp + "," + String.valueOf(row[i].charAt(1));
                            transitionMap.put(row[0] + row[i].charAt(0), temp);
                        }else{
                            filledSymbols.add(symbol);
                            transitionMap.put(row[0] + row[i].charAt(0), String.valueOf(row[i].charAt(1)));
                        }
                    }
                }
                setEpsilon(row, i, filledEpsilon);
            }
        }
    }

    public void setEpsilon(String[] row, int i, Set<String> filledEpsilon){
        // If any of the state does not have a symbol, store it into the respective epsilon column
        // If there is existing value for the epsilon column, concat it with current value first,
        // otherwise just store the current value
        if(row[i].length() == 1 && !row[i].equals("ε")){
            
            if(filledEpsilon.contains(row[0])){
                String temp = transitionMap.get(row[0] + "ε");
                temp = temp + "," + row[i];
                transitionMap.put(row[0] + "ε", temp);
            }else{
                filledEpsilon.add(row[0]);
                transitionMap.put(row[0] + "ε", row[i]);
            }
        }
    }
}
