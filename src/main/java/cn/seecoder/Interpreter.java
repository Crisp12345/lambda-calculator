package cn.seecoder;

public class Interpreter {
    private Parser parser;
    private AST astAfterParser;

    //构造函数
    public Interpreter(Parser p){
        parser = p;
        astAfterParser = p.parse();
        //System.out.println("After parser:"+astAfterParser.toString());
    }

    //定义判断类型的函数，减少代码量
    private  boolean isAbstraction(AST ast){
        return ast instanceof Abstraction;
    }
    private  boolean isApplication(AST ast){
        return ast instanceof Application;
    }
    private  boolean isIdentifier(AST ast) { return ast instanceof Identifier; }

    //Interpreter对外接口
    public AST eval(){
        return evalAST(astAfterParser);
    }

    //Interpreter主体，需要递归调用
    private AST evalAST(AST ast){
        while(true) {
            if (isApplication(ast)) {
                //如果Application的左子树是Application，就求左子树的值
                if (isApplication(((Application) ast).lhs)) {
                    ((Application) ast).lhs = evalAST(((Application) ast).lhs);
                    if (isApplication(((Application) ast).lhs)) {
                        return ast;
                    }
                }
                //如果Application的左子树是Abstraction，右子树是Application，就求右子树的值并带入
                else if (isAbstraction(((Application) ast).lhs)) {
                    if (isApplication(((Application) ast).rhs)) {
                        ((Application) ast).rhs = evalAST(((Application) ast).rhs);
                    }
                    ast = substitute(((Abstraction) ((Application) ast).lhs).body, ((Application) ast).rhs);
                }
                else {
                    //如果Application的右子树是Application，就求右子树的值
                    if (isApplication(((Application) ast).rhs)) {
                        ((Application) ast).rhs = evalAST(((Application) ast).rhs);
                        return ast;
                    }
                    //如果Application的右子树是Abstraction，就求右子树的值
                    else if (isAbstraction(((Application) ast).rhs)) {
                        ((Application) ast).rhs = evalAST(((Application) ast).rhs);
                        return ast;
                    }
                    else {
                        return ast;
                    }
                }
            }
            else if (isAbstraction(ast)) {
                //如果整体是Abstraction，就对body求值
                Abstraction abs = ((Abstraction) ast);
                abs.body = evalAST(((Abstraction) abs).body);
                return ast;
            } else if (isIdentifier(ast)) {
                return ast;
            }
        }
    }

    //+1：value中的变量的德布鲁因值和node中不统一，因为替最外层，所以加1
    //-1：替换完成后最外层消去了
    private AST substitute(AST node,AST value){
        return shift(-1,subst(node,shift(1,value,0),0),0);
    }

    /**
     *  value替换node节点中的变量：
     *  如果节点是Application，分别对左右树替换；
     *  如果node节点是abstraction，替入node.body时深度得+1；
     *  如果node是identifier，则替换De Bruijn index值等于depth的identifier（替换之后value的值加深depth）

     *@param value 替换成为的value
     *@param node 被替换的整个节点
     *@param depth 外围的深度

             
     *@return AST
     *@exception  (方法有异常的话加)


     */
    private AST subst(AST node, AST value, int depth){
        try{
            //如果替换application，则左树右树都要替换
            if(isApplication(node))
                return new Application(subst(((Application) node).lhs,value,depth),
                        subst(((Application) node).rhs,value,depth));
            //如果替换抽象的body，depth就要加1，这个德布鲁因值代表最外层的param，一直升到超过深度
            else if(isAbstraction(node))
                return new Abstraction((((Abstraction) node).param),
                        subst(((Abstraction) node).body,value,depth+1));
            //如果替换Identifier，替换之后消掉，要位移depth
            else if(isIdentifier(node)){
                //if(Integer.toString(depth).equals(((Identifier) node).value))
                if(Integer.toString(depth).equals(((Identifier) node).value))
                    return shift(depth,value,0);
                else
                    return node;
            }
        }catch(Exception e){
            System.out.println("Wrong!");
        }
        return null;
    }

    /**

     *  De Bruijn index值位移
     *  如果节点是Applation，分别对左右树位移；
     *  如果node节点是abstraction，新的body等于旧node.body位移by（from得+1）；
     *  如果node是identifier，则新的identifier的De Bruijn index值如果大于等于from则加by，
     *  否则加0（超出内层的范围的外层变量才要shift by位）.
     *  不然会被误解

        *@param by 位移的距离
     *@param node 位移的节点
     *@param from 内层的深度

             
     *@return AST
     *@exception  (方法有异常的话加)


     */

    private AST shift(int by, AST node,int from){
        try{
            //如果对application位移，左右子树都要位移
            if(isApplication(node))
                return new Application(shift(by,((Application) node).lhs,from),
                        shift(by,((Application) node).rhs,from));
            //如果对abstraction位移，from要加1，多考虑一层，新的抽象是旧body位移之后的值
            else if(isAbstraction(node))
                return new Abstraction((Identifier)((Abstraction) node).param,
                        shift(by,((Abstraction) node).body,from+1));
            //如果后面深度大，带入后会误解为前面的depth也大，所以要位移
            else if (isIdentifier(node)){
                int temp;
                temp=Integer.parseInt(((Identifier) node).value);
                return new Identifier(((Identifier) node).name,temp+(temp>=from?by:0)+"");
            }
        }catch(Exception e){
            System.out.println("Wrong!");
        }
        return null;
    }


    static String ZERO = "(\\f.\\x.x)";
    static String SUCC = "(\\n.\\f.\\x.f (n f x))";
    static String ONE = app(SUCC, ZERO);
    static String TWO = app(SUCC, ONE);
    static String THREE = app(SUCC, TWO);
    static String FOUR = app(SUCC, THREE);
    static String FIVE = app(SUCC, FOUR);
    static String PLUS = "(\\m.\\n.((m "+SUCC+") n))";
    static String POW = "(\\b.\\e.e b)";       // POW not ready
    static String PRED = "(\\n.\\f.\\x.n(\\g.\\h.h(g f))(\\u.x)(\\u.u))";
    static String SUB = "(\\m.\\n.n"+PRED+"m)";
    static String TRUE = "(\\x.\\y.x)";
    static String FALSE = "(\\x.\\y.y)";
    static String AND = "(\\p.\\q.p q p)";
    static String OR = "(\\p.\\q.p p q)";
    static String NOT = "(\\p.\\a.\\b.p b a)";
    static String IF = "(\\p.\\a.\\b.p a b)";
    static String ISZERO = "(\\n.n(\\x."+FALSE+")"+TRUE+")";
    static String LEQ = "(\\m.\\n."+ISZERO+"("+SUB+"m n))";
    static String EQ = "(\\m.\\n."+AND+"("+LEQ+"m n)("+LEQ+"n m))";
    static String MAX = "(\\m.\\n."+IF+"("+LEQ+" m n)n m)";
    static String MIN = "(\\m.\\n."+IF+"("+LEQ+" m n)m n)";

    private static String app(String func, String x){
        return "(" + func + x + ")";
    }
    private static String app(String func, String x, String y){
        return "(" +  "(" + func + x +")"+ y + ")";
    }
    private static String app(String func, String cond, String x, String y){
        return "(" + func + cond + x + y + ")";
    }

    public static void main(String[] args) {
        // write your code here


        String[] sources = {
                ZERO,//0
                ONE,//1
                TWO,//2
                THREE,//3
                app(PLUS, ZERO, ONE),//4
                app(PLUS, TWO, THREE),//5
                app(POW, TWO, TWO),//6
                app(PRED, ONE),//7
                app(PRED, TWO),//8
                app(SUB, FOUR, TWO),//9
                app(AND, TRUE, TRUE),//10
                app(AND, TRUE, FALSE),//11
                app(AND, FALSE, FALSE),//12
                app(OR, TRUE, TRUE),//13
                app(OR, TRUE, FALSE),//14
                app(OR, FALSE, FALSE),//15
                app(NOT, TRUE),//16
                app(NOT, FALSE),//17
                app(IF, TRUE, TRUE, FALSE),//18
                app(IF, FALSE, TRUE, FALSE),//19
                app(IF, app(OR, TRUE, FALSE), ONE, ZERO),//20
                app(IF, app(AND, TRUE, FALSE), FOUR, THREE),//21
                app(ISZERO, ZERO),//22
                app(ISZERO, ONE),//23
                app(LEQ, THREE, TWO),//24
                app(LEQ, TWO, THREE),//25
                app(EQ, TWO, FOUR),//26
                app(EQ, FIVE, FIVE),//27
                app(MAX, ONE, TWO),//28
                app(MAX, FOUR, TWO),//29
                app(MIN, ONE, TWO),//30
                app(MIN, FOUR, TWO),//31
        };

        //for(int i=0 ; i<sources.length; i++) {
            int i=9;

            String source = sources[i];

            System.out.println(i+":"+source);

            Lexer lexer = new Lexer(source);

            Parser parser = new Parser(lexer);

            Interpreter interpreter = new Interpreter(parser);

            AST result = interpreter.eval();

            System.out.println(i+":" + result.toString());

        //}

    }
}




