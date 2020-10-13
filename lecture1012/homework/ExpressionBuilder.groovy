import javax.swing.*
import java.awt.*

//TASK The MarkupBuilder in Groovy can transform a hierarchy of method calls and nested closures into a valid XML document.
//Create a NumericExpressionBuilder builder that will read a user-specified hierarchy of simple math expressions and build a tree representation of it.
//It will feature a toString() method that will pretty-print the expression tree into a string with the same semantics, as verified by the assert on the last line.
//This means that parentheses must be placed where necessary with respect to the mathematical operator priorities.
//Change or add to the code in the script. Reuse the infrastructure code at the bottom of the script.
enum Op {
    PLUS, MINUS, MUL, DIV
}

class NumericExpressionBuilder extends BuilderSupport {
    private Item root = null

    @Override
    protected void setParent(Object parent, Object child) {
        if (parent.type() == Item.Type.EXPR) {
            if (parent.left == null) {
                parent.left = child
            } else {
                parent.right = child
            }
        }
    }

    @Override
    protected Object createNode(Object nodeName) {
        createNode nodeName, null, null
    }

    @Override
    protected Object createNode(Object nodeName, Object value) {
        createNode nodeName, null, value
    }

    @Override
    protected Object createNode(Object nodeName, Map attrs) {
        createNode nodeName, attrs, null
    }

    @Override
    protected Object createNode(Object nodeName, Map attrs, Object value) {
        final Item node
        switch (nodeName as String) {
            case 'number':
                node = new NumItem(attrs?.value)
            break
            case 'variable':
                node = new VarItem(attrs?.value)
            break
            case '+':
                node = new BinExprItem(Op.PLUS)
            break
            case '-':
                node = new BinExprItem(Op.MINUS)
            break
            case '*':
                node = new BinExprItem(Op.MUL)
            break
            case '/':
                node = new BinExprItem(Op.DIV)
            break
            default:
                node = new ErrorItem()
            break
        }
        
        if (root == null) root = node
        else node
    }

    @Override
    String toString() {
        root
    }
}

abstract class Item {
    abstract Type type()

    final enum Type {
        NUM, VAR, EXPR, ERROR
    }
}

final class NumItem extends Item {
    final Integer value

    NumItem(value) {
        this.value = value
    }

    @Override
    final Type type() { Type.NUM }

    @Override
    String toString() {
        value
    }
}

final class VarItem extends Item {
    final String value

    VarItem(value) {
        this.value = value
    }

    @Override
    final Type type() { Type.VAR }

    @Override
    String toString() {
        value
    }
}

final class BinExprItem extends Item {
    Item left
    Item right
    final Op op

    final enum Assoc {
        LEFT, RIGHT, BOTH, NONE
    }

    BinExprItem(op) {
        this.op = op
    }

    @Override
    final Type type() { Type.EXPR }

    @Override
    String toString() {
        boolean pLeft = (left.type() == Type.EXPR) &&
            (  (left.getPriority() < getPriority())
            || (left.op == op && (getAssoc() == Assoc.RIGHT || getAssoc() == Assoc.NONE))
            )
        
        boolean pRight = (right.type() == Type.EXPR) &&
            (  (right.getPriority() < getPriority())
            || (right.op == op && (getAssoc() == Assoc.LEFT || getAssoc() == Assoc.NONE))
            )
        
        "${pLeft ? "($left)" : left} ${translateOp()} ${pRight ? "($right)" : right}"
    }

    private String translateOp() {
        switch (op) {
            case Op.PLUS: '+'
            break
            case Op.MINUS: '-'
            break
            case Op.MUL: '*'
            break
            case Op.DIV: '/'
            break
        }
    }

    private int getPriority() {
        switch (op) {
            case Op.PLUS: 100
            break
            case Op.MINUS: 200
            break
            case Op.MUL: 300
            break
            case Op.DIV: 400
            break
        }
    }

    private Assoc getAssoc() {
        switch (op) {
            case Op.PLUS: Assoc.BOTH
            break
            case Op.MINUS: Assoc.LEFT
            break
            case Op.MUL: Assoc.BOTH
            break
            case Op.DIV: Assoc.LEFT
            break
        }
    }
}

class ErrorItem extends Item {
    ErrorItem() {
    }

    @Override
    final Type type() { Type.ERROR }

    @Override String toString() {
        "ERROR"
    }
}

//------------------------- Do not modify beyond this point!

def build(builder, String specification) {
    def binding = new Binding()
    binding['builder'] = builder
    new GroovyShell(binding).evaluate(specification)
    return builder
}

//Custom expression to display. It should be eventually pretty-printed as 1 + x * (2 - 3)
String description = '''
builder.'+' {
    number(value: 1)
    '*' {
        variable(value: 'x')
        '-' {
            number(value: 2)
            number(value: 3)
        }
    }
}
'''

//XML builder building an XML document
def xml = build(new groovy.xml.MarkupBuilder(), description)
println xml.toString()

//NumericExpressionBuilder displaying the expression
def expression = build(new NumericExpressionBuilder(), description)
println (expression.toString())
assert '1 + x * (2 - 3)' == expression.toString()