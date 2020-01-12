package Test4.Parser;

import org.jetbrains.annotations.NotNull;

abstract class Atom extends Element{
    //  <atom>    ::= <char>                      (A)
    //              | '(' <expression> ')'        (B)

    private static class A extends Atom{
        private final @NotNull Character ch;

        public A(@NotNull Character ch) {
            this.ch = ch;
        }

        @Override
        public String toString() {
            return String.format("Atom(%s)", this.ch.toString());
        }

        @Override
        public String toRegexString() {
            return this.ch.toString();
        }
    }

    private static class B extends Atom{
        private final @NotNull  Expression expression;

        public B(@NotNull Expression expression) {
            this.expression = expression;
        }

        @Override
        public String toString() {
            return String.format("Atom(%s)", this.expression.toString());
        }

        @Override
        public String toRegexString() {
            return String.format("(%s)", this.expression.toRegexString());
        }
    }

    private Atom(){
    }

    public static Atom make(Character ch){
        return new A(ch);
    }

    public static Atom make(Expression expression) {
        return new B(expression);
    }
}
