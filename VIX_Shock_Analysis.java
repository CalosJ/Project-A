import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// this is our
public class VIX_Shock_Analysis {

    // create a class so that we can store it as objects in our arraylist
    static class StockRecord {

        String date;
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

    // this function is for Person 3 part
    // it checks how SP500 behaves after each VIX shock event date
    public static void analyzeSPXResponse(ArrayList<StockRecord> spxData, ArrayList<StockRecord> eventList, String label) {

        // counters to store how many times each result happens
        int totalEvents = 0;
        int day1Up = 0;
        int day1Down = 0;
        int day2Up = 0;
        int day2Down = 0;
        int atLeastOneUp = 0;
        int bothUp = 0;
        int bothDown = 0;

        // loop through each event day from the VIX event list
        for (int e = 0; e < eventList.size(); e++) {
            String eventDate = eventList.get(e).date;

            // find the same date inside the SP500 data
            for (int i = 0; i < spxData.size(); i++) {
                if (spxData.get(i).date.equals(eventDate)) {

                    // make sure there are at least 2 more trading days after the event day
                    // because we need to check i+1 and i+2
                    if (i + 2 < spxData.size()) {
                        totalEvents++;

                        // event day close
                        double day0 = spxData.get(i).value;

                        // next day close
                        double day1 = spxData.get(i + 1).value;

                        // second next day close
                        double day2 = spxData.get(i + 2).value;

                        // check if SP500 went up or down on day i+1 compared to the event day
                        boolean isDay1Up = day1 > day0;

                        // check if SP500 went up or down on day i+2 compared to day i+1
                        boolean isDay2Up = day2 > day1;

                        // count results for day i+1
                        if (isDay1Up) {
                            day1Up++;
                        } else {
                            day1Down++;
                        }

                        // count results for day i+2
                        if (isDay2Up) {
                            day2Up++;
                        } else {
                            day2Down++;
                        }

                        // check if at least one of the next two days is up
                        if (isDay1Up || isDay2Up) {
                            atLeastOneUp++;
                        }

                        // check if both next two days are up
                        if (isDay1Up && isDay2Up) {
                            bothUp++;
                        }

                        // check if both next two days are down
                        if (!isDay1Up && !isDay2Up) {
                            bothDown++;
                        }
                    }

                    // once matching date is found, stop searching in spxData for that event
                    break;
                }
            }
        }

        // print the final results for this event category
        System.out.println("\n===== " + label + " =====");
        System.out.println("Total events: " + totalEvents);
        System.out.println("SP500 up on i+1: " + day1Up);
        System.out.println("SP500 down on i+1: " + day1Down);
        System.out.println("SP500 up on i+2: " + day2Up);
        System.out.println("SP500 down on i+2: " + day2Down);
        System.out.println("At least one of next two days is up: " + atLeastOneUp);
        System.out.println("Both next two days are up: " + bothUp);
        System.out.println("Both next two days are down: " + bothDown);
    }

    public static void main(String[] args) throws IOException {
        // read both files and store them in a arraylist separately
        ArrayList<StockRecord> spxData = readCSV("spx_data.csv");
        ArrayList<StockRecord> vixData = readCSV("vix_data.csv");

        // arraylist for storing our percentage change
        ArrayList<StockRecord> percentArray = new ArrayList<>();

        // calculates the percentage of current day and the next day value
        // and stores the percentage change on the date of the next day
        for (int i = 0; i < vixData.size() - 1; i++) {
            double d = ((vixData.get(i + 1).value - vixData.get(i).value) / vixData.get(i).value) * 100;
            StockRecord n = new StockRecord(vixData.get(i + 1).date, d);
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

        // This part is to Identify events: VIX up >10%, >20%, down >10%, down >20%
        for (int i = 0; i < percentArray.size(); i++) {
            if (percentArray.get(i).value > 20) { // greater than 20
                StockRecord temp = new StockRecord(percentArray.get(i).date, percentArray.get(i).value);
                aboveP20Ar.add(temp);
            } else if (percentArray.get(i).value > 10) { // greater than 10 but not greater than 20
                StockRecord temp = new StockRecord(percentArray.get(i).date, percentArray.get(i).value);
                ArP10toP20.add(temp);

            } else if (percentArray.get(i).value < -20) { // value < -20
                StockRecord temp = new StockRecord(percentArray.get(i).date, percentArray.get(i).value);
                belowN20Ar.add(temp);
            } else if (percentArray.get(i).value < -10) { // value < -10 but not less than -20
                StockRecord temp = new StockRecord(percentArray.get(i).date, percentArray.get(i).value);
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

        // SP500 response analysis
        // this uses the event lists already created above
        // and checks what happens in SP500 after each type of VIX shock
        analyzeSPXResponse(spxData, aboveP20Ar, "VIX above 20%");
        analyzeSPXResponse(spxData, ArP10toP20, "VIX 10% to 20%");
        analyzeSPXResponse(spxData, belowN20Ar, "VIX below -20%");
        analyzeSPXResponse(spxData, ArN10toN20, "VIX -10% to -20%");
    }
}