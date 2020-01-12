package RegexFA.Parser;

import org.jetbrains.annotations.NotNull;

abstract class Factor extends Element{
    //  <factor>  ::= <atom> '*'          (A)
    //              | <atom>              (B)

    private static class A extends Factor{
        private final @NotNull Atom atom;
        public A(@NotNull Atom atom) {
            this.atom = atom;
        }
        @Override
        public String toString() {
            return String.format("Factor(%s, *)", atom.toString());
        }

        @Override
        public String toRegexString() {
            return String.format("%s*", atom.toRegexString());
        }
    }

    private static class B extends Factor{
        private final @NotNull Atom atom;
        public B(@NotNull Atom atom) {
            this.atom = atom;
        }
        @Override
        public String toString() {
            return String.format("Factor(%s)", atom.toString());
        }

        @Override
        public String toRegexString() {
            return String.format("%s", atom.toRegexString());
        }
    }

    private Factor(){
    }

    public static Factor make(@NotNull Atom atom, boolean star) {
        if (star){
            return new A(atom);
        } else {
            return new B(atom);
        }
    }


}
