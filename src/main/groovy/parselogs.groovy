//println "hello world from groovy version ${GroovySystem.version}"

println "Parse file: ${args}"
//println "Parse file: ${args[0]}"

def filename = 'test.log'
File logfile = new File(filename)  // inFile is a string file name

if(!logfile.exists()) {
    println "${filename} does not exist"
    return;
}

println "parsing ${filename}"


logfile.eachLine{line ->
   println line
}

