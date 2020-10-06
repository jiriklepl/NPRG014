//TASK Define the unless (aka if not) method

unless = {cond, stmt -> if (!cond) stmt() }

unless(1 > 5) {
    println "Condition not satisfied!"
}

println 'done'