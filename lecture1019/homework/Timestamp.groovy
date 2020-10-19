import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.ClassHelper
import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass("CreatedAtTransform")
public @interface CreatedAt {
    String name() default "";
}


@GroovyASTTransformation(phase = SEMANTIC_ANALYSIS)
public class CreatedAtTransform implements ASTTransformation {

    public void visit(ASTNode[] astNodes, SourceUnit source) {
        ClassNode annotatedClass = astNodes[1]

        FieldNode field = annotatedClass.addField("__timestamp", Opcodes.ACC_PRIVATE, ClassHelper.Long_TYPE, new ConstantExpression(0L))
        FieldExpression fieldVar = fieldX(annotatedClass, field.name)
        
        ASTNode resetTimestamp = macro(CompilePhase.SEMANTIC_ANALYSIS, true) {
            this.__timestamp = new Date().getTime()
        }

        annotatedClass.addConstructor(Opcodes.ACC_PUBLIC, [] as Parameter[], [] as ClassNode[], resetTimestamp)

        for (def method in annotatedClass.getMethods()) {
            def stmt = method.getCode()
            method.setCode(new BlockStatement([resetTimestamp, stmt], new VariableScope()))
        }

        ASTNode body = macro(CompilePhase.SEMANTIC_ANALYSIS, true) { $v{fieldVar} }
        annotatedClass.addMethod(astNodes[0].members.name.value, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, ClassHelper.Long_TYPE, [] as Parameter[], [] as ClassNode[], body)
    }
}

final calculator = new GroovyShell(this.class.getClassLoader()).evaluate('''
@CreatedAt(name = "timestamp")
class Calculator {
    int sum = 0
    
    def add(int value) {
        int v = sum + value
        sum = v
    }

    def subtract(int value) {
        sum -= value
    }
}

new Calculator()
''')

assert System.currentTimeMillis() >= calculator.timestamp()
assert calculator.timestamp() == calculator.timestamp()
def oldTimeStamp = calculator.timestamp()

sleep(1000)
calculator.add(10)
assert calculator.sum == 10

assert oldTimeStamp < calculator.timestamp()
assert calculator.timestamp() == calculator.timestamp()
oldTimeStamp = calculator.timestamp()

sleep(1000)
calculator.subtract(1)
assert calculator.sum == 9
assert oldTimeStamp < calculator.timestamp()
assert calculator.timestamp() == calculator.timestamp()

println 'well done'