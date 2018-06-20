package Task1;

import lowlevel.ParsedFile;
import lowlevel.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BinaryCode extends StateEncoding<BinaryCode> {
    HashMap<State, Long> encodingBasedOnInputs;


    public BinaryCode(ParsedFile fsm) {
        super(fsm, StateEncoding.BINARY);
    }

    @Override
    protected List<Long> generate(int n) {
        List<Long> erg = new ArrayList<>();
        for(int i = 0; i< (Math.pow(2, n)); i++){
            erg.add((long)i);
        }


        return erg;
    }

    @Override
    public int getMinNumberOfBits(int n) {
        int log = (int) Math.ceil(Math.log(n)/Math.log(2));
        numberofBits = log;
        return log;
    }

    @Override
    public void encoding() {
        this.encodingBasedOnInputs  = new HashMap<>();
        int n = this.getMinNumberOfBits(this.fsm.getNum_states());
        ArrayList<Long> codeList = (ArrayList<Long>) this.generate(n);
        int one = (int) (Math.pow(2,n) -1);
        int index = codeList.indexOf((long)one);
        codeList.remove(index);
        codeList.add(0,(long)one);


        for(State state : this.orderedWeightetListByInOut){
            if(!this.encodingBasedOnInputs.containsKey(state)) {
                long stateCode = codeList.get(0);
                int hamming = Integer.MAX_VALUE;
                for(State out: this.outputStateMapByBranches.get(state)){
                    if(this.encodingBasedOnInputs.containsKey(out)){
                        for(long code: codeList) {
                            int hamingtmp = this.calHammingDistance(this.encodingBasedOnInputs.get(out),code);
                            if(hamingtmp<hamming){
                                stateCode = code;
                                hamming = hamingtmp;
                            }
                        }
                    }
                }
                for(State in: this.inputStateMapByBranches.get(state)){
                    if(this.encodingBasedOnInputs.containsKey(in)){
                        for(long code: codeList) {
                            int hamingtmp = this.calHammingDistance(this.encodingBasedOnInputs.get(in),code);
                            if(hamingtmp<hamming){
                                stateCode = code;
                                hamming = hamingtmp;
                            }
                        }
                    }
                }
                this.encodingBasedOnInputs.put(state, stateCode);
                codeList.remove(codeList.indexOf(stateCode));
            }
            for(State stateBefore: this.inputStateMapByBranches.get(state)){
                if(this.outputStateMapByBranches.get(state).contains(stateBefore) && !this.encodingBasedOnInputs.containsKey(stateBefore)){
                    long stateCode = codeList.get(0);
                    int hammingDistance = this.calHammingDistance(this.encodingBasedOnInputs.get(state),stateCode);
                    for(long code: codeList){
                        int hammingTmp = this.calHammingDistance(this.encodingBasedOnInputs.get(state),code);
                        if(hammingTmp<hammingDistance){
                            stateCode = code;
                            hammingDistance = hammingTmp;
                        }
                    }
                    this.encodingBasedOnInputs.put(stateBefore,stateCode);
                    codeList.remove(codeList.indexOf(stateCode));
                }
                /*else{

                    if(!this.encodingBasedOnInputs.containsKey(stateBefore)) {
                        long stateCode = codeList.get(0);
                        for (long code : codeList) {
                            int hammingTmp = this.calHammingDistance(this.encodingBasedOnInputs.get(state), code);
                            if (hammingTmp ==1) {
                                stateCode = code;
                                break;
                            }
                        }
                        this.encodingBasedOnInputs.put(stateBefore, stateCode);
                        codeList.remove(codeList.indexOf(stateCode));
                    }
                }*/
            }
        }
        for(State state: this.fsm.getStates()){
            state.setCode(this.encodingBasedOnInputs.get(state));
        }


    }
}
