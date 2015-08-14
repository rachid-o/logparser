import com.xeiam.xchart.BitmapEncoder
import com.xeiam.xchart.Chart
import com.xeiam.xchart.ChartBuilder
import com.xeiam.xchart.StyleManager
import com.xeiam.xchart.SwingWrapper
import java.text.DecimalFormat;

/**
 * Script to retrieve durations from logfiles and outputs it to CSV
 * Also generates charts in png. 
 */


//def filename = args.length > 0 ? args[0]: "awm-5-cycles.log"
def filename = args.length > 0 ? args[0]: "example.log"


println "Parsing: $filename"

File logFile = new File(filename)
if(!logFile.exists()) {
    println "${filename} does not exist"
    return;
}

def methodDurations = [:]
def KEY_FINISHED = "Duration of"
def NEWLINE = System.getProperty("line.separator")
def csvFilename = filename + ".csv"
def cvsFile = new File(csvFilename)
cvsFile.delete()    // Remove this line to add to the existing file

logFile.eachLine{line ->
    if(line.contains(KEY_FINISHED)) {
        finishedLine = line.substring(line.lastIndexOf(KEY_FINISHED) + KEY_FINISHED.length()).trim()
        tokens = finishedLine.split('\\s')
        methodName = tokens[0..4].join(" ")
        //duration = tokens[1]
        duration = tokens.last()
        cvsFile.append("\"$methodName\";$duration" + NEWLINE)

        if(!methodDurations[methodName]) {
            methodDurations[methodName] = [];
        }
        methodDurations[methodName].add(duration.toInteger()/1000);  // Convert millis to seconds
    }
}
println "Created CSV: ${csvFilename}"

println "Averages (in seconds):"
def averageFormat = new DecimalFormat("#.##")
methodDurations.each{ method, durations ->
    avg = averageFormat.format(durations.sum() / durations.size())
    println "\t${avg}\t - ${method}"
}


/**
 *  Generate charts
 */
def chartBuilder = new ChartBuilder().chartType(StyleManager.ChartType.Line)
        .width(1200).height(600).xAxisTitle("cycle").yAxisTitle("seconds")

Chart chartCombined = chartBuilder.title(filename).build();

methodDurations.each{ method, durations ->
    yData = (1..durations.size())    // List of index numbers of each element in durations
    chartCombined.addSeries(method, yData, durations);

    chartName = (filename + "-" + method).replaceAll("\\W+", "_") // remove illegal characters in filename
    Chart singleChart = chartBuilder.title(chartName).build();
    singleChart.addSeries(method, yData, durations);
    saveChart(singleChart, chartName);
}

saveChart(chartCombined, filename);


def saveChart(chart, name) {
    //new SwingWrapper(chart).displayChart();   // Show the chart
    BitmapEncoder.saveBitmap(chart, name, BitmapEncoder.BitmapFormat.PNG);
    println "Created graph: ${name}.png"
}
