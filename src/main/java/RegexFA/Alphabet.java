package RegexFA;

import java.util.*;

public enum Alphabet {
    Unary(1, () -> new char[]{'1'}),
    Binary(2, () -> new char[]{'0', '1'}),
    Decimal(10, () -> {
        char[] alphabet = new char[10];
        for (int i = 0; i < 10; i++) {
            alphabet[i] = (char) ('0' + i);
        }
        return alphabet;
    }),
    English(26, () -> {
        char[] alphabet = new char[26];
        for (int i = 0; i < 26; i++) {
            alphabet[i] = (char) ('a' + i);
        }
        return alphabet;
    }),
    Alphanumeric(36, () -> {
        char[] alphabet = new char[36];
        for (int i = 0; i < 26; i++) {
            alphabet[i] = (char) ('a' + i);
        }
        for (int i = 0; i < 10; i++) {
            alphabet[26+i] = (char) ('0' + i);
        }
        return alphabet;
    }),
    ;

    public static final char Empty = 'ε';
    public final int n;
    public final List<Character> alphabetList;
    public final Set<Character> alphabetSet;
    public final Map<Character, Integer> invertMap;

    Alphabet(int n, Thunk<char[]> alphabetCallable) {
        this.n = n + 1;
        char[] alphabet = alphabetCallable.get();
        List<Character> temp_alphabetList = new ArrayList<>(this.n);
        Set<Character> temp_alphabetSet = new HashSet<>(this.n);
        Map<Character, Integer> temp_invertMap = new HashMap<>(this.n);
        temp_alphabetList.add(Alphabet.Empty);
        temp_alphabetSet.add(Alphabet.Empty);
        temp_invertMap.put(Alphabet.Empty, 0);
        for (int i = 1; i < this.n; i++) {
            temp_alphabetList.add(alphabet[i - 1]);
            temp_alphabetSet.add(alphabet[i - 1]);
            temp_invertMap.put(alphabet[i - 1], i);
        }
        this.alphabetList = Collections.unmodifiableList(temp_alphabetList);
        this.alphabetSet = Collections.unmodifiableSet(temp_alphabetSet);
        this.invertMap = Collections.unmodifiableMap(temp_invertMap);
    }
}