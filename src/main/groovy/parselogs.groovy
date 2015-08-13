/**
 * Script to retrieve durations from logfiles and outputs it to CSV
 */

def filename = args.length > 0 ? args[0]: "test.log"
println "Parsing: $filename"

File logfile = new File(filename)  // inFile is a string file name
if(!logfile.exists()) {
    println "${filename} does not exist"
    return;
}

def KEY_FINISHED = "FINISHED"
logfile.eachLine{line ->
    if(line.contains("FINISHED")) {
        finishedLine = line.substring(line.lastIndexOf(KEY_FINISHED) + KEY_FINISHED.length()).trim()
        tokens = finishedLine.split('\\s')
        methodName = tokens[0]
        duration = tokens[1]
        println "\"$methodName\";$duration"
    }
}

