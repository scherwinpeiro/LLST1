package main;

import Task1.ImplicationTable;
import Task1.StateEncoding;
import io.Parser;
import lowlevel.ParsedFile;
//import org.stringtemplate.v4.*;
import java.io.File;

import lowlevel.State;

import java.io.*;
import java.util.HashMap;

/**
 * Main class
 * @author Wolf & Gottschling
 * @author Henrique Ogawa
 * @author Hendrik Schöffmann
 *
 */
public class Main {

    private static String input_file_name;
    private static ParsedFile fsm;

    public static void main(String[] args) {






       // if (args.length > 0) {
         //   for (String arg : args) {
                System.out.println(" Current working directory : " + System.getProperty("user.dir"));

               // input_file_name = arg;
                input_file_name = Main.class.getResource("../dk17.kiss2").getPath();
                Parser p = new Parser();
                p.parseFile(input_file_name);

                // Representation of the FSM
                fsm = p.getParsedFile();


                // TODO - here you go
                System.out.printf("%n---------------------------------------------------------------------------%n");
                System.out.printf("                   Welcome to Verilog KISS FSM Generator%n");
                System.out.printf("---------------------------------------------------------------------------%n%n");
                System.out.printf("Parsed KISS file informations:%n");
                System.out.printf("  - FSM KISS file: %s%n", input_file_name);
                System.out.printf("  - Number of inputs: %d%n", fsm.getNumInputs());
                System.out.printf("  - Number of outputs: %d%n", fsm.getNumOutputs());
                System.out.printf("  - Number of states: %d%n", fsm.getNum_states());
                System.out.printf("  - Number of transitions: %d%n%n", fsm.getNum_transitions());


                //if no reset state, set to first state of array
           /* if (fsm.getInitialState() == null) {
                fsm.setInitialState(fsm.getStates()[0]);
            }*/

                createVerilogFileTypeD();
           // }

       // } else {
          //  System.out.println("No input argument given");
        //}
    }

    private static void createVerilogFile() {
        String fsm_name = input_file_name.replaceAll(".kiss2", "").replaceAll(".*kiss_files/","");
        State[] states = fsm.getStates();

        //creating map with output corresponding to the next state, setting new state names
        HashMap moore_output = new HashMap<State, Long>();
        HashMap statenames = new HashMap<String, String>();
        for (int i = 0; i < states.length; i++) {
            long[][] outputs = states[i].getOutputs();
            for (int j = 0; j < outputs.length; j++) {
                State nextState = states[i].getNextState(outputs[j][1]);
                long output = outputs[j][2];
                moore_output.put(nextState, output);
                statenames.put(states[i].getName(), "S" + i);
            }

        }

        //write verilog code line by line
        try {
            File file = new File("verilog/" + fsm_name + ".v");
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("module " + fsm_name + " (clk, data_in, ");
            if (fsm.getInitialState() != null) {
                writer.write("reset, ");
            }
            writer.write("data_out);");
            writer.newLine();
            writer.newLine();
            writer.write("\t// ports");
            writer.newLine();
            writer.write("\tinput clk;");
            writer.newLine();
            if (fsm.getInitialState() != null) {
                writer.write("\tinput reset;");
                writer.newLine();
            }
            writer.write("\tinput [" + (fsm.getNumInputs()-1) + ":0] data_in;");
            writer.newLine();
            writer.write("\toutput reg [" + (fsm.getNumOutputs()-1) + ":0] data_out;");
            writer.newLine();
            writer.newLine();
            writer.write("\t// Declare state register");
            writer.newLine();
            writer.write("\treg [" + (Integer.toBinaryString(fsm.getNum_states()-1).length()-1) + ":0] state;");
            writer.newLine();
            writer.newLine();
            writer.write("\t// Declare states");
            writer.newLine();

            //Declare the states in the state register, states are unordered
            for (int i = 0; i < states.length; i++) {
                writer.write("\tparameter ");
                //writer.write(states[i].getName() + " = " + i);
                writer.write(statenames.get(states[i].getName()) + " = " + i);
                writer.write(";");
                writer.newLine();
            }
            writer.newLine();

            //Declare output for moore machine
            writer.write("\t// Output of the states");
            writer.newLine();
            writer.write("\talways @ (state) begin");
            writer.newLine();
            writer.write("\t\tcasez (state)");
            writer.newLine();
            for (int i = 0; i < states.length; i++) {
                    //writer.write("\t\t\t" + states[i].getName() + ":");
                    writer.write("\t\t\t" + statenames.get(states[i].getName()) + ":");
                    writer.newLine();
                    writer.write("\t\t\t\tdata_out = " + fsm.getNumOutputs() + "'b" + formatInputOutput((long) moore_output.get(states[i])) + ";");
                    writer.newLine();
            }
            writer.write("\t\tendcase");
            writer.newLine();
            writer.write("\tend");
            writer.newLine();

            // @henrique - next state functions
            writer.newLine();
            writer.newLine();
            writer.write("\t// Determine the next state");
            writer.newLine();
            if (fsm.getInitialState() != null){
                writer.write("\talways @ (posedge clk or posedge reset) begin");
                writer.newLine();
                writer.write("\t\tif (reset)");
                writer.newLine();
                writer.write("\t\t\tstate <= " + statenames.get(fsm.getInitialState().getName()) + ";");
                writer.newLine();
                writer.write("\t\telse");
                writer.newLine();
            }
            else {
                writer.write("\talways @ (posedge clk) begin");
                writer.newLine();
            }
            writer.write("\t\tcase (state)");
            writer.newLine();

            // for each state
            int j = 0;
            for (State fsm_state : fsm.getStates()){
                //writer.write("\t\t\t" + fsm_state.getName() + ":");
                writer.write("\t\t\t" + statenames.get(states[j].getName()) + ":");
                writer.newLine();
                writer.write("\t\t\t\tcasez (data_in)");
                j++;
                writer.newLine();
                // get transitions
                long [][] state_transition = fsm_state.getTransitions();
                for (int i = 0; i < fsm_state.getTransitions().length; i++) {
                    writer.write("\t\t\t\t\t" + fsm.getNumInputs() + "'b" + formatInputOutput(state_transition[i][1]) + ": ");
                    writer.write("state <= " + statenames.get(fsm_state.getNextState(state_transition[i][1]).getName()) + ";");
                    writer.newLine();
                }
                writer.write("\t\t\t\tendcase");
                writer.newLine();
                writer.newLine();
            }

            writer.write("\t\t\tdefault:");
            writer.newLine();
            writer.write("\t\t\t\tbegin");
            writer.newLine();
            writer.write("\t\t\t\t\tstate <= " + statenames.get(states[0].getName()) + ";");
            writer.newLine();
            writer.write("\t\t\t\tend");
            writer.newLine();
            writer.newLine();

            writer.write("\t\tendcase");
            writer.newLine();
            writer.write("\tend");
            writer.newLine();
            writer.write("endmodule");
            writer.newLine();
            System.out.println("Path to generated file: " + System.getProperty("user.dir") + "/verilog/" + fsm_name + ".v");
            //System.out.println(fsm.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get transition
        //System.out.println(Long.toBinaryString(fsm.getStates()[0].getTransitions()[3][1]));
    }

    private static void createVerilogFileTypeD() {
        String fsm_name = input_file_name.replaceAll(".kiss2", "").replaceAll(".*kiss_files/","");
        State[] states = fsm.getStates();

        //setting new state names
        HashMap statenames = new HashMap<String, String>();
        for (int i = 0; i < states.length; i++) {
           statenames.put(states[i].getName(), "S" + i);
        }

        //write verilog code line by line
        try {
            File file = new File("verilog/" + fsm_name + ".v");
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("module " + fsm_name + " (clk, data_in, ");
            if (fsm.getInitialState() != null) {
                writer.write("reset, ");
            }
            writer.write("data_out);");
            writer.newLine();
            writer.newLine();
            writer.write("\t// ports");
            writer.newLine();
            writer.write("\tinput clk;");
            writer.newLine();
            if (fsm.getInitialState() != null) {
                writer.write("\tinput reset;");
                writer.newLine();
            }
            writer.write("\tinput [" + (fsm.getNumInputs()-1) + ":0] data_in;");
            writer.newLine();
            writer.write("\toutput reg [" + (fsm.getNumOutputs()-1) + ":0] data_out;");
            writer.newLine();
            writer.newLine();
            writer.write("\t// Declare state register");
            writer.newLine();
            writer.write("\treg [" + (Integer.toBinaryString(fsm.getNum_states()-1).length()-1) + ":0] state;");
            writer.newLine();
            writer.newLine();
            writer.write("\t// Declare states");
            writer.newLine();

            //Declare the states in the state register, states are unordered
            for (int i = 0; i < states.length; i++) {
                writer.write("\tparameter ");
                //writer.write(states[i].getName() + " = " + i);
                writer.write(statenames.get(states[i].getName()) + " = " + i);
                writer.write(";");
                writer.newLine();
            }
            writer.newLine();

            // @henrique - next state functions
            writer.write("\t// Determine the next state and output");
            writer.newLine();
            if (fsm.getInitialState() != null){
                writer.write("\talways @ (posedge clk or posedge reset) begin");
                writer.newLine();
                writer.write("\t\tif (reset)");
                writer.newLine();
                writer.write("\t\t\tstate <= " + statenames.get(fsm.getInitialState().getName()) + ";");
                writer.newLine();
                writer.write("\t\telse");
                writer.newLine();
            }
            else {
                writer.write("\talways @ (posedge clk) begin");
                writer.newLine();
            }
            writer.write("\t\tcase (state)");
            writer.newLine();

            // for each state
            int j = 0;
            for (State fsm_state : fsm.getStates()){
                //writer.write("\t\t\t" + fsm_state.getName() + ":");
                writer.write("\t\t\t" + statenames.get(states[j].getName()) + ":");
                writer.newLine();
                writer.write("\t\t\t\tcasez (data_in)");
                j++;
                writer.newLine();
                // get transitions
                long [][] state_transition = fsm_state.getTransitions();
                for (int i = 0; i < state_transition.length; i++) {
                    writer.write("\t\t\t\t\t" + fsm.getNumInputs() + "'b" + formatInputOutput(state_transition[i][1]) + ":");
                    writer.newLine();
                    writer.write("\t\t\t\t\t\tbegin");
                    writer.newLine();
                    writer.write("\t\t\t\t\t\t\tstate <= " + statenames.get(fsm_state.getNextState(state_transition[i][1]).getName()) + ";");
                    writer.newLine();
                    writer.write("\t\t\t\t\t\t\tdata_out <= " + fsm.getNumOutputs() + "'b" + formatInputOutput((long) fsm_state.output(state_transition[i][1])) + ";");
                    writer.newLine();
                    writer.write("\t\t\t\t\t\tend");
                    writer.newLine();
                }
                writer.write("\t\t\t\tendcase");
                writer.newLine();
                writer.newLine();
            }

            writer.write("\t\t\tdefault:");
            writer.newLine();
            writer.write("\t\t\t\tbegin");
            writer.newLine();
            writer.write("\t\t\t\t\tstate <= " + statenames.get(states[0].getName()) + ";");
            writer.newLine();
            writer.write("\t\t\t\tend");
            writer.newLine();
            writer.newLine();

            writer.write("\t\tendcase");
            writer.newLine();
            writer.write("\tend");
            writer.newLine();
            writer.write("endmodule");
            writer.newLine();
            System.out.println("Path to generated file: " + System.getProperty("user.dir") + "/verilog/" + fsm_name + ".v");
            //System.out.println(fsm.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get transition
        //System.out.println(Long.toBinaryString(fsm.getStates()[0].getTransitions()[3][1]));
    }

    private static String formatInputOutput(long value) {
        //zero padding
        String binary = Long.toString(value, 2);
        while (binary.length() % 2 != 0) {
            binary = "0" + binary;
        }

        //reconstruct real binary value
        String real_binary = "";
        for (int i = 0; i < binary.length(); i+=2) {
            String temp = "" + binary.charAt(i) + binary.charAt(i+1);
            if (temp.equals("01")) {
                real_binary += "0";
            } else if (temp.equals("10")) {
                real_binary += "1";
            } else if (temp.equals("11")) {
                real_binary += "?";
            }
        }
        return real_binary;
    }


    public static  ParsedFile getFSM(){
        return Main.fsm;
    }

}
