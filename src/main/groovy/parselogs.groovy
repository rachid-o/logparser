import com.xeiam.xchart.BitmapEncoder
import com.xeiam.xchart.Chart
import com.xeiam.xchart.ChartBuilder
import com.xeiam.xchart.StyleManager

/**
 * Script to retrieve durations from logfiles and outputs it to CSV
 */


def filename = args.length > 0 ? args[0]: "test.log"
println "Parsing: $filename"

File logFile = new File(filename)  // inFile is a string file name
if(!logFile.exists()) {
    println "${filename} does not exist"
    return;
}
def csvFilename = filename + ".csv"

def cvsFile = new File(csvFilename)

def map = [:]
def KEY_FINISHED = "FINISHED"
logFile.eachLine{line ->
    if(line.contains("FINISHED")) {
        finishedLine = line.substring(line.lastIndexOf(KEY_FINISHED) + KEY_FINISHED.length()).trim()
        tokens = finishedLine.split('\\s')
        methodName = tokens[0]
        duration = tokens[1]
        cvsFile.write("\"$methodName\";$duration")

        if(!map[methodName]) {
            map[methodName] = [];
        }
        map[methodName].add(duration.toInteger())
    }
}
println "Created CSV: ${csvFilename}"


Chart chart = new ChartBuilder().chartType(StyleManager.ChartType.Line)
        .width(800).height(600).title(filename)
        .xAxisTitle("cycle").yAxisTitle("milliseconds").build();

map.each{ method, durations ->
    yData = (1..durations.size())    // List of index numbers of each element in durations
    chart.addSeries(method, yData, durations);
}


//new SwingWrapper(chart).displayChart();
BitmapEncoder.saveBitmap(chart, filename, BitmapEncoder.BitmapFormat.PNG);
println "Created graph: ${filename}.png"