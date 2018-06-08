package Task1;

import lowlevel.ParsedFile;

public class ImplicationTable extends StateEncoding<ImplicationTable> {


    public ImplicationTable(ParsedFile fsm, int encodingType){
        super(fsm, encodingType);
    }


    @Override
    public ImplicationTable setOrderEncoding() {
        // TODO Methode muss implementriert werden



        return this;
    }
}
