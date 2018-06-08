package Task1;

import lowlevel.ParsedFile;
import lowlevel.State;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class StateEncoding<T> {
    // encoding types

    public final static int GRAY = 0;
    public final static int ONE_HOT = 1;
    public final static int JOHNSON = 2;
    public final static int BINARY = 3;
    public final static int HUFFMAN = 4;
    public final static int HAMMING = 5;




    private ParsedFile fsm;
    private ArrayList<State> orderedList;
    private HashMap<State,String> encodedMap;
    private int encodingType = -1;

    StateEncoding(ParsedFile fsm, int encodingType)
    {
        this.fsm = fsm;
        this.orderedList = new ArrayList<State>();
        this.encodedMap = new HashMap<State, String>();
        this.encodingType = encodingType;
    }



    public abstract T setOrderEncoding();

    protected void setEncoding(int encodingType){
        switch(encodingType){
            // TODO types of Encoding, then call the proper methode o encode the state

        }
    }


    public HashMap<State, String> getEncodedMap(){
        return this.encodedMap;
    }


    /**
     * Liefert die Anzahl der Don't Cares zuück
     * @param transitionCode
     * @return
     */
    public int getNumberOfDontCares(int transitionCode){
        String binary = Integer.toBinaryString(transitionCode);
        int res = 0;
        for(int i = 0; i< (this.fsm.getNumInputs()*2); i = i+2){
            String bit = new StringBuilder().append(binary.charAt(i)).append(binary.charAt(i+1)).toString();
            if (bit.equals("11"))res++;
        }
        return res;
    }















}
