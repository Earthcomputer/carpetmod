package carpet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Debug {

    public static void ensureMainThread() {
        if (!Thread.currentThread().getName().equals("Server thread")) {
            System.err.println("Not on main thread!");
            try (PrintWriter pw = getDebugOutputFile()) {
                new Exception("Not on main thread, on thread \"" + Thread.currentThread().getName() + "\"").printStackTrace(pw);
                pw.flush();
            } catch (IOException e) {
                System.err.println("Unable to save debug report");
                e.printStackTrace();
            }
        }
    }

    private static PrintWriter getDebugOutputFile() throws IOException {
        File file = new File(CarpetServer.minecraft_server.getDataDirectory(), "crash-reports");
        if (!file.exists())
            file.mkdirs();
        file = new File(file, "debug-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt");
        return new PrintWriter(new FileWriter(file));
    }

}
