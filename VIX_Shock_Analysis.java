import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

         // printing  out the events when VIX up >10%, >20%, down >10%, down >20%
        System.out.println("20% and above ");
        for (int i = 0 ; i< aboveP20Ar.size(); i++){
             System.out.println(aboveP20Ar.get(i).date + ", " + aboveP20Ar.get(i).value);
        }
        System.out.println();
        System.out.println("10% to 20%");
        for (int i = 0 ; i< ArP10toP20.size(); i++){
             System.out.println(ArP10toP20.get(i).date + ", " + ArP10toP20.get(i).value);
        }
        System.out.println();
        System.out.println("below -20%");
        for (int i = 0 ; i< belowN20Ar.size(); i++){
             System.out.println(belowN20Ar.get(i).date + ", " + belowN20Ar.get(i).value);
        }
        System.out.println();
        System.out.println("-10% to -20");
        for (int i = 0 ; i< ArN10toN20.size(); i++){
             System.out.println(ArN10toN20.get(i).date + ", " + ArN10toN20.get(i).value);
        }
        
       
    }


}


