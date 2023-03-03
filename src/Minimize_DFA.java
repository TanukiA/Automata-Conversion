import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Minimize_DFA {

    private ArrayList<String> Q;
    private ArrayList<String> F; 
    private Set<Character> symbols;
    private ArrayList<ArrayList<String>> stateList; 
    private ArrayList<Integer> finalGroupIndices;
    private ArrayList<Integer> nonFinalGroupIndices;
    private ArrayList<String> finalGroup;
    private ArrayList<String> nonFinalGroup;

    public Minimize_DFA(ArrayList<String> Q, Set<Character> symbols, ArrayList<ArrayList<String>> stateList, ArrayList<String> F){
        this.Q = new ArrayList<String>();
        this.Q.addAll(Q);
        this.symbols = new TreeSet<Character>(); 
        this.symbols.addAll(symbols);
        this.stateList = new ArrayList<ArrayList<String>>();
        this.stateList.addAll(stateList);
        this.F = new ArrayList<String>(F);
    }

    public ArrayList<ArrayList<String>> getMinimizedStateList(){
        return stateList;
    }

    public ArrayList<String> getMinimizedQ(){
        return Q;
    }

    public int getRowNum(){
        return Q.size();
    }

    public void removeUnreachableStates(){

        int countUnreachable;
        // For each alphabet, check whether it can be reached by any of the symbols
        // Repeat until there is no more unreachable state exists
        do{
            
            countUnreachable = 0;
            for(int i = 0; i < Q.size(); i++){
                // count variable is initiated for each different state to keep track of number of symbol that alphabet is not found
                int countNotFound = 0;
                for(int j = 0; j < stateList.size(); j++){
                    // If the state cannot be found from current symbols' transition destinations, increment countNotFound
                    if(Q.get(i) != null && !stateList.get(j).contains(Q.get(i))){
                        countNotFound++;
                    // If the state can be found, but it is a self-loop only and not reachable by others, increment countNotFound
                    }else if(Q.get(i) != null){
                        ArrayList<Integer> indexFound = new ArrayList<Integer>();
                        for(int k = 0; k < stateList.get(j).size(); k++){
                            if(stateList.get(j).get(k) != null && stateList.get(j).get(k).equals(Q.get(i))){
                                indexFound.add(k);
                            }
                        }
                        if(indexFound.size() == 1 && indexFound.get(0) == i){
                            countNotFound++;
                        }
                    }
                }
                // If the countNotFound was incremented for all symbols, means the state is not reachable
                // Set the Q and stateList values of the unreachable index to null (remove later)
                if(countNotFound == stateList.size()){
                
                    Q.set(i, null);
                    for(ArrayList<String> innerList: stateList){
                        innerList.set(i, null);
                    }
                    countUnreachable++;
                }
            }

        }while(countUnreachable > 0);

        removeNullElements();
        // Since 3 functions (classifyStates, decomposeGroup & addGroupCombination) are still consider halfway 
        // of the minimization, we decided not to display their outputs
        // Instead, our minimization output is based on the previous functions to avoid confusion
        // The stated functions are working correctly, but our issue is not able to proceed to the next step
        classifyStates();
        decomposeGroup();
    }

    // Remove the unreachable elements where previously set as null temporarily
    public void removeNullElements(){
        Q.removeAll(Collections.singleton(null));
        for(ArrayList<String> innerList: stateList){
            innerList.removeAll(Collections.singleton(null));
        }
    }

    public void classifyStates(){

        // Convert ArrayList of Q to List of List
        // Convert from ["A", "B,C"] to [["A"], ["B", "C"]]
        List<List<String>> newQ = new ArrayList<List<String>>();

        for (String e: Q) {
            newQ.add(Arrays.asList(e.split(",")));
        }

        // Check whether the states are final states or not
        // Use boolean array to store the result of final state checking
        boolean[] finalStates = new boolean[getRowNum()];
        Arrays.fill(finalStates, false);
        for(int i = 0; i < newQ.size(); i++){
            for(int j = 0; j < newQ.get(i).size(); j++){
                if(F.contains(newQ.get(i).get(j))){
                    finalStates[i] = true;
                    break;
                }
            }
        }

        finalGroupIndices = new ArrayList<Integer>();
        nonFinalGroupIndices = new ArrayList<Integer>();
        finalGroup = new ArrayList<String>();
        nonFinalGroup = new ArrayList<String>();

        // Store indices and state values to respective group (final/nonFinal)
        for(int i = 0; i < getRowNum(); i++){
            if(finalStates[i] == true){
                finalGroupIndices.add(i);
                finalGroup.add(Q.get(i));
            }else{
                nonFinalGroupIndices.add(i);
                nonFinalGroup.add(Q.get(i));
            }
        }
    }

    public void decomposeGroup() {

        ArrayList<String> finalGroupTrans = addGroupCombination(finalGroup, finalGroupIndices);
        ArrayList<String> nonFinalGroupTrans = addGroupCombination(nonFinalGroup, nonFinalGroupIndices);

        // End here
    }

    // This method is used for both final group and non-final group to add their transition destination's 
    // combination and return respective ArrayList
    public ArrayList<String> addGroupCombination(ArrayList<String> group, ArrayList<Integer> groupIndex){

        ArrayList<String> groupTrans = new ArrayList<String>();

        // Iterate through each alphabet in the group to get the transition value of each symbol
        for(int i = 0; i < group.size(); i++){
            ArrayList<String> reachedStates = new ArrayList<String>();
            for(int j = 0; j < stateList.size(); j++){
                // Store index of transition value of the alphabet to ArrayList
                reachedStates.add(stateList.get(j).get(groupIndex.get(i)));
            }

            String combination = "";
            // For each reached state, classify into either final group or non-final group
            // Add the classification combination to the ArrayList and return
            for(int k = 0; k < reachedStates.size(); k++){
                if(group.contains(reachedStates.get(k))){
                    combination = combination + "F"; // Final group
                }else{
                    combination = combination + "N"; // Non-final group
                }
            }
            groupTrans.add(combination);
        }

        return groupTrans;
    }
    
}
