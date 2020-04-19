package RegexFA.Graph;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
}
