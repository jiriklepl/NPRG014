final a = {
    final b = {
        final c = {value + 10}
        c()
    }
    b()
}

//TASK Set the delegates so that to code passes and uses the value below
final values = [value: 20]
a.delegate = values // delegates get propagated
println a()