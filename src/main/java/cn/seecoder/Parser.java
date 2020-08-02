package cn.seecoder;

import java.util.ArrayList;

public class Parser {
    private Lexer lexer;

    //构造函数
    public Parser(Lexer l){
        lexer = l;
    }

    //parser的主体，从term开始构建语法树
    public AST parse(){
        AST ast = term(new ArrayList<>());
        return ast;
    }

    private AST atom(ArrayList<String> ctx){
        String param;
        String paramValue;

        if(lexer.skip(TokenType.LPAREN)){ //如果atom以左括号开始，说明是(Term)的形式
            AST aTerm = term(ctx); //调用term处理中间Term形式的atom
            if(lexer.skip(TokenType.RPAREN))
                return aTerm;
        }else if(lexer.next(TokenType.LCID)){ //如果atom以LCID开始
            param= lexer.tokenValue; //param赋值为LCID的值
            paramValue =""+ ctx.indexOf(param); //paramValue为param的德布鲁因值
            lexer.match(TokenType.LCID); //如果匹配，继续读取下一个token
            return new Identifier(param,paramValue); //用LCID的值构造Identifier
        }
        return  null;
    }

    private AST application(ArrayList<String> ctx){ //application要么是Application Atom,要么是Atom
        AST lhs=atom(ctx);
        AST rhs;
        while(true){
            rhs=atom(ctx);
            if(rhs==null){
                return lhs; //如果没有右子树，说明是Atom形式，返回左子树Atom
            }else{
                lhs=new Application(lhs,rhs); //如果有右子树，说明是Application Atom，生成Application返回
            }
        }
    }

    private AST term(ArrayList<String> ctx){
        String param;
        String paramValue;
        if(lexer.skip(TokenType.LAMBDA)){ //检查token是否为LAMBDA，并读取下一个token，识别出这是一个Abstraction
            if(lexer.next(TokenType.LCID)){ //检查下一个token是否为LCID
                param=lexer.tokenValue; //param为token的值
                lexer.match(TokenType.LCID);
                if(lexer.skip(TokenType.DOT)){ //如果下一个token是DOT,就继续读取
                    ctx.add(0,param); //在ctx的0索引处加入param
                    paramValue=""+ctx.indexOf(param);
                    AST aTerm=term(ctx); //递归地创建语法树，相当于abstraction的body
                    ctx.remove(ctx.indexOf(param)); //创建完成部分后将ctx的元素退出
                    return new Abstraction(new Identifier(param,paramValue),aTerm);
                }
            }
        }else
            return application(ctx); //如果term不符合上面Abstraction的模式，就是一个Application
        return null;
    }
}
