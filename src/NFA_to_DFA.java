import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

public class NFA_to_DFA{
 
    private TreeMap<String, String> transitionMap;
    private ArrayList<String> Q;
    private ArrayList<ArrayList<String>> possibleStateList; // Store all possible states of first table column
    private ArrayList<ArrayList<String>> newStateList; // Store the states of resulting DFA
    private Set<Character> symbols;
    private ArrayList<String> newQ; // Store the String values ready to be inserted into first table column
    
    public NFA_to_DFA(ArrayList<String> Q, Set<Character> symbols, TreeMap<String, String> transitionMapWithoutEpsilon){
        this.transitionMap = new TreeMap<String, String>();
        this.transitionMap.putAll(transitionMapWithoutEpsilon);
        this.Q = new ArrayList<String>();
        this.Q.addAll(Q);
        this.symbols = new TreeSet<Character>(); 
        this.symbols.addAll(symbols);
        newQ = new ArrayList<String>();
    }

    public ArrayList<ArrayList<String>> getPossibleStateList(){
        return possibleStateList;
    }

    public ArrayList<ArrayList<String>> getNewStateList(){
        return newStateList;
    }

    public ArrayList<String> getNewQ(){
        return newQ;
    }

    public int getRowNum(){
        return possibleStateList.size();
    }

    public void computeTableData(){
        ArrayList<String> stateList = new ArrayList<String>(Q);
        possibleStateList = new ArrayList<ArrayList<String>>();

        // Generate all possible set of states using recursive method
        generateCombinations(stateList, 0, new ArrayList<>(), possibleStateList);
        // Sort the possibleStateList using a custom comparator
        Collections.sort(possibleStateList, new CustomComparator());

        set_DFA_data();
        removeExtraNull(newStateList);
        removeDuplicates(newStateList);
        sortStrCombination(newStateList);
        replaceEmptyWithNull(possibleStateList);
    }

    public static void generateCombinations(ArrayList<String> stateList, int index, ArrayList<String> currentList, 
        ArrayList<ArrayList<String>> possibleStateList) {
        // Base case: add the current list to the result when all possible elements are already processed
        if (index == stateList.size()) {
            possibleStateList.add(currentList);
            return;
        }
    
        // Recursive case: generate lists with and without the current element
        generateCombinations(stateList, index + 1, currentList, possibleStateList);
        ArrayList<String> newList = new ArrayList<>(currentList);
        newList.add(stateList.get(index));
        generateCombinations(stateList, index + 1, newList, possibleStateList);
    }

    // Custom Comparator is used for sorting (prepare for table display)
    // The inner ArrayLists are first sorted by ascending order of size
    // Then, if the list sizes are same, sort the elements alphabetically.
    private static class CustomComparator implements Comparator<ArrayList<String>> {
        @Override
        public int compare(ArrayList<String> list1, ArrayList<String> list2) {
            // If the sizes are the same, sort the inner lists alphabetically
            if (list1.size() == list2.size()) {
                Collections.sort(list1);
                Collections.sort(list2);
                // Compare the first elements of the sorted lists
                return list1.get(0).compareTo(list2.get(0));
            }
            // Otherwise, sort the lists by size
            return list1.size() - list2.size();
        }
    }

    public void set_DFA_data(){

        int sizeOfTransitionMap = transitionMap.size();

        newStateList = new ArrayList<ArrayList<String>>();

        // For each symbol, create inner ArrayList and add the respective values retrieved from transitionMap.
        // At the end of each symbol iteration, add the completed inner ArrayList to newStateList (outer ArrayList).
        for(Character c: symbols){
            int count = 0;
            ArrayList<String> innerList = new ArrayList<String>();
            // Add null for first row of DFA
            innerList.add("∅");
            for(Map.Entry<String, String> entry : transitionMap.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                if(key.contains(String.valueOf(c))){
                    innerList.add(value);
                }
                if(count == sizeOfTransitionMap-1){
                    newStateList.add(innerList);
                }
                count++;
            }
        }
    
        ArrayList<ArrayList<String>> tempList = new ArrayList<ArrayList<String>>(possibleStateList);
        ArrayList<ArrayList<String>> listWithoutOriStates = removeOriginalStates(tempList);
        
        // Iterate through each state and for each symbol, get each alphabet (e.g. get A from [A,B])
        // For each symbols of the alphabet, get values from the transitionMap and store into respective ArrayList 
        for(ArrayList<String> state: listWithoutOriStates){
            int countState = state.size();
            int countSymbol = 0;
            
            for(Character c: symbols){
                int count = 0;
                String unionResult = "";
                for(String alphabet: state){
                    for(Map.Entry<String, String> entry : transitionMap.entrySet()){
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if(key.equals(alphabet + c)){
                            // If the unionResult is still empty (first time), append directly
                            if(!value.equals("∅") && unionResult.equals("")){
                                unionResult = unionResult + value;
                            // If the unionResult already has other value, add comma and append
                            }else if(!value.equals("∅") && !unionResult.equals("")){
                                unionResult = unionResult + "," + value;
                            // If the value is null and unionResult is still empty (first time), append directly
                            // No append happen for the case where value is null, but unionResult already has other value
                            }else if(value.equals("∅") && unionResult.equals("")){
                                unionResult = unionResult + value;
                            }
                        }
                    }

                    // At the end of a state's symbol iteration, add the final union result to inner ArrayList of newStateList
                    // This part is runned once for all DFA states
                    if(count == countState-1){
                        newStateList.get(countSymbol).add(unionResult);
                    }
                    count++;
                }
                countSymbol++;
            }
        }
    }

    // Remove the inner lists that have size 1 (Remove original states without combination e.g. [A],[B])
    public ArrayList<ArrayList<String>> removeOriginalStates(ArrayList<ArrayList<String>> list){
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).size() == 1) {
                list.remove(i);
                i--;
            }
        }
        return list;
    }

    // Replace [] element with "∅" for first column of DFA table
    public void replaceEmptyWithNull(ArrayList<ArrayList<String>> list){
       
        ArrayList<String> innerList = list.get(0);
        if(innerList.isEmpty()){
            innerList.add("∅");
        }
    }

    public void sortStrCombination(ArrayList<ArrayList<String>> list){

        for(int i = 0; i < list.size(); i++){
            for(int j = 0; j < list.get(i).size(); j++){
                List<String> temp = Arrays.asList(list.get(i).get(j).split(","));
                // Sort the list in ascending order
                Collections.sort(temp);
                // Join the sorted list back and set the sorted values
                list.get(i).set(j, String.join(",", temp));
            }
        }
    }

    // Remove unneeded null element from the state (e.g. Remove ∅ from [∅,A] as it is no longer empty)
    public void removeExtraNull(ArrayList<ArrayList<String>> list){
        for(int i = 0; i < list.size(); i++){
            for(int j = 0; j < list.get(i).size(); j++){
                if(list.get(i).get(j).length() > 1){
                    String result = list.get(i).get(j).replaceAll("∅,", "");
                    list.get(i).set(j, result);
                }
            }
        }
    }

    // Remove duplicated element from the state (e.g. Remove duplicated B from [B,A,B])
    public void removeDuplicates(ArrayList<ArrayList<String>> list){
        for(int i = 0; i < list.size(); i++){
            for(int j = 0; j < list.get(i).size(); j++){
                String[] arr = list.get(i).get(j).split(",");

                // Remove duplicates from the array using set
                Set<String> set = new HashSet<>(Arrays.asList(arr));

                // Join the array back and set the values without duplicates
                list.get(i).set(j, String.join(",", set));
            }
        }
    }

    public void addElementsToNewQ(ArrayList<ArrayList<String>> list){
        for(int i = 0; i < list.size(); i++){
            // Create a StringJoiner with a comma delimiter
            StringJoiner joiner = new StringJoiner(",");
            String firstElement = "";

            // If the list is not the first one, add the elements of the list to the StringJoiner
            // For the first list (with null only), add to the String directly
            if(i > 0){
                for (int j = 0; j < list.get(i).size(); j++) {
                    joiner.add(list.get(i).get(j));
                }
                String merged = joiner.toString();
                newQ.add(merged);
            }else{
                firstElement = firstElement + list.get(i).get(0);
                newQ.add(firstElement);
            }
        }
    }
    
}