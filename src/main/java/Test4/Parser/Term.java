package Test4.Parser;

import org.jetbrains.annotations.NotNull;

abstract class Term extends Element{
    //  <term>       ::= <factor> <term>          (A)
    //                 | <factor>                 (B)

    private static class A extends Term{
        private final @NotNull Factor factor;
        private final @NotNull Term term;
        public A(@NotNull Factor factor, @NotNull Term term) {
            this.factor = factor;
            this.term = term;
        }

        @Override
        public String toString() {
            return String.format("Term(%s, %s)", factor.toString(), term.toString());
        }

        @Override
        public String toRegexString() {
            return String.format("%s%s", factor.toRegexString(), term.toRegexString());
        }
    }

    private static class B extends Term{
        private final @NotNull Factor factor;
        public B(@NotNull Factor factor) {
            this.factor = factor;
        }

        @Override
        public String toString() {
            return String.format("Term(%s)", factor.toString());
        }

        @Override
        public String toRegexString() {
            return String.format("%s", factor.toRegexString());
        }
    }

    private Term(){
    }

    public static Term make(@NotNull Factor factor, @NotNull Term term) {
        return new A(factor, term);
    }

    public static Term make(@NotNull Factor factor) {
        return new B(factor);
    }
}
