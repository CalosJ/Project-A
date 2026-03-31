import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class VIX_Shock_Analysis {
    

static class StockRecord{

String date ;
double value;


   StockRecord(String date, double value) {
            this.date = date;
            this.value = value;
        }
    }



    public static ArrayList<StockRecord> readCSV(String filePath) throws IOException {
        ArrayList<StockRecord> list = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine(); // skip header

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
        ArrayList<StockRecord> spxData = readCSV("spx_data.csv");
        ArrayList<StockRecord> vixData = readCSV("vix_data.csv");

        ArrayList<StockRecord> percentArray = new ArrayList<>();
        for (int i =1 ; i < vixData.size(); i++){
            double d = ( (vixData.get(i).value - vixData.get(i-1).value) / vixData.get(i-1).value ) * 100;
            StockRecord n = new StockRecord(vixData.get(i).date,d);
            percentArray.add(n);
        }
     
        for (int i = 0 ; i< percentArray.size(); i++){
            System.out.println(percentArray.get(i).date + ", " + percentArray.get(i).value);
        }
    }


}

