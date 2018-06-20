package Task1;

import lowlevel.ParsedFile;
import lowlevel.State;

import java.util.ArrayList;
import java.util.List;

public class OneHotCode extends StateEncoding<OneHotCode> {



    public OneHotCode(ParsedFile fsm) {
        super(fsm, StateEncoding.ONE_HOT);
    }

    @Override
    protected  List<Long> generate(int n) {
        List<Long> res = new ArrayList<>();
        for(int i = 0; i< n; i++){
            long code = 1;
            code = code << i;
            res.add(code);
        }
        return res;
    }

    @Override
    public int getMinNumberOfBits(int n) {
        numberofBits = n;
        return n;
    }

    @Override
    public void encoding() {
        ArrayList<Long> codeList = (ArrayList<Long>) this.generate(this.fsm.getNum_states());
        for(int i = 0; i< this.fsm.getStates().length; i++){
            this.fsm.getStates()[i].setCode(codeList.get(i));
        }
    }
}
