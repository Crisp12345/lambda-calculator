package cn.seecoder;

public class Abstraction extends AST {
    public Identifier param;//变量
    public AST body;//表达式

    public Abstraction(Identifier p, AST b){
        param = p; //抽象左子树的参数
        body = b; //抽象的右子树
    }

    //打印抽象的参数名字和主体
//    public String toString(){
//        return "\\"+param.toString()+"."+body.toString();
//    }

    //只打印抽象的主体，省略参数名字
    public String toString(){
        return "\\"+"."+body.toString();
    }
}


































