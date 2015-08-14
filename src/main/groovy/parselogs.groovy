import com.xeiam.xchart.BitmapEncoder
import com.xeiam.xchart.Chart
import com.xeiam.xchart.ChartBuilder
import com.xeiam.xchart.StyleManager
import com.xeiam.xchart.SwingWrapper

/**
 * Script to retrieve durations from logfiles and outputs it to CSV
 * Also generates charts in png. 
 */



def filename = args.length > 0 ? args[0]: "example.log"
println "Parsing: $filename"

File logFile = new File(filename)
if(!logFile.exists()) {
    println "${filename} does not exist"
    return;
}

def methodDurations = [:]
def KEY_FINISHED = "FINISHED"
def NEWLINE = System.getProperty("line.separator")
def csvFilename = filename + ".csv"
def cvsFile = new File(csvFilename)
cvsFile.delete()    // Remove this line to add to the existing file

logFile.eachLine{line ->
    if(line.contains("FINISHED")) {
        finishedLine = line.substring(line.lastIndexOf(KEY_FINISHED) + KEY_FINISHED.length()).trim()
        tokens = finishedLine.split('\\s')
        methodName = tokens[0]
        duration = tokens[1]
        cvsFile.append("\"$methodName\";$duration" + NEWLINE)

        if(!methodDurations[methodName]) {
            methodDurations[methodName] = [];
        }
        methodDurations[methodName].add(duration.toInteger())
    }
}
println "Created CSV: ${csvFilename}"


/**
 *  Generate charts
 */

def chartBuilder = new ChartBuilder().chartType(StyleManager.ChartType.Line)
        .width(800).height(600).title(filename)
        .xAxisTitle("cycle").yAxisTitle("milliseconds")

Chart chartCombined = chartBuilder.build();

methodDurations.each{ method, durations ->
    yData = (1..durations.size())    // List of index numbers of each element in durations
    chartCombined.addSeries(method, yData, durations);

    Chart singleChart = chartBuilder.build();
    singleChart.addSeries(method, yData, durations);
    saveChart(singleChart, filename + "-" + method);
}

saveChart(chartCombined, filename);


def saveChart(chart, name) {
    //new SwingWrapper(chart).displayChart();   // Show the chart
    BitmapEncoder.saveBitmap(chart, name, BitmapEncoder.BitmapFormat.PNG);
    println "Created graph: ${name}.png"
}
