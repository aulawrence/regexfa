package Test4.Parser;

import org.jetbrains.annotations.NotNull;

abstract class Expression extends Element{

    //  <expression> ::= <term> '|' <expression>    (A)
    //                 | <term>                     (B)
    //

    private static class A extends Expression{
        private final @NotNull  Term term;
        private final @NotNull  Expression expression;

        public A(@NotNull Term term, @NotNull Expression expression) {
            this.term = term;
            this.expression = expression;
        }

        @Override
        public String toString() {
            return String.format("Expression(%s, %s)", this.term.toString(), this.expression.toString());
        }

        @Override
        public String toRegexString() {
            return String.format("%s|%s",this.term.toRegexString(), this.expression.toRegexString());
        }
    }

    private static class B extends Expression{
        private final @NotNull Term term;

        public B(@NotNull Term term) {
            this.term = term;
        }

        @Override
        public String toString() {
            return String.format("Expression(%s)", this.term.toString());
        }

        @Override
        public String toRegexString() {
            return this.term.toRegexString();
        }
    }

    private Expression(){
    }

    public static Expression make(Term term, Expression expression){
        return new A(term, expression);
    }

    public static Expression make(Term term){
        return new B(term);
    }

}
