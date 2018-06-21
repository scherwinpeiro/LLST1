package Task1;

import com.sun.javafx.geom.Edge;
import javafx.util.Pair;
import lowlevel.ParsedFile;
import lowlevel.State;

import java.lang.reflect.Array;
import java.util.*;

public abstract class StateEncoding<T> {
    final protected class Edge <T>{
        public T start;
        public T end;
        public double weight;
    }







    // encoding types

    public final static int GRAY = 0;
    public final static int ONE_HOT = 1;
    public final static int JOHNSON = 2;
    public final static int BINARY = 3;
    public final static int HUFFMAN = 4;
    public final static int DEPTHFIRST = 5;
    public final static int PRIM = 6;
    public final static int KRUSKAL = 7;


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


    public int getCodeWidth() {
        long maxCode = -1;
        for (int i = 0; i < fsm.getNum_states(); i++) {
            maxCode = fsm.getStates()[i].getCode() > maxCode ? fsm.getStates()[i].getCode() : maxCode;
        }
        return Long.toBinaryString(maxCode).length();
    }


    public static int transition_weight(long input) {
        int weight = 1;
        while (input != 0) {
            if ((input & 3) == 3) {
                weight = weight << 1;
            }
            input = input >> 2;
        }
        return weight;
    }


    public float evaluate() {
        float fitness = 0;
        long[][] transistions;
        int sumweight;
        for (int i = 0; i < fsm.getNum_states(); i++) {
            transistions = fsm.getStates()[i].getTransitions();
            sumweight = 0;
            for (int j = 0; j < transistions.length; j++) {
                fitness += (transition_weight(transistions[j][1]))
                        * hamming_dist(transistions[j][0], transistions[j][2]);
                sumweight += transition_weight(transistions[j][1]);
            }
        }
        return fitness / ((1l<<fsm.getNumInputs())*fsm.getNum_states());
    }



    private static int hamming_dist(long l, long m) {
        // TODO Auto-generated method stub
        return Long.bitCount(m ^ l);
    }


    private State assumedNext(State curr, int param) {
        HashMap<State, Integer> weightNextState = new HashMap<State, Integer>();
        long transistions[][] = curr.getTransitions();
        int max = 0;
        for (int i = 0; i < transistions.length; i++) {
            // uncoded
            if (param == 0 && curr.getNextState(transistions[i][1]).getCode() == -1) {
                weightNextState.put(curr.getNextState(transistions[i][1]), transition_weight(transistions[i][1]));
            }
            // real next
            if (param == 1 && !curr.getNextState(transistions[i][1]).equals(curr)) {
                weightNextState.put(curr.getNextState(transistions[i][1]), transition_weight(transistions[i][1]));
            }
            // real next unencoded
            if (param == 2 && !curr.getNextState(transistions[i][1]).equals(curr)
                    && curr.getNextState(transistions[i][1]).getCode() == -1) {
                weightNextState.put(curr.getNextState(transistions[i][1]), transition_weight(transistions[i][1]));
            }
        }
        if (weightNextState.isEmpty()) {
            return null;
        }
        max = (Collections.max(weightNextState.values()));
        for (Map.Entry<State, Integer> entry : weightNextState.entrySet()) {
            if (entry.getValue() == max) {
                return entry.getKey();
            }
        }
        return null;
    }




    protected State assumed_next_uncodeed(State curr) {
        return assumedNext(curr, 0);
    }

    protected State assumed_real_next(State curr) {
        return assumedNext(curr, 1);
    }

    protected State assumed_real_next_uncodeed(State curr) {
        return assumedNext(curr, 2);
    }

    protected ArrayList<Edge<State>> get_all_Edges(){
        ArrayList<Edge<State>> allEdges = new ArrayList<>();
        long[][]transistions;
        State curr;
        Edge<State> temp;
        HashMap<Pair<State, State>, Integer> edges = new HashMap<>();
        for (int i = 0; i < fsm.getNum_states(); i++) {
            curr=fsm.getStates()[i];
            transistions=curr.getTransitions();
            for (int j = 0; j < transistions.length; j++) {
                if (!curr.getNextState(transistions[j][1]).equals(curr)) {
                    if (edges.containsKey(new Pair<State, State>(curr, curr.getNextState(transistions[j][1])))) {
                        edges.put(new Pair<State, State>(curr, curr.getNextState(transistions[j][1])),
                                edges.get(new Pair<State, State>(curr, curr.getNextState(transistions[j][1])))
                                        + transition_weight(transistions[j][1]));
                    } else if (edges.containsKey(new Pair<State, State>(curr.getNextState(transistions[j][1]), curr))) {
                        edges.put(new Pair<State, State>(curr.getNextState(transistions[j][1]), curr),
                                edges.get(new Pair<State, State>(curr.getNextState(transistions[j][1]), curr))
                                        + transition_weight(transistions[j][1]));
                    } else {
                        edges.put(new Pair<State, State>(curr, curr.getNextState(transistions[j][1])),
                                transition_weight(transistions[j][1]));
                    }
                }
            }
        }
        for (Map.Entry<Pair<State, State>, Integer> entry : edges.entrySet()) {
            temp=new Edge<State>();
            temp.start=entry.getKey().getKey();
            temp.end=entry.getKey().getValue();
            temp.weight=entry.getValue();
            allEdges.add(temp);
        }
        return allEdges;
    }


    protected long minHammingdistance(long code, ArrayList<Long> codes) {
        long opt = -1;
        int optHammingDistance=Integer.MAX_VALUE;
        for (int i = 0; i < codes.size(); i++) {
            if (hamming_dist(code, codes.get(i))<optHammingDistance) {
                optHammingDistance=hamming_dist(code, codes.get(i));
                opt=codes.get(i);
            }
        }
        return opt;
    }


    protected void reset_encoding() {
        for (int j = 0; j < fsm.getNum_states(); j++) {
            fsm.getStates()[j].setCode(-1);
        }
    }









}
