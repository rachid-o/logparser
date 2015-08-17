
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


def filename = args.length > 0 ? args[0]: "../example.log"



File logFile = new File(filename)
if(!logFile.exists()) {
    println "${filename} does not exist"
    return;
}
def path = "../${logFile.name}_" + new Date().format("yyyyMMdd-HH.mm.ss")

println "Parsing: $filename"
println "Output: $path"


def methods = [:]
def KEY_FINISHED = "Duration of"
def KEY_CYCLE = "Scale."
def NEWLINE = System.getProperty("line.separator")
def csvFilename = logFile.name + ".csv"
def outputFolder = new File(path)
outputFolder.mkdirs()
def cvsFile = new File("$path/$csvFilename")
cvsFile.delete()    // Remove this line to add to the existing file

currentCyclename = "unknown cycle"
logFile.eachLine{line ->
    if(line.startsWith(KEY_CYCLE)) {
        tokens = line.split('\\s')
        resUnits = tokens[2]
        resPerUnit = tokens[5]
        cycleName = "${resUnits}-${resPerUnit}"
        currentCyclename = cycleName
    } else if(line.contains(KEY_FINISHED)) {
        finishedLine = line.substring(line.lastIndexOf(KEY_FINISHED) + KEY_FINISHED.length()).trim()
        tokens = finishedLine.split('\\s')
        methodName = tokens[0..4].join(" ")
        duration = tokens.last()
        //cvsFile.append("\"$methodName\";$duration" + NEWLINE)

        if(!methods[methodName]) {
            methods[methodName] = [:]
        }
        if(!methods[methodName][currentCyclename]) {
            methods[methodName][currentCyclename] = [];
        }
        methods[methodName][currentCyclename].add(duration.toInteger()/1000);  // Convert millis to seconds
    }
}


// Create CSV file with averages
println "Averages (in seconds):"
cvsFile.append("\"method\";\"cycle\";\"average\"" + NEWLINE)
def averageFormat = new DecimalFormat("#.##")
 methods.each{ method, cycles ->
    cycles.each{cycleName, durations ->
        seriesName = "$cycleName $method"
        avg = averageFormat.format(durations.sum() / durations.size())
        cvsFile.append("\"$method\";\"$cycleName\";$avg" + NEWLINE)
    }
}
println "Created CSV: ${csvFilename}"


//*/
//System.exit(0)
/**
 *  Generate charts
 */
def chartBuilder = new ChartBuilder().chartType(StyleManager.ChartType.Line)
        .width(1200).height(600).xAxisTitle("Scale").yAxisTitle("seconds")


methodAvgs = [:]
methods.each{ method, cycles ->

    cycleAvg = [:]
    methodAvgs[method] = []
    cycles.each{cycleName, durations ->
        seriesName = "$method $cycleName"
        avg = durations.sum() / durations.size()
        cycleAvg[cycleName] = avg
        methodAvgs[method] << avg
    }
    chartName = (logFile.name + "-" + method).replaceAll("\\W+", "_") // remove illegal characters in filename
    Chart singleChart = chartBuilder.title(chartName).build();
    //singleChart.getStyleManager().setYAxisMax(1.0);

    yData = (1..cycleAvg.size())
    singleChart.addSeries(method, yData, cycleAvg.values());
    
    saveChart(singleChart, path+"/"+chartName);
}

Chart chartCombined = chartBuilder.title(logFile.name).build();
methodAvgs.each{ method, averages -> 
    yData = (1..averages.size())
    chartCombined.addSeries(method, yData, averages);
}
saveChart(chartCombined, path+"/"+logFile.name);


def saveChart(chart, filename) {
    //new SwingWrapper(chart).displayChart();   // Show the chart
    BitmapEncoder.saveBitmap(chart, filename, BitmapEncoder.BitmapFormat.PNG);
    println "Created graph: ${filename}.png"
}
