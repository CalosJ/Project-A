import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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

    // holds the summary of one event category (e.g. "VIX up > 10%")
    // used to package results so we can also write them out to the dashboard
    static class CategoryResult {

        String label;
        int totalEvents;
        int day1Up;
        int day1Down;
        int day2Up;
        int day2Down;
        int atLeastOneUp;
        int bothUp;
        int bothDown;
        ArrayList<StockRecord> events;

        CategoryResult(String label, ArrayList<StockRecord> events) {
            this.label = label;
            this.events = events;
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
    // returns a CategoryResult so the dashboard can use the numbers later
    public static CategoryResult analyzeSPXResponse(ArrayList<StockRecord> spxData, ArrayList<StockRecord> eventList, String label) {

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

                        // check if SP500 went up or down on day i+2 compared to the event day
                        boolean isDay2Up = day2 > day0;

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

        // package up everything so we can feed the dashboard later
        CategoryResult result = new CategoryResult(label, eventList);
        result.totalEvents = totalEvents;
        result.day1Up = day1Up;
        result.day1Down = day1Down;
        result.day2Up = day2Up;
        result.day2Down = day2Down;
        result.atLeastOneUp = atLeastOneUp;
        result.bothUp = bothUp;
        result.bothDown = bothDown;
        return result;
    }

    // writes all the analysis results into data.js, which the HTML dashboard loads
    // we use a JS file (instead of pure JSON) so the dashboard works when opened
    // directly with file:// (no local server needed)
    public static void writeDataJS(ArrayList<CategoryResult> results) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter("data.js"));
        pw.println("// Auto-generated by VIX_Shock_Analysis.java");
        pw.println("const VIX_DATA = {");
        pw.println("  categories: [");

        for (int c = 0; c < results.size(); c++) {
            CategoryResult r = results.get(c);
            pw.println("    {");
            pw.println("      label: \"" + r.label + "\",");
            pw.println("      totalEvents: " + r.totalEvents + ",");
            pw.println("      day1Up: " + r.day1Up + ",");
            pw.println("      day1Down: " + r.day1Down + ",");
            pw.println("      day2Up: " + r.day2Up + ",");
            pw.println("      day2Down: " + r.day2Down + ",");
            pw.println("      atLeastOneUp: " + r.atLeastOneUp + ",");
            pw.println("      bothUp: " + r.bothUp + ",");
            pw.println("      bothDown: " + r.bothDown + ",");
            pw.println("      events: [");
            for (int e = 0; e < r.events.size(); e++) {
                StockRecord rec = r.events.get(e);
                pw.print("        { date: \"" + rec.date + "\", value: " + rec.value + " }");
                if (e < r.events.size() - 1) {
                    pw.print(",");
                }
                pw.println();
            }
            pw.println("      ]");
            pw.print("    }");
            if (c < results.size() - 1) {
                pw.print(",");
            }
            pw.println();
        }

        pw.println("  ]");
        pw.println("};");
        pw.close();
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


        // one arraylist for each percentage threshold
        // these thresholds are cumulative, so an event with VIX +25% will be
        // counted in BOTH vixUp10 and vixUp20 (since >25 is also >10 and >20)
        ArrayList<StockRecord> vixUp10 = new ArrayList<>();
        ArrayList<StockRecord> vixUp20 = new ArrayList<>();
        ArrayList<StockRecord> vixDown10 = new ArrayList<>();
        ArrayList<StockRecord> vixDown20 = new ArrayList<>();

        // identify events: VIX up >10%, up >20%, down >10%, down >20%
        for (int i = 0; i < percentArray.size(); i++) {
            String date = percentArray.get(i).date;
            double value = percentArray.get(i).value;

            if (value > 10) {
                vixUp10.add(new StockRecord(date, value));
            }
            if (value > 20) {
                vixUp20.add(new StockRecord(date, value));
            }
            if (value < -10) {
                vixDown10.add(new StockRecord(date, value));
            }
            if (value < -20) {
                vixDown20.add(new StockRecord(date, value));
            }
        }

        // write VIX shock events to files
        PrintWriter pw1 = new PrintWriter(new FileWriter("vix_up_10.txt"));
        pw1.println("VIX up > 10%");
        for (StockRecord r : vixUp10) {
            pw1.println(r.date + ", " + r.value);
        }
        pw1.close();

        PrintWriter pw2 = new PrintWriter(new FileWriter("vix_up_20.txt"));
        pw2.println("VIX up > 20%");
        for (StockRecord r : vixUp20) {
            pw2.println(r.date + ", " + r.value);
        }
        pw2.close();

        PrintWriter pw3 = new PrintWriter(new FileWriter("vix_down_10.txt"));
        pw3.println("VIX down > 10%");
        for (StockRecord r : vixDown10) {
            pw3.println(r.date + ", " + r.value);
        }
        pw3.close();

        PrintWriter pw4 = new PrintWriter(new FileWriter("vix_down_20.txt"));
        pw4.println("VIX down > 20%");
        for (StockRecord r : vixDown20) {
            pw4.println(r.date + ", " + r.value);
        }
        pw4.close();

        // SP500 response analysis
        // this uses the event lists already created above
        // and checks what happens in SP500 after each type of VIX shock
        ArrayList<CategoryResult> allResults = new ArrayList<>();
        allResults.add(analyzeSPXResponse(spxData, vixUp10, "VIX up > 10%"));
        allResults.add(analyzeSPXResponse(spxData, vixUp20, "VIX up > 20%"));
        allResults.add(analyzeSPXResponse(spxData, vixDown10, "VIX down > 10%"));
        allResults.add(analyzeSPXResponse(spxData, vixDown20, "VIX down > 20%"));

        // write everything to data.js so the HTML dashboard can visualize it
        writeDataJS(allResults);
        System.out.println("\nWrote data.js for the HTML dashboard.");
        System.out.println("Open index.html in a browser to see the charts.");
    }
}