package Task1;

import lowlevel.ParsedFile;
import lowlevel.State;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class StateEncoding<T> {
    // encoding types

    public final static int GRAY = 0;
    public final static int ONE_HOT = 1;
    public final static int JOHNSON = 2;
    public final static int BINARY = 3;
    public final static int HUFFMAN = 4;

    public static int numberofBits = 0;



    protected HashMap<State, Long> stateCodeBasedOnInputs;
    protected HashMap<State, Long> stateCodeBasedOnOutputs;


    protected ArrayList<State> orderedWeightetListByInOut;
    protected ParsedFile fsm;
    protected ArrayList<State> orderedWeightedListByInput;
    protected ArrayList<State> orderedWeightedListByOutput;
    protected HashMap<State,String> encodedMap;
    protected int encodingType = -1;
    protected HashMap<State, HashMap<State, ArrayList<Long>>> inputStatesMap;
    protected HashMap<State, HashMap<State, ArrayList<Long>>> outputStateMap;

    protected HashMap<State,ArrayList<State>> outputStateMapByBranches;
    protected HashMap<State,ArrayList<State>> inputStateMapByBranches;


    protected HashMap<State, Integer>  stateWeight;

    StateEncoding(ParsedFile fsm, int encodingType)
    {
        this.fsm = fsm;
        this.encodedMap = new HashMap<State, String>();
        this.encodingType = encodingType;
        this.initInputStateMap();
        this.setOutputStateMap();
        this.setWeightedStatesByOutput();
        this.setWeightedStatesByInput();
        this.sortBranchesByInput();
        this.sortBranchesByOutput();
        this.setOrderedWeightetListByInOut();
    }



    protected abstract List<Long> generate(int n);






    public HashMap<State, String> getEncodedMap(){
        return this.encodedMap;
    }


    /**
     * Liefert die Anzahl der Don't Cares zuück
     * @param transitionCode
     * @return
     */
    protected int getNumberOfDontCares(long transitionCode){
        String binary = Long.toBinaryString(transitionCode);
        if(binary.length() != this.fsm.getNumInputs()*2){
            for(int i = 0; i< (this.fsm.getNumInputs()*2)-binary.length(); i++){
                binary = "0"+binary;
            }
        }
        int res = 0;
        for(int i = 0; i< (this.fsm.getNumInputs()*2); i = i+2){
            String bit = new StringBuilder().append(binary.charAt(i)).append(binary.charAt(i+1)).toString();
            if (bit.equals("11"))res++;
        }
        return res;
    }


    /**
     *
     */
    private void initInputStateMap(){
        this.inputStatesMap = new HashMap<State, HashMap<State, ArrayList<Long>>>();
        for(State state : this.fsm.getStates()){
            HashMap<State, ArrayList<Long>> tmp = state.getNextStateMap();
            for(State nextState : tmp.keySet()) {
                if (!state.equals(nextState)) {
                    if (!this.inputStatesMap.containsKey(nextState)) {
                        HashMap<State, ArrayList<Long>> entry = new HashMap<State, ArrayList<Long>>();
                        entry.put(state, tmp.get(nextState));
                        this.inputStatesMap.put(nextState, entry);
                    } else {
                        if (this.inputStatesMap.get(nextState).containsKey(state)) {
                            this.inputStatesMap.get(nextState).get(state).addAll(tmp.get(state));
                        } else {
                            this.inputStatesMap.get(nextState).put(state, tmp.get(nextState));
                        }
                    }
                }
            }
        }
        if(this.inputStatesMap.keySet().size()!= this.fsm.getNum_states()){
            for(State state: this.fsm.getStates()){
                if(!this.inputStatesMap.containsKey(state)){
                    HashMap<State, ArrayList<Long>> mpState = new HashMap<>();
                    ArrayList<Long> aTmp = new ArrayList<>();

                    this.inputStatesMap.put(state,null);
                }
            }
        }

    }


    private void setOutputStateMap(){
        this.outputStateMap = new HashMap<State, HashMap<State, ArrayList<Long>>>();
        for(State state: this.fsm.getStates()){
            HashMap<State, ArrayList<Long>> tmp = (HashMap<State, ArrayList<Long>>)state.getNextStateMap().clone();
            tmp.remove(state);
            this.outputStateMap.put(state, tmp);
        }
    }


    public void printStatesInputMap() {
        for (State state : this.inputStatesMap.keySet()) {
            System.out.println("State:" + state.getName());
            if (this.inputStatesMap.get(state) != null) {
                for (State commingState : this.inputStatesMap.get(state).keySet()) {
                    System.out.print("Transition from State: " + commingState.getName() + " -> ");
                    for (long transition : this.inputStatesMap.get(state).get(commingState))
                        System.out.print(Long.toBinaryString(transition) + " ");
                    System.out.println();
                }
                System.out.println();
            } else {
                System.out.println("No coming transitions.");
            }
        }
    }


    public void printStatesOutputMap(){
        for(State state: this.outputStateMap.keySet()){
            System.out.println("State:" + state.getName());

                for (State commingState : this.outputStateMap.get(state).keySet()) {
                    System.out.print("Transition to State: " + commingState.getName() + " -> ");
                    for (long transition : this.outputStateMap.get(state).get(commingState))
                        System.out.print(Long.toBinaryString(transition) + " ");
                    System.out.println();
                }
                System.out.println();

        }
    }



    private void setWeightedStatesByInput(){
        HashMap<State, Integer> tmpState = new HashMap<>();
       for(State state: this.inputStatesMap.keySet()){
           if(this.inputStatesMap.get(state) != null)
                tmpState.put(state, this.inputStatesMap.get(state).keySet().size());
       }
       this.orderedWeightedListByInput = new ArrayList<>();
       for(State state: tmpState.keySet()){
           if(this.orderedWeightedListByInput.size() == 0) this.orderedWeightedListByInput.add(state);
           else{
               for(State sArray: this.orderedWeightedListByInput){
                   if(tmpState.get(state)>=tmpState.get(sArray)){
                       this.orderedWeightedListByInput.add(this.orderedWeightedListByInput.indexOf(sArray),state);
                       break;
                   }
               }
               if(!orderedWeightedListByInput.contains(state)) this.orderedWeightedListByInput.add(state);
           }
       }
       for(State state: this.fsm.getStates()){
           if(!this.orderedWeightedListByInput.contains(state)){
               this.orderedWeightedListByInput.add(state);
           }
       }
    }

    private void setWeightedStatesByOutput(){
        HashMap<State, Integer> tmpState = new HashMap<>();
        for(State state: this.outputStateMap.keySet()){
            tmpState.put(state, this.outputStateMap.get(state).keySet().size());
        }
        this.orderedWeightedListByOutput = new ArrayList<>();
        for(State state: tmpState.keySet()){
            if(this.orderedWeightedListByOutput.size() == 0) this.orderedWeightedListByOutput.add(state);
            else{
                for(State sArray: this.orderedWeightedListByOutput){
                    if(tmpState.get(state)>=tmpState.get(sArray)){
                        this.orderedWeightedListByOutput.add(this.orderedWeightedListByOutput.indexOf(sArray),state);
                        break;
                    }
                }
                if(!orderedWeightedListByOutput.contains(state)) this.orderedWeightedListByOutput.add(state);
            }
        }
    }

    public void printWeightedStatesByInput(){
        this.orderedWeightedListByInput.forEach(s -> System.out.print(s.getName() + " "));
        System.out.println();
    }


    public void printWeightedStatesByOutput(){
        this.orderedWeightedListByOutput.forEach(s -> System.out.print(s.getName() + " "));
        System.out.println();
    }



    protected int calHammingDistance(long a, long b){
        long haming = a ^ b;
        return Long.bitCount(haming);
    }



    protected abstract int getMinNumberOfBits(int n);


    public abstract void encoding();


    private void sortBranchesByInput(){
        this.inputStateMapByBranches = new HashMap<>();
        for(State state: this.inputStatesMap.keySet()){
            ArrayList<State> tmp = new ArrayList<>();
            if(this.inputStatesMap.get(state) != null) {
                for (State stateBefore : this.inputStatesMap.get(state).keySet()) {
                    if (tmp.size() == 0) tmp.add(stateBefore);
                    else {
                        int probability = 0;
                        for (Long transition : this.inputStatesMap.get(state).get(stateBefore)) {
                            probability += Math.pow(2, this.getNumberOfDontCares(transition));
                        }
                        for (State sArray : tmp) {
                            int probabilityTmp = 0;
                            for (Long transition : this.inputStatesMap.get(state).get(sArray)) {
                                probabilityTmp += Math.pow(2, this.getNumberOfDontCares(transition));
                            }
                            if (probability >= probabilityTmp) {
                                tmp.add(tmp.indexOf(sArray), stateBefore);
                                break;
                            }
                        }
                        if (!tmp.contains(stateBefore)) tmp.add(stateBefore);
                    }
                }
            }
            this.inputStateMapByBranches.put(state, tmp);
        }
    }








    public void printInputStateMapByBranches(){
        for(State state: this.inputStateMapByBranches.keySet()){
            System.out.println("State: "+ state.getName());
            for(State statebefore: this.inputStateMapByBranches.get(state)){
                System.out.print(statebefore.getName() + " ");
            }
            System.out.println();
        }
    }


    private void sortBranchesByOutput(){

        this.outputStateMapByBranches = new HashMap<>();
        for(State state: this.outputStateMap.keySet()){

            ArrayList<State> tmp = new ArrayList<>();
            for(State stateBefore: this.outputStateMap.get(state).keySet()){
                if(tmp.size() ==0 ) tmp.add(stateBefore);
                else{
                    int probability  = 0;
                    for(Long transition: this.outputStateMap.get(state).get(stateBefore)){
                        probability += Math.pow(2, this.getNumberOfDontCares(transition));
                    }
                    for(State sArray: tmp){
                        int probabilityTmp = 0;
                        for(Long transition: this.outputStateMap.get(state).get(sArray)){
                            probabilityTmp += Math.pow(2, this.getNumberOfDontCares(transition));
                        }
                        if(probability>=probabilityTmp){
                            tmp.add(tmp.indexOf(sArray),stateBefore);
                            break;
                        }
                    }
                    if(!tmp.contains(stateBefore)) tmp.add(stateBefore);
                }
            }
            this.outputStateMapByBranches.put(state,tmp);
        }
    }


    public void printOutputStateMapByBranches(){
        for(State state: this.outputStateMapByBranches.keySet()){
            System.out.println("State: "+ state.getName());
            for(State statebefore: this.outputStateMapByBranches.get(state)){
                System.out.print(statebefore.getName() + " ");
            }
            System.out.println();
        }
    }

    private void setOrderedWeightetListByInOut(){
        this.orderedWeightetListByInOut = new ArrayList<State>();
        HashMap<State, Integer> stateInOut = new HashMap<>();
        for(State state: this.inputStateMapByBranches.keySet()){
            stateInOut.put(state, this.inputStateMapByBranches.get(state).size()+this.outputStateMapByBranches.get(state).size());
        }
        for(State state: stateInOut.keySet()){
            if(this.orderedWeightetListByInOut.size() == 0)
                this.orderedWeightetListByInOut.add(state);
            else{
                for(State sArray: this.orderedWeightetListByInOut){
                    if(stateInOut.get(state)>=stateInOut.get(sArray)){
                        this.orderedWeightetListByInOut.add(this.orderedWeightetListByInOut.indexOf(sArray),state);
                        break;
                    }
                }
                if(!orderedWeightetListByInOut.contains(state)) this.orderedWeightetListByInOut.add(state);
            }
        }

    }

    public void printOrderedWeightetListByInOut(){
        this.orderedWeightetListByInOut.forEach(e-> System.out.print(e.getName() + "  " ));
        System.out.println();
    }


    public void printStatesCode(){
        ArrayList<State> states = new ArrayList<State>();
        Collections.addAll(states, this.fsm.getStates());
        states.forEach(e->{
            System.out.print(e.getName() +": ");
            String binary = Long.toBinaryString(e.getCode());
            if(binary.length() != this.getMinNumberOfBits(this.fsm.getNum_states())){
                int bin_length = binary.length();
                int state_num = this.getMinNumberOfBits(this.fsm.getNum_states());
                for(int i = 0; i< state_num-bin_length; i++){
                    binary = "0"+binary;
                }
            }
            System.out.println(binary);
        });
    }


}
