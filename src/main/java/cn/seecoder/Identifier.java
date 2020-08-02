package cn.seecoder;

public class Identifier extends AST {

    public String name; //名字
    public String value; //De Bruijn index值

    //Identifier的构造方法
    public Identifier(String n, String v) {
        name = n;
        value = v;
    }

    //打印Identifier的De Bruijn值
    public String toString() {
        return value;
    }

    //打印Identifier的名字，在测试中不需要
//    public String toString() {
//        return name;
//    }
}