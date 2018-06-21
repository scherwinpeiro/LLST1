package Task1;

import lowlevel.ParsedFile;
import lowlevel.State;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Depthfirst extends StateEncoding<Depthfirst> {
    public Depthfirst(ParsedFile fsm){

        super(fsm, StateEncoding.DEPTHFIRST);
    }





    @Override
    protected List<Long> generate(int numberOfBits) {

        if(numberOfBits==0){
            List<Long> result = new ArrayList<Long>();
            result.add(0L);
            return result;
        }

        List<Long> result = generate(numberOfBits-1);
        int numToAdd = 1<<(numberOfBits-1);

        for(int i=result.size()-1; i>=0; i--){
            result.add(numToAdd+result.get(i));
        }

        return result;
    }

    @Override
    protected int getMinNumberOfBits(int n) {
        int log = (int) Math.ceil(Math.log(n)/Math.log(2));
        numberofBits = log;
        return log;
    }

    @Override
    public void encoding() {
        this.reset_encoding();
        State curr, next = null;
        int newstart = 0;
        //ArrayList<Long> codes = johnson_Code(fsm.getNum_states());
        ArrayList<Long> codes = (ArrayList<Long>) this.generate(getMinNumberOfBits(this.fsm.getNum_states()));
        long optCode;
        if (fsm.getInitialState() != null) {
            curr = fsm.getInitialState();
        } else {
            curr = fsm.getStates()[0];
        }
        curr.setCode(codes.get(0));
        codes.remove(0);
        for (int j = 1; j < fsm.getNum_states(); j++) {
            newstart = 0;
            do {
                next = assumed_next_uncodeed(curr);
                // keinen uncodierten nachfolgerknoten gefunden... beginne an einer neuen stelle
                if (next == null) {
                    curr = next = fsm.getStates()[newstart++];
                }
            } while (next.getCode() != -1);
            optCode=minHammingdistance(curr.getCode(), codes);
            next.setCode(optCode);
            Iterator<Long> itr = codes.iterator();
            while( itr.hasNext() ){
                if (itr.next()==optCode) {
                    itr.remove();
                    break;
                }
            }
            curr = next;
        }
    }
}
