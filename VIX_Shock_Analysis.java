import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// this is our 
public class VIX_Shock_Analysis {
    
// create a class so that we can store it as objects in our arraylist
static class StockRecord{

String date ;
double value;

    // constuctor that stores date and value
   StockRecord(String date, double value) {
            this.date = date;
            this.value = value;
        }
    }


    // a function to read files
    public static ArrayList<StockRecord> readCSV(String filePath) throws IOException {
        ArrayList<StockRecord> list = new ArrayList<>();

        
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine(); 

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            String date = parts[0].trim();
            double value = Double.parseDouble(parts[1].trim());
            list.add(new StockRecord(date, value));
        } 

        br.close();
        return list;
    }

    public static void main(String[] args) throws IOException {
        // read both files and store them in a arraylist separately
        ArrayList<StockRecord> spxData = readCSV("spx_data.csv");
        ArrayList<StockRecord> vixData = readCSV("vix_data.csv");
        
        // arraylist for storing our percentage change 
        ArrayList<StockRecord> percentArray = new ArrayList<>();
        // calculates the percentage of current day and the next day value and stores the percentage change on the date of the next day
        for (int i = 0 ; i < vixData.size()-1; i++){
            double d = ( (vixData.get(i+1).value - vixData.get(i).value) / vixData.get(i).value ) * 100;
            StockRecord n = new StockRecord(vixData.get(i+1).date,d);
            percentArray.add(n);
        }
        
        // prints out the percentageArray
        // for (int i = 0 ; i< percentArray.size(); i++){
        //     System.out.println(percentArray.get(i).date + ", " + percentArray.get(i).value);
        // }

        // arraylist for each percentage
        ArrayList<StockRecord> aboveP20Ar = new ArrayList<>();
        ArrayList<StockRecord> ArP10toP20 = new ArrayList<>();
        ArrayList<StockRecord> belowN20Ar = new ArrayList<>();
        ArrayList<StockRecord> ArN10toN20 = new ArrayList<>();
 
 
        //  This part is to  Identify events: VIX up >10%, >20%, down >10%, down >20%

        for (int i = 0 ; i < percentArray.size();  i++){
            if (percentArray.get(i).value>20 ){ // greater than 20
                StockRecord temp = new StockRecord(percentArray.get(i).date,percentArray.get(i).value);
                aboveP20Ar.add(temp);
            }
            else if ( percentArray.get(i).value>10){ // 10 > percentage < 20
                StockRecord temp = new StockRecord(percentArray.get(i).date,percentArray.get(i).value);
                ArP10toP20.add(temp);

            }
            else if (percentArray.get(i).value < -20){ // value < -20
                StockRecord temp = new StockRecord(percentArray.get(i).date,percentArray.get(i).value);
                belowN20Ar.add(temp);
            }
            else if (percentArray.get(i).value < -10){ // value < -10 but greater than > -20
                StockRecord temp = new StockRecord(percentArray.get(i).date,percentArray.get(i).value);
                ArN10toN20.add(temp);
            }
            
        }

        // write VIX shock events to file
        PrintWriter pw1 = new PrintWriter(new FileWriter("vix_above_20.txt"));
        pw1.println("20% and above");
        for (StockRecord r : aboveP20Ar) {
            pw1.println(r.date + ", " + r.value);
        }
        pw1.close();

        PrintWriter pw2 = new PrintWriter(new FileWriter("vix_10_to_20.txt"));
        pw2.println("10% to 20%");
        for (StockRecord r : ArP10toP20) {
            pw2.println(r.date + ", " + r.value);
        }
        pw2.close();

        PrintWriter pw3 = new PrintWriter(new FileWriter("vix_below_neg20.txt"));
        pw3.println("below -20%");
        for (StockRecord r : belowN20Ar) {
            pw3.println(r.date + ", " + r.value);
        }
        pw3.close();

        PrintWriter pw4 = new PrintWriter(new FileWriter("vix_neg10_to_neg20.txt"));
        pw4.println("-10% to -20%");
        for (StockRecord r : ArN10toN20) {
            pw4.println(r.date + ", " + r.value);
        }
        pw4.close();
        
       // Spx Data












    }


}


