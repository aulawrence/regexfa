package RegexFA.Graph;


import RegexFA.Alphabet;
import RegexFA.Parser.ParserException;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static RegexFA.Parser.RegexParser.toGraph;
import static RegexFA.Parser.RegexParser.verify;

public class GraphViz implements Closeable {
    private final Path dir;
    private final boolean isTempDir;
    private final Set<Path> fileList;

    public GraphViz() throws IOException {
        this(Files.createTempDirectory("regexfa"), true);
    }

    public GraphViz(Path dir) throws IOException {
        this(dir, false);
    }

    private GraphViz(Path dir, boolean isTempDir) throws IOException {
        this.dir = dir;
        if (!Files.isDirectory(dir)) {
            throw new IOException(String.format("%s is not a directory.", dir.toString()));
        }
        this.isTempDir = isTempDir;
        this.fileList = new HashSet<>();
    }

    public Path toImage(String dotString, String imageName) throws IOException, InterruptedException {
        assert !imageName.contains("/") && !imageName.contains("\\");

        Path dotPath = dir.resolve(imageName + ".dot");
        Path pngPath = dir.resolve(imageName + ".png");

        FileOutputStream os = new FileOutputStream(dotPath.toFile());
        os.write(dotString.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        fileList.add(dotPath);
        fileList.add(pngPath);

        String[] cmd = {"dot", "-Tpng", dotPath.getFileName().toString()};
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(dir.toFile());
        builder.redirectOutput(pngPath.toFile());
        Process process = builder.start();
        boolean exitCleanly = process.waitFor(15, TimeUnit.SECONDS);
        if (!exitCleanly) {
            System.err.printf("Process running dot exited with error code %d.%n", process.exitValue());
            return null;
        }
        return pngPath;
    }

    @Override
    public void close() {
        if (isTempDir) {
            for (Path file : fileList) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    System.err.printf("Cannot delete file: %s.%n", e.getMessage());
                }
            }
            try {
                Files.delete(dir);
            } catch (IOException e) {
                System.err.printf("Cannot delete directory: %s.%n", e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String pattern = "(0|1(01*0)*1)*";
        try {
            verify(pattern, Alphabet.Binary);
            NFAGraph g = toGraph(pattern, Alphabet.Binary);
            DFAGraph h = g.toDFA();
            DFAGraph i = h.minimize();
            Node dfaNode = h.getRootNode().getEdges()[Alphabet.Binary.invertMap.get('1')];
            GraphViz graphViz = new GraphViz(Paths.get("temp"));
            graphViz.toImage(g.toDotString_colorNFA(dfaNode), "nfa");
            graphViz.toImage(h.toDotString_colorDFA(dfaNode), "dfa");
            graphViz.toImage(i.toDotString_colorMinDFA(dfaNode), "mindfa");
            graphViz.close();
        } catch (ParserException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
