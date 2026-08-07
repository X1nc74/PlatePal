package platepal.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import platepal.exception.DataAccessException;

/**
 * Reads and writes the whole PlatePal data file.
 *
 * <p>This is the only class in the project that knows the file exists. The
 * repository layer asks it for a {@link PlatePalData} object and hands one back
 * to be saved; nothing above the repository imports Gson or java.nio.
 *
 * <p>A missing, empty or blank file is treated as "no data yet" and produces an
 * empty {@link PlatePalData}, so a fresh clone of the repository starts cleanly
 * instead of crashing. A file that exists but contains broken JSON is a
 * different situation and is reported as an error, because silently discarding
 * everyone's data would be worse than stopping.
 */
public class JsonDataStore {

    private static final String DEFAULT_PATH = "data/platepal-data.json";

    private final Path file;
    private final Gson gson;

    public JsonDataStore() {
        this(Path.of(DEFAULT_PATH));
    }

    public JsonDataStore(Path file) {
        this.file = file;
        this.gson = GsonFactory.create();
    }

    /**
     * Loads the data file.
     *
     * @return the stored data, or an empty container if the file does not exist
     * @throws DataAccessException if the file exists but cannot be read or parsed
     */
    public PlatePalData load() {
        try {
            if (!Files.exists(file) || Files.size(file) == 0) {
                PlatePalData empty = new PlatePalData();
                empty.repair();
                return empty;
            }
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                PlatePalData data = gson.fromJson(reader, PlatePalData.class);
                if (data == null) {
                    data = new PlatePalData();
                }
                data.repair();
                return data;
            }
        } catch (JsonParseException e) {
            throw new DataAccessException(
                    "The data file " + file + " is not valid JSON.", e);
        } catch (IOException e) {
            throw new DataAccessException("Could not read " + file + ".", e);
        }
    }

    /**
     * Writes the data file, creating the parent directory if needed.
     *
     * @throws DataAccessException if the file cannot be written
     */
    public void save(PlatePalData data) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new DataAccessException("Could not write " + file + ".", e);
        }
    }

    public Path getFile() {
        return file;
    }
}
