import javax.xml.transform.Result;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class Simulation {
    public static void main(String[] args) throws InvalidWeightException, IOException, SQLException {
        File TestData = new File("Synoptic Project Test Data.txt");
        RunTestData(TestData);
    }

    private static void RunTestData(File TestData) throws IOException, SQLException, InvalidWeightException {
        //The url of database
        System.out.println("Connecting to database...");
        final String url = "jdbc:postgresql://localhost:5432/postgres";
        final Properties props = new Properties();
        //The username and password to access the database
        props.setProperty("user", "postgres");
        props.setProperty("password", "");
        Connection conn = DriverManager.getConnection(url, props);
        Statement st = conn.createStatement();
        //Sets the path to the schema
        st.execute("Set SEARCH_PATH to 'Synoptic2', public");
        System.out.println("SUCCESSFULLY CONNECTED");

        //Load the info for the transaction, and compare the current funds to the maximum funds, see if its 0 or less than and compare the current weight to the max weight of the corresponding material
        BufferedReader reader = null;

        try{
            reader = new BufferedReader(new FileReader(TestData));
            String str;

            int transactionID = 1;

            while((str = reader.readLine()) != null){
                String[] DataInfo = str.split(System.lineSeparator());
                for(String data : DataInfo){
                    String[] individualData = data.split(";");
                    //Creates a result set to store the values retrieved from the db.
                    ResultSet rs;
                    //Checks to see what machine is being accessed
                    if(Integer.parseInt(individualData[2]) == 1){
                        //Runs an sql statement
                        rs = st.executeQuery("SELECT PlasticWeight, MetalWeight, EWasteWeight, CurrentFunds FROM Machine WHERE MachineID = 01");
                    }
                    else{
                        //Runs an sql statement
                        rs = st.executeQuery("SELECT PlasticWeight, MetalWeight, EWasteWeight, CurrentFunds FROM Machine WHERE MachineID = 02");
                    }
                    //Iterates through the result set
                    rs.next();
                    //Assigns the values in the result set to variables
                    double pWeight = rs.getDouble(1);
                    double mWeight = rs.getDouble(2);
                    double eWeight = rs.getDouble(3);
                    int cFunds = rs.getInt(4);
                    //Makes a new machine object using the variables just made
                    Machine machine = new Machine(pWeight, mWeight, eWeight, cFunds);

                    //Will compare the max weight of the material to the current weight of the material
                    if(Integer.parseInt(individualData[0]) == 1){
                        double plasticWeight = machine.getCurrentPlasticWeight();
                        double maxPlasticWeight = machine.getMaxPlasticWeight();
                        //Checks to see if the current weight plus the new weight is greater than the max weight the machine can hold
                        if(maxPlasticWeight < (plasticWeight + Double.parseDouble(individualData[1]))){
                            System.out.println("Plastic container was full and has now been emptied.");
                            //Checks to see what machine is being used
                            if(Integer.parseInt(individualData[2]) == 1) {
                                st.execute("UPDATE Machine SET PlasticWeight = 0 WHERE MachineID = 01");
                            }
                            else {
                                st.execute("UPDATE Machine SET PlasticWeight = 0 WHERE MachineID = 02");
                            }
                        }
                        else{
                            //Creates a new object for plastic using the weight defined in the file
                            Plastic plastic = new Plastic(Double.parseDouble(individualData[1]));
                            //Checks to see which machine is being used
                            if(Integer.parseInt(individualData[2]) == 1) {
                                //Creates a sql statement that takes in a variable from java
                                PreparedStatement changePlasticWeight01;
                                changePlasticWeight01 = conn.prepareStatement("UPDATE Machine SET PlasticWeight = ? WHERE MachineID = 01");
                                //Assigns the variable to the sql statement
                                changePlasticWeight01.setDouble(1,(machine.getCurrentPlasticWeight() + Double.parseDouble(individualData[1])));
                                changePlasticWeight01.execute();
                            }
                            else {
                                //Creates a sql statement that takes in a variable from java
                                PreparedStatement changePlasticWeight02;
                                changePlasticWeight02 = conn.prepareStatement("UPDATE Machine SET PlasticWeight = ? WHERE MachineID = 02");
                                //Assigns the variable to the sql statement
                                changePlasticWeight02.setDouble(1,(machine.getCurrentPlasticWeight() + Double.parseDouble(individualData[1])));
                                changePlasticWeight02.execute();
                            }
                            //Checks to see if the transaction will reduce the machine funds to less than 0
                            if(0 > (machine.getCurrentFunds() - plastic.calculateValue())){
                                System.out.println("Machine does not contain enough funds to complete the transaction.");
                                System.out.println("Resetting funds to maximum amount.");
                                //Checks to see what machine is being used
                                if(Integer.parseInt(individualData[2]) == 1) {
                                    //Resets the machines current funds
                                    st.execute("UPDATE Machine SET CurrentFunds = 25000 WHERE MachineID = 01");
                                }
                                else {
                                    //Resets the machines current funds
                                    st.execute("UPDATE Machine SET CurrentFunds = 25000 WHERE MachineID = 02");
                                }
                            }
                            else{
                                PreparedStatement updateCurrentFunds;
                                //Checks to see what machine is being used
                                if(Integer.parseInt(individualData[2]) == 1) {
                                    //Creates a sql statement that takes in a variable from java
                                    updateCurrentFunds = conn.prepareStatement("UPDATE Machine SET CurrentFunds = ? WHERE MachineID = 01");
                                }
                                else {
                                    //Creates a sql statement that takes in a variable from java
                                    updateCurrentFunds = conn.prepareStatement("UPDATE Machine SET CurrentFunds = ? WHERE MachineID = 02");
                                }
                                //Assigns the variable to the sql statement
                                updateCurrentFunds.setDouble(1,(int) Math.round(plastic.calculateValue()));
                                updateCurrentFunds.execute();

                                //Creates a sql statement that takes in a variable from java
                                PreparedStatement addTransaction;
                                addTransaction = conn.prepareStatement("INSERT INTO Transaction (TransactionID, MaterialID, Value, Weight, MachineID, Date) VALUES (?, ?, ?, ?, ?, ?)");
                                //Assigns the variable to the sql statement
                                addTransaction.setInt(1,transactionID);
                                addTransaction.setInt(2,Integer.parseInt(individualData[0]));
                                addTransaction.setInt(3, (int) Math.round(plastic.calculateValue()));
                                addTransaction.setDouble(4,Double.parseDouble(individualData[1]));
                                addTransaction.setInt(5, Integer.parseInt(individualData[2]));
                                addTransaction.setDate(6, Date.valueOf(individualData[3]));
                                addTransaction.execute();
                                System.out.println("TRANSACTION SUCCESSFUL - Dispensing " + (int) Math.round(plastic.calculateValue()));
                                //Increments the transactionID by 1 to show the next transaction is a new one
                                transactionID += 1;
                            }
                        }
                    }
                    //The same as the previous if statement but uses the metal object instead
                    else if(Integer.parseInt(individualData[0]) == 2){
                        double metalWeight = machine.getCurrentMetalWeight();
                        double maxMetalWeight = machine.getMaxMetalWeight();
                        if(maxMetalWeight < (metalWeight + Double.parseDouble(individualData[1]))){
                            System.out.println("Metal container was full and has now been emptied.");
                            if(Integer.parseInt(individualData[2]) == 1) {
                                st.execute("UPDATE Machine SET MetalWeight = 0 WHERE MachineID = 01");
                            }
                            else {
                                st.execute("UPDATE Machine SET MetalWeight = 0 WHERE MachineID = 02");
                            }
                        }
                        else{
                            Metal metal = new Metal(Double.parseDouble(individualData[1]));
                            if(Integer.parseInt(individualData[2]) == 1) {
                                PreparedStatement changeMetalWeight01;
                                changeMetalWeight01 = conn.prepareStatement("UPDATE Machine SET MetalWeight = ? WHERE MachineID = 01");
                                changeMetalWeight01.setDouble(1,(machine.getCurrentMetalWeight() + Double.parseDouble(individualData[1])));
                                changeMetalWeight01.execute();
                            }
                            else {
                                PreparedStatement changeMetalWeight02;
                                changeMetalWeight02 = conn.prepareStatement("UPDATE Machine SET MetalWeight = ? WHERE MachineID = 02");
                                changeMetalWeight02.setDouble(1,(machine.getCurrentMetalWeight() + Double.parseDouble(individualData[1])));
                                changeMetalWeight02.execute();
                            }
                            if(0 > (machine.getCurrentFunds() - metal.calculateValue())){
                                System.out.println("Machine does not contain enough funds to complete the transaction.");
                                System.out.println("Resetting funds to maximum amount.");
                                if(Integer.parseInt(individualData[2]) == 1) {
                                    st.execute("UPDATE Machine SET CurrentFunds = 25000 WHERE MachineID = 01");
                                }
                                else {
                                    st.execute("UPDATE Machine SET CurrentFunds = 25000 WHERE MachineID = 02");
                                }
                            }
                            else{
                                PreparedStatement updateCurrentFunds;
                                if(Integer.parseInt(individualData[2]) == 1) {
                                    updateCurrentFunds = conn.prepareStatement("UPDATE Machine SET CurrentFunds = ? WHERE MachineID = 01");
                                }
                                else {
                                    updateCurrentFunds = conn.prepareStatement("UPDATE Machine SET CurrentFunds = ? WHERE MachineID = 02");
                                }
                                updateCurrentFunds.setDouble(1,(int) Math.round(metal.calculateValue()));
                                updateCurrentFunds.execute();

                                PreparedStatement addTransaction;
                                addTransaction = conn.prepareStatement("INSERT INTO Transaction (TransactionID, MaterialID, Value, Weight, MachineID, Date) VALUES (?, ?, ?, ?, ?, ?)");
                                addTransaction.setInt(1,transactionID);
                                addTransaction.setInt(2,Integer.parseInt(individualData[0]));
                                addTransaction.setInt(3, (int) Math.round(metal.calculateValue()));
                                addTransaction.setDouble(4,Double.parseDouble(individualData[1]));
                                addTransaction.setInt(5, Integer.parseInt(individualData[2]));
                                addTransaction.setDate(6, Date.valueOf(individualData[3]));
                                addTransaction.execute();
                                System.out.println("TRANSACTION SUCCESSFUL - Dispensing " + (int) Math.round(metal.calculateValue()));
                                transactionID += 1;
                            }
                        }
                    }
                    //The same as the previous if statement but uses the E-Waste object instead
                    else if(Integer.parseInt(individualData[0]) == 3){
                        double e_WasteWeight = machine.getCurrentE_WasteWeight();
                        double maxE_WasteWeight = machine.getMaxE_WasteWeight();
                        if(maxE_WasteWeight < (e_WasteWeight + Double.parseDouble(individualData[1]))){
                            System.out.println("E-Waste container was full and has now been emptied.");
                            if(Integer.parseInt(individualData[2]) == 1) {
                                st.execute("UPDATE Machine SET EWasteWeight = 0 WHERE MachineID = 01");
                            }
                            else {
                                st.execute("UPDATE Machine SET EWasteWeight = 0 WHERE MachineID = 02");
                            }
                        }
                        else{
                            E_Waste e_Waste = new E_Waste(Double.parseDouble(individualData[1]));
                            if(Integer.parseInt(individualData[2]) == 1) {
                                PreparedStatement changeE_WasteWeight01;
                                changeE_WasteWeight01 = conn.prepareStatement("UPDATE Machine SET EWasteWeight = ? WHERE MachineID = 01");
                                changeE_WasteWeight01.setDouble(1,(machine.getCurrentE_WasteWeight() + Double.parseDouble(individualData[1])));
                                changeE_WasteWeight01.execute();
                            }
                            else {
                                PreparedStatement changeE_WasteWeight02;
                                changeE_WasteWeight02 = conn.prepareStatement("UPDATE Machine SET EWasteWeight = ? WHERE MachineID = 02");
                                changeE_WasteWeight02.setDouble(1,(machine.getCurrentE_WasteWeight() + Double.parseDouble(individualData[1])));
                                changeE_WasteWeight02.execute();
                            }
                            if(0 > (machine.getCurrentFunds() - e_Waste.calculateValue())){
                                System.out.println("Machine does not contain enough funds to complete the transaction.");
                                System.out.println("Resetting funds to maximum amount.");
                                if(Integer.parseInt(individualData[2]) == 1) {
                                    st.execute("UPDATE Machine SET CurrentFunds = 25000 WHERE MachineID = 01");
                                }
                                else {
                                    st.execute("UPDATE Machine SET CurrentFunds = 25000 WHERE MachineID = 02");
                                }
                            }
                            else{
                                PreparedStatement updateCurrentFunds;
                                if(Integer.parseInt(individualData[2]) == 1) {
                                    updateCurrentFunds = conn.prepareStatement("UPDATE Machine SET CurrentFunds = ? WHERE MachineID = 01");
                                }
                                else {
                                    updateCurrentFunds = conn.prepareStatement("UPDATE Machine SET CurrentFunds = ? WHERE MachineID = 02");
                                }
                                updateCurrentFunds.setDouble(1,(int) Math.round(e_Waste.calculateValue()));
                                updateCurrentFunds.execute();

                                PreparedStatement addTransaction;
                                addTransaction = conn.prepareStatement("INSERT INTO Transaction (TransactionID, MaterialID, Value, Weight, MachineID, Date) VALUES (?, ?, ?, ?, ?, ?)");
                                addTransaction.setInt(1,transactionID);
                                addTransaction.setInt(2,Integer.parseInt(individualData[0]));
                                addTransaction.setInt(3, (int) Math.round(e_Waste.calculateValue()));
                                addTransaction.setDouble(4,Double.parseDouble(individualData[1]));
                                addTransaction.setInt(5, Integer.parseInt(individualData[2]));
                                addTransaction.setDate(6, Date.valueOf(individualData[3]));
                                addTransaction.execute();
                                System.out.println("TRANSACTION SUCCESSFUL - Dispensing " + (int) Math.round(e_Waste.calculateValue()));
                                transactionID += 1;
                            }
                        }
                    }
                }
            }
        }
        catch(IOException e){
            System.err.println(e);
        }
        finally{
            if(reader != null){
                try{
                    reader.close();
                }
                catch (IOException e){
                    System.err.println(e);
                }
            }
        }
    }
}