package RegexFA;

import java.util.*;
import java.util.concurrent.Callable;

public enum Alphabet {
    Unary(1, () -> {
        List<Character> alphabet = new ArrayList<>(1);
        alphabet.add('1');
        return alphabet;
    }),
    Binary(2, () -> {
        List<Character> alphabet = new ArrayList<>(2);
        alphabet.add('0');
        alphabet.add('1');
        return alphabet;
    }),
    Decimal(10, () -> {
        List<Character> alphabet = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            alphabet.add((char) ('0' + i));
        }
        return alphabet;
    }),
    English(26, () -> {
        List<Character> alphabet = new ArrayList<>(26);
        for (int i = 0; i < 26; i++) {
            alphabet.add((char) ('a' + i));
        }
        return alphabet;
    }),
    Alphanumeric(36, () -> {
        List<Character> alphabet = new ArrayList<>(36);
        for (int i = 0; i < 26; i++) {
            alphabet.add((char) ('a' + i));
        }
        for (int i = 0; i < 10; i++) {
            alphabet.add((char) ('0' + i));
        }
        return alphabet;
    });

    public static final char Empty = 'ε';
    public final int n;
    public final Set<Character> alphabetSet;
    public final Set<Character> alphabetSetWithEmpty;

    Alphabet(int n, Thunk<List<Character>> alphabetCallable) {
        this.n = n + 1;
        List<Character> alphabetList = alphabetCallable.get();
        this.alphabetSet = Collections.unmodifiableSet(new LinkedHashSet<>(alphabetList));
        alphabetList.add(0, Alphabet.Empty);
        this.alphabetSetWithEmpty = Collections.unmodifiableSet(new LinkedHashSet<>(alphabetList));
    }
}