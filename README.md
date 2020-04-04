# RegexFA
Convert a minimal subset of regex to NFA/ DFA/ Min-DFA

Supports
- Greedy quantifiers *, +, ?
- Groups
- Alternatives

Taking from https://stackoverflow.com/a/32760631, the grammar should be:
```
<expression> ::= <term> '|' <expression>
               | <term>
<term>       ::= <factor> <term>
               | <factor>
<factor>     ::= <atom> '*'
               | <atom> '+'
               | <atom> '?'
               | <atom>
<atom>       ::= <char>
               | '(' <expression> ')'
```

## Dependencies
Maven is used to manage dependencies. See [pom.xml](pom.xml)

## Runtime Dependencies
You probably need to install graphviz on your computer so that the `dot` command is available. See [here](https://github.com/nidi3/graphviz-java#user-content-how-it-works).


## Build
```
mvn clean compile
```

## Run
```
mvn javafx:run
```

![](images/screenshot1.png)