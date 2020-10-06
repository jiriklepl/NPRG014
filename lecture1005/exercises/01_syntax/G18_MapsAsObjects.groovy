final person = [:]
person.name = 'Joe'
person.age = 30

//TASK Make the code work
person.introduceYourself = {println "Hi, my name is $name and I am $age years old."}
person.introduceYourself.delegate = person
person.introduceYourself()
