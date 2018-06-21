package Task1;

import lowlevel.ParsedFile;
import lowlevel.State;

import java.util.*;

public class Kruskal extends StateEncoding<Kruskal> {

    public Kruskal(ParsedFile fsm){
        super(fsm, StateEncoding.KRUSKAL);
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
        ArrayList<Edge<State>> edges = get_all_Edges();
        Collections.sort(edges,new Comparator<Edge<State>>() {
            @Override
            public int compare(Edge<State> o1, Edge<State> o2)
            {

                return  (int) (o2.weight-o1.weight);
            }
        });
        //ArrayList<Long> codes = bin_Code(1<<Integer.toBinaryString(fsm.getNum_states()).length());
        ArrayList<Long> codes = (ArrayList<Long>) this.generate(getMinNumberOfBits(this.fsm.getNum_states()));
        //ArrayList<Long> codes = gray_Code(fsm.getNum_states());
        //ArrayList<Long> codes = johnson_Code(fsm.getNum_states());
        long optCode;
        //edges.get(0).start.setCode(codes.get(0));
        //edges.get(0).end.setCode(codes.get(1));
        //edges.remove(0);
        //codes.remove(0);
        //codes.remove(0);
        int i=0;
        while (edges.size()!=0) {
            if (edges.get(i).start.getCode()!=-1&&edges.get(i).end.getCode()!=-1) {
                edges.remove(i);
                i=0;
            }else if (edges.get(i).start.getCode()==-1&&edges.get(i).end.getCode()==-1) {
                edges.get(i).start.setCode(codes.get(0));
                codes.remove(0);
                i=0;
            }else {
                if (edges.get(i).start.getCode()==-1) {
                    optCode=minHammingdistance(edges.get(i).end.getCode(),codes);
                    edges.get(i).start.setCode(optCode);
                }else {
                    optCode=minHammingdistance(edges.get(i).start.getCode(),codes);
                    edges.get(i).end.setCode(optCode);
                }
                i=0;
                Iterator<Long> itr = codes.iterator();
                while( itr.hasNext() ){
                    if (itr.next()==optCode) {
                        itr.remove();
                        break;
                    }
                }
            }
        }
    }
}
