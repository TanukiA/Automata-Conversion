import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class eNFA_to_NFA {

    private TreeMap<String, String> transitionMap;
    // Store epsilon closure found
    // e.g. {"C": "B"} for start : end states of epsilon transition
    private TreeMap<String, String> eClosure; 

    public eNFA_to_NFA(TreeMap<String, String> transitionMapWithEpsilon){
        transitionMap = new TreeMap<String, String>();
        // Add all previous elements to the new TreeMap
        transitionMap.putAll(transitionMapWithEpsilon);
        eClosure = new TreeMap<String, String>();
    }

    public TreeMap<String, String> getTransitionMap(){
        return transitionMap;
    }

    public void findTransitionToRemove(){
        // Iterate through the map and check if the key contains "ε" and the value is not "∅" (which means epsilon transition)
        for (Map.Entry<String, String> entry : transitionMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.contains("ε") && !value.equals("∅")) {
                eClosure.put(String.valueOf(key.charAt(0)), value);
            }
        }
    }

    public void findReachableStates(){
        for (String trans: transitionMap.keySet()) {
            String value = transitionMap.get(trans);
            // Find states reachable from the epsilon transition, add state to the column value
            for (String e_key: eClosure.keySet()){
                if (!trans.contains("ε") && value.contains(e_key)) {
                    String temp = value;
                    temp = temp + "," + eClosure.get(e_key);
                    transitionMap.put(trans, temp);
                }
            }
        }
    }

    public void removeEpsilonCol(){

        // Iterate to check if the keys contains the substring "ε"
        // If yes, remove the key-value pair from the map (remove epsilon columns)
        for (Iterator<Map.Entry<String, String>> it = transitionMap.entrySet().iterator(); it.hasNext();){
            Map.Entry<String, String> x = it.next();
            if(x.getKey().contains("ε")){
                it.remove();
            }
        }
    }
    
}