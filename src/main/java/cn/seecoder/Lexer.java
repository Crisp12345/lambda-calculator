package cn.seecoder;

import java.util.regex.Pattern;

public class Lexer{

    public String source; //输入的待解析的字符串
    public int index; //解析过程中使用的字符串下标
    public TokenType token; //解析出来的token类型
    public String tokenValue; //token的值

    //构造方法
    public Lexer(String s){
        index = 0;
        source = s;
        nextToken();
    }

    //获取下一个token
    private TokenType nextToken(){
        char c;
        String pattern1="\\s+"; //匹配空值
        String pattern2="[a-z]"; //匹配小写字母
        String pattern3="[a-zA-Z]"; //匹配小写字母和大写字母

        //跳过空值，直到非空值开始匹配
        do{
            c=nextChar();
        }while(Pattern.matches(pattern1,String.valueOf(c)));

        //判断c的类型
        switch(c){
            case '\\':
                token = TokenType.LAMBDA;
                tokenValue=null;break;
            case '.':
                token =TokenType.DOT;
                tokenValue=null;break;
            case '(':
                token =TokenType.LPAREN;
                tokenValue=null;break;
            case ')':
                token =TokenType.RPAREN;
                tokenValue=null;break;
            case '\0':
                token =TokenType.EOF;
                tokenValue=null;break;
            default:
                if(Pattern.matches(pattern2,String.valueOf(c))){
                    StringBuilder s = new StringBuilder();
                    do{
                        s.append(c);
                        c=nextChar();
                    }while(Pattern.matches(pattern3,String.valueOf(c)));

                    index--; //退回前面判断括号情况
                    token=TokenType.LCID;
                    tokenValue=s.toString();
                    //System.out.println(s);
                }
        }
        System.out.println(token);
        return token;
    }

    //获取下一个字符
    private char nextChar(){
        //如果index值增加到了source的长度，意味着读完了，要加上EOF
        if(index >= source.length()){
            return '\0';
        }
        //否则读取source中的下一个字符，调用nextChar时index加1
        return source.charAt(index++);
    }


    //检查token的类型是否为t
    public boolean next(TokenType t){
        return token==t ;
    }

    //断言token类型匹配，继续读取下一个token
    public void match(TokenType t){
        assert(token==t);
        nextToken();
    }

    //如果token类型匹配，就读取下一个token
    public boolean skip(TokenType t){
        if(token==t){
            nextToken();
            return true;
        }
        else
            return false;
    }
}
