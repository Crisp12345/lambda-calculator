package cn.seecoder;

public class Application extends AST {
    public AST lhs;//左树
    public AST rhs;//右树

    //构造方法
    public Application(AST l, AST s) {
        lhs = l;
        rhs = s;
    }

    //从左向右打印Application的子树
    public String toString() {
        if (lhs == null)
            return rhs.toString();
        else if (rhs == null)
            return lhs.toString();
        else
            return "(" + lhs.toString() + " " + rhs.toString() + ")";
    }
}